package com.digitalbias.android;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class BrowseScriptureActivity extends ListActivity {
	
	public static final String TAG = "Scriptures";
	public static final String GO_BACK_COMMAND = "CONTINUE_BACK";
	
	private static final int ACTIVITY_BROWSE_VOLUME = 0;
	private static final int ACTIVITY_PREFERENCES = 1;
	private static final int ACTIVITY_DOWNLOAD = 2;
	private static final int ACTIVITY_BOOKMARK = 3;
	
	private Cursor mVolumeCursor;
	private ScriptureDbAdapter mAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyPreferences();
        initializeDatabase();
        if(mAdapter.canMakeValidConnection()){
	    	fetchAllVolumes();
        } else {
        	getGoodDatabase();
        }
        closeDatabase();
    }
    
    private void initializeDatabase() {
        if(mAdapter != null) {
        	mAdapter.close();
        }
        mAdapter = new ScriptureDbAdapter(this);
	}

    private void closeDatabase() {
        mAdapter.close();
        mAdapter = null;
	}

	protected void getGoodDatabase(){
    	showDownloadDialog();
    }
    
    protected void showDownloadDialog(){

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Invalid Database. Do you wish to download a database or choose an existing one?");
		builder.setTitle("Invalid database");
		builder.setNegativeButton("Choose", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				openPreferences();
			}
		});
		builder.setPositiveButton("Download", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				startDownloadDatabase();
			}
		});
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
    }
    
    protected void startDownloadDatabase(){
        Intent i = new Intent(this, DownloadDatabaseActivity.class);
        startActivityForResult(i, ACTIVITY_DOWNLOAD);
    }
    
    protected void startManageBookmarks(){
        Intent i = new Intent(this, ManageBookmarksActivity.class);
        startActivityForResult(i, ACTIVITY_BOOKMARK);
    }
    
    protected void applyPreferences() throws SQLiteException {
        setTheme(SetPreferencesActivity.getPreferedTheme(this));
        setContentView(R.layout.scripture_list);
    }
    
    private void fetchAllVolumes(){
		mAdapter.open();
    	mVolumeCursor = mAdapter.fetchAllVolumes();
    	startManagingCursor(mVolumeCursor);
    	String[] from = new String[] { ScriptureDbAdapter.VOLUME_TITLE };
    	int[] to = new int[] {R.id.text1 };
    	
    	SimpleCursorAdapter volumes = new SimpleCursorAdapter(this, R.layout.scripture_row, mVolumeCursor, from, to);
    	setListAdapter(volumes);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
        Cursor c = mVolumeCursor;
        c.moveToPosition(position);
        Intent i = new Intent(this, BrowseVolumeActivity.class);
        i.putExtra(ScriptureDbAdapter.TABLE_ID, id);
        i.putExtra(ScriptureDbAdapter.VOLUME_TITLE, c.getString(
                c.getColumnIndexOrThrow(ScriptureDbAdapter.VOLUME_TITLE)));
        i.putExtra(ScriptureDbAdapter.VOLUME_TITLE_LONG, c.getString(
                c.getColumnIndexOrThrow(ScriptureDbAdapter.VOLUME_TITLE_LONG)));
        startActivityForResult(i, ACTIVITY_BROWSE_VOLUME);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == ACTIVITY_PREFERENCES) {
        	applyPreferences();
        }
        Log.i(TAG, "preferences applied");
        initializeDatabase();
        if(mAdapter.canMakeValidConnection()){
            Log.i(TAG, "getting volumes");
	    	fetchAllVolumes();
        } else {
            Log.i(TAG, "need new database");
        	getGoodDatabase();
        }
        closeDatabase();
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browse_menu, menu);
    	return result;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
        case R.id.open_preferences:
        	openPreferences();
        	break;
        case R.id.download_database:
        	startDownloadDatabase();
        	break;
        case R.id.manage_bookmarks:
        	startManageBookmarks();
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void openPreferences(){
        Intent i = new Intent(this, SetPreferencesActivity.class);
        startActivityForResult(i, ACTIVITY_PREFERENCES);
    }
    
}