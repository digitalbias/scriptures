package com.digitalbias.android;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

public class DownloadDatabaseActivity extends ListActivity {
	
	private List<DatabaseLocation> mDatabaseList;
//	private static final String DATABASE_LIST_URI = "http://scriptures.digitalbias.com/scripture_list.xml";
//	private static final String DATABASE_LIST_URI = "http://androidscriptures.googlecode.com/files/scripture_list.xml.xml";
	private static final String DATABASE_LIST_URI = "http://www.mediafire.com/file/jyzbdngleji/scripture_list.xml";
    private static final int IO_BUFFER_SIZE = 4 * 1024;
    private ProgressBar mProgressBar;
	
	private class DownloadFilesTask extends UserTask<Object, Integer, File> {
	    private int mProgress;
	    private int mFileSize;
		
        @Override
        public void onPreExecute() {
            showProgress();
        }
		
		public File doInBackground(Object... fileInfo) {
			URL url = (URL)fileInfo[0];
			File destFile = (File)fileInfo[1];
			
			try {
				downloadFile(destFile, url);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return destFile;
		}
		
		public void onProgressUpdate(Integer... progress) {
			setProgressPercent(progress[0]);
		}
		
		public void setProgressPercent(int progressPercent){
			mProgressBar.setProgress(progressPercent);
		}
		
		public void onPostExecute(File result) {
            Log.i(BrowseScriptureActivity.TAG, "Done: " + Long.toString(result.length()));
			hideProgress();
		}

	    public void downloadFile(File destinationFile, URL url) throws IOException {
	    	if(!destinationFile.exists()) destinationFile.createNewFile();

	    	BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destinationFile), IO_BUFFER_SIZE);
	        HttpGet get = new HttpGet(url.toString());

	        HttpEntity entity = null;
	        InputStream in = null;
	        try {
	        	HttpClient client = new DefaultHttpClient();
	            final HttpResponse response = client.execute(get);
	            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	                entity = response.getEntity();
	                mProgress = 0;
	                mFileSize = (int)entity.getContentLength();
	                in = entity.getContent();
	                copy(in, out);
	                out.flush();
	            }
	        } finally {
	        	closeStream(out);
	        	closeStream(in);
	        }
	    }
		
		private void copy(InputStream in, OutputStream out) throws IOException {
	        byte[] b = new byte[IO_BUFFER_SIZE];
	        int read;
	        while ((read = in.read(b)) != -1) {
	            out.write(b, 0, read);
	        	mProgress = mProgress + IO_BUFFER_SIZE;
				publishProgress((int) ((mProgress / (float) mFileSize) * 100));
	        }
	    }

	    private void closeStream(Closeable stream) {
	        if (stream != null) {
	            try {
	                stream.close();
	            } catch (IOException e) {
	                Log.e(BrowseScriptureActivity.TAG, "Could not close stream", e);
	            }
	        }
	    }

	}
    
    private void hideProgress() {
        if (mProgressBar.getVisibility() != View.GONE) {
//            final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            mProgressBar.setVisibility(View.GONE);
//            mProgressBar.startAnimation(fadeOut);
        }
    }

    private void showProgress() {
        if (mProgressBar.getVisibility() != View.VISIBLE) {
//            final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            mProgressBar.setVisibility(View.VISIBLE);
//            mProgressBar.startAnimation(fadeIn);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SetPreferencesActivity.getPreferedTheme(this));
        setContentView(R.layout.database_dialog);
        Button button = (Button) findViewById(R.id.done_button);
        button.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view){
                Bundle bundle = new Bundle();
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
        	}
        });
        button = (Button) findViewById(R.id.refresh_button);
        button.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view){
        		setupList();
        	}
        });
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        setupList();
    }
	
    protected void setupList(){
        mDatabaseList = getDatabaseList();
    	
//    	ArrayAdapter adapter = new SimpleCursorAdapter(this, R.layout.scripture_row, mVolumeCursor, from, to);
    	ArrayAdapter<DatabaseLocation> adapter = new ArrayAdapter<DatabaseLocation>(this, R.layout.scripture_row, mDatabaseList);
    	setListAdapter(adapter);
    }
    
    protected List<DatabaseLocation> getDatabaseList(){
    	DownloadXmlHandler handler = new DownloadXmlHandler();
    	handler.createDatabaseList(this, DATABASE_LIST_URI);
		return handler.getDatabaseList();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	DatabaseLocation location = mDatabaseList.get(position);
    	downloadDatabase(location.url);
    }

    protected String getFilename(URL url){
    	String result = "";
    	String filename = url.getFile();
    	int index = filename.lastIndexOf("/");
    	result = filename.substring(index+1);
    	return result;
    }
    
    protected void downloadDatabase(URL url){
    	try {
    		File destinationFile = new File(SetPreferencesActivity.getDatabaseDirectory() + getFilename(url));
    		if(!destinationFile.getParentFile().exists()) destinationFile.getParentFile().mkdirs();
    		if(destinationFile.exists()){
				showOverwriteFileDialog(url, destinationFile);
    		} else {
				getDatabase(url, destinationFile);
    		}
		} catch (ClientProtocolException e) {
			Log.e(BrowseScriptureActivity.TAG, url.toString(), e);
		} catch (IOException e) {
			Log.e(BrowseScriptureActivity.TAG, url.toString(), e);
		}
    }

    protected void getDatabase(URL url, File destinationFile) throws IOException {
    	Object[] array = {url,destinationFile};
    	new DownloadFilesTask().execute(array); 
    }
    
    private void showFailure(String message){
    	
    }
    
    protected void showOverwriteFileDialog(final URL url, final File file) throws IOException{
    	if(file.exists()){
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("Database already exists. Overwrite?");
    		builder.setTitle("Invalid database");
    		builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int which){
    			}
    		});
    		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int which){
    				try {
    					getDatabase(url, file);
    				} catch (IOException e){
    					showFailure(e.getMessage());
    				}
    			}
    		});
    		builder.setCancelable(false);
    		AlertDialog dialog = builder.create();
    		dialog.show();
    	}
    }
}
