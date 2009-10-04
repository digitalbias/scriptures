package com.digitalbias.android;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class SelectBookmarkActivity extends ListActivity {

	private ScriptureDbAdapter mAdapter;
	private Cursor mCursor;
	
	private Long mBookId;
	private Long mChapterId;
	private Long mPosition;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(SetPreferencesActivity.getPreferedTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_list);

        registerForContextMenu(getListView());

        Bundle extras = getIntent().getExtras();
        mBookId = extras.getLong(ScriptureDbAdapter.BOOK_ID);
        mChapterId = extras.getLong(ScriptureDbAdapter.CHAPTER_NUM);
        mPosition = extras.getLong(ScriptureDbAdapter.BOOKMARK_POSITION);
        populateList(extras);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bookmark_context_menu, menu);
    }

    private void setupUiElements(Bundle extras){
    }
    
    private void populateList(Bundle extras){
		setupUiElements(extras);
	
		mAdapter = new ScriptureDbAdapter(this.getSharedPreferences(SetPreferencesActivity.PREFS_NAME, 0));
		mAdapter.open();
		fetchBookmarks();
    }
    
    private void fetchBookmarks(){
    	mCursor = mAdapter.fetchBookmarks();
    	startManagingCursor(mCursor);
    	String[] from = new String[] { ScriptureDbAdapter.CHAPTER_TITLE, ScriptureDbAdapter.BOOKMARK_TITLE};
    	int[] to = new int[] {R.id.chapter_title, R.id.title };
    	
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.bookmark_row, mCursor, from, to);
    	setListAdapter(adapter);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
        Cursor c = mCursor;
        c.moveToPosition(position);
        long bookmarkId = c.getLong(c.getColumnIndex("_id"));
        selectBookmark(bookmarkId);
    }
    
    protected void selectBookmark(final long bookmarkId){
		mAdapter.updateBookmark(new Long(bookmarkId), mBookId, mChapterId, mPosition);
        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }
    
    private void log(String message){
		if(BrowseScriptureActivity.DEBUG){
	    	Log.d("bookmarks", message);
	    	Toast.makeText(this, message, Toast.LENGTH_LONG);
    	}
    }
}
