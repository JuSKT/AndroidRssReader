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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;

import com.nerdability.android.ArticleListActivity;
import com.nerdability.android.R;
import com.nerdability.android.container.ArticleContent;
import com.nerdability.android.db.DbAdapter;
import com.nerdability.android.model.Article;
import com.nerdability.android.preferences.AppPreferences;
import com.nerdability.android.rss.parser.RssHandler;

@SuppressLint("NewApi")
public class RssRefreshService extends IntentService {

	private int result = Activity.RESULT_CANCELED;
	public static final String RESULT = "result";
	public static final String NOTIFICATION = "com.nerdability.android.receiver";
	private boolean newData = false;

	public RssRefreshService() {
		super("RssRefreshService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

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
						a = dba.getBlogListing(a.getGuid());
						dba.close();
						ArticleContent.addItem(a);
						newData = true;
					}
				}
				if (newData) {
					result = Activity.RESULT_OK;
					publishResults(result);
					newData = false;
					createNotifications();
				} else {
					result = Activity.RESULT_CANCELED;
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
		}.execute(AppPreferences.getRss_feed_url_text(getApplicationContext()));
	}

	private void createNotifications() {
		// Notification by sound
		if (AppPreferences
				.getNotifications_new_rss_feed(getApplicationContext())) {
			Ringtone ring = RingtoneManager
					.getRingtone(
							getApplicationContext(),
							Uri.parse(AppPreferences
									.getNotifications_new_rss_feed_ringtone(getApplicationContext())));
			ring.play();
		}

		// Notification by vibration
		if (AppPreferences
				.getNotifications_new_rss_feed(getApplicationContext())
				&& AppPreferences
						.getNotifications_new_rss_feed_vibrate(getApplicationContext())) {
			Vibrator vibrator = (Vibrator) getApplicationContext()
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(1000);
		}

		// Notification by a notification
		if (AppPreferences
				.getNotifications_new_rss_feed(getApplicationContext())) {

			Intent i = new Intent(getApplicationContext(),
					ArticleListActivity.class);
			PendingIntent pIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, i, 0);

			// Build notification
			Notification noti = new Notification.Builder(
					getApplicationContext()).setContentTitle("New feed")
					.setContentText("Check the new feed")
					.setSmallIcon(R.drawable.rssicon).setContentIntent(pIntent)
					// .addAction(R.drawable.rssicon, "Call", pIntent)
					// .addAction(R.drawable.rssicon, "More", pIntent)
					// .addAction(R.drawable.rssicon, "And more", pIntent)
					.build();
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			// hide the notification after its selected
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(0, noti);
		}
	}

	private void publishResults(int result) {
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(RESULT, result);
		sendBroadcast(intent);
	}

}
