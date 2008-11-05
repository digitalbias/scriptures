package com.digitalbias.android;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ReadChapterActivity extends Activity implements OnTouchListener, OnGestureListener {
	private ScriptureDbAdapter mAdapter;
	private Cursor mCursor;
	private Long mChapterId;
	private Long mBookId;

    private TextView mTitleText;
	private Button mBookButton;
	private Button mVolumeButton;
	private Button mHomeButton;

	GestureDetector mGestureDetector; 
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SetPreferencesActivity.getPreferedTheme(this));
        setContentView(R.layout.chapter);

        setupGestures();
        
        mTitleText = (TextView) findViewById(R.id.chapter_title);

        mBookButton = (Button) findViewById(R.id.back_book);
        mVolumeButton = (Button) findViewById(R.id.back_volume);
        mHomeButton = (Button) findViewById(R.id.back_home);
        
        mBookId = null;
        Bundle extras = getIntent().getExtras();
        populateList(extras);
    }
    
    protected void setupGestures(){
        mGestureDetector = new GestureDetector(this); 
        mGestureDetector.setIsLongpressEnabled(true); 
    	TextView verses = (TextView)findViewById(R.id.verses);
    	verses.setOnTouchListener(this); 
    }
    
    private Bundle populateReturnBundle(Bundle bundle, String returnActivityClassName){
    	
    	Cursor c = mAdapter.fetchSingleBook(mBookId.toString());
    	
        bundle.putLong(ScriptureDbAdapter.TABLE_ID, mBookId);
        bundle.putString(ScriptureDbAdapter.BOOK_TITLE, c.getString(
                c.getColumnIndexOrThrow(ScriptureDbAdapter.BOOK_TITLE)));
        bundle.putString(ScriptureDbAdapter.BOOK_SUBTITLE, c.getString(
                c.getColumnIndexOrThrow(ScriptureDbAdapter.BOOK_SUBTITLE)));
        bundle.putString(ScriptureDbAdapter.VOLUME_TITLE, mVolumeButton.getText().toString());
    	bundle.putString(BrowseScriptureActivity.GO_BACK_COMMAND, returnActivityClassName);
    	
    	return bundle;
    }

    private void setupUiElements(Bundle extras){
    	String title = extras.getString(ScriptureDbAdapter.CHAPTER_TITLE);
    	String bookTitle = extras.getString(ScriptureDbAdapter.BOOK_TITLE_SHORT);
    	String volumeTitle = extras.getString(ScriptureDbAdapter.VOLUME_TITLE);

    	mTitleText.setText(title);
    	mBookButton.setText(bookTitle);
    	mVolumeButton.setText(volumeTitle);

        mBookButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view){
        		goBack(BrowseBookActivity.class.getName());
        	}
        });
        mVolumeButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view){
        		goBack(BrowseVolumeActivity.class.getName());
        	}
        });
        mHomeButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view){
        		goBack(BrowseScriptureActivity.class.getName());
        	}
        });

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
    	
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chapter_menu, menu);
    	
    	return result;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
	        case R.id.open_previous:
	        	openPrevious();
	        	break;
	        case R.id.open_next:
	        	openNext();
	        	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void openPrevious(){
    	Bundle extras = mAdapter.getPreviousBookAndChapter(mBookId,mChapterId);
    	populateList(extras);
    }

    public void openNext(){
    	Bundle extras = mAdapter.getNextBookAndChapter(mBookId,mChapterId);
    	populateList(extras);
    }
    
    private void goBack(String returnToClass){
        Bundle bundle = new Bundle();
        bundle = populateReturnBundle(bundle, returnToClass);
        Intent mIntent = new Intent();
        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);
        finish();
    }
    
    private void populateList(Bundle extras){
        if(extras != null) {
        	mChapterId = extras.getLong(ScriptureDbAdapter.TABLE_ID);
        	mBookId = extras.getLong(ScriptureDbAdapter.BOOK_ID);
        	
        	setupUiElements(extras);
        	
	        mAdapter = new ScriptureDbAdapter(this);
			mAdapter.open();
			fetchAllVerses();
        }
    }
    
    private void fetchAllVerses(){
    	mCursor = mAdapter.fetchBookVerses(mBookId.toString(), mChapterId.toString());

    	StringBuilder builder = new StringBuilder();
    	
    	
    	while(mCursor.moveToNext()){
    		boolean pilcrow = ((Long)mCursor.getLong(mCursor.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_PILCROW))).longValue() == 1;
    		builder.append("<p><span>");
    		builder.append(mCursor.getString(mCursor.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_NUMBER)));
    		builder.append("</span> ");
    		
    		if(pilcrow) {
    			builder.append("&#182; ");
    		}
    		
    		builder.append(mCursor.getString(mCursor.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_TEXT)));
    		builder.append("</p>");
    	}
    	
    	Spanned text = Html.fromHtml(builder.toString());
    	TextView verses = (TextView)findViewById(R.id.verses);
    	verses.setText(text, TextView.BufferType.SPANNABLE);
    	verses.setTextSize(SetPreferencesActivity.getPreferedFontSize(this));
    }

	public boolean onTouch(View view, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	public boolean onDown(MotionEvent e) {
		return true;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//		Toast.makeText(this, "fling", Toast.LENGTH_SHORT).show();
//		Log.i("fling", Float.toString(e1.getX()));
//		Log.i("fling", Float.toString(e2.getX()));
		float difference = e1.getX() - e2.getX(); 
//		Log.i("fling", Float.toString(difference));
		if(difference > 0) {
	    	openNext();
		} else {
	    	openPrevious();
		}
		return false;
	}

	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "long press", Toast.LENGTH_SHORT).show();
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
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
