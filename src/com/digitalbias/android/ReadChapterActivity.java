package com.digitalbias.android;

import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
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
	
	public static final String CALLING_ACTIVITY = "callingActivity";
	
	private static final int highlightColor = Color.parseColor("#ffff88"); 

	private ScriptureDbAdapter mAdapter;
	private Cursor mCursor;
	private Long mChapterId;
	private Long mVolumeId;
	private Long mBookId;
	private Long mSelectedVerseId;

    private TextView mTitleText;
	private Button mBookButton;
	private Button mVolumeButton;
	private Button mHomeButton;
	private ScrollView mScrollView; 
	
	private GestureDetector mGestureDetector;
	private OrientationEventListener mOrientationEventListener;

	protected Context mContext;
	protected String mCalledFrom;
	protected int mScrollTo;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(SetPreferencesActivity.getPreferedTheme(this));
        super.onCreate(savedInstanceState);
    	log("creating read activity");

    	setContentView(R.layout.read_chapter);
    	log("content view set");
        mContext = this;
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
    	log("found scroll view");
//        mOrientationEventListener = new OrientationEventListener(mContext)  {
//			@Override
//			public void onOrientationChanged(int orientation) {
//				onOrientationChange(orientation);
//			}
//        };

    	log("setting up gestures");
        setupGestures();
        
        mTitleText = (TextView) findViewById(R.id.chapter_title);
        mBookButton = (Button) findViewById(R.id.back_book);
        mVolumeButton = (Button) findViewById(R.id.back_volume);
        mHomeButton = (Button) findViewById(R.id.back_home);
        
        mBookId = null;
        mVolumeId = null;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null){
            setMembers(extras);
        }
    	log("activity created");
    }
    
    private Bundle extrasFromSearchData(Intent intent){
    	Bundle result = new Bundle();
        String verseId = intent.getDataString();
        if(verseId == null){
        	Bundle extras = intent.getExtras();
        	verseId = Long.toString(extras.getLong(ScriptureDbAdapter.VERSE_ID));
        }
        log(verseId);        
    	
        if(mAdapter == null){
        	mAdapter = new ScriptureDbAdapter(this.getSharedPreferences(SetPreferencesActivity.PREFS_NAME, 0));
			mAdapter.open();
        }

        Cursor verse = mAdapter.fetchSingleVerse(verseId);
		Long verseNumber = verse.getLong(verse.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_NUMBER));
		Long bookId = verse.getLong(verse.getColumnIndex(ScriptureDbAdapter.BOOK_ID));
		Long volumeId = verse.getLong(verse.getColumnIndex(ScriptureDbAdapter.VOLUME_ID));
		String verseText = verse.getString(verse.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_TEXT));
		Long chapterId = verse.getLong(verse.getColumnIndex(ScriptureDbAdapter.CHAPTER_ID));
		
        Cursor c = mAdapter.fetchSingleVolume(volumeId.toString());
        String volumeTitle = c.getString(c.getColumnIndex(ScriptureDbAdapter.VOLUME_TITLE_LONG));
		
		
		result.putLong(ScriptureDbAdapter.VERSE_ID, Long.parseLong(verseId));
		result.putLong(ScriptureDbAdapter.VERSE_NUMBER, verseNumber);
		result.putString(ScriptureDbAdapter.VERSE_TEXT, verseText);
		result.putInt(BrowseScriptureActivity.BROWSE_MODE, BrowseScriptureActivity.OPEN_VERSE_MODE);

        c = mAdapter.fetchSingleChapter(bookId.toString(), chapterId.toString());
        String chapterTitle = c.getString(c.getColumnIndexOrThrow(ScriptureDbAdapter.CHAPTER_TITLE));

    	c = mAdapter.fetchSingleBook(bookId.toString());
    	String shortTitle = c.getString(c.getColumnIndex(ScriptureDbAdapter.BOOK_TITLE_SHORT));
    	String bookTitle = c.getString( c.getColumnIndexOrThrow(ScriptureDbAdapter.BOOK_TITLE));    	
        
        result.putLong(ScriptureDbAdapter.TABLE_ID, chapterId);
        result.putLong(ScriptureDbAdapter.BOOK_ID, bookId);
        result.putLong(ScriptureDbAdapter.VOLUME_ID, volumeId);
        result.putString(ScriptureDbAdapter.VOLUME_TITLE, volumeTitle);
        result.putString(ScriptureDbAdapter.CHAPTER_TITLE, chapterTitle);
        result.putString(ScriptureDbAdapter.BOOK_TITLE, bookTitle);
        result.putString(ScriptureDbAdapter.BOOK_TITLE_SHORT, shortTitle);
		
    	return result;
    }

    protected void setMembers(Bundle bundle){
    	String title = bundle.getString(ScriptureDbAdapter.CHAPTER_TITLE);
    	String bookTitle = bundle.getString(ScriptureDbAdapter.BOOK_TITLE_SHORT);
    	String volumeTitle = bundle.getString(ScriptureDbAdapter.VOLUME_TITLE);

    	mChapterId = bundle.getLong(ScriptureDbAdapter.TABLE_ID);
    	mBookId = bundle.getLong(ScriptureDbAdapter.BOOK_ID);
    	mVolumeId = bundle.getLong(ScriptureDbAdapter.VOLUME_ID);
    	mCalledFrom = bundle.getString(CALLING_ACTIVITY);
    	if(mCalledFrom == null) mCalledFrom = BrowseScriptureActivity.class.getName();
    	if(mCalledFrom.equals(ManageBookmarksActivity.class.getName()) 
    	   || (bundle.getLong(ScriptureDbAdapter.BOOKMARK_POSITION) != 0) ){
    		mScrollTo = (int)bundle.getLong(ScriptureDbAdapter.BOOKMARK_POSITION, 0);
    	} else {
    		mScrollTo = 0;
    	}
    	
    	log("getting browse mode");
    	int browseMode = bundle.getInt(BrowseScriptureActivity.BROWSE_MODE, BrowseScriptureActivity.BROWSE_BOOK_MODE); 
    	if(browseMode == BrowseScriptureActivity.OPEN_VERSE_MODE){
    		mSelectedVerseId = bundle.getLong(ScriptureDbAdapter.VERSE_ID);
        	log("mSelectedVerse = " + mSelectedVerseId.toString());
//    	} else {
//    		mSelectedVerseNum = null;
//        	log("mSelectedVerse = null");
    	}
    	

    	mTitleText.setText(title);
    	mBookButton.setText(bookTitle);
    	mVolumeButton.setText(volumeTitle);
    	
    }
    
    protected void onPostCreate(Bundle bundle){
    	super.onPostCreate(bundle);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
    	if(extras == null) {
    		extras = extrasFromSearchData(intent);
        	intent.putExtras(extras);
        	setMembers(extras);
    	}
        populateList();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle bundle){
    	log("pausing");
    	
    	String title = mTitleText.getText().toString();
    	String bookTitle = mBookButton.getText().toString();
    	String volumeTitle = mVolumeButton.getText().toString();
    	mScrollTo = mScrollView.getScrollY();

    	bundle.putString(ScriptureDbAdapter.CHAPTER_TITLE, title);
    	bundle.putString(ScriptureDbAdapter.BOOK_TITLE_SHORT, bookTitle);
    	bundle.putString(ScriptureDbAdapter.VOLUME_TITLE, volumeTitle);
    	bundle.putLong(ScriptureDbAdapter.TABLE_ID, mChapterId);
    	bundle.putLong(ScriptureDbAdapter.BOOK_ID, mBookId);
    	bundle.putLong(ScriptureDbAdapter.BOOKMARK_POSITION, mScrollTo);

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
    protected void onStop() {
//		if(SetPreferencesActivity.getOrientationPreference(this)){
//			mOrientationEventListener.disable();
//		}
        super.onStop();
    }

    protected void onResume(){
    	super.onResume();
    	log("resuming");
    	populateList();
    	
		if (mScrollTo != -1){
	    	mScrollView = (ScrollView)findViewById(R.id.scroll_view);
	    	mScrollView.postDelayed(new Runnable(){
				public void run() {
					mScrollView.scrollTo(0, mScrollTo);
					log("scrolling to: " + mScrollTo);
					mScrollTo = -1;
				}
	    	}, 200);
		}
		
//		if(SetPreferencesActivity.getOrientationPreference(this)){
//			mOrientationEventListener.enable();
//		}
    }
    
    @Override
    protected void onPause(){
    	super.onPause();
    	mScrollTo = mScrollView.getScrollY(); 
    }
    
    protected void setupGestures(){
        mGestureDetector = new GestureDetector(this); 
        mGestureDetector.setIsLongpressEnabled(true); 
    	mScrollView.setOnTouchListener(this);
    }
    
    private Bundle populateReturnBundle(Bundle bundle, int returnBrowseMode){
    	
    	Cursor c = mAdapter.fetchSingleBook(mBookId.toString());
    	mVolumeId = c.getLong(c.getColumnIndex(ScriptureDbAdapter.VOLUME_ID));
    	
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
    	if(SetPreferencesActivity.getScrollToTopOnChange(mContext)){
	    	mScrollView.scrollTo(0, 0);
    	}
    }

    public void openNext(){
    	Bundle extras = mAdapter.getNextBookAndChapter(mBookId,mChapterId);
    	setMembers(extras);
    	populateList();
    	if(SetPreferencesActivity.getScrollToTopOnChange(mContext)){
	    	mScrollView.scrollTo(0, 0);
    	}
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
    	extras.putLong(ScriptureDbAdapter.BOOKMARK_POSITION, getScrollPosition());
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
				showMessage("Bookmark created");
				dialog.dismiss();
			}
		});
    	
    	dialog.show();
    }
    
    protected int getScrollPosition(){
    	return mScrollView.getScrollY();
    }
    
    protected void createBookmark(Dialog dialog){
    	EditText text = (EditText)dialog.findViewById(R.id.title_edit);
    	String title = text.getText().toString();
    	Integer book = new Integer(mBookId.intValue());
    	Integer chapter = new Integer(mChapterId.intValue());
    	Integer scrollPosition = new Integer(getScrollPosition()); 
    	mAdapter.createBookmark(book, chapter, title, scrollPosition);
    }
    
    private void goBack(int returnMode){
        Bundle bundle = new Bundle();
        bundle = populateReturnBundle(bundle, returnMode);
        Intent intent = new Intent(this, BrowseScriptureActivity.class);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        mAdapter.close();
        startActivity(intent);
        finish();
    }
    
    private void populateList(){
    	log("populate");
    	setupUiElements();
    	log("done populating");
    	
        if(mAdapter == null){
        	mAdapter = new ScriptureDbAdapter(this.getSharedPreferences(SetPreferencesActivity.PREFS_NAME, 0));
			mAdapter.open();
        }
		fetchAllVerses();
    }
    
    private void fetchAllVerses(){
    	log("getting verses");
    	mCursor = mAdapter.fetchBookVerses(mBookId.toString(), mChapterId.toString());

    	StringBuilder builder = new StringBuilder();
    	
    	while(mCursor.moveToNext()){
    		String verseNumber = mCursor.getString(mCursor.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_NUMBER));
    		String verseId = mCursor.getString(mCursor.getColumnIndexOrThrow(ScriptureDbAdapter.TABLE_ID));

    		boolean selected = false;
    		if(mSelectedVerseId != null && mSelectedVerseId.equals(Long.parseLong(verseId))) {
    			selected = true;
    		}
    		
    		boolean pilcrow = ((Long)mCursor.getLong(mCursor.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_PILCROW))).longValue() == 1;
    		if(selected) { 
    			builder.append("<colorize>");
    		}
    		builder.append("<p><span>");
    		builder.append(verseNumber);
    		builder.append("</span> ");
    		
    		if(pilcrow) {
    			builder.append("&#182; ");
    		}
    		String verseText = mCursor.getString(mCursor.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_TEXT)); 
    		builder.append(verseText);
    		builder.append("</p>");
    		if(selected) { 
    			builder.append("</colorize>");
    		}
    	}
    	
    	Spanned text = Html.fromHtml(builder.toString(), null, new Html.TagHandler() {
			public void handleTag(boolean opening, String tag, Editable output,
					XMLReader xmlReader) {
				if(tag.equalsIgnoreCase("colorize") && !opening){
					log("---------------------handle tag: " + tag);
					log("output size: " + output.length());
					int startPoint = getSpanStartPoint(output, mSelectedVerseId);
					int highlightSize = getHightlightSpanSize(mSelectedVerseId);
					int endPoint = startPoint + highlightSize;
					log("start: " + startPoint);
					log("size: " + highlightSize);
					log("end: " + endPoint);
					output.setSpan(new BackgroundColorSpan(highlightColor), startPoint, endPoint, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
					log("---------------------handle tag end");
				}
			}
			
		    private int getSpanStartPoint(Editable output, Long verseId) {
		    	Cursor verse = mAdapter.fetchSingleVerse(verseId.toString());
				String verseNumber = verse.getString(verse.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_NUMBER));
				return output.toString().indexOf(verseNumber+ " ");
			}
		    
			private int getHightlightSpanSize(Long verseId) {
		    	Cursor verse = mAdapter.fetchSingleVerse(verseId.toString());
		    	int textSize = verse.getString(verse.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_TEXT)).length();
				boolean pilcrow = ((Long)verse.getLong(verse.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_PILCROW))).longValue() == 1;
				int verseNumberSize = verse.getString(verse.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_NUMBER)).length();
				int result = textSize + verseNumberSize + 1;
				result = pilcrow ? result + 2 : result;
				return result;
		    }
		    
		});
    	TextView verses = (TextView)findViewById(R.id.verses);
    	verses.setTextKeepState(text, TextView.BufferType.SPANNABLE);
    	verses.setTextSize(SetPreferencesActivity.getPreferedFontSize(this));
    	log("done getting verses");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        log("result");
        if (resultCode == RESULT_OK && requestCode == ACTIVITY_SELECT_BOOKMARK) {
        	showMessage("Bookmark updated");
        }
    }
    
	public boolean onTouch(View view, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	public boolean onDown(MotionEvent e) {
		return mScrollView.onTouchEvent(e);
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		float xDifference = e1.getX() - e2.getX();
		float yDifference = e1.getY() - e2.getY();
		if(Math.abs(xDifference) > Math.abs(yDifference)) {
			if(xDifference > 0) {
		    	openNext();
			} else {
		    	openPrevious();
			}
		}
		return false;
	}

	public void onLongPress(MotionEvent e) {
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	
	public void showMessage(String message){
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	public void log(String message){
		if(BrowseScriptureActivity.DEBUG){
			Log.i("read",message);
//			showMessage(message);
		}
	}

	private void onOrientationChange(int orientation) {
		if(orientation >= 225 && orientation <= 300){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}
}
