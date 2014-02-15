package com.nerdability.android.db;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.nerdability.android.model.Article;

public class DbAdapter {

	public static final String KEY_ROWID = BaseColumns._ID;
	public static final String KEY_GUID = "guid";
	public static final String KEY_READ = "read";
	public static final String KEY_OFFLINE = "offline";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_PUBDATE = "pubDate";
	public static final String KEY_AUTHOR = "author";
	public static final String KEY_URL = "url";
	public static final String KEY_ENCODED_CONTENT = "encoded_content";

	private static final String DATABASE_NAME = "rssreader";
	private static final String DATABASE_TABLE = "article";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE_LIST_TABLE = "create table "
			+ DATABASE_TABLE + " (" + KEY_ROWID
			+ " integer primary key autoincrement, " + KEY_GUID
			+ " text not null, " + KEY_READ + " boolean not null, "
			+ KEY_OFFLINE + " boolean not null, " + KEY_TITLE + " text, "
			+ KEY_DESCRIPTION + " text, " + KEY_PUBDATE + " text, "
			+ KEY_AUTHOR + " text, " + KEY_URL + " text, "
			+ KEY_ENCODED_CONTENT + " text " + ");";

	private SQLiteHelper sqLiteHelper;
	private SQLiteDatabase sqLiteDatabase;
	private Context context;

	public DbAdapter(Context c) {
		context = c;

		// openToWrite();
		//
		// try {
		// insertBlogListingWithData("001", "Title1", "Description1", new
		// Date(), "Author1", new URL("http://www.url.eu"), "Encoded content1");
		// insertBlogListingWithData("002", "Title2", "Description2", new
		// Date(), "Author2", new URL("http://www.url.eu"), "Encoded content2");
		// insertBlogListingWithData("003", "Title3", "Description3", new
		// Date(), "Author3", new URL("http://www.url.eu"), "Encoded content3");
		// insertBlogListingWithData("004", "Title4", "Description4", new
		// Date(), "Author4", new URL("http://www.url.eu"), "Encoded content4");
		// insertBlogListingWithData("005", "Title5", "Description5", new
		// Date(), "Author5", new URL("http://www.url.eu"), "Encoded content5");
		// insertBlogListingWithData("006", "Title6", "Description6", new
		// Date(), "Author6", new URL("http://www.url.eu"), "Encoded content6");
		// insertBlogListingWithData("007", "Title7", "Description7", new
		// Date(), "Author7", new URL("http://www.url.eu"), "Encoded content7");
		// insertBlogListingWithData("008", "Title8", "Description8", new
		// Date(), "Author8", new URL("http://www.url.eu"), "Encoded content8");
		// insertBlogListingWithData("009", "Title9", "Description9", new
		// Date(), "Author9", new URL("http://www.url.eu"), "Encoded content9");
		// } catch (MalformedURLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// close();
	}

