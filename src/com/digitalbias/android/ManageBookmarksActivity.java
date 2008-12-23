package com.digitalbias.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ManageBookmarksActivity extends ListActivity {

	private static final int ACTIVITY_READ_CHAPTER = 0;
	
	private static final int GOTO_MENU_ORDER = 0;
	private static final int EDIT_MENU_ORDER = 1;
	private static final int DELETE_MENU_ORDER = 2;
	
	private Context mContext;
	private ScriptureDbAdapter mAdapter;
	private Cursor mCursor;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setTheme(SetPreferencesActivity.getPreferedTheme(this));
        setContentView(R.layout.bookmark_list);

        registerForContextMenu(getListView());

        Bundle extras = getIntent().getExtras();
        populateList(extras);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bookmark_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        long bookmarkId = info.id;
        int menuId = item.getOrder();
        switch (menuId){
        	case GOTO_MENU_ORDER:
        		gotoBookmark(bookmarkId);
        		break;
        	case EDIT_MENU_ORDER:
        		editBookmark(bookmarkId);
        		break;
        	case DELETE_MENU_ORDER:
        		deleteBookmark(bookmarkId);
        		break;
        }
        return true;
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
        Cursor c = mCursor;
        c.moveToPosition(position);
        long bookmarkId = c.getLong(c.getColumnIndex("_id"));
        gotoBookmark(bookmarkId);
    }
    
    protected void gotoBookmark(long bookmarkId){
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
    
    protected void editBookmark(final long bookmarkId){
        debug("edit: " + bookmarkId);
    	final Dialog dialog = new Dialog(this);
    	dialog.setTitle(R.string.add_bookmark);
    	dialog.setContentView(R.layout.add_bookmark);
    	
    	Cursor bookmarkInfo = mAdapter.fetchSingleBookmark(bookmarkId);
    	
    	TextView locationText = (TextView) dialog.findViewById(R.id.location_text);
    	locationText.setText(bookmarkInfo.getString(bookmarkInfo.getColumnIndex(ScriptureDbAdapter.BOOKMARK_CHAPTER_TITLE)));
    	EditText titleEdit = (EditText) dialog.findViewById(R.id.title_edit);
    	titleEdit.setText(bookmarkInfo.getString(bookmarkInfo.getColumnIndex(ScriptureDbAdapter.BOOKMARK_TITLE)));
    	final long bookId = bookmarkInfo.getLong(bookmarkInfo.getColumnIndex(ScriptureDbAdapter.BOOKMARK_BOOK_ID));
    	final long chapter = bookmarkInfo.getLong(bookmarkInfo.getColumnIndex(ScriptureDbAdapter.BOOKMARK_CHAPTER));
    	
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
		    	EditText titleEdit = (EditText) dialog.findViewById(R.id.title_edit);
				String title = titleEdit.getText().toString();
		    	mAdapter.updateBookmark(new Long(bookmarkId), new Long(bookId), new Long(chapter), title);
				debug("bookmark updated");
				dialog.dismiss();
				fetchBookmarks();
			}
		});
    	
    	dialog.show();
    }
    
    protected void deleteBookmark(final long bookmarkId){
        debug("delete: " + bookmarkId);
        mContext = this;
        AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("Delete bookmark?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	mAdapter.deleteBookmark(new Long(bookmarkId));
            	Toast.makeText(mContext, "Bookmark Deleted", Toast.LENGTH_LONG);
            	fetchBookmarks();
            }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
        .create();
        
        dialog.show();
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

    private void debug(String message){
		if(BrowseScriptureActivity.DEBUG){
	    	Log.d("bookmarks", message);
	    	Toast.makeText(this, message, Toast.LENGTH_LONG);
    	}
    }
}
