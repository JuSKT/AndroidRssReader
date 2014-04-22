package com.nerdability.android.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class PdfContentProvider extends ContentProvider {
	private static final String PDFPATH = "public_pdfs/";

	@Override
	public String getType(Uri uri) {
		return "application/pdf";
	}

	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode)
			throws FileNotFoundException {
		AssetManager am = getContext().getAssets();
		String file_name = uri.getLastPathSegment();

		if (file_name == null)
			throw new FileNotFoundException();
		AssetFileDescriptor afd = null;
		try {
			afd = am.openFd(PDFPATH + file_name + ".mp3");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return afd;
	}

	private final static String[] COLUMNS = { OpenableColumns.DISPLAY_NAME,
			OpenableColumns.SIZE };

	@Override
	/**
	 * This function is required for it to work on the Quickoffice at Android 4.4 (KitKat)
	 */
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (projection == null) {
			projection = COLUMNS;
		}

		String[] cols = new String[projection.length];
		Object[] values = new Object[projection.length];
		int i = 0;
		for (String col : projection) {
			if (OpenableColumns.DISPLAY_NAME.equals(col)) {
				cols[i] = OpenableColumns.DISPLAY_NAME;
				values[i++] = uri.getLastPathSegment();
			} else if (OpenableColumns.SIZE.equals(col)) {
				cols[i] = OpenableColumns.SIZE;
				values[i++] = AssetFileDescriptor.UNKNOWN_LENGTH;
			}
		}

		cols = copyOf(cols, i);
		values = copyOf(values, i);

		final MatrixCursor cursor = new MatrixCursor(cols, 1);
		cursor.addRow(values);
		return cursor;
	}

	private static String[] copyOf(String[] original, int newLength) {
		final String[] result = new String[newLength];
		System.arraycopy(original, 0, result, 0, newLength);
		return result;
	}

	private static Object[] copyOf(Object[] original, int newLength) {
		final Object[] result = new Object[newLength];
		System.arraycopy(original, 0, result, 0, newLength);
		return result;
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
}