package com.digitalbias.android;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class SetPreferencesActivity extends Activity {
	
	protected static final String PREFS_NAME = "ScripturePrefs";

	protected static final String DEFAULT_THEME = "Theme_Light";
	protected static final String THEME_PREF = "theme";
	
	protected static final int LIGHT_THEME = 0;
	protected static final int DARK_THEME = 1;

	protected static final String DEFAULT_FONT = "Small";
	protected static final String VERSE_SIZE_FONT_PREF = "verseSizeFont";
	
	protected static final boolean DEFAULT_SCREEN_ORIENTATION = true;
	protected static final String SCREEN_ORIENTATION_PREF = "useScreenOrientation";

	protected static final String SCROLL_TO_TOP_ON_CHAPTER_CHANGE_PREF = "scrollToTopOnChapterChange";
	protected static final boolean SCROLL_TO_TOP_ON_CHAPTER_CHANGE_DEFAULT = true;
	
	protected static final int SMALL_FONT = 0;
	protected static final int MEDIUM_FONT = 1;
	protected static final int LARGE_FONT = 2;

	protected static final int SMALL_FONT_SIZE = 13;
	protected static final int MEDIUM_FONT_SIZE = 18;
	protected static final int LARGE_FONT_SIZE = 21;

	protected static final int CHOOSE_FILE_ACTIVITY = 0;
	
    protected static final String DEFAULT_DATABASE_NAME = "scriptures.db";
    protected static final String DEFAULT_DATABASE_DIRECTORY = "/scriptures/";
    protected static final String DATABASE_PREF = "database_location";

    protected boolean themeChanged = false;
    
    public static String getDatabaseDirectory(){
    	return android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + DEFAULT_DATABASE_DIRECTORY;
    }
    
    public static void resetDatabasePreferences(SharedPreferences settings){
    	SharedPreferences.Editor editor = settings.edit();
    	
		editor.putString(DATABASE_PREF, getDatabaseDirectory() + DEFAULT_DATABASE_NAME);
		editor.commit();
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	
        setTheme(getPreferedTheme(this));
        setContentView(R.layout.preferences);
    	
        String preference = settings.getString(DATABASE_PREF, getDatabaseDirectory() + DEFAULT_DATABASE_NAME);
        EditText text = (EditText)findViewById(R.id.databaseEntry);
        text.setText(preference);
        
        int fontSize = settings.getInt(VERSE_SIZE_FONT_PREF, SMALL_FONT_SIZE);
        Spinner spinner = (Spinner)findViewById(R.id.fontSizeSpinner);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.font_size, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        setSpinnerFontSize(spinner, fontSize);

//        preference = settings.getString(THEME_PREF, DEFAULT_THEME);
//        spinner = (Spinner)findViewById(R.id.themeSpinner);
//        adapter = ArrayAdapter.createFromResource(this, R.array.themes, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        setSpinnerTheme(spinner, preference);
        
        boolean useScreen = settings.getBoolean(SCREEN_ORIENTATION_PREF, DEFAULT_SCREEN_ORIENTATION);
        CheckBox checkbox = (CheckBox)findViewById(R.id.screenOrientationCheckbox);
        checkbox.setChecked(useScreen);

        boolean scrollToTop = settings.getBoolean(SCROLL_TO_TOP_ON_CHAPTER_CHANGE_PREF, SCROLL_TO_TOP_ON_CHAPTER_CHANGE_DEFAULT);
        checkbox = (CheckBox)findViewById(R.id.scrollToTop);
        checkbox.setChecked(scrollToTop);

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
        themeChanged = false;
    }
    
    protected void openFileDialog(View v){
        Intent i = new Intent(this, FileList.class);
        i.putExtra(DATABASE_PREF, getTextValue(R.id.databaseEntry));
        startActivityForResult(i, CHOOSE_FILE_ACTIVITY);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(intent != null){
	        Bundle extras = intent.getExtras();
	    	String databaseLocation = extras.getString(DATABASE_PREF);
	        EditText text = (EditText)findViewById(R.id.databaseEntry);
	        text.setText(databaseLocation);
        }
    }

    public static boolean getScrollToTopOnChange(Context context){
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(SCROLL_TO_TOP_ON_CHAPTER_CHANGE_PREF, SCROLL_TO_TOP_ON_CHAPTER_CHANGE_DEFAULT);
    }
    
    public static int getPreferedTheme(Context context){
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        String preference = settings.getString(THEME_PREF, DEFAULT_THEME);
        return getThemePreference(preference);
    }
    
    public static int getPreferedFontSize(Context context){
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt(VERSE_SIZE_FONT_PREF, SMALL_FONT_SIZE);
    }
    
    public static boolean getOrientationPreference(Context context){
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(SCREEN_ORIENTATION_PREF, DEFAULT_SCREEN_ORIENTATION);
    }
    
    protected static int getThemePreference(String themeValue){
    	int result = R.style.customLightTheme;
//    	if(themeValue.indexOf("Dark") >= 0){
//    		result = R.style.customBlackTheme;
//    	}
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

    protected void setSpinnerFontSize(Spinner spinner, int preference){
    	int spinnerSelection = SMALL_FONT;
    	switch(preference){
    	case LARGE_FONT_SIZE:
    		spinnerSelection = LARGE_FONT;
    		break;
    	case MEDIUM_FONT_SIZE:
    		spinnerSelection = MEDIUM_FONT;
    		break;
    	}
    	spinner.setSelection(spinnerSelection);
    }
    
    protected int getSelectedFontSize(int controlId){
		int result = SMALL_FONT_SIZE;
    	Spinner spinner = (Spinner)findViewById(controlId);
    	String selectedItem = spinner.getSelectedItem().toString();
    	if(selectedItem.indexOf("Large") >=0 ){
    		result = LARGE_FONT_SIZE;
    	} else if(selectedItem.indexOf("Medium")>=0) {
    		result = MEDIUM_FONT_SIZE;
    	}
    	return result;
    }
    
    protected boolean getBoolean(int controlId){
    	boolean result = DEFAULT_SCREEN_ORIENTATION;
    	
    	CheckBox checkbox = (CheckBox)findViewById(controlId);
    	result = checkbox.isChecked();
    	
    	return result;
    }
    
    private boolean savePreferences(){
    	
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	
    	String preference = getTextValue(R.id.databaseEntry);
    	File file = new File(preference);
    	Log.i("pref", Boolean.toString(file.exists()));
    	if(!file.exists()) return false;
		editor.putString(DATABASE_PREF, preference);
    	
//    	preference = getSelectedTheme(R.id.themeSpinner);
//    	String oldPreference = settings.getString(THEME_PREF, preference);
//    	themeChanged = !preference.equalsIgnoreCase(oldPreference);
//    	editor.putString(THEME_PREF, preference);

    	int fontSize = getSelectedFontSize(R.id.fontSizeSpinner);
    	editor.putInt(VERSE_SIZE_FONT_PREF, fontSize);

    	boolean useScreen = getBoolean(R.id.screenOrientationCheckbox);
    	editor.putBoolean(SCREEN_ORIENTATION_PREF, useScreen);

        boolean scrollToTop = getBoolean(R.id.scrollToTop);
        editor.putBoolean(SCROLL_TO_TOP_ON_CHAPTER_CHANGE_PREF, scrollToTop);

        editor.commit();
    	return true;
    }
    
    private void onCloseClick(int resultCode){
        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        intent.putExtras(bundle);
        if(themeChanged) {
        	log("theme changed. Restart required for theme to be applied correctly");
        	resultCode = BrowseScriptureActivity.RESULT_RESTART;
        }
        log("setting result code to: " + resultCode);
        setResult(resultCode, intent);
        finish();
    }
    
    private void log(String message){
    	if(BrowseScriptureActivity.DEBUG){
    		Log.d("Preferences", message);
    	}
    }
}
