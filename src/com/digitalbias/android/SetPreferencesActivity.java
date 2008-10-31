package com.digitalbias.android;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SetPreferencesActivity extends Activity {
	protected static final String PREFS_NAME = "ScripturePrefs";

	protected static final String DEFAULT_THEME = "Theme_Light";
	protected static final String THEME_PREF = "theme";
	
	protected static final int LIGHT_THEME = 0;
	protected static final int DARK_THEME = 1;

	protected static final int CHOOSE_FILE_ACTIVITY = 0;
	
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

    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	
        setTheme(getPreferedTheme(this));
        setContentView(R.layout.preferences);
    	
        String preference = settings.getString(DATABASE_PREF, DEFAULT_DATABASE_NAME);
        EditText text = (EditText)findViewById(R.id.databaseEntry);
        text.setText(preference);
        
        preference = settings.getString(THEME_PREF, DEFAULT_THEME);
        Spinner spinner = (Spinner)findViewById(R.id.themeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.themes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        setSpinnerTheme(spinner, preference);
        
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
        button = (Button)findViewById(R.id.chooseFileButton);
        button.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
                openFileDialog(v);
        	}
        });
        
    }
    
    protected void openFileDialog(View v){
        Intent i = new Intent(this, FileList.class);
        i.putExtra(DATABASE_PREF, getTextValue(R.id.databaseEntry));
        startActivityForResult(i, CHOOSE_FILE_ACTIVITY);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Bundle extras = intent.getExtras();
    	String databaseLocation = extras.getString(DATABASE_PREF);
        EditText text = (EditText)findViewById(R.id.databaseEntry);
        text.setText(databaseLocation);
    }

    public static int getPreferedTheme(Context context){
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        String preference = settings.getString(THEME_PREF, DEFAULT_THEME);
        return getThemePreference(preference);
    }
    
    protected static int getThemePreference(String themeValue){
    	int result = android.R.style.Theme_Light_NoTitleBar;
    	if(themeValue.indexOf("Dark") >= 0){
    		result = android.R.style.Theme_Black_NoTitleBar;
    	}
    	return result;
    }
    
    protected void setSpinnerTheme(Spinner spinner, String themeValue){
    	if(themeValue.indexOf("Dark") >= 0){
    		spinner.setSelection(DARK_THEME);
    	} else {
    		spinner.setSelection(LIGHT_THEME);
    	}
    }

    protected String getTextValue(int controlId){
    	EditText text = (EditText)findViewById(controlId);
        String preference = text.getText().toString();
        return preference;
    }
    
    protected String getSelectedTheme(int controlId){
    	Spinner spinner = (Spinner)findViewById(controlId);
        return spinner.getSelectedItem().toString();
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
    	
    	preference = getSelectedTheme(R.id.themeSpinner);
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
