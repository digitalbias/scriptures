package com.digitalbias.db.migration;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public abstract class Migration {
	protected SQLiteDatabase mDatabase;
	
	protected static final String TABLE_EXISTS_QUERY = "SELECT count(*) AS count FROM sqlite_master WHERE type = 'table' AND name = ?";
	private static final String NEW_METADATA = "INSERT INTO metadata (key, value) VALUES (?, ?)";
	private static final String UPDATE_METADATA = "UPDATE metadata SET value = ? WHERE key = ?";

	public Migration(SQLiteDatabase database){
		mDatabase = database;
	}
	
	public abstract boolean migrate();
	public abstract boolean revert();

	protected boolean updateDatabaseVersion(int newVersion){
		String databaseVersion = Integer.toString(newVersion);
		updateMetadata("database_version", databaseVersion);
		return true;
	}
	
	protected void updateMetadata(String key, String value){
    	Object[] args = new Object[] { value, key };
		executeSQL(UPDATE_METADATA, args);
	}
	
	protected void addMetadataKeyValuePair(String key, String value){
    	Object[] args = new Object[] { key, value };
		executeSQL(NEW_METADATA, args);
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

    protected boolean tableExists(SQLiteDatabase database, String tableName){
    	boolean result = false;
    	
    	Cursor cursor = null;
    	try {
        	String[] args = new String[] { tableName };
    		cursor = queryAndMoveToFirst(database, TABLE_EXISTS_QUERY, args);
    		int count = cursor.getInt(cursor.getColumnIndex("count"));
    		result = count > 0;
    	} catch (SQLiteException e){
    	} catch (NullPointerException e){
    	} finally {
    		if(cursor != null) cursor.close();
    	}
    	return result;
    }
    
	
}
