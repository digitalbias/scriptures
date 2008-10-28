package com.digitalbias.android;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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
    
	private static final String VOLUME_QUERY_STRING = "SELECT volume_id as _id, volume_title, volume_title_long FROM lds_scriptures_volumes ORDER BY _id";
	private static final String BOOK_QUERY_STRING = "SELECT book_id as _id, book_title, book_title_short, book_title_jst, book_subtitle, num_chapters FROM lds_scriptures_books WHERE volume_id = ? ORDER BY _id";
	private static final String CHAPTER_QUERY_STRING = "SELECT DISTINCT v.chapter as _id, (b.book_title || ' ' || v.chapter) AS chapter_title, v.book_id as book_id FROM lds_scriptures_verses v, lds_scriptures_books b WHERE v.book_id = b.book_id AND v.book_id = ? ";
	private static final String VERSE_QUERY_STRING_FOR_BOOKS = "SELECT verse_id as _id, volume_id, book_id, chapter, verse, pilcrow, verse_scripture, verse_title, verse_title_short FROM lds_scriptures_verses WHERE book_id = ? AND chapter = ? ORDER BY volume_id, book_id, chapter, verse";
	private static final String VERSE_QUERY_STRING_FOR_VOLUME = "SELECT verse_id as _id, volume_id, book_id, chapter, verse, pilcrow, verse_scripture, verse_title, verse_title_short FROM lds_scriptures_verses WHERE volume_id = ? ORDER BY volume_id, book_id, chapter, verse";

	private static final String SINGLE_VERSE_QUERY = "SELECT verse_id as _id, volume_id, book_id, chapter, verse, pilcrow, verse_scripture, verse_title, verse_title_short FROM lds_scriptures_verses WHERE _id = ?";
	private static final String SINGLE_BOOK_QUERY = "SELECT book_id as _id, volume_id, book_title, book_title_short, book_title_jst, book_subtitle, num_chapters FROM lds_scriptures_books WHERE _id = ? ORDER BY _id";
	private static final String SINGLE_VOLUME_QUERY = "SELECT volume_id as _id, volume_title, volume_title_long FROM lds_scriptures_volumes WHERE _id = ?";
	
	
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
	    SharedPreferences settings = mCtx.getSharedPreferences(SetPreferencesActivity.PREFS_NAME, 0);
	    String databaseLocation = settings.getString(SetPreferencesActivity.DATABASE_PREF, SetPreferencesActivity.DEFAULT_DATABASE_NAME);
		
	    File databaseFile = new File(databaseLocation);
    	if(!databaseFile.exists()){
		    SetPreferencesActivity.resetDatabasePreferences(settings);
		    databaseLocation = settings.getString(SetPreferencesActivity.DATABASE_PREF, SetPreferencesActivity.DEFAULT_DATABASE_NAME);
    	}

    	mDatabase = SQLiteDatabase.openDatabase(databaseLocation, null,  SQLiteDatabase.OPEN_READONLY);
    	return this;
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

    public Cursor fetchSingleBook(String id){
    	String[] args = new String[] {id};
    	return queryAndMoveToFirst(SINGLE_BOOK_QUERY, args);
    }
    
    public Cursor fetchSingleVolume(String id){
    	String[] args = new String[] {id};
    	return queryAndMoveToFirst(SINGLE_VOLUME_QUERY, args);
    }

}
