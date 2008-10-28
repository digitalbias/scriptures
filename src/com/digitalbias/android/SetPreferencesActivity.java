package com.digitalbias.android;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SetPreferencesActivity extends Activity {
	protected static final String PREFS_NAME = "ScripturePrefs";

	protected static final String DEFAULT_THEME = "Theme_Light";
	protected static final String THEME_PREF = "theme";

    protected static final String DEFAULT_DATABASE_NAME = "/sdcard/scriptures.db";
    protected static final String DATABASE_PREF = "database_location";

    public static void resetDatabasePreferences(SharedPreferences settings){
    	SharedPreferences.Editor editor = settings.edit();
		editor.putString(DATABASE_PREF, DEFAULT_DATABASE_NAME);
		editor.commit();
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Light);
        setContentView(R.layout.preferences);

    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String preference = settings.getString(DATABASE_PREF, DEFAULT_DATABASE_NAME);
        EditText text = (EditText)findViewById(R.id.databaseEntry);
        text.setText(preference);
        
        preference = settings.getString(THEME_PREF, DEFAULT_THEME);
        text = (EditText)findViewById(R.id.themeEntry);
        text.setText(preference);
        
        Button button = (Button)findViewById(R.id.okButton);
        button.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		if (savePreferences()){
	                onCloseClick(RESULT_OK);
        		}
        	}
        });
        button = (Button)findViewById(R.id.cancelButton);
        button.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
                onCloseClick(RESULT_CANCELED);
        	}
        });
        
    }
    
    protected String getTextValue(int controlId){
    	EditText text = (EditText)findViewById(controlId);
        String preference = text.getText().toString();
        return preference;
    }
    
    private boolean savePreferences(){
    	
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	
    	String preference = getTextValue(R.id.databaseEntry);
    	File file = new File(preference);
    	if(!file.exists()) {
    		return false;
    	}
		editor.putString(DATABASE_PREF, preference);
    	
    	preference = getTextValue(R.id.themeEntry);
    	editor.putString(THEME_PREF, preference);

    	editor.commit();
    	return true;
    }
    
    private void onCloseClick(int resultCode){
        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(resultCode, intent);
        finish();
    }
}
