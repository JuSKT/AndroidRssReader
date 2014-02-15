package com.nerdability.android.rss.service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.nerdability.android.container.ArticleContent;
import com.nerdability.android.db.DbAdapter;
import com.nerdability.android.model.Article;
import com.nerdability.android.rss.parser.RssHandler;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class RssRefreshService extends IntentService {

	private int result = Activity.RESULT_CANCELED;
	public static final String URL = "urlpath";
	public static final String RESULT = "result";
	public static final String NOTIFICATION = "com.nerdability.android.receiver";

	private static final String BLOG_URL = "http://www.ombudsman.europa.eu/rss/rss.xml";

	public RssRefreshService() {
		super("RssRefreshService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		RssHandler rh = new RssHandler();
		URL url = null;
		try {

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			url = new URL(BLOG_URL);

			xr.setContentHandler(rh);
			xr.parse(new InputSource(url.openStream()));

			Log.e("ASYNC", "PARSING FINISHED");

		} catch (IOException e) {
			Log.e("RSS Handler IO", e.getMessage() + " >> " + e.toString());
		} catch (SAXException e) {
			Log.e("RSS Handler SAX", e.toString());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Log.e("RSS Handler Parser Config", e.toString());
		}
		
		boolean newData = false;

		for (Article a : rh.getArticleList()) {
			Log.d("DB", "Searching DB for GUID: " + a.getGuid());
			DbAdapter dba = new DbAdapter(getApplicationContext());
			dba.openToRead();
			Article fetchedArticle = dba.getBlogListing(a.getGuid());
			dba.close();
			if (fetchedArticle == null) {
				Log.d("DB", "Found entry for first time: " + a.getTitle());
				dba = new DbAdapter(getApplicationContext());
				dba.openToWrite();
				// dba.insertBlogListing(a.getGuid());
				dba.insertBlogListingWithData(a.getGuid(), a.getTitle(),
						a.getDescription(), a.getPubDate(), a.getAuthor(),
						a.getUrl(), a.getEncodedContent());
				dba.close();
				newData = true;
			} else {
				a.setDbId(fetchedArticle.getDbId());
				a.setOffline(fetchedArticle.isOffline());
				a.setRead(fetchedArticle.isRead());
			}
			ArticleContent.addItem(a);
		}
		
		result = Activity.RESULT_OK;
		
//		if (newData) {
			publishResults(result);
//		}
	}

	private void publishResults(int result) {
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(RESULT, result);
		sendBroadcast(intent);
	}

}
