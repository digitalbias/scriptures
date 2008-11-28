package com.digitalbias.android;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ManageBookmarksActivity extends ListActivity implements OnGestureListener {

	private static final int ACTIVITY_READ_CHAPTER = 0;
	
	private ScriptureDbAdapter mAdapter;
	private Cursor mCursor;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SetPreferencesActivity.getPreferedTheme(this));
        setContentView(R.layout.bookmark_list);

        Bundle extras = getIntent().getExtras();
        populateList(extras);
    }

    private void setupUiElements(Bundle extras){
    }
    
    private void populateList(Bundle extras){
		setupUiElements(extras);
	
		mAdapter = new ScriptureDbAdapter(this);
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
    	Toast.makeText(this, "list", Toast.LENGTH_SHORT);
        Cursor c = mCursor;
        c.moveToPosition(position);
        long bookmarkId = c.getLong(c.getColumnIndex("_id"));
        Cursor chapter = getChapter(bookmarkId);
        Intent i = new Intent(this, ReadChapterActivity.class);
        i.putExtra(ScriptureDbAdapter.TABLE_ID, mCursor.getLong(mCursor.getColumnIndex(ScriptureDbAdapter.CHAPTER_NUM)));
        i.putExtra(ScriptureDbAdapter.BOOK_TITLE, getBookTitle(chapter));
        i.putExtra(ScriptureDbAdapter.BOOK_ID, getBookId(chapter));
        i.putExtra(ScriptureDbAdapter.VOLUME_TITLE, getVolumeTitle(chapter));
        i.putExtra(ScriptureDbAdapter.BOOK_TITLE_SHORT, getShortBookTitle(chapter));
        i.putExtra(ScriptureDbAdapter.CHAPTER_TITLE, getChapterTitle(chapter));
        
        startActivityForResult(i, ACTIVITY_READ_CHAPTER);
    }
    
    private Cursor getChapter(long bookmarkId){
    	long bookId = mCursor.getLong(mCursor.getColumnIndex(ScriptureDbAdapter.BOOK_ID));
    	long chapterId = mCursor.getLong(mCursor.getColumnIndex(ScriptureDbAdapter.CHAPTER_NUM));
    	return mAdapter.fetchSingleChapter(Long.toString(bookId), Long.toString(chapterId));
    }
    
    private String getBookTitle(Cursor c){
    	return c.getString(c.getColumnIndex(ScriptureDbAdapter.BOOK_TITLE));
    }
    
    private Long getBookId(Cursor c){
    	return c.getLong(c.getColumnIndex(ScriptureDbAdapter.BOOK_ID));
    }
    
    private String getVolumeTitle(Cursor c){
    	return c.getString(c.getColumnIndex(ScriptureDbAdapter.VOLUME_TITLE_LONG));
    }
    
    private String getShortBookTitle(Cursor c){
    	return c.getString(c.getColumnIndex(ScriptureDbAdapter.BOOK_TITLE_SHORT));
    }

    private String getChapterTitle(Cursor c){
    	return c.getString(c.getColumnIndex(ScriptureDbAdapter.CHAPTER_TITLE));
    }

    private void log(String message){
    	Log.i("bookmarks", message);
    }

    public void onLongPress(MotionEvent e) {
		Toast.makeText(this, "long press", Toast.LENGTH_SHORT).show();
	}

	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}