	public DbAdapter openToRead() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getReadableDatabase();
		return this;
	}

	public DbAdapter openToWrite() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		sqLiteHelper.close();
	}

	public class SQLiteHelper extends SQLiteOpenHelper {
		public SQLiteHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_LIST_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}

	public long insertBlogListing(String guid) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_GUID, guid);
		initialValues.put(KEY_READ, false);
		initialValues.put(KEY_OFFLINE, false);
		return sqLiteDatabase.insert(DATABASE_TABLE, null, initialValues);
	}

	public long insertBlogListingWithData(String guid, String title,
			String description, String pubDate, String author, URL url,
			String encodedContent) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_GUID, guid);
		initialValues.put(KEY_READ, false);
		initialValues.put(KEY_OFFLINE, false);

		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_DESCRIPTION, description);
		initialValues.put(KEY_PUBDATE, pubDate);
		initialValues.put(KEY_AUTHOR, author);
		initialValues.put(KEY_URL, url.toString());
		initialValues.put(KEY_ENCODED_CONTENT, encodedContent);

		return sqLiteDatabase.insert(DATABASE_TABLE, null, initialValues);
	}

	public List<Article> getAllArticles() {
		List<Article> articles = new ArrayList<Article>();

		Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_GUID, KEY_READ, KEY_OFFLINE, KEY_TITLE,
				KEY_DESCRIPTION, KEY_PUBDATE, KEY_AUTHOR, KEY_URL,
				KEY_ENCODED_CONTENT }, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Article article = cursorToArticle(cursor);
			articles.add(article);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return articles;
	}

	private Article cursorToArticle(Cursor cursor) {
		Article a = new Article();

		a.setGuid(cursor.getString(cursor.getColumnIndex(KEY_GUID)));
		a.setRead(cursor.getInt(cursor.getColumnIndex(KEY_READ)) > 0);
		a.setDbId(cursor.getLong(cursor.getColumnIndex(KEY_ROWID)));
		a.setOffline(cursor.getInt(cursor.getColumnIndex(KEY_OFFLINE)) > 0);

		a.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
		a.setDescription(cursor.getString(cursor
				.getColumnIndex(KEY_DESCRIPTION)));

		String strDate = cursor.getString(cursor.getColumnIndex(KEY_PUBDATE));
		// DateFormat formatter = new
		// SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
		// Date pDate = new Date();
		// try {
		// pDate = formatter.parse(strDate);
		// } catch (ParseException e) {
		// // Log.e("DATE PARSING",
		// "IN DB cursorToArticle : Error parsing date..");
		// // pDate = formatter.parse("Sat, 01 Jan 2000 00:00:00 GMT");
		// pDate = new Date();
		// }
		// a.setPubDate(pDate);
		a.setPubDate(strDate);

		a.setAuthor(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)));
		try {
			a.setUrl(new URL(cursor.getString(cursor.getColumnIndex(KEY_URL))));
		} catch (MalformedURLException e) {
			// e.printStackTrace();
		}
		a.setEncodedContent(cursor.getString(cursor
				.getColumnIndex(KEY_ENCODED_CONTENT)));

		return a;
	}

	// public Article getBlogListing(String guid) throws SQLException {
	// Cursor mCursor =
	// sqLiteDatabase.query(true, DATABASE_TABLE, new String[] {
	// KEY_ROWID,
	// KEY_GUID,
	// KEY_READ,
	// KEY_OFFLINE
	// },
	// KEY_GUID + "= '" + guid + "'",
	// null,
	// null,
	// null,
	// null,
	// null);
	// if (mCursor != null && mCursor.getCount() > 0) {
	// mCursor.moveToFirst();
	// Article a = new Article();
	// a.setGuid(mCursor.getString(mCursor.getColumnIndex(KEY_GUID)));
	// a.setRead(mCursor.getInt(mCursor.getColumnIndex(KEY_READ)) > 0);
	// a.setDbId(mCursor.getLong(mCursor.getColumnIndex(KEY_ROWID)));
	// a.setOffline(mCursor.getInt(mCursor.getColumnIndex(KEY_OFFLINE)) > 0);
	// return a;
	// }
	// return null;
	// }

	public Article getBlogListing(String guid) throws SQLException {
		Cursor mCursor = sqLiteDatabase.query(true, DATABASE_TABLE,
				new String[] { KEY_ROWID, KEY_GUID, KEY_READ, KEY_OFFLINE,
						KEY_TITLE, KEY_DESCRIPTION, KEY_PUBDATE, KEY_AUTHOR,
						KEY_URL, KEY_ENCODED_CONTENT }, KEY_GUID + "= '" + guid
						+ "'", null, null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			Article a = new Article();
			a.setGuid(mCursor.getString(mCursor.getColumnIndex(KEY_GUID)));
			a.setRead(mCursor.getInt(mCursor.getColumnIndex(KEY_READ)) > 0);
			a.setDbId(mCursor.getLong(mCursor.getColumnIndex(KEY_ROWID)));
			a.setOffline(mCursor.getInt(mCursor.getColumnIndex(KEY_OFFLINE)) > 0);

			a.setTitle(mCursor.getString(mCursor.getColumnIndex(KEY_TITLE)));
			a.setDescription(mCursor.getString(mCursor
					.getColumnIndex(KEY_DESCRIPTION)));

			String strDate = mCursor.getString(mCursor
					.getColumnIndex(KEY_PUBDATE));
			// DateFormat formatter = new
			// SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",
			// Locale.ENGLISH);
			// Date pDate = new Date();
			// try {
			// pDate = formatter.parse(strDate);
			// } catch (ParseException e) {
			// // Log.e("DATE PARSING",
			// "IN DB getBlogListing : Error parsing date..");
			// // pDate = formatter.parse("Sat, 01 Jan 2000 00:00:00 GMT");
			// pDate = new Date();
			// }
			// a.setPubDate(pDate);
			a.setPubDate(strDate);

			a.setAuthor(mCursor.getString(mCursor.getColumnIndex(KEY_AUTHOR)));
			try {
				a.setUrl(new URL(mCursor.getString(mCursor
						.getColumnIndex(KEY_URL))));
			} catch (MalformedURLException e) {
				// e.printStackTrace();
			}
			a.setEncodedContent(mCursor.getString(mCursor
					.getColumnIndex(KEY_ENCODED_CONTENT)));
			return a;
		}
		return null;
	}

	public boolean markAsUnread(String guid) {
		ContentValues args = new ContentValues();
		args.put(KEY_READ, false);
		return sqLiteDatabase.update(DATABASE_TABLE, args, KEY_GUID + "='"
				+ guid + "'", null) > 0;
	}

	public boolean markAsRead(String guid) {
		ContentValues args = new ContentValues();
		args.put(KEY_READ, true);
		return sqLiteDatabase.update(DATABASE_TABLE, args, KEY_GUID + "='"
				+ guid + "'", null) > 0;
	}

	public boolean saveForOffline(String guid) {
		ContentValues args = new ContentValues();
		args.put(KEY_OFFLINE, true);
		return sqLiteDatabase.update(DATABASE_TABLE, args, KEY_GUID + "='"
				+ guid + "'", null) > 0;
	}
}