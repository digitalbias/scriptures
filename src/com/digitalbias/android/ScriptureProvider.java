package com.digitalbias.android;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

public class ScriptureProvider  extends ContentProvider {

    public static String AUTHORITY = "scripture";

    private static final int SEARCH_SUGGEST = 0;
    private static final int SHORTCUT_REFRESH = 1;
    private static final UriMatcher sURIMatcher = buildUriMatcher();
    
    /**
     * The columns we'll include in our search suggestions.  There are others that could be used
     * to further customize the suggestions, see the docs in {@link SearchManager} for the details
     * on additional columns that are supported.
     */
    private static final String[] COLUMNS = {
            "_id",  // must include this column
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
            };

    /**
     * Sets up a uri matcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
//        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, SHORTCUT_REFRESH);
//        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SHORTCUT_REFRESH);
        return matcher;
    }

	private Cursor mCursor;
	private ScriptureDbAdapter mAdapter;

	@Override
	public boolean onCreate() {
		initializeDatabase();
        return true;
	}

    /**
     * Note: this is unused as is, but if we included
     * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our results, we
     * could expect to receive refresh queries on this uri for the id provided, in which case we
     * would return a cursor with a single item representing the refreshed suggestion data.
     */
    private Cursor refreshShortcut(String shortcutId, String[] projection) {
        return null;
    }

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        if (!TextUtils.isEmpty(selection)) {
            throw new IllegalArgumentException("selection not allowed for " + uri);
        }
        if (selectionArgs != null && selectionArgs.length != 0) {
            throw new IllegalArgumentException("selectionArgs not allowed for " + uri);
        }
        if (!TextUtils.isEmpty(sortOrder)) {
            throw new IllegalArgumentException("sortOrder not allowed for " + uri);
        }
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                String query = null;
                if (uri.getPathSegments().size() > 1) {
                    query = uri.getLastPathSegment().toLowerCase();
                }
                return getSuggestions(query, projection);
            case SHORTCUT_REFRESH:
                String shortcutId = null;
                if (uri.getPathSegments().size() > 1) {
                    shortcutId = uri.getLastPathSegment();
                }
                return refreshShortcut(shortcutId, projection);
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
	}

    private Cursor getSuggestions(String query, String[] projection) {
        String processedQuery = query == null ? "" : query.toLowerCase();
        
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        
        Log.i("search provider", processedQuery);
    	if (!processedQuery.equals("")) {
	    	Cursor searchResults = mAdapter.searchAllVerses(processedQuery);
	
	    	while(searchResults.moveToNext()){
	            cursor.addRow(columnValuesOfWord(searchResults));
	    	}
    	}
        return cursor;
    }

    private Object[] columnValuesOfWord(Cursor cursor) {
    	StringBuilder builder = new StringBuilder();
    	
		String[] result = new String[COLUMNS.length];
		result[0] = cursor.getString(cursor.getColumnIndexOrThrow(ScriptureDbAdapter.TABLE_ID)); 
		result[1] = cursor.getString(cursor.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_TEXT)); 
    	
		builder.append(cursor.getString(cursor.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_SHORT_TITLE)));
		builder.append("<p>");
		builder.append(cursor.getString(cursor.getColumnIndexOrThrow(ScriptureDbAdapter.VERSE_TEXT)));
		builder.append("</p>");

		result[2] = Html.fromHtml(builder.toString()).toString(); 
		result[3] = cursor.getString(cursor.getColumnIndexOrThrow(ScriptureDbAdapter.TABLE_ID)); 
		
    	return result;
    }
    
    private void initializeDatabase() {
        if(mCursor != null) {
        	mCursor.close();
        	mCursor = null;
        }
        if(mAdapter != null) {
        	mAdapter.close();
        }
        mAdapter = new ScriptureDbAdapter(getContext().getSharedPreferences(SetPreferencesActivity.PREFS_NAME, 0));
	}
    
    /**
     * All queries for this provider are for the search suggestion and shortcut refresh mime type.
     */
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
//            case SHORTCUT_REFRESH:
//                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

	@Override
	public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        throw new UnsupportedOperationException();
	}

    @Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
        throw new UnsupportedOperationException();
	}

}
