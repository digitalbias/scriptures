package com.digitalbias.db.migration;

import android.database.sqlite.SQLiteDatabase;

public class Migration0 extends Migration {
	private static final String CREATE_METADATA_TABLE = "CREATE TABLE metadata (_id INTEGER PRIMARY KEY, key TEXT, value TEXT)";
	private static final String CREATE_BOOKMARKS_TABLE = "CREATE TABLE bookmarks (_id INTEGER PRIMARY KEY, book_id INTEGER, chapter INTEGER, title TEXT);";
	private static final String CREATE_NOTES_TABLE = "CREATE TABLE notes (_id INTEGER PRIMARY KEY, book_id INTEGER, chapter INTEGER, verse_id INTEGER, note TEXT);";
	private static final String CREATE_MARKINGS_TABLE = "CREATE TABLE markings (_id INTEGER PRIMARY KEY, book_id INTEGER, chapter INTEGER, verse_id INTEGER, mark_type INTEGER, color TEXT);";

	private static final String DROP_BOOKMARKS_TABLE = "DROP TABLE;";
	private static final String DROP_NOTES_TABLE = "DROP TABLE notes;";
	private static final String DROP_MARKINGS_TABLE = "DROP TABLE markings;";

	public Migration0(SQLiteDatabase database) {
		super(database);
	}

    public boolean migrate() {
		createMissingTables();
		return updateDatabaseVersion(0);
    }
    
    protected void createMissingTables(){
    	if(!tableExists(mDatabase, "bookmarks")){
    		mDatabase.execSQL(CREATE_BOOKMARKS_TABLE);
    	} 
    	if(!tableExists(mDatabase, "notes")){
    		mDatabase.execSQL(CREATE_NOTES_TABLE);
    	}
    	if(!tableExists(mDatabase, "markings")){
    		mDatabase.execSQL(CREATE_MARKINGS_TABLE);
    	}
    	if(!tableExists(mDatabase, "metadata")){
    		mDatabase.execSQL(CREATE_METADATA_TABLE);
    		insertInitialData();
    	}
    }

	private void insertInitialData() {
		addMetadataKeyValuePair("database_version", "-1");
	}
    
    public boolean revert() {
		mDatabase.execSQL(DROP_BOOKMARKS_TABLE);
		mDatabase.execSQL(DROP_NOTES_TABLE);
		mDatabase.execSQL(DROP_MARKINGS_TABLE);
    	
		return updateDatabaseVersion(-1);
    }
	
}
