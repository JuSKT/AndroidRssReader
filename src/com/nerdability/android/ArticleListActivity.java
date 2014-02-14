package com.nerdability.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.nerdability.android.adapter.ArticleListAdapter;
import com.nerdability.android.db.DbAdapter;
import com.nerdability.android.rss.domain.Article;

public class ArticleListActivity extends FragmentActivity implements ArticleListFragment.Callbacks {

    private boolean mTwoPane;
    private DbAdapter dba;
    
    public ArticleListActivity(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        dba = new DbAdapter(this);
        
//        dba.openToWrite();
//		try {
//			dba.insertBlogListingWithData(Article.md5("Title1"), "Title1", "Description1", new Date().toString(), "Author1", new URL("http://www.url.eu"), "Encoded content1");
//			dba.insertBlogListingWithData(Article.md5("Title2"), "Title2", "Description2", new Date().toString(), "Author2", new URL("http://www.url.eu"), "Encoded content2");
//			dba.insertBlogListingWithData(Article.md5("Title3"), "Title3", "Description3", new Date().toString(), "Author3", new URL("http://www.url.eu"), "Encoded content3");
//			dba.insertBlogListingWithData(Article.md5("Title4"), "Title4", "Description4", new Date().toString(), "Author4", new URL("http://www.url.eu"), "Encoded content4");
//			dba.insertBlogListingWithData(Article.md5("Title5"), "Title5", "Description5", new Date().toString(), "Author5", new URL("http://www.url.eu"), "Encoded content5");
//			dba.insertBlogListingWithData(Article.md5("Title6"), "Title6", "Description6", new Date().toString(), "Author6", new URL("http://www.url.eu"), "Encoded content6");
//			dba.insertBlogListingWithData(Article.md5("Title7"), "Title7", "Description7", new Date().toString(), "Author7", new URL("http://www.url.eu"), "Encoded content7");
//			dba.insertBlogListingWithData(Article.md5("Title8"), "Title8", "Description8", new Date().toString(), "Author8", new URL("http://www.url.eu"), "Encoded content8");
//			dba.insertBlogListingWithData(Article.md5("Title9"), "Title9", "Description9", new Date().toString(), "Author9", new URL("http://www.url.eu"), "Encoded content9");
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
//		dba.close();

        if (findViewById(R.id.article_detail_container) != null) {
            mTwoPane = true;
            ((ArticleListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.article_list))
                    .setActivateOnItemClick(true);
        }
    }


	@Override
    public void onItemSelected(String id) {
        Article selected = (Article) ((ArticleListFragment) getSupportFragmentManager().findFragmentById(R.id.article_list)).getListAdapter().getItem(Integer.parseInt(id));
        
        //mark article as read
        dba.openToWrite();
        dba.markAsRead(selected.getGuid());
        dba.close();
        selected.setRead(true);
        ArticleListAdapter adapter = (ArticleListAdapter) ((ArticleListFragment) getSupportFragmentManager().findFragmentById(R.id.article_list)).getListAdapter();
        adapter.notifyDataSetChanged();
        Log.e("CHANGE", "Changing to read: ");
        
        
        //load article details to main panel
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putSerializable (Article.KEY, selected);
            
            ArticleDetailFragment fragment = new ArticleDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.article_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, ArticleDetailActivity.class);
//            detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, id);
            detailIntent.putExtra(Article.KEY, selected);
            startActivity(detailIntent);
        }
    }
	
}
