package com.nerdability.android;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class PresentationFragment extends Fragment {

	public PresentationFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View rootView = inflater.inflate(R.layout.fragment_presentation,
				container, false);

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		getActivity().getMenuInflater().inflate(R.menu.presentation, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent settingsIntent = new Intent(getActivity(),
					SettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		case R.id.actionbar_open:
			openPdfWithDeviceApp();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressWarnings("unused")
	private boolean openPdfWithWebView() {
		//http://www.survivingwithandroid.com/2013/03/android-fragment-tutorial-webview-example.html
		return false;
	}

	private boolean openPdfWithDeviceApp() {
		String surl = "http://www.ville.montreal.qc.ca/pls/portal/docs/PAGE/PES_PUBLICATIONS_FR/PUBLICATIONS/MAISON_PROPRE_JARDIN_VERT.PDF";
//		String apptype = "application/pdf";
//		if (openURLWithType(surl, apptype) == false) {
//			Toast.makeText(
//					getActivity(),
//					"Sorry, I didn't find any application in your device to read the PDF file.",
//					Toast.LENGTH_LONG).show();
//		}
//		return false;
		
//		PdfHandler pdf = new PdfHandler(getActivity());
//		pdf.openPdf(surl);
		
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);

		Uri uri = Uri.parse("content://com.nerdability.android.util.PdfContentProvider/" + surl);
		intent.setDataAndType(uri, "application/pdf");

		startActivity(intent);
		
		return false;
	}
	
	public boolean openURLWithType(String url, String type) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setDataAndType(uri, type);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		try {
			startActivity(intent);
			return true;
		} catch (ActivityNotFoundException e) {
			Log.e("LTM", "Activity not found: " + url, e);
		}		
		return false;
	}

}
