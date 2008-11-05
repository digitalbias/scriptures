package com.digitalbias.android;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;

public class ScriptureDbAdapter {

    public static final String TABLE_ID = "_id";
    
    public static final String VOLUME_ID = "volume_id";
    public static final String VOLUME_TITLE = "volume_title";
    public static final String VOLUME_TITLE_LONG = "volume_title_long";
    
    public static final String BOOK_ID = "book_id";
    public static final String BOOK_TITLE = "book_title";
    public static final String BOOK_TITLE_SHORT = "book_title_short";
    public static final String BOOK_SUBTITLE = "book_subtitle";
    public static final String BOOK_NUM_CHAPTERS = "num_chapters";
    
    public static final String CHAPTER_NUM = "chapter";
    public static final String CHAPTER_TITLE = "chapter_title";

    public static final String VERSE_NUMBER = "verse";
    public static final String VERSE_TEXT = "verse_scripture";
    public static final String VERSE_PILCROW = "pilcrow";
    
    private static final String VALIDATAION_QUERY = "SELECT COUNT(*) as book_count FROM sqlite_master WHERE name = 'books'";
    
	private static final String VOLUME_QUERY_STRING = "SELECT volume_id as _id, volume_title, volume_title_long FROM volumes ORDER BY _id";
	private static final String BOOK_QUERY_STRING = "SELECT book_id as _id, book_title, book_title_short, book_title_jst, book_subtitle, num_chapters FROM books WHERE volume_id = ? ORDER BY _id";
	private static final String CHAPTER_QUERY_STRING = "SELECT DISTINCT v.chapter as _id, (b.book_title || ' ' || v.chapter) AS chapter_title, v.book_id as book_id FROM verses v, books b WHERE v.book_id = b.book_id AND v.book_id = ? ";
	private static final String VERSE_QUERY_STRING_FOR_BOOKS = "SELECT verse_id as _id, volume_id, book_id, chapter, verse, pilcrow, verse_scripture, verse_title, verse_title_short FROM verses WHERE book_id = ? AND chapter = ? ORDER BY volume_id, book_id, chapter, verse";
	private static final String VERSE_QUERY_STRING_FOR_VOLUME = "SELECT verse_id as _id, volume_id, book_id, chapter, verse, pilcrow, verse_scripture, verse_title, verse_title_short FROM verses WHERE volume_id = ? ORDER BY volume_id, book_id, chapter, verse";

	private static final String SINGLE_VERSE_QUERY = "SELECT verse_id as _id, volume_id, book_id, chapter, verse, pilcrow, verse_scripture, verse_title, verse_title_short FROM verses WHERE _id = ?";
	private static final String SINGLE_CHAPTER_QUERY_STRING = "SELECT DISTINCT v.chapter as _id, (b.book_title || ' ' || v.chapter) AS chapter_title, v.book_id as book_id FROM verses v, books b WHERE v.book_id = b.book_id AND v.book_id = ? AND v.chapter = ?";
	private static final String SINGLE_BOOK_QUERY = "SELECT book_id as _id, volume_id, book_title, book_title_short, book_title_jst, book_subtitle, num_chapters FROM books WHERE _id = ? ORDER BY _id";
	private static final String SINGLE_VOLUME_QUERY = "SELECT volume_id as _id, volume_title, volume_title_long FROM volumes WHERE _id = ?";
	
	private static final String GET_MAX_BOOK_ID = "SELECT MAX(book_id) AS book_id FROM books";
	
    private final Context mCtx;
    private SQLiteDatabase mDatabase;

    public ScriptureDbAdapter(Context ctx){
    	mCtx = ctx;
    }
    
    /**
     * Open the database. 
     * @return this (self reference, allows this to be called when initialized)
     * @throws SQLException if the database cannot be opened.
     */
    public ScriptureDbAdapter open() {
	    String databaseLocation = getDatabaseLocation();
    	mDatabase = SQLiteDatabase.openDatabase(databaseLocation, null,  SQLiteDatabase.OPEN_READWRITE);
    	return this;
    }
    
    public String getDatabaseLocation(){
	    SharedPreferences settings = mCtx.getSharedPreferences(SetPreferencesActivity.PREFS_NAME, 0);
	    String databaseLocation = settings.getString(SetPreferencesActivity.DATABASE_PREF, SetPreferencesActivity.DEFAULT_DATABASE_NAME);
		
	    File databaseFile = new File(databaseLocation);
    	if(!databaseFile.exists()){
		    SetPreferencesActivity.resetDatabasePreferences(settings);
		    databaseLocation = settings.getString(SetPreferencesActivity.DATABASE_PREF, SetPreferencesActivity.DEFAULT_DATABASE_NAME);
    	}
    	return databaseLocation;
    }
    
    public boolean canMakeValidConnection(){
    	boolean result = false;
    	File databaseFile = new File(getDatabaseLocation());
    	if (databaseFile.exists()){
    		Log.i("db", "file exists");
    		result = isValidDatabase();
    	}
    	return result;
    }
    
    protected boolean isValidDatabase(){
    	boolean result = false;
    	SQLiteDatabase database = null;
    	Cursor cursor = null;
    	try {
	    	database = SQLiteDatabase.openDatabase(getDatabaseLocation(), null, SQLiteDatabase.OPEN_READONLY);
	    	cursor = database.rawQuery(VALIDATAION_QUERY, null);
	    	cursor.moveToFirst();
	    	int book_count = cursor.getInt(cursor.getColumnIndexOrThrow("book_count")); 
	    	result = book_count >= 1;
    		Log.i("db", Integer.toString(book_count));
    	} catch (SQLiteException e){
    		Log.e("db", "sqlite exception", e);
    	} finally {
	    	if(cursor != null) cursor.close();
	    	if(database != null) database.close();
    	}
    	return result;
    }

