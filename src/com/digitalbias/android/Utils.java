package com.digitalbias.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class Utils {

	public static View createLayout(int resource, Context context){
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(resource, null, false);
	}
	
}
