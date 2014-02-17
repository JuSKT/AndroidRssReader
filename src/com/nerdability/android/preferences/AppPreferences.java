package com.nerdability.android.preferences;

import android.content.Context;
import android.preference.PreferenceManager;

public class AppPreferences {

	public static void setRss_feed_url_text(Context context,
			String rss_feed_url_text) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putString("rss_feed_url_text", rss_feed_url_text).commit();
	}

	public static String getRss_feed_url_text(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("rss_feed_url_text", "");
	}

	public static void setSync_frequency(Context context, String sync_frequency) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putString("sync_frequency", sync_frequency).commit();
	}

	public static int getSync_frequency(Context context) {
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(
				context).getString("sync_frequency", "360"));
	}

	public static void setNotifications_new_rss_feed(Context context,
			boolean notifications_new_rss_feed) {
		PreferenceManager
				.getDefaultSharedPreferences(context)
				.edit()
				.putBoolean("notifications_new_rss_feed",
						notifications_new_rss_feed).commit();
	}

	public static boolean getNotifications_new_rss_feed(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("notifications_new_rss_feed", false);
	}

	public static String getNotifications_new_rss_feed_ringtone(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("notifications_new_rss_feed_ringtone", "");
	}

	public static boolean getNotifications_new_rss_feed_vibrate(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("notifications_new_rss_feed_vibrate", false);
	}

}
