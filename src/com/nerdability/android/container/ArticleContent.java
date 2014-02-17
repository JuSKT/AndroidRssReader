package com.nerdability.android.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nerdability.android.model.Article;

/**
 * Helper class for providing sample content for user interfaces
 */
public class ArticleContent {

	/**
	 * An array of sample (Article) items.
	 */
	public static List<Article> ITEMS = new ArrayList<Article>();

	/**
	 * A map of sample (Article) items, by ID.
	 */
	public static Map<String, Article> ITEMS_MAP = new HashMap<String, Article>();

	public static boolean newDataFetched = false;

	static {

	}

	public static void addItem(Article article) {
		ITEMS.add(article);
		ITEMS_MAP.put(article.getGuid(), article);
		
	}
	
	public static void addItems(List<Article> articles) {
		for (Article article : articles) {
			ITEMS.add(article);
			ITEMS_MAP.put(article.getGuid(), article);
		}
		//Sort
		sortListAndMap();
	}

	public static void modify(Article article) {
		// Make the change in the map
		ITEMS_MAP.put(article.getGuid(), article);
		// Sort 
		sortListAndMap();
	}

	public static void delete(Article article) {
		// Make the change in the map
		ITEMS_MAP.remove(article.getGuid());
		ITEMS.remove(article);
	}
	
	private static void sortListAndMap(){
		ITEMS_MAP = sortByValues(ITEMS_MAP);
		// Make the change in the list
		ITEMS.clear();
		ITEMS.addAll(ITEMS_MAP.values());
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Article> cloneList() {
		return (ArrayList<Article>) ((ArrayList<Article>) ITEMS).clone();
	}

	/*
	 * Paramterized method to sort Map e.g. HashMap or Hashtable in Java throw
	 * NullPointerException if Map contains null key
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K extends Comparable, V extends Comparable> Map<K, V> sortByKeys(
			Map<K, V> map) {
		List<K> keys = new LinkedList<K>(map.keySet());
		Collections.sort(keys);

		// LinkedHashMap will keep the keys in the order they are inserted
		// which is currently sorted on natural ordering
		Map<K, V> sortedMap = new LinkedHashMap<K, V>();
		for (K key : keys) {
			sortedMap.put(key, map.get(key));
		}

		return sortedMap;
	}

	/*
	 * Java method to sort Map in Java by value e.g. HashMap or Hashtable throw
	 * NullPointerException if Map contains null values It also sort values even
	 * if they are duplicates
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K extends Comparable, V extends Comparable> Map<K, V> sortByValues(
			Map<K, V> map) {
		List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {

			@Override
			public int compare(Entry<K, V> lhs, Entry<K, V> rhs) {
				return lhs.getValue().compareTo(rhs.getValue());
			}

		});

		// LinkedHashMap will keep the keys in the order they are inserted
		// which is currently sorted on natural ordering
		Map<K, V> sortedMap = new LinkedHashMap<K, V>();

		for (Map.Entry<K, V> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

}
