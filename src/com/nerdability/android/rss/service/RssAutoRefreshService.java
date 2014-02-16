package com.nerdability.android.rss.service;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.nerdability.android.container.ArticleContent;
import com.nerdability.android.db.DbAdapter;
import com.nerdability.android.model.Article;
import com.nerdability.android.rss.parser.RssHandler;

public class RssAutoRefreshService extends Service {

	private static final String BLOG_URL = "http://www.ombudsman.europa.eu/rss/rss.xml";
	private final IBinder mBinder = new MyBinder();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		new AsyncTask<String, Void, List<Article>>() {

			protected void onPreExecute() {
				Log.e("ASYNC SERVICE", "PRE EXECUTE");
			}

			protected void onPostExecute(final List<Article> articles) {
				Log.e("ASYNC SERVICE", "POST EXECUTE");
				for (Article a : articles) {
					DbAdapter dba = new DbAdapter(getApplicationContext());
					dba.openToRead();
					Article fetchedArticle = dba.getBlogListing(a.getGuid());
					dba.close();
					if (fetchedArticle == null) {
						dba = new DbAdapter(getApplicationContext());
						dba.openToWrite();
						dba.insertBlogListingWithData(a.getGuid(),
								a.getTitle(), a.getDescription(),
								a.getPubDate(), a.getAuthor(), a.getUrl(),
								a.getEncodedContent());
						dba.close();
						ArticleContent.addItem(a);
					}
				}
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

					Log.e("ASYNC SERVICE", "PARSING FINISHED");
					return rh.getArticleList();

				} catch (IOException e) {
					Log.e("RSS Handler IO",
							e.getMessage() + " >> " + e.toString());
				} catch (SAXException e) {
					Log.e("RSS Handler SAX", e.toString());
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					Log.e("RSS Handler Parser Config", e.toString());
				}

				return rh.getArticleList();
			}
		}.execute(BLOG_URL);

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		public RssAutoRefreshService getService() {
			return RssAutoRefreshService.this;
		}
	}

}
