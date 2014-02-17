package com.nerdability.android;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.nerdability.android.adapter.ArticleListAdapter;
import com.nerdability.android.container.ArticleContent;
import com.nerdability.android.db.DbAdapter;
import com.nerdability.android.model.Article;
import com.nerdability.android.preferences.AppPreferences;
import com.nerdability.android.rss.service.RssRefreshService;

public class ArticleListActivity extends FragmentActivity implements
		ArticleListFragment.Callbacks {

	private boolean mTwoPane;
	private DbAdapter dba;

	private Intent intent;
	private PendingIntent pintent;
	private AlarmManager alarm;
	private Calendar cal = Calendar.getInstance();

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				int resultCode = bundle.getInt(RssRefreshService.RESULT);
				if (resultCode == Activity.RESULT_OK) {
					Toast.makeText(getApplicationContext(),
							"New data fetched, updating the list...",
							Toast.LENGTH_LONG).show();
					ArticleListAdapter adapter = (ArticleListAdapter) ((ArticleListFragment) getSupportFragmentManager()
							.findFragmentById(R.id.article_list))
							.getListAdapter();
					if (ArticleContent.ITEMS.isEmpty()) {
						dba.openToRead();
						ArticleContent.addItems(dba.getAllArticles());
						dba.close();
						adapter.addAll(ArticleContent.cloneList());
					} else {
						adapter.addAll(ArticleContent.cloneList());
					}
					adapter.notifyDataSetChanged();
				} else {
					Toast.makeText(getApplicationContext(),
							"No new data found", Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	public ArticleListActivity() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_list);

		if (findViewById(R.id.article_detail_container) != null) {
			mTwoPane = true;
			((ArticleListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.article_list))
					.setActivateOnItemClick(true);
		}

		dba = new DbAdapter(getApplicationContext());
		
		PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
		PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);

		loadServiceSchedule();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(
				RssRefreshService.NOTIFICATION));

		// I don't know why I can't do it in the detail fragment. It's working
		// in tablet mode.
		ArticleListAdapter adapter = new ArticleListAdapter(this,
				ArticleContent.cloneList());
		((ArticleListFragment) getSupportFragmentManager().findFragmentById(
				R.id.article_list)).setListAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemSelected(String id) {
		Article selected = (Article) ((ArticleListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.article_list)).getListAdapter().getItem(
				Integer.parseInt(id));
		dba.openToWrite();
		dba.markAsRead(selected.getGuid());
		dba.close();
		selected.setRead(true);
		ArticleContent.modify(selected);

		ArticleListAdapter adapter = (ArticleListAdapter) ((ArticleListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.article_list)).getListAdapter();
		// int indexArticleRemoved = adapter.getPosition(selected);
		// adapter.remove(selected);
		// adapter.insert(selected, indexArticleRemoved);
		adapter.notifyDataSetChanged();

		// load article details to main panel
		if (mTwoPane) {
			Bundle arguments = new Bundle();
			arguments.putSerializable(Article.KEY, selected);

			ArticleDetailFragment fragment = new ArticleDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.article_detail_container, fragment).commit();

		} else {
			Intent detailIntent = new Intent(this, ArticleDetailActivity.class);
			// detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, id);
			detailIntent.putExtra(Article.KEY, selected);
			startActivity(detailIntent);
		}
	}

	private void loadServiceSchedule() {
		if (AppPreferences.getSync_frequency(getApplicationContext()) != -1) {
			long REPEAT_TIME = 1000 * 60 * AppPreferences
					.getSync_frequency(getApplicationContext());

			intent = new Intent(this, RssRefreshService.class);
			pintent = PendingIntent.getService(this, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
					cal.getTimeInMillis(), REPEAT_TIME, pintent);
		}
	}

}