    public void close() {
    	if(mDatabase != null) mDatabase.close();
    }
    
    protected Cursor query(String queryString, String[] args){
        return mDatabase.rawQuery(queryString, args);
    }
    
    protected Cursor queryAndMoveToFirst(String queryString, String[] args){
        Cursor cursor = mDatabase.rawQuery(queryString, args);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
    
    protected Bundle fillChapterBundle(Long bookId, Long chapterId){
    	Bundle bundle = new Bundle();
    	
    	bundle.putLong(TABLE_ID, chapterId);
    	bundle.putLong(BOOK_ID, bookId);
    	bundle.putString(CHAPTER_TITLE, getChapterTitle(bookId,chapterId));
    	bundle.putString(BOOK_TITLE_SHORT, getBookTitle(bookId));
    	bundle.putString(VOLUME_TITLE, getVolumeTitle(bookId));
    	
    	return bundle;
    }
    
    protected String getChapterTitle(Long bookId, Long chapterId){
    	Cursor cursor = fetchSingleChapter(bookId.toString(), chapterId.toString());
    	String result = cursor.getString(cursor.getColumnIndexOrThrow(CHAPTER_TITLE));
    	return result;
    }
    
    protected String getBookTitle(Long bookId){
    	Cursor cursor = fetchSingleBook(bookId.toString());
    	String result = cursor.getString(cursor.getColumnIndexOrThrow(BOOK_TITLE_SHORT));
    	return result;
    }
    
    protected String getVolumeTitle(Long bookId){
    	Cursor cursor = fetchSingleBook(bookId.toString());
    	Long volumeId = cursor.getLong(cursor.getColumnIndexOrThrow(VOLUME_ID));
    	cursor = fetchSingleVolume(volumeId.toString());
    	String result = cursor.getString(cursor.getColumnIndexOrThrow(VOLUME_TITLE_LONG));
    	return result;
    }
    
    protected Long getMaxBookId(){
    	Cursor cursor = queryAndMoveToFirst(GET_MAX_BOOK_ID, null);
    	Long result = cursor.getLong(cursor.getColumnIndexOrThrow(BOOK_ID));
    	return result;
    }
    
    public Bundle getPreviousBookAndChapter(Long bookId, Long chapterId){
    	if(chapterId.intValue() == 1){
    		if(bookId == 1){
    			bookId = getMaxBookId();
    		} else {
    			bookId = bookId - 1;
    		}
        	Cursor bookQuery = fetchSingleBook(bookId.toString());
    		chapterId = bookQuery.getLong(bookQuery.getColumnIndexOrThrow(BOOK_NUM_CHAPTERS));
    	} else {
    		chapterId = chapterId - 1;
    	}
    	return fillChapterBundle(bookId, chapterId);
    }

    public Bundle getNextBookAndChapter(Long bookId, Long chapterId){
    	Cursor bookQuery = fetchSingleBook(bookId.toString());
    	Long numChapters = bookQuery.getLong(bookQuery.getColumnIndexOrThrow(BOOK_NUM_CHAPTERS));
    	if(chapterId.equals(numChapters)){
    		Long maxBookId = getMaxBookId();
    		if(bookId.equals(maxBookId)){
    			bookId = Long.valueOf(1);
    		} else {
    			bookId = bookId + 1;
    		}
			chapterId = Long.valueOf(1);
    	} else {
    		chapterId = chapterId + 1;
    	}
    	//Log.d("ScriptureDB", "chapterId:" + chapterId.toString());
    	//Log.d("ScriptureDB", "bookId: " + bookId.toString());
    	
    	return fillChapterBundle(bookId, chapterId);
    }
    
    public Cursor fetchAllVolumes(){
        return queryAndMoveToFirst(VOLUME_QUERY_STRING, null);
    }
    
    public Cursor fetchVolumeBooks(String volumeId) {
    	String[] args = new String[] { volumeId};
        return queryAndMoveToFirst(BOOK_QUERY_STRING, args);
    }

    public Cursor fetchBookChapters(String bookId){
    	String[] args = new String[] { bookId};
        return queryAndMoveToFirst(CHAPTER_QUERY_STRING, args);
    }
    
    public Cursor fetchBookVerses(String bookId, String chapter) {
    	String[] args = new String[] { bookId, chapter};
        return query(VERSE_QUERY_STRING_FOR_BOOKS, args);
    }
    
    public Cursor fetchVolumeVerses(String volumeId) {
    	String[] args = new String[] { volumeId};
        return query(VERSE_QUERY_STRING_FOR_VOLUME, args);
    }
    
    public Cursor fetchSingleVerse(String id){
    	String[] args = new String[] {id};
    	return queryAndMoveToFirst(SINGLE_VERSE_QUERY, args);
    }

    public Cursor fetchSingleChapter(String bookId, String chapterId){
    	String[] args = new String[] {bookId, chapterId};
    	return queryAndMoveToFirst(SINGLE_CHAPTER_QUERY_STRING, args);
    }

    public Cursor fetchSingleBook(String id){
    	String[] args = new String[] {id};
    	return queryAndMoveToFirst(SINGLE_BOOK_QUERY, args);
    }
    
    public Cursor fetchSingleVolume(String id){
    	String[] args = new String[] {id};
    	return queryAndMoveToFirst(SINGLE_VOLUME_QUERY, args);
    }

}
