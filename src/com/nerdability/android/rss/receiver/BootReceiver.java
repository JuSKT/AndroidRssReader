package com.nerdability.android.rss.receiver;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nerdability.android.preferences.AppPreferences;
import com.nerdability.android.rss.service.RssRefreshService;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (AppPreferences.getSync_frequency(context) != -1) {
			long REPEAT_TIME = 1000 * 60 * AppPreferences
					.getSync_frequency(context);

			intent = new Intent(context, RssRefreshService.class);
			PendingIntent pintent = PendingIntent.getService(context, 0,
					intent, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager alarm = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, Calendar
					.getInstance().getTimeInMillis(), REPEAT_TIME, pintent);
		}
	}
}