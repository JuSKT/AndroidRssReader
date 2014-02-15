package com.nerdability.android.preferences;

import android.content.Context;
import android.preference.PreferenceManager;

public class AppPreferences {

	public static void setNimp(Context context, String nimp) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putString("nimp", nimp).commit();
	}

	public static String getNimp(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("nimp", "");
	}

	public static int getStarredClass(Context context) {
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(
				context).getString("starredclass", ""));
	}

	public static void setStarredClass(Context context, int id) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putString("starredclass", String.valueOf(id)).commit();
	}

	public static String getUserEmail(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("email", "");
	}

	public static boolean getMarkNumericDisplay(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("markdisplay", false);
	}

	public static String getMarkMaxNumeric(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("maxmark", "");
	}

	public static String getRingtone(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("ringtone", "");
	}

	public static boolean getSongActivated(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("songs", false);
	}
}
