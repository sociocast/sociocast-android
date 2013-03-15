package com.sociocast.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class AppStatus {

	public static final String TAG = AppStatus.class.getName();
	
    private static AppStatus instance = new AppStatus();
    static Context context;
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    boolean connected = false;

    public static AppStatus getInstance(Context ctx) {
        context = ctx;
        return instance;
    }

    public boolean isOnline(Context con) {
        Log.d(TAG, "Checking to see if device is online...");
    	try {
            connectivityManager = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                networkInfo.isConnected();  
            Log.d(TAG, "Device " + (connected ? "is" : "is not") + " online");
            return connected;
        } catch (Exception e) {            	
            Log.e("connectivity", e.toString());
        }   
    	Log.d(TAG, "Device " + (connected ? "is" : "is not") + " online");
        return connected;
    }
}
