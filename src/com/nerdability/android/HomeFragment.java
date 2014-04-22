package com.nerdability.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nerdability.android.util.FlowTextHelper;


public class HomeFragment extends Fragment {

	public HomeFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_home, container,
				false);

		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
//		String text = getString(R.string.home_body_title_text);
//
//        Drawable dIcon = getResources().getDrawable(R.drawable.ombudsman_logo);
//        int leftMargin = dIcon.getIntrinsicWidth() + 10;
//
//        ImageView icon = (ImageView) getView().findViewById(R.id.ombudsman_logo);
//        icon.setBackgroundDrawable(dIcon);
//
//        SpannableString ss = new SpannableString(text);
//        ss.setSpan(new MyLeadingMarginSpan2(3, leftMargin), 0, ss.length(), 0);
//
//        TextView messageView = (TextView) getView().findViewById(R.id.home_body_title);
//        messageView.setText(ss);
        
        
        ImageView thumbnailView = (ImageView) getView().findViewById(R.id.ombudsman_logo);
        TextView messageView = (TextView) getView().findViewById(R.id.home_body_title);
        String text = getString(R.string.home_body_title_text);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        FlowTextHelper.tryFlowText(text, thumbnailView, messageView, display, 10);
	}
	
	
	
	
}
