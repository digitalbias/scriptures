package com.digitalbias.db.migration;

import android.database.sqlite.SQLiteDatabase;

public class Migration1 extends Migration {
	private static final String ALTER_BOOKMARKS_TABLE = "ALTER TABLE bookmarks ADD COLUMN position INTEGER";
	private static final String REMOVE_BOOKMARKS_POSITION = "ALTER TABLE bookmarks DROP COLUMN position";

	public Migration1(SQLiteDatabase database) {
		super(database);
	}

	public boolean migrate() {
		alterBookmarkTable();
		return updateDatabaseVersion(1);
	}
	
	protected void alterBookmarkTable(){
		mDatabase.execSQL(ALTER_BOOKMARKS_TABLE);
	}
	
	public boolean revert(){
		removePosition();
		return updateDatabaseVersion(0);
	}
	
	protected void removePosition(){
		executeSQL(REMOVE_BOOKMARKS_POSITION, null);
	}
	
}
