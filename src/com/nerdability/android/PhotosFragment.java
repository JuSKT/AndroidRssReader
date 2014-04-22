package com.nerdability.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotosFragment extends Fragment {

	public PhotosFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_photos, container,
				false);


		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		
		GridView gridView = (GridView) getView().findViewById(R.id.gridview);
		gridView.setAdapter(new MyAdapter(getActivity()));
		
		gridView.setOnItemClickListener(new OnItemClickListener() 
		{
		    public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
		    {
		        // this 'mActivity' parameter is Activity object, you can send the current activity.
//		        Intent i = new Intent(mActivity, ActvityToCall.class);
//		        mActivity.startActivity(i);
		    }
		});
	}



	private class MyAdapter extends BaseAdapter {
		private List<Item> items = new ArrayList<Item>();
		private LayoutInflater inflater;

		public MyAdapter(Context context) {
			inflater = LayoutInflater.from(context);

			items.add(new Item("Image 1", R.drawable.nature1));
			items.add(new Item("Image 2", R.drawable.nature2));
			items.add(new Item("Image 3", R.drawable.tree1));
			items.add(new Item("Image 4", R.drawable.nature3));
			items.add(new Item("Image 5", R.drawable.tree2));
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int i) {
			return items.get(i);
		}

		@Override
		public long getItemId(int i) {
			return items.get(i).drawableId;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			View v = view;
			ImageView picture;
			TextView name;

			if (v == null) {
				v = inflater.inflate(R.layout.gridview_item, viewGroup, false);
				v.setTag(R.id.picture, v.findViewById(R.id.picture));
				v.setTag(R.id.text, v.findViewById(R.id.text));
			}

			picture = (ImageView) v.getTag(R.id.picture);
			name = (TextView) v.getTag(R.id.text);

			Item item = (Item) getItem(i);

			picture.setImageResource(item.drawableId);
			name.setText(item.name);

			return v;
		}

		private class Item {
			final String name;
			final int drawableId;

			Item(String name, int drawableId) {
				this.name = name;
				this.drawableId = drawableId;
			}
		}
	}
}
