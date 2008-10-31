package com.digitalbias.android;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class BrowseBookActivity extends ListActivity {
	private ScriptureDbAdapter mAdapter;
	private Cursor mCursor;
	private Long mBookId;

	private static final int ACTIVITY_BROWSE_CHAPTER = 0;

    private TextView mTitleText;
    private TextView mSubTitleText;
	private Button mVolumeButton;
	private Button mHomeButton;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SetPreferencesActivity.getPreferedTheme(this));
        setContentView(R.layout.book_list);

        mTitleText = (TextView) findViewById(R.id.book_title);
        mSubTitleText = (TextView) findViewById(R.id.book_subtitle);
        mVolumeButton = (Button) findViewById(R.id.back_volume);
        mHomeButton = (Button) findViewById(R.id.back_home);
        
        mBookId = null;
        Bundle extras = getIntent().getExtras();
        populateList(extras);
    }

    private void setupUiElements(Bundle extras){

    	String title = extras.getString(ScriptureDbAdapter.BOOK_TITLE);
    	String subTitle = extras.getString(ScriptureDbAdapter.BOOK_SUBTITLE);
    	mTitleText.setText(title);
    	mSubTitleText.setText(subTitle);
    	
    	String volumeTitle = extras.getString(ScriptureDbAdapter.VOLUME_TITLE);
    	mVolumeButton.setText(volumeTitle);

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
    
    private Bundle populateReturnBundle(Bundle bundle, String returnActivityClassName){
    	
    	Cursor c = mAdapter.fetchSingleBook(mBookId.toString());
    	Long volumeId = c.getLong(c.getColumnIndexOrThrow(ScriptureDbAdapter.VOLUME_ID));
    	
        Log.i(BrowseScriptureActivity.TAG, mBookId.toString() + " " + volumeId.toString()); 
    	
    	bundle.putLong(ScriptureDbAdapter.TABLE_ID, volumeId);
//        bundle.putString(ScriptureDbAdapter.BOOK_TITLE, c.getString(
//                c.getColumnIndexOrThrow(ScriptureDbAdapter.BOOK_TITLE)));
//        bundle.putString(ScriptureDbAdapter.BOOK_SUBTITLE, c.getString(
//                c.getColumnIndexOrThrow(ScriptureDbAdapter.BOOK_SUBTITLE)));
    	bundle.putString(BrowseScriptureActivity.GO_BACK_COMMAND, returnActivityClassName);
    	
    	return bundle;
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
        	mBookId = extras.getLong(ScriptureDbAdapter.TABLE_ID);
        	
        	setupUiElements(extras);

        	mAdapter = new ScriptureDbAdapter(this);
			mAdapter.open();
			fetchAllChapters();
        }
    }
    
    private void fetchAllChapters(){
    	mCursor = mAdapter.fetchBookChapters(mBookId.toString());
    	startManagingCursor(mCursor);
    	String[] from = new String[] { ScriptureDbAdapter.CHAPTER_TITLE};
    	int[] to = new int[] {R.id.text1 };
    	
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.scripture_row, mCursor, from, to);
    	setListAdapter(adapter);
    }

    private String getShortBookTitle(Long bookId){
    	Cursor c = mAdapter.fetchSingleBook(bookId.toString());
    	return c.getString(c.getColumnIndex(ScriptureDbAdapter.BOOK_TITLE_SHORT));
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
        Cursor c = mCursor;
        c.moveToPosition(position);
        Intent i = new Intent(this, BrowseChapterActivity.class);
        i.putExtra(ScriptureDbAdapter.TABLE_ID, id);
        i.putExtra(ScriptureDbAdapter.BOOK_TITLE, mTitleText.getText());
        i.putExtra(ScriptureDbAdapter.BOOK_ID, mBookId);
        i.putExtra(ScriptureDbAdapter.VOLUME_TITLE, mVolumeButton.getText());
        i.putExtra(ScriptureDbAdapter.BOOK_TITLE_SHORT, getShortBookTitle(mBookId));
        i.putExtra(ScriptureDbAdapter.CHAPTER_TITLE, c.getString(
                c.getColumnIndexOrThrow(ScriptureDbAdapter.CHAPTER_TITLE)));
        
        startActivityForResult(i, ACTIVITY_BROWSE_CHAPTER);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(intent != null){
	        Bundle extras = intent.getExtras();
	        
	        String goBackCommand = extras.getString(BrowseScriptureActivity.GO_BACK_COMMAND);
	        if(goBackCommand.equals(this.getClass().getName())){
		    	populateList(extras);
	        } else {
	        	goBack(goBackCommand);
	        }
        }
    }

}
