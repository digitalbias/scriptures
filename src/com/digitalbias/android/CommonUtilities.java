package com.digitalbias.android;

import android.app.Activity;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.view.OrientationListener;

public class CommonUtilities {

	public static SensorManager setupAccelerometer(Activity activity, SensorListener listener){
        SensorManager sensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
        if(listener != null) {
	        sensorManager.registerListener(listener, SensorManager.SENSOR_ACCELEROMETER);
        }
        return sensorManager;
	}
	
	public static SensorManager setupOrientation(Activity activity, OrientationListener listener){
        SensorManager sensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
        if(listener != null) {
	        sensorManager.registerListener(listener, SensorManager.SENSOR_ORIENTATION);
        }
        return sensorManager;
	}
	
}
