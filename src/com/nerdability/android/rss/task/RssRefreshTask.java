package com.nerdability.android.rss.task;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nerdability.android.ArticleListFragment;
import com.nerdability.android.adapter.ArticleListAdapter;
import com.nerdability.android.container.ArticleContent;
import com.nerdability.android.db.DbAdapter;
import com.nerdability.android.model.Article;
import com.nerdability.android.rss.parser.RssHandler;

public class RssRefreshTask extends AsyncTask<String, Void, List<Article>> {

	private ProgressDialog progress;
	private Context context;
	private ArticleListFragment articleListFrag;

	public RssRefreshTask(ArticleListFragment articleListFragment) {
		context = articleListFragment.getActivity();
		articleListFrag = articleListFragment;
		progress = new ProgressDialog(context);
		progress.setMessage("Loading...");
	}

	protected void onPreExecute() {
		Log.e("ASYNC", "PRE EXECUTE");
		progress.show();
	}

	protected void onPostExecute(final List<Article> articles) {
		Log.e("ASYNC", "POST EXECUTE");
		articleListFrag.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ArticleContent.ITEMS.clear();
				ArticleContent.ITEMS_MAP.clear();
				for (Article a : articles) {
					Log.d("DB", "Searching DB for GUID: " + a.getGuid());
					DbAdapter dba = new DbAdapter(articleListFrag.getActivity());
					dba.openToRead();
					Article fetchedArticle = dba.getBlogListing(a.getGuid());
					dba.close();
					if (fetchedArticle == null) {
						Log.d("DB",
								"Found entry for first time: " + a.getTitle());
						dba = new DbAdapter(articleListFrag.getActivity());
						dba.openToWrite();
						// dba.insertBlogListing(a.getGuid());
						dba.insertBlogListingWithData(a.getGuid(),
								a.getTitle(), a.getDescription(),
								a.getPubDate(), a.getAuthor(), a.getUrl(),
								a.getEncodedContent());
						dba.close();
					} else {
						a.setDbId(fetchedArticle.getDbId());
						a.setOffline(fetchedArticle.isOffline());
						a.setRead(fetchedArticle.isRead());
					}
//					ArticleContent.addItem(a);
				}
				ArticleContent.addItems(articles);

				if (articles == null || articles.isEmpty()) {
					DbAdapter dba = new DbAdapter(articleListFrag.getActivity());
					dba.openToRead();
					ArticleContent.addItems(dba.getAllArticles());
					dba.close();
				}

				ArticleListAdapter adapter = new ArticleListAdapter(
						articleListFrag.getActivity(), ArticleContent
								.cloneList());
				articleListFrag.setListAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		});
		progress.dismiss();
	}

	@Override
	protected List<Article> doInBackground(String... urls) {
		String feed = urls[0];

		RssHandler rh = new RssHandler();

		URL url = null;
		try {

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			url = new URL(feed);

			xr.setContentHandler(rh);
			xr.parse(new InputSource(url.openStream()));

			Log.e("ASYNC", "PARSING FINISHED");
			return rh.getArticleList();

		} catch (IOException e) {
			Log.e("RSS Handler IO", e.getMessage() + " >> " + e.toString());
		} catch (SAXException e) {
			Log.e("RSS Handler SAX", e.toString());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Log.e("RSS Handler Parser Config", e.toString());
		}

		return rh.getArticleList();

	}
}