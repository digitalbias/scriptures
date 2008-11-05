package com.digitalbias.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;


public class DownloadXmlHandler extends DefaultHandler {

	private boolean inTitle = false;
	private boolean inLocation = false;
	
	private DatabaseLocation currentDatabase = new DatabaseLocation();
	private List<DatabaseLocation> mDatabaseList = new ArrayList<DatabaseLocation>();

	public List<DatabaseLocation> getDatabaseList(){
		return mDatabaseList;
	}
	
	public void createDatabaseList(Context context, String uri){
		try {

			URL url = new URL(uri);
			InputStream is = url.openStream(); 
			
	    	SAXParserFactory spf = SAXParserFactory.newInstance();
	    	SAXParser sp = spf.newSAXParser();
	    	XMLReader xr = sp.getXMLReader();
	    	xr.setContentHandler(this);
            xr.parse(new InputSource(is));
		} catch (MalformedURLException e){
			Log.e(BrowseScriptureActivity.TAG, uri, e);
		} catch (IOException e) {
			Log.e(BrowseScriptureActivity.TAG, uri, e);
		} catch (SAXException e) {
			Log.e(BrowseScriptureActivity.TAG, uri, e);
		} catch (ParserConfigurationException e){
			Log.e(BrowseScriptureActivity.TAG, uri, e);
		}
	}
	
	public void startElement(String url, String name, String qName, Attributes attrs){
		if (name.trim().equals("title")){
			inTitle = true;
		} else if (name.trim().equals("location")){
			inLocation = true;
		}
	}
	
	 public void characters(char ch[], int start, int length) {
        String chars = (new String(ch).substring(start, start + length));
 
        try {
            if (inLocation) currentDatabase.url = new URL(chars);
            if (inTitle) currentDatabase.title = chars;
        } catch (MalformedURLException e) {
			Log.e(BrowseScriptureActivity.TAG, "error", e);
        }
 
    }
	 
	 public void endElement(String uri, String name, String qName) throws SAXException {
		if (name.trim().equals("title")){
			inTitle = false;
		} else if (name.trim().equals("location")){
			inLocation = false;
		}

		if (currentDatabase.title != null && currentDatabase.url != null) {
		    mDatabaseList.add(currentDatabase);
			currentDatabase = new DatabaseLocation();
		}
	}
	
}