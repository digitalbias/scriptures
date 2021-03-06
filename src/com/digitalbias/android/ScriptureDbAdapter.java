package com.digitalbias.android;

import java.io.File;

import com.digitalbias.db.migration.DatabaseUpgrader;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;

public class ScriptureDbAdapter {

    protected static final int DATABASE_VERSION = 1; 
    
	public static final String TABLE_ID = "_id";
    
    public static final String VOLUME_ID = "volume_id";
    public static final String VOLUME_TITLE = "volume_title";
    public static final String VOLUME_TITLE_LONG = "volume_title_long";
    
    public static final String BOOK_ID = "book_id";
    public static final String BOOK_TITLE = "book_title";
    public static final String BOOK_TITLE_SHORT = "book_title_short";
    public static final String BOOK_SUBTITLE = "book_subtitle";
    public static final String BOOK_NUM_CHAPTERS = "num_chapters";
    
    public static final String CHAPTER_ID = "chapter";
    public static final String CHAPTER_NUM = "chapter";
    public static final String CHAPTER_TITLE = "chapter_title";

    public static final String VERSE_ID = "verse_id";
    public static final String VERSE_NUMBER = "verse";
    public static final String VERSE_SHORT_TITLE = "verse_title_short";
    public static final String VERSE_TEXT = "verse_scripture";
    public static final String VERSE_PILCROW = "pilcrow";
    
    public static final String BOOKMARK_NUMBER = "_id";
    public static final String BOOKMARK_BOOK_ID = "book_id";
    public static final String BOOKMARK_CHAPTER = "chapter";
    public static final String BOOKMARK_CHAPTER_TITLE = "chapter_title";
    public static final String BOOKMARK_TITLE = "title";
    public static final String BOOKMARK_POSITION = "position";
    
    private static final String GET_DATABASE_VERSION = "SELECT value FROM metadata WHERE key = 'database_version'";
    
	private static final String VOLUME_QUERY_STRING = "SELECT volume_id as _id, volume_title, volume_title_long FROM volumes ORDER BY _id";
	private static final String BOOK_QUERY_STRING = "SELECT book_id as _id, book_title, book_title_short, book_title_jst, book_subtitle, num_chapters FROM books WHERE volume_id = ? ORDER BY _id";
	private static final String CHAPTER_QUERY_STRING = "SELECT DISTINCT v.chapter as _id, (b.book_title || ' ' || v.chapter) AS chapter_title, v.book_id as book_id FROM verses v, books b WHERE v.book_id = b.book_id AND v.book_id = ? ";
	private static final String VERSE_QUERY_STRING_FOR_BOOKS = "SELECT verse_id as _id, volume_id, book_id, chapter, verse, pilcrow, verse_scripture, verse_title, verse_title_short FROM verses WHERE book_id = ? AND chapter = ? ORDER BY volume_id, book_id, chapter, verse";
	private static final String VERSE_QUERY_STRING_FOR_VOLUME = "SELECT verse_id as _id, volume_id, book_id, chapter, verse, pilcrow, verse_scripture, verse_title, verse_title_short FROM verses WHERE volume_id = ? ORDER BY volume_id, book_id, chapter, verse";

	private static final String VERSE_QUERY_STRING_FOR_ALL = "SELECT verse_id as _id, volume_id, book_id, chapter, verse, pilcrow, verse_scripture, verse_title, verse_title_short FROM verses WHERE verse_scripture like ? LIMIT 25";

	private static final String SINGLE_VERSE_QUERY = "SELECT verse_id as _id, volume_id, book_id, chapter, verse, pilcrow, verse_scripture, verse_title, verse_title_short FROM verses WHERE _id = ?";
	private static final String SINGLE_CHAPTER_QUERY_STRING = "SELECT DISTINCT v.chapter as _id, (b.book_title || ' ' || v.chapter) AS chapter_title, v.book_id AS book_id, o.volume_title AS volume_title, o.volume_title_long AS volume_title_long, b.book_title AS book_title, b.book_title_short AS book_title_short FROM verses v, books b, volumes o WHERE o.volume_id = b.volume_id AND v.book_id = b.book_id AND v.book_id = ? AND v.chapter = ?";
	private static final String SINGLE_BOOK_QUERY = "SELECT book_id as _id, volume_id, book_title, book_title_short, book_title_jst, book_subtitle, num_chapters FROM books WHERE _id = ? ORDER BY _id";
	private static final String SINGLE_VOLUME_QUERY = "SELECT volume_id as _id, volume_title, volume_title_long FROM volumes WHERE _id = ?";

	private static final String GET_MAX_BOOK_ID = "SELECT MAX(book_id) AS book_id FROM books";
	
	private static final String ALL_BOOKMARKS_QUERY = "SELECT DISTINCT m._id AS _id, (b.book_title || ' ' || v.chapter) AS chapter_title, m.book_id as book_id , m.chapter as chapter, m.title as title, m.position as position FROM verses v, books b, bookmarks m WHERE v.book_id = b.book_id AND m.book_id = v.book_id AND v.chapter = m.chapter ORDER BY book_id, chapter";
	private static final String SINGLE_BOOKMARK_QUERY = "SELECT DISTINCT m._id AS _id, (b.book_title || ' ' || v.chapter) AS chapter_title, m.book_id as book_id , m.chapter as chapter, m.title as title, m.position as position FROM verses v, books b, bookmarks m WHERE v.book_id = b.book_id AND m.book_id = v.book_id AND v.chapter = m.chapter AND m._id = ?";
	private static final String NEW_BOOKMARK = "INSERT INTO bookmarks (book_id, chapter, title) VALUES (?, ?, ?, ?)";
	private static final String UPDATE_BOOKMARK = "UPDATE bookmarks SET book_id = ?, chapter = ?, title = ?, position = ? WHERE _id = ?";
	private static final String MOVE_BOOKMARK = "UPDATE bookmarks SET book_id = ?, chapter = ?, position = ? WHERE _id = ?";
	private static final String DELETE_BOOKMARK = "DELETE FROM bookmarks WHERE _id = ?";

	
	private static final String ALL_NOTES_QUERY = "SELECT _id, book_id, chapter, verse_id, note FROM notes";
	private static final String CHAPTER_NOTES_QUERY = "SELECT _id, book_id, chapter, verse_id, note FROM notes WHERE book_id = ? AND chapter = ?";
	private static final String NOTES_QUERY_EXACT_SEARCH = "SELECT _id, book_id, chapter, verse_id, note FROM notes WHERE note LIKE '%?%'";
	private static final String NEW_NOTE = "INSERT notes";
	private static final String UPDATE_NOTE = "UPDATE notes";
	private static final String DELETE_NOTE = "DELETE FROM notes WHERE _id = ?";

	
	private static final String ALL_MARKINGS_QUERY = "SELECT _id, book_id, chapter, verse_id, mark_type, color FROM markings";
	private static final String CHAPTER_MARKINGS_QUERY = "SELECT _id, book_id, chapter, verse_id, mark_type, color FROM markings WHERE book_id = ? AND chapter = ?";

	private SharedPreferences settings = null;
    private static SQLiteDatabase mDatabase;
    private static int connectionCount = 0;

    public ScriptureDbAdapter(SharedPreferences preferences){
	    settings = preferences; 
	    	//getSharedPreferences(SetPreferencesActivity.PREFS_NAME, 0);
    }
    
    /**
     * Open the database. 
     * @return this (self reference, allows this to be called when initialized)
     * @throws SQLException if the database cannot be opened.
     */
    public ScriptureDbAdapter open() {
    	if(mDatabase == null){
		    String databaseLocation = getDatabaseLocation();
	    	mDatabase = SQLiteDatabase.openDatabase(databaseLocation, null,  SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    	}
    	connectionCount = connectionCount + 1;
    	return this;
    }
    
    public String getDatabaseLocation(){
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
    		log("db", "file exists");
    		result = isValidDatabase();
    	}
    	return result;
    }
    
    public static int getDatabaseVersion(SQLiteDatabase database){
    	int result = -1; 
    	Cursor cursor = null;
    	try {
	    	cursor = database.rawQuery(GET_DATABASE_VERSION, null);
	    	cursor.moveToFirst();
	    	result = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("value")));
    	} catch (SQLiteException e) {
    	} finally {
	    	if(cursor != null) cursor.close();
    	}
    	return result;
    }

    protected boolean isValidDatabase(){
    	boolean result = false;
    	SQLiteDatabase database = null;
    	try {
	    	database = SQLiteDatabase.openDatabase(getDatabaseLocation(), null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	    	int databaseVersion = getDatabaseVersion(database);
	    	result = databaseVersion == DATABASE_VERSION;
	    	if (!result) {
	    		upgrade(database);
	    		result = DATABASE_VERSION == getDatabaseVersion(database);
	    	}
    	} catch (SQLiteException e){
    		log("db", "sqlite exception", e);
    	} finally {
	    	if(database != null) database.close();
    	}
    	return result;
    }

    public void upgrade(SQLiteDatabase database){
    	int databaseVersion = getDatabaseVersion(database);
    	while(databaseVersion != DATABASE_VERSION) {
    		DatabaseUpgrader.upgradeDatabase(database);
    		databaseVersion = getDatabaseVersion(database);
    	}
    }
    
    public void close() {
    	connectionCount = connectionCount -1;
    	if(connectionCount <= 0 && mDatabase != null) { 
    		mDatabase.close();
    		mDatabase = null;
    	}
    }
    
    protected Cursor query(SQLiteDatabase database, String queryString, String[] args){
        return database.rawQuery(queryString, args);
    }

    protected Cursor query(String queryString, String[] args){
        return query(mDatabase, queryString, args);
    }
    
    protected Cursor queryAndMoveToFirst(SQLiteDatabase database, String queryString, String[] args){
        Cursor cursor = database.rawQuery(queryString, args);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
    
    protected Cursor queryAndMoveToFirst(String queryString, String[] args){
    	return queryAndMoveToFirst(mDatabase, queryString, args);
    }

    protected void executeSQL(SQLiteDatabase database, String queryString, Object[] args){
    	database.execSQL(queryString, args);
    }
    
    protected void executeSQL(String queryString, Object[] args){
    	executeSQL(mDatabase, queryString, args);
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
    	log("ScriptureDB", "chapterId:" + chapterId.toString());
    	log("ScriptureDB", "bookId: " + bookId.toString());
    	
    	return fillChapterBundle(bookId, chapterId);
    }
    
    protected void log(String tag, String message){
		if(BrowseScriptureActivity.DEBUG){
	    	Log.i(tag, message);
		}
    }
    
    protected void log(String tag, String message, Throwable th){
		if(BrowseScriptureActivity.DEBUG){
	    	Log.e(tag, message, th);
		}
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
    
    public Cursor fetchBookmarks() {
    	String[] args = new String[] { };
        return query(ALL_BOOKMARKS_QUERY, args);
    }
    
    public void createBookmark(Integer book, Integer chapter, String title, Integer position){
//    	Object[] args = new Object[] { book, chapter, title };
    	ContentValues values = new ContentValues();
    	values.put("book_id", book);
    	values.put("chapter", chapter);
    	values.put("title", title);
    	values.put("position", position);
    	long rowId = mDatabase.insertOrThrow("bookmarks", "", values);
//    	executeSQL(NEW_BOOKMARK, args);
    	log("db","bookmark created: " + Long.toString(rowId));
    }
    
    public void updateBookmark(Long bookmarkId, Long book, Long chapter, String title, Long position){
    	Object[] args = new Object[] { book, chapter, title, position, bookmarkId };
    	executeSQL(UPDATE_BOOKMARK, args);
    }

	public void updateBookmark(Long bookmarkId, Long book, Long chapter, Long position) {
    	Object[] args = new Object[] { book, chapter, position, bookmarkId };
    	executeSQL(MOVE_BOOKMARK, args);
	}
	
    public void deleteBookmark(Long bookmark_id){
    	Object[] args = new Object[] { bookmark_id };
    	executeSQL(DELETE_BOOKMARK, args);
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

	public Cursor fetchSingleBookmark(long bookmarkId) {
    	String[] args = new String[] {Long.toString(bookmarkId)};
    	return queryAndMoveToFirst(SINGLE_BOOKMARK_QUERY, args);
	}
	
	public Cursor searchAllVerses(String contentString) {
		String[] args = new String[] {'%' + contentString + '%'};
		return query(VERSE_QUERY_STRING_FOR_ALL, args);
	}

}
