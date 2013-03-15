package com.sociocast.android.receiver;

import com.sociocast.android.service.SociocastService;
import com.sociocast.android.util.SociocastConstants;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * This Receiver class is designed to listen for changes in connectivity.
 * 
 * When we lose connectivity the relevant Service classes will automatically
 * disable passive  updates and queue pending messages.
 * 
 * This class will restart the Sociocast service to retry pending messages
 * and re-enables passive messages updates.
 */
public class ConnectivityChangedReceiver extends BroadcastReceiver {
	
	public static final String TAG = SociocastService.class.getName();	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    
		Log.d(TAG, "ConnectivtyManager received Intent...");
		
		// Check if we are connected to an active data network.
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    
		if (isConnected) {
			PackageManager pm = context.getPackageManager();
      
			ComponentName connectivityReceiver = new ComponentName(context, ConnectivityChangedReceiver.class);
      
			// The default state for this Receiver is disabled. it is only
			// enabled when a Service disables updates pending connectivity.
			pm.setComponentEnabledSetting(connectivityReceiver,
					PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, 
					PackageManager.DONT_KILL_APP);
            
			// Commit any queued checkins now that we have connectivity
      
			Log.d(TAG, "ConnectivtyManager sending change in connectivity intent");
			
			Intent sociocastServiceIntent = new Intent(context, SociocastService.class);
			sociocastServiceIntent.putExtra(SociocastConstants.EXTRA_CONNECTIVITY_CHANGE, SociocastConstants.CONNECTIVITY_IS_NOW_ON);
			context.startService(sociocastServiceIntent);
		}
	}
}