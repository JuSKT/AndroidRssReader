package com.nerdability.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.nerdability.android.adapter.ArticleListAdapter;
import com.nerdability.android.container.ArticleContent;
import com.nerdability.android.db.DbAdapter;
import com.nerdability.android.model.Article;
import com.nerdability.android.preferences.AppPreferences;
import com.nerdability.android.rss.task.RssRefreshTask;

public class ArticleListFragment extends ListFragment {

	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private Callbacks mCallbacks = sDummyCallbacks;
	private int mActivatedPosition = ListView.INVALID_POSITION;
	private RssRefreshTask rssRefreshTask;

	public interface Callbacks {
		public void onItemSelected(String id);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	public ArticleListFragment() {
		setHasOptionsMenu(true); // this enables us to set actionbar from
									// fragment
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (ArticleContent.ITEMS.isEmpty()) {
			DbAdapter dba = new DbAdapter(getActivity());
			dba.openToRead();
			ArticleContent.addItems(dba.getAllArticles());
			dba.close();
		}

		ArticleListAdapter adapter = new ArticleListAdapter(getActivity(),
				ArticleContent.cloneList());
		this.setListAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);
		mCallbacks.onItemSelected(String.valueOf(position));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	public void setActivateOnItemClick(boolean activateOnItemClick) {
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	public void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.refreshmenu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.actionbar_refresh) {
			refreshList();
			return true;
		} else if (id == R.id.actionbar_settings) {
			Intent settingsIntent = new Intent(getActivity(),
					SettingsActivity.class);
			startActivity(settingsIntent);
		}
		return false;
	}

	private void refreshList() {
		rssRefreshTask = new RssRefreshTask(this);
		rssRefreshTask.execute(AppPreferences.getRss_feed_url_text(this
				.getActivity()));
	}
}
