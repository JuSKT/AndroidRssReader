package com.nerdability.android;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.nerdability.android.adapter.ArticleListAdapter;
import com.nerdability.android.adapter.NavDrawerListAdapter;
import com.nerdability.android.container.ArticleContent;
import com.nerdability.android.db.DbAdapter;
import com.nerdability.android.model.Article;
import com.nerdability.android.model.NavDrawerItem;
import com.nerdability.android.preferences.AppPreferences;
import com.nerdability.android.rss.service.RssRefreshService;

public class MainActivity extends FragmentActivity implements
		ArticleListFragment.Callbacks {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

	private boolean mTwoPane;
	private DbAdapter dba;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				if(getSupportFragmentManager().findFragmentById(R.id.frame_container) instanceof ArticleListFragment){
					int resultCode = bundle.getInt(RssRefreshService.RESULT);
					if (resultCode == Activity.RESULT_OK) {
						Toast.makeText(getApplicationContext(),
								"New data fetched, updating the list...",
								Toast.LENGTH_LONG).show();
						ArticleListAdapter adapter = (ArticleListAdapter) ((ArticleListFragment) getSupportFragmentManager()
								.findFragmentById(R.id.frame_container))
								.getListAdapter();
//						if (ArticleContent.ITEMS.isEmpty()) {
//							dba.openToRead();
//							ArticleContent.addItems(dba.getAllArticles());
//							dba.close();
//						}
						adapter.addAll(ArticleContent.cloneList());
						adapter.notifyDataSetChanged();
					} else {
						Toast.makeText(getApplicationContext(),
								"No new data found", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// -------------------------------------------------

		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons
				.getResourceId(0, -1)));
		// Find People
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons
				.getResourceId(1, -1)));
		// Photos
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons
				.getResourceId(2, -1)));
		// Communities, Will add a counter here
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons
				.getResourceId(3, -1), false, "22"));
		// Pages
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons
				.getResourceId(4, -1)));
		// What's hot, We will add a counter here
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons
				.getResourceId(5, -1), false, "50+"));

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
		}

		// -------------------------------------------------

		if (findViewById(R.id.article_detail_container) != null) {
			mTwoPane = true;
			((ArticleListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.article_list))
					.setActivateOnItemClick(true);
		}

		dba = new DbAdapter(this);

		PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
		PreferenceManager
				.setDefaultValues(this, R.xml.pref_notification, false);

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

		if (getSupportFragmentManager().findFragmentById(R.id.frame_container) instanceof ArticleListFragment) {
			// I don't know why I can't do it in the detail fragment. It's
			// working
			// in tablet mode.
			ArticleListAdapter adapter = new ArticleListAdapter(this,
					ArticleContent.cloneList());
			((ArticleListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.frame_container))
					.setListAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/***
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
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

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			fragment = new HomeFragment();
			break;
		case 1:
			// fragment = new FindPeopleFragment();
			fragment = new PresentationFragment();
			break;
		case 2:
			// fragment = new PhotosFragment();
			fragment = new ComplainFragment();
			break;
		case 3:
			fragment = new CommunityFragment();
			break;
		case 4:
			// fragment = new PagesFragment();
			fragment = new PhotosFragment();
			break;
		case 5:
			fragment = new ArticleListFragment();
			// listFragment = new FragmentActivity();
			break;

		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = ((android.support.v4.app.FragmentManager) getSupportFragmentManager());
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onItemSelected(String id) {
		Article selected = (Article) ((ArticleListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.frame_container)).getListAdapter()
				.getItem(Integer.parseInt(id));
		dba.openToWrite();
		dba.markAsRead(selected.getGuid());
		dba.close();
		selected.setRead(true);
		ArticleContent.modify(selected);

		ArticleListAdapter adapter = (ArticleListAdapter) ((ArticleListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.frame_container)).getListAdapter();
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

}
