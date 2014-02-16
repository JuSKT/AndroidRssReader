package com.nerdability.android;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.nerdability.android.adapter.ArticleListAdapter;
import com.nerdability.android.container.ArticleContent;
import com.nerdability.android.db.DbAdapter;
import com.nerdability.android.model.Article;
import com.nerdability.android.rss.service.MyStartServiceReceiver;
import com.nerdability.android.rss.service.RssAutoRefreshService;
import com.nerdability.android.rss.service.RssRefreshService;

public class ArticleListActivity extends FragmentActivity implements
		ArticleListFragment.Callbacks {

	private boolean mTwoPane;
	private DbAdapter dba;

	private RssAutoRefreshService s;

	// private Intent intent;
	// private PendingIntent pintent;

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
					Log.d("RECEIVER", "Service worked great");
					ArticleListAdapter adapter = (ArticleListAdapter) ((ArticleListFragment) getSupportFragmentManager()
							.findFragmentById(R.id.article_list))
							.getListAdapter();
					adapter.clear();
					adapter.addAll(ArticleContent.ITEMS);
					adapter.notifyDataSetChanged();
				} else {
					Toast.makeText(getApplicationContext(),
							"No new data found", Toast.LENGTH_LONG).show();
					Log.d("RECEIVER", "Service not worked");
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
		dba = new DbAdapter(this);

		// loadServiceSchedule2();

		if (findViewById(R.id.article_detail_container) != null) {
			mTwoPane = true;
			((ArticleListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.article_list))
					.setActivateOnItemClick(true);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// unregisterReceiver(receiver);
		// stopService(intent);

		// unbindService(mConnection);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// registerReceiver(receiver, new IntentFilter(
		// RssRefreshService.NOTIFICATION));
		// startService(intent);

		// Intent intent = new Intent(this, RssAutoRefreshService.class);
		// bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			RssAutoRefreshService.MyBinder b = (RssAutoRefreshService.MyBinder) binder;
			s = b.getService();
			Toast.makeText(ArticleListActivity.this, "Connected",
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			s = null;
		}
	};

	public void onClick(View view) {
		if (s != null) {
			// Toast.makeText(this, "lol",
			// Toast.LENGTH_SHORT).show();
			Log.e("CLICK", "Clicking");
			// ArticleListAdapter adapter = (ArticleListAdapter)
			// ((ArticleListFragment) getSupportFragmentManager()
			// .findFragmentById(R.id.article_list)).getListAdapter();
			// adapter.clear();
			// adapter.addAll(ArticleContent.ITEMS);
			// adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemSelected(String id) {
		Article selected = (Article) ((ArticleListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.article_list)).getListAdapter().getItem(
				Integer.parseInt(id));

		// mark article as read
		dba.openToWrite();
		dba.markAsRead(selected.getGuid());
		dba.close();
		selected.setRead(true);
		ArticleContent.modify(selected);

		ArticleListAdapter adapter = (ArticleListAdapter) ((ArticleListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.article_list)).getListAdapter();
		int indexArticleRemoved = adapter.getPosition(selected);
		adapter.remove(selected);
		adapter.insert(selected, indexArticleRemoved);
		adapter.notifyDataSetChanged();
		// Log.e("CHANGE", "Changing to read: ");

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
		long REPEAT_TIME = 1000 * 30;
		AlarmManager service = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(getApplicationContext(),
				MyStartServiceReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(
				getApplicationContext(), 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar cal = Calendar.getInstance();
		// start 30 seconds after boot completed
		cal.add(Calendar.SECOND, 30);
		// fetch every 30 seconds
		// InexactRepeating allows Android to optimize the energy consumption
		service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				cal.getTimeInMillis(), REPEAT_TIME, pending);
	}

	private void loadServiceSchedule2() {
		long REPEAT_TIME = 1000 * 30;
		Calendar cal = Calendar.getInstance();

		// intent = new Intent(this, RssRefreshService.class);
		// pintent = PendingIntent.getService(this, 0, intent,
		// PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// Start every 30 seconds
		// alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
		// cal.getTimeInMillis(), REPEAT_TIME, pintent);
	}

}
