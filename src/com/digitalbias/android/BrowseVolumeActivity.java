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

public class BrowseVolumeActivity extends ListActivity {

	private ScriptureDbAdapter mAdapter;
	private Cursor mBookCursor;
	private Long mVolumeId;

	private static final int ACTIVITY_BROWSE_BOOK = 0;

    private TextView mTitleText;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Light);
        setContentView(R.layout.volume_list);

        mTitleText = (TextView) findViewById(R.id.volume_title);
        Button homeButton = (Button) findViewById(R.id.back_home);
        homeButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view){
        		goBack(BrowseScriptureActivity.class.getName());
        	}
        });
        
        mVolumeId = null;
        Bundle extras = getIntent().getExtras();
        populateList(extras);
    }

    private void goBack(String returnToClass){
        Bundle bundle = new Bundle();
        Intent mIntent = new Intent();
        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);
        finish();
    }
    
    private void populateList(Bundle extras){
        if(extras != null) {
        	mVolumeId = extras.getLong(ScriptureDbAdapter.TABLE_ID);
        	
	        mAdapter = new ScriptureDbAdapter(this);
			mAdapter.open();

        	Log.i(BrowseScriptureActivity.TAG, mVolumeId.toString());
			Cursor c = mAdapter.fetchSingleVolume(mVolumeId.toString());
        	String title = c.getString(c.getColumnIndex(
        			ScriptureDbAdapter.VOLUME_TITLE_LONG));
        	mTitleText.setText(title);

        	fetchAllBooks();
        }
    }
    
    private void fetchAllBooks(){
    	mBookCursor = mAdapter.fetchVolumeBooks(mVolumeId.toString());
    	startManagingCursor(mBookCursor);
    	String[] from = new String[] { ScriptureDbAdapter.BOOK_TITLE };
    	int[] to = new int[] {R.id.text1 };
    	
    	SimpleCursorAdapter volumes = new SimpleCursorAdapter(this, R.layout.scripture_row, mBookCursor, from, to);
    	setListAdapter(volumes);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
        Cursor c = mBookCursor;
        c.moveToPosition(position);
        Intent i = new Intent(this, BrowseBookActivity.class);
        i.putExtra(ScriptureDbAdapter.VOLUME_ID, mVolumeId);
        i.putExtra(ScriptureDbAdapter.TABLE_ID, id);
        i.putExtra(ScriptureDbAdapter.VOLUME_TITLE, mTitleText.getText());
        i.putExtra(ScriptureDbAdapter.BOOK_TITLE, c.getString(
                c.getColumnIndexOrThrow(ScriptureDbAdapter.BOOK_TITLE)));
        i.putExtra(ScriptureDbAdapter.BOOK_SUBTITLE, c.getString(
                c.getColumnIndexOrThrow(ScriptureDbAdapter.BOOK_SUBTITLE)));
        startActivityForResult(i, ACTIVITY_BROWSE_BOOK);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Bundle extras = intent.getExtras();
    	
        String goBackCommand = extras.getString(BrowseScriptureActivity.GO_BACK_COMMAND);
        if(goBackCommand.equals(this.getClass().getName())){
	    	populateList(extras);
        } else {
        	goBack(goBackCommand);
        }
    }
}
