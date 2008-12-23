package com.digitalbias.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ReadChapterActivity extends Activity implements OnTouchListener, OnGestureListener {

	private static final int ACTIVITY_MANAGE_BOOKMARKS = 0;
	private static final int ACTIVITY_BROWSE = 1;
	private static final int ACTIVITY_SELECT_BOOKMARK = 2;
	
	private static final String CALLING_ACTIVITY = "callingActivity";

	private ScriptureDbAdapter mAdapter;
	private Cursor mCursor;
	private Long mChapterId;
	private Long mVolumeId;
	private Long mBookId;

    private TextView mTitleText;
	private Button mBookButton;
	private Button mVolumeButton;
	private Button mHomeButton;

	private GestureDetector mGestureDetector; 
	protected Context mContext;
	protected int mCalledFrom;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SetPreferencesActivity.getPreferedTheme(this));
        setContentView(R.layout.read_chapter);
        mContext = this;

        setupGestures();
        
        mTitleText = (TextView) findViewById(R.id.chapter_title);

        mBookButton = (Button) findViewById(R.id.back_book);
        mVolumeButton = (Button) findViewById(R.id.back_volume);
        mHomeButton = (Button) findViewById(R.id.back_home);
        
        mBookId = null;
        mVolumeId = null;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        setMembers(extras);
    }

    protected void setMembers(Bundle bundle){
    	String title = bundle.getString(ScriptureDbAdapter.CHAPTER_TITLE);
    	String bookTitle = bundle.getString(ScriptureDbAdapter.BOOK_TITLE_SHORT);
    	String volumeTitle = bundle.getString(ScriptureDbAdapter.VOLUME_TITLE);

    	mChapterId = bundle.getLong(ScriptureDbAdapter.TABLE_ID);
    	mBookId = bundle.getLong(ScriptureDbAdapter.BOOK_ID);
    	mVolumeId = bundle.getLong(ScriptureDbAdapter.VOLUME_ID);
    	mCalledFrom = bundle.getInt(CALLING_ACTIVITY, ACTIVITY_BROWSE);
    	
    	mTitleText.setText(title);
    	mBookButton.setText(bookTitle);
    	mVolumeButton.setText(volumeTitle);
    }
    
    protected void onPostCreate(Bundle bundle){
    	super.onPostCreate(bundle);
        populateList();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle bundle){
    	log("pausing");
    	
    	String title = mTitleText.getText().toString();
    	String bookTitle = mBookButton.getText().toString();
    	String volumeTitle = mVolumeButton.getText().toString();

    	bundle.putString(ScriptureDbAdapter.CHAPTER_TITLE, title);
    	bundle.putString(ScriptureDbAdapter.BOOK_TITLE_SHORT, bookTitle);
    	bundle.putString(ScriptureDbAdapter.VOLUME_TITLE, volumeTitle);
    	bundle.putLong(ScriptureDbAdapter.TABLE_ID, mChapterId);
    	bundle.putLong(ScriptureDbAdapter.BOOK_ID, mBookId);

    	if(mAdapter != null) {
    		mAdapter.close();
        	mAdapter = null;
    	}
    	
    	super.onSaveInstanceState(bundle);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle bundle){
    	super.onRestoreInstanceState(bundle);
    	log("unpausing");
    	setMembers(bundle);
    	populateList();
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	log("resuming");
    	populateList();
    }
    
    protected void setupGestures(){
        mGestureDetector = new GestureDetector(this); 
        mGestureDetector.setIsLongpressEnabled(true); 
//    	TextView verses = (TextView)findViewById(R.id.verses);
    	ScrollView scrollView = (ScrollView)findViewById(R.id.scroll_view);
    	scrollView.setOnTouchListener(this);
//    	verses.setOnTouchListener(this); 
    }
    
    private Bundle populateReturnBundle(Bundle bundle, int returnBrowseMode){
    	
    	Cursor c = mAdapter.fetchSingleBook(mBookId.toString());
    	
        bundle.putLong(ScriptureDbAdapter.TABLE_ID, mBookId);
        bundle.putLong(ScriptureDbAdapter.BOOK_ID, mBookId);
        bundle.putLong(ScriptureDbAdapter.VOLUME_ID, mVolumeId);
        bundle.putString(ScriptureDbAdapter.BOOK_TITLE, c.getString(
                c.getColumnIndexOrThrow(ScriptureDbAdapter.BOOK_TITLE)));
        bundle.putString(ScriptureDbAdapter.BOOK_SUBTITLE, c.getString(
                c.getColumnIndexOrThrow(ScriptureDbAdapter.BOOK_SUBTITLE)));
        bundle.putString(ScriptureDbAdapter.VOLUME_TITLE, mVolumeButton.getText().toString());
    	bundle.putInt(BrowseScriptureActivity.BROWSE_MODE, returnBrowseMode);
    	
    	return bundle;
    }

    private void setupUiElements(){
        mBookButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view){
        		goBack(BrowseScriptureActivity.BROWSE_BOOK_MODE);
        	}
        });
        mVolumeButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view){
        		goBack(BrowseScriptureActivity.BROWSE_VOLUME_MODE);
        	}
        });
        mHomeButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view){
        		goBack(BrowseScriptureActivity.BROWSE_SCRIPTURES_MODE);
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
	        case R.id.manage_bookmarks:
	        	manageBookmarks();
	        	break;
	        case R.id.add_bookmark:
	        	addBookmark();
	        	break;
	        case R.id.move_bookmark:
	        	moveBookmark();
	        	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void openPrevious(){
    	Bundle extras = mAdapter.getPreviousBookAndChapter(mBookId,mChapterId);
    	setMembers(extras);
    	populateList();
    }

    public void openNext(){
    	Bundle extras = mAdapter.getNextBookAndChapter(mBookId,mChapterId);
    	setMembers(extras);
    	populateList();
    }
    
    protected void manageBookmarks(){
        Intent i = new Intent(this, ManageBookmarksActivity.class);
        startActivityForResult(i, ACTIVITY_MANAGE_BOOKMARKS);
    }
    
    protected void moveBookmark(){
    	Intent i = new Intent(this, SelectBookmarkActivity.class);
    	Bundle extras = new Bundle();
    	extras.putLong(ScriptureDbAdapter.BOOK_ID, mBookId);
    	extras.putLong(ScriptureDbAdapter.CHAPTER_NUM, mChapterId);
    	i.putExtras(extras);
    	startActivityForResult(i, ACTIVITY_SELECT_BOOKMARK);
    }
    
    protected void addBookmark(){
    	final Dialog dialog = new Dialog(this);
    	dialog.setTitle(R.string.add_bookmark);
    	dialog.setContentView(R.layout.add_bookmark);
    	
    	TextView locationText = (TextView) dialog.findViewById(R.id.location_text);
    	locationText.setText(mTitleText.getText());
    	
    	log("Create ok and cancel listeners, then create bookmark if needed.");
		Button button = (Button)dialog.findViewById(R.id.cancel_button);
		button.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		button = (Button)dialog.findViewById(R.id.ok_button);
		button.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				dialog.hide();
				createBookmark(dialog);
				log("bookmark created");
				dialog.dismiss();
			}
		});
    	
    	dialog.show();
    }
    
    protected void createBookmark(Dialog dialog){
    	EditText text = (EditText)dialog.findViewById(R.id.title_edit);
    	String title = text.getText().toString();
    	Integer book = new Integer(mBookId.intValue());
    	Integer chapter = new Integer(mChapterId.intValue());
    	mAdapter.createBookmark(book, chapter, title);
    }
    
    private void goBack(int returnMode){
        Bundle bundle = new Bundle();
        bundle = populateReturnBundle(bundle, returnMode);
        Intent mIntent = new Intent();
        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);
        mAdapter.close();
        finish();
    }
    
    private void populateList(){
    	log("populate");
    	setupUiElements();
    	
        if(mAdapter == null){
        	mAdapter = new ScriptureDbAdapter(this);
			mAdapter.open();
        }
		fetchAllVerses();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        log("result");
        if (resultCode == RESULT_OK && requestCode == ACTIVITY_SELECT_BOOKMARK) {
    		Toast.makeText(mContext, "Bookmark updated", Toast.LENGTH_LONG).show();
        }
    }
    
	public boolean onTouch(View view, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	public boolean onDown(MotionEvent e) {
		ScrollView scrollView = (ScrollView)findViewById(R.id.scroll_view);
		return scrollView.onTouchEvent(e);
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//		Toast.makeText(this, "fling", Toast.LENGTH_SHORT).show();
		log(Float.toString(e1.getX()));
		log(Float.toString(e2.getX()));
		float xDifference = e1.getX() - e2.getX();
		float yDifference = e1.getY() - e2.getY();
		if(Math.abs(xDifference) > Math.abs(yDifference)) {
			log(Float.toString(xDifference));
			if(xDifference > 0) {
		    	openNext();
			} else {
		    	openPrevious();
			}
		}
		return false;
	}

	public void onLongPress(MotionEvent e) {
		//Toast.makeText(this, "long press", Toast.LENGTH_SHORT).show();
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	
	public void log(String message){
		if(BrowseScriptureActivity.DEBUG){
			Log.i("read",message);
			Toast.makeText(this, message, Toast.LENGTH_LONG);
		}
	}

}
