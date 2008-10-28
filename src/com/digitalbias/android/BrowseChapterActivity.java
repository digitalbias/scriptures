package com.digitalbias.android;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BrowseChapterActivity extends Activity {
	private ScriptureDbAdapter mAdapter;
	private Cursor mCursor;
	private Long mChapterId;
	private Long mBookId;

    private TextView mTitleText;
	private Button mBookButton;
	private Button mVolumeButton;
	private Button mHomeButton;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Light);
        setContentView(R.layout.chapter);

        mTitleText = (TextView) findViewById(R.id.chapter_title);

        mBookButton = (Button) findViewById(R.id.back_book);
        mVolumeButton = (Button) findViewById(R.id.back_volume);
        mHomeButton = (Button) findViewById(R.id.back_home);
        
        mBookId = null;
        Bundle extras = getIntent().getExtras();
        populateList(extras);
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
    	Log.i(BrowseScriptureActivity.TAG, mBookId.toString() + " " + mChapterId.toString());
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
    }

}
