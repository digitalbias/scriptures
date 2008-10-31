package com.digitalbias.android;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileList extends ListActivity {
	
	private List<String> items = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(SetPreferencesActivity.getPreferedTheme(this));
        setContentView(R.layout.directory_list);
        Bundle extras = getIntent().getExtras();
    	String databaseLocation = extras.getString(SetPreferencesActivity.DATABASE_PREF);
        try {
        	File file = new File(databaseLocation);
        	file = file.getParentFile();
        	fill(file.listFiles());
        } catch (Exception e){
            fill(new File("/").listFiles());
        }
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id)  {
    	super.onListItemClick(l, v, position, id);
		if (position  == 0) {
			fillWithRoot();
		} else {
			File file = new File(items.get(position));
			if (file.isDirectory())
				fill(file.listFiles());
			else {
				String filePath = "";
				try {
					filePath = file.getCanonicalPath();
			        Bundle bundle = new Bundle();
			        bundle.putString(SetPreferencesActivity.DATABASE_PREF, filePath);
			        Intent intent = new Intent();
			        intent.putExtras(bundle);
			        setResult(0, intent);
			        finish();
				} catch (IOException e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("That's a file, not a directory");
					builder.setTitle("Not a Directory");
					builder.setCancelable(true);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
		}
	}

    private void fillWithRoot() {
    	fill(new File("/").listFiles());
    }

	private void fill(File[] files) {
		items = new ArrayList<String>();
		items.add(getString(R.string.to_top));
		for (File file : files)
			items.add(file.getPath());
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,
				R.layout.file_row, items);
		setListAdapter(fileList);
	}
}