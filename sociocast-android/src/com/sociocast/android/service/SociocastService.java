package com.sociocast.android.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.sociocast.android.content_provider.QueuedEventContentProvider;
import com.sociocast.android.model.EntityObservation;
import com.sociocast.android.receiver.ConnectivityChangedReceiver;
import com.sociocast.android.util.AppStatus;
import com.sociocast.android.util.SociocastConstants;
import com.sociocast.android.util.SociocastUtils;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class SociocastService extends IntentService {

	public static final String TAG = SociocastService.class.getName();
            
    public final static String API_PROD = "http://api.sociocast.com/1.0";
    public final static String API_SANDBOX = "http://api-sandbox.sociocast.com/1.0";
    
    protected ContentResolver contentResolver;
    protected AlarmManager alarmManager;
    protected PendingIntent retryQueuedCheckinsPendingIntent;
    protected SharedPreferences sharedPreferences;
    protected Editor sharedPreferencesEditor;
    //protected SharedPreferenceSaver sharedPreferenceSaver;    
    
	public SociocastService() {
		super(TAG);
		
		Log.println(Log.INFO, TAG, "Starting SociocastService()...");		
	}
	
	public static String getAPIUrl(boolean sandbox) {
		return sandbox ? API_SANDBOX : API_PROD;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();		
		this.contentResolver = getContentResolver();
	    this.alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		
	    //sharedPreferences = getSharedPreferences(PlacesConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
	    //sharedPreferencesEditor = sharedPreferences.edit();
	    //sharedPreferenceSaver = PlatformSpecificImplementationFactory.getSharedPreferenceSaver(this);
	    
	    Intent retryIntent = new Intent(SociocastConstants.RETRY_QUEUED_ACTION);
	    retryQueuedCheckinsPendingIntent = PendingIntent.getBroadcast(this, 0, retryIntent, PendingIntent.FLAG_UPDATE_CURRENT);		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
			
        // When an intent is received by this Service, this method
        // is called on a new thread.		
		Log.println(Log.INFO, TAG, "Received Intent DATA -> " + intent.getDataString());
		Log.println(Log.INFO, TAG, "Received Intent EXTRAS -> " + intent.getExtras().size());
		
        Uri action = intent.getData(); //REST URL
        
        Bundle extras = intent.getExtras();
        if (!extras.containsKey(SociocastConstants.EXTRA_CONNECTIVITY_CHANGE) && (extras == null || action == null)) {
            // Extras contain our ResultReceiver and data is our REST action.  
            // So, without these components we can't do anything useful.
            Log.e(TAG, "You did not pass extras or data with the Intent.");            
            return;
        } 
                
        // We default to GET if no verb was specified.
        int verb = extras.getInt(SociocastConstants.EXTRA_HTTP_VERB, SociocastUtils.GET);
        
        String apiObject = intent.getStringExtra(SociocastConstants.EXTRA_API_OBJECT);
        
        // Get the UUID
        //String uuid = extras.getString(SociocastConstants.EXTRA_UUID);
        Log.println(Log.INFO, TAG, "Getting Bundle params...");
        Bundle params = extras.getParcelable(SociocastConstants.EXTRA_PARAMS);
       
        Log.println(Log.INFO, TAG, "Getting REsultReceiver...");
        ResultReceiver receiver = extras.getParcelable(SociocastConstants.EXTRA_RESULT_RECEIVER);  
        
        // Create UUID for request
        String uuid = SociocastUtils.getUUID();
        
		// TODO: These need to be defined
        /*
		long timeStamp = intent.getLongExtra(SociocastConstants.EXTRA_KEY_TIME_STAMP, 0);
		String reference = intent.getStringExtra(SociocastConstants.EXTRA_KEY_REFERENCE);
		String id = intent.getStringExtra(SociocastConstants.EXTRA_KEY_ID);
		*/
		
		// If we're not connected then disable the retry Alarm, enable the Connectivity Changed Receiver
	    // and add the new checkin directly to the queue. The Connectivity Changed Receiver will listen
	    // for when we connect to a network and start this service to retry the checkins.
		if (!AppStatus.getInstance(this).isOnline(this)) {
			// No connection so no point triggering an alarm to retry until we're connected.
			alarmManager.cancel(retryQueuedCheckinsPendingIntent);
	      
			// Enable the Connectivity Changed Receiver to listen for connection to a network
			// so we can commit the pending checkins.
			PackageManager pm = getPackageManager();
			ComponentName connectivityReceiver = new ComponentName(this, ConnectivityChangedReceiver.class);
			pm.setComponentEnabledSetting(connectivityReceiver,
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 
					PackageManager.DONT_KILL_APP);
			  
			// If event observation only, add this event to the queue.			
			if(intent.getStringExtra(SociocastConstants.EXTRA_API_OBJECT).equals(
					EntityObservation.class.getSimpleName()))
				addToQueue(uuid, action, params);
      	
    	} else { 

    		// Check to see if the service intent was a connectivity change, if so skip this section
    		if(!extras.getBoolean(SociocastConstants.EXTRA_CONNECTIVITY_CHANGE)) {
    			// Send the request, if it is an event observation and doesn't work add to queue
    			if (!sendRequest(action, verb, params, receiver, apiObject)) {
					if(intent.getStringExtra(SociocastConstants.EXTRA_API_OBJECT).equals(
							EntityObservation.class.getSimpleName())) 				
						addToQueue(uuid, action, params);
				}
    		}
			
			ArrayList<String> successfulEvents = new ArrayList<String>();
			Cursor queuedEvents = contentResolver.query(QueuedEventContentProvider.CONTENT_URI, null, null, null, null);
			try {
				Log.d(TAG, "Looping through queued events (" + queuedEvents.getCount() + ") found");
				// Retry each event.
		        while (queuedEvents.moveToNext()) {
		        	// Loop through queue and retry  	
		            long queuedTimeStamp =  queuedEvents.getLong(queuedEvents.getColumnIndex(QueuedEventContentProvider.KEY_TIME_STAMP));
		            String json = queuedEvents.getString(queuedEvents.getColumnIndex(QueuedEventContentProvider.KEY_JSON));
		            String queuedId =  queuedEvents.getString(queuedEvents.getColumnIndex(QueuedEventContentProvider.KEY_ID));
		            action = Uri.parse(queuedEvents.getString(queuedEvents.getColumnIndex(QueuedEventContentProvider.KEY_ACTION)));
		            
		            // Recreate the bundle
		            Bundle newParams = new Bundle();
		            newParams.putString(SociocastConstants.EXTRA_PARAMS_JSON, json);
		            newParams.putLong(SociocastConstants.EXTRA_TIMESTAMP, queuedTimeStamp);		            		
		            
		            Log.d(TAG, "Sending queued event " + json);
		            if (queuedId == null || sendRequest(action, SociocastUtils.POST, newParams, null, apiObject)) // Send null receiver
		            	successfulEvents.add(queuedId);		        	
		        }
		        
		        // Delete the queued events that were successful.
		        if (successfulEvents.size() > 0) {
		        	StringBuilder sb = new StringBuilder("("+QueuedEventContentProvider.KEY_ID + "='" + successfulEvents.get(0) + "'");
		        	for (int i = 1; i < successfulEvents.size(); i++)
		        		sb.append(" OR " + QueuedEventContentProvider.KEY_ID + " = '" + successfulEvents.get(i) + "'");
		        	sb.append(")");
		        	Log.d(TAG, "Delete statement = " + sb.toString());
		        	int deleteCount = contentResolver.delete(QueuedEventContentProvider.CONTENT_URI, sb.toString(), null);
		        	Log.d(TAG, "Deleted: " + deleteCount);
		        }		        
		        
			} finally {
				queuedEvents.close(); 
			}    		    	
    	}
		
	}
	
	protected boolean sendRequest(Uri action, int verb, Bundle params, ResultReceiver receiver, String apiObject) {
        try {            
            // Here we define our base request object which we will
            // send to our REST service via HttpClient.
            HttpRequestBase request = null;
            
            // Let's build our request based on the HTTP verb we were
            // given.
            switch (verb) {
                case SociocastUtils.GET: {
                	Log.println(Log.INFO, TAG, "Creating GET Request...");
                    request = new HttpGet();
                    SociocastUtils.attachUriWithQuery(request, action, params);
                }
                break;           
                
                case SociocastUtils.POST: {
                	Log.d(TAG, "Creating POST Request...");
                	request = new HttpPost();
                    request.setURI(new URI(action.toString()));
                    
                    // Construct POST Request
                    HttpPost postRequest = (HttpPost) request;
                    postRequest.setHeader("Content-Type","application/json");                    
                    if (params != null) {                    	
                    	//UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(SociocastUtils.paramsToList(params), HTTP.UTF_8);
                    	StringEntity json = new StringEntity(params.get(SociocastConstants.EXTRA_PARAMS_JSON).toString());
                    	Log.d(TAG, "POSTing JSON..." + params.get(SociocastConstants.EXTRA_PARAMS_JSON).toString());
                        postRequest.setEntity(json);
                    }
                }
                break;                             
            }
            
            if (request != null) {
            	
            	Log.d(TAG, "Creating HttpRequest...");
            	HttpClient client = new DefaultHttpClient();
                
                // Let's send some useful debug information so we can monitor things
                // in LogCat.
                Log.d(TAG, "Executing request: " + SociocastUtils.verbToString(verb) + ": " + action.toString());
                                
                // Finally, we send our request using HTTP. This is the synchronous
                // long operation that we need to run on this thread.
                HttpResponse response = client.execute(request);
                
                HttpEntity responseEntity = response.getEntity();
                StatusLine responseStatus = response.getStatusLine();
                int statusCode = responseStatus != null ? responseStatus.getStatusCode() : 0;
                
                // Our ResultReceiver allows us to communicate back the results to the caller. This
                // class has a method named send() that can send back a code and a Bundle
                // of data. ResultReceiver and IntentService abstract away all the IPC code
                // we would need to write to normally make this work.
                if (responseEntity != null) {                	
                	Bundle resultData = new Bundle();
                    resultData.putString(SociocastConstants.REST_RESULT, EntityUtils.toString(responseEntity));
                    resultData.putString(SociocastConstants.RESULT_API_OBJECT, apiObject);                    
                    if(receiver != null) receiver.send(statusCode, resultData);
                    Log.d(TAG, "Got HttpResponse statusCode: " + statusCode + " data " + resultData);
                    if(statusCode == SociocastConstants.RESPONSE_OK_STATUS_CODE) return true;                                       
                } else {
                	Log.d(TAG, "Response Entity is null...");
                	if(receiver != null) receiver.send(statusCode, null);
                }                                                 
            } else {
            	return false;
            }
        }
        catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect. " + SociocastUtils.verbToString(verb) + ": " + action.toString(), e);
            if(receiver != null) receiver.send(0, null);
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "A UrlEncodedFormEntity was created with an unsupported encoding " + e.getMessage(), e);
            if(receiver != null) receiver.send(0, null);            
        }
        catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException - There was a problem when sending the request " + e.getMessage(), e);
            if(receiver != null) receiver.send(0, null);
        }
        catch (IOException e) {
            Log.e(TAG, "IOException - There was a problem when sending the request " + e.getMessage(), e);
            e.printStackTrace();
            if(receiver != null) receiver.send(0, null);        
        }     
        
        return false;
	}
	
	/**
	 * Adds an event to the {@link QueuedEventContentProvider} to be retried.
	 * @param timeStamp event timestamp
	 * @param id event unique identifier
	 * @return Successfully added to the queue
	 */
	protected boolean addToQueue(String uuid, Uri action, Bundle params) {
				
        long timestamp = params.getLong(SociocastConstants.EXTRA_TIMESTAMP, 0);	
        String json = params.getString(SociocastConstants.EXTRA_PARAMS_JSON);
        
        Log.d(TAG, "Adding event to queue " + uuid + " " + params.getString(SociocastConstants.EXTRA_PARAMS_JSON));
		
		// Construct the new row.
		ContentValues values = new ContentValues();
		values.put(QueuedEventContentProvider.KEY_ID, uuid);
		values.put(QueuedEventContentProvider.KEY_TIME_STAMP, timestamp);
		values.put(QueuedEventContentProvider.KEY_JSON, json);
		values.put(QueuedEventContentProvider.KEY_ACTION, action.toString());

		try {
			// Insert the event 
			if (contentResolver.insert(QueuedEventContentProvider.CONTENT_URI, values) != null) {
				Log.d(TAG, "Added event to database successfuly");
				return true;
			}
		} catch (Exception ex) { 
			Log.e(TAG, "Queuing event " + json + " failed.");
		}
    
		return false;
	}	

}
