package com.nerdability.android;

import java.util.Calendar;

import com.nerdability.android.adapter.ArticleListAdapter;
import com.nerdability.android.container.ArticleContent;
import com.nerdability.android.db.DbAdapter;
import com.nerdability.android.preferences.AppPreferences;
import com.nerdability.android.rss.service.RssRefreshService;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
		PreferenceManager
				.setDefaultValues(this, R.xml.pref_notification, false);

		loadServiceSchedule();

		Intent articleListIntent = new Intent(this, ArticleListActivity.class);
		startActivity(articleListIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void loadServiceSchedule() {
		if (AppPreferences.getSync_frequency(getApplicationContext()) != -1) {
			long REPEAT_TIME = 1000 * 60 * AppPreferences
					.getSync_frequency(getApplicationContext());

			Intent intent = new Intent(this, RssRefreshService.class);
			PendingIntent pintent = PendingIntent.getService(this, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, Calendar
					.getInstance().getTimeInMillis(), REPEAT_TIME, pintent);
		}
	}

}
