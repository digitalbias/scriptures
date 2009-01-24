package com.digitalbias.db.migration;

import com.digitalbias.android.ScriptureDbAdapter;

import android.database.sqlite.SQLiteDatabase;

public class DatabaseUpgrader {
	
	public static void upgradeDatabase(SQLiteDatabase database){
		int databaseVersion = ScriptureDbAdapter.getDatabaseVersion(database);
		Migration migration = null;
		switch(databaseVersion){
			case -1:
				migration = new Migration0(database);
				break;
			case 0:
				migration = new Migration1(database);
				break;
			case 1:
				break;
		}
		if(migration != null) migration.migrate();
	}
	
}
