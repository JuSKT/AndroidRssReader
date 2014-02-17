package com.nerdability.android.model;

import java.io.Serializable;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

public class Article implements Serializable, Comparable<Article>,
		Comparator<Article> {

	public static final String KEY = "ARTICLE";

	private static final long serialVersionUID = 1L;
	private String guid;
	private String title;
	private String description;
	private String pubDate;
	private String author;
	private URL url;
	private String encodedContent;
	private boolean read;
	private boolean offline;
	private long dbId;

	public Article() {
		super();
	}

	public Article(String title, String description, String pubDate,
			String author, URL url, String encodedContent, boolean read,
			boolean offline) {
		super();
		this.title = title;
		this.description = description;
		this.pubDate = pubDate;
		this.author = author;
		this.url = url;
		this.encodedContent = encodedContent;
		this.read = read;
		this.offline = offline;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public void setDescription(String description) {
		this.description = extractCData(description);
	}

	public String getDescription() {
		return description;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public String getPubDate() {
		return pubDate;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setEncodedContent(String encodedContent) {
		this.encodedContent = extractCData(encodedContent);
	}

	public String getEncodedContent() {
		return encodedContent;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	public long getDbId() {
		return dbId;
	}

	public void setDbId(long dbId) {
		this.dbId = dbId;
	}

	/**
	 * Delete the CDATA tag of the string
	 * 
	 * @param data
	 *            the string to work with
	 * @return the string with the CDATA tag deleted
	 */
	private String extractCData(String data) {
		if (data != null) {
			data = data.replaceAll("<!\\[CDATA\\[", "");
			data = data.replaceAll("\\]\\]>", "");
		}
		return data;
	}

	/**
	 * Generate the specific Guid for articles
	 * 
	 * @param str
	 *            the title of the news (in our case)
	 * @return the custom generated Guid
	 */
	public static String generateGuid(String str) {
		String strmd5 = md5(str);
		strmd5.concat(String.valueOf(generateRandomIntNumberByRange(1000000,
				9999999)));
		return strmd5;
	}

	/**
	 * Generate a md5 of the string
	 * 
	 * @param str
	 *            the string
	 * @return the generated md5 value of the string
	 */
	public static String md5(String str) {
		MessageDigest md = null;
		StringBuffer sb = new StringBuffer();
		try {
			md = MessageDigest.getInstance("MD5");

			md.update(str.getBytes());
			byte byteData[] = md.digest();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * Generate a random number between a minimum and a maximum
	 * 
	 * @param Min
	 *            the minimum value
	 * @param Max
	 *            the maximum value
	 * @return the random number
	 */
	public static int generateRandomIntNumberByRange(int Min, int Max) {
		return Min + (int) (Math.random() * ((Max - Min) + 1));
	}

	/**
	 * Compare articles with the ID of the database
	 */
	@Override
	public int compareTo(Article a) {
		if (a.dbId > this.dbId) {
			return -1;
		} else if (a.dbId == this.dbId) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Compare articles with the ID of the database
	 */
	@Override
	public int compare(Article lhs, Article rhs) {
		if (lhs.getDbId() > rhs.getDbId()) {
			return -1;
		} else if (lhs.getDbId() == rhs.getDbId()) {
			return 0;
		} else {
			return 1;
		}
	}

}