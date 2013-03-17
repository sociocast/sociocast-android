package com.sociocast.android;

import java.util.Date;
import java.util.List;

import com.sociocast.android.model.ContentProfile;
import com.sociocast.android.model.EntityAttributes;
import com.sociocast.android.model.EntityObservation;
import com.sociocast.android.model.EntityProfile;
import com.sociocast.android.service.SociocastService;
import com.sociocast.android.util.SociocastConstants;
import com.sociocast.android.util.SociocastException;
import com.sociocast.android.util.SociocastUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Core class for interacting with the Sociocast API. 
 *
 * <p>Call {@link #newInstance(Context context, String apikey, String secret, String clid, boolean sandbox)} with
 * your main application activity and your Sociocast apikey, secret, client id. Additionally @param sandbox should be
 * set to true when testing and false when you want to push data to the live environment. 
 *
 * <p>Once you have an instance, you can call general Sociocast API methods. 
 *
 * <p>The Sociocast library will periodically send information to
 * Sociocast servers, so your application will need to have
 * <tt>android.permission.INTERNET</tt>.  
 *
 * <p>A typical use-case for the library might look like this:
 *
 * <p>In addition to this documentation, you may wish to take a look at
 * <a href="https://github.com/sociocast/">for additional information.</a>.
 *
 * <p>There is also <a href="http://www.sociocast.com/">general documentation</a>
 * available at sociocast.com
 *
 * @see <a href="https://sociocast.com">getting started documentation for the Sociocast platform</a>
 */
public class Sociocast implements SociocastAPI {

	public static final String TAG = Sociocast.class.getName();
    
    private boolean sandbox;    
    private String apikey;
    private String secret;    
	private String clid;
    
    private Context context;
    private ResultReceiver receiver;
    
    private static Sociocast instance;
    
    public static Sociocast newInstance(Context context, String apikey, String secret, String clid, boolean sandbox) {
    	if(instance == null) {
    		instance = new Sociocast(context, apikey, secret, clid, sandbox);
    		return instance;
    	} else {
    		return instance;
    	}
    }
    
	private Sociocast(Context context, String apikey, String secret, String clid, boolean sandbox) {
		this.sandbox = sandbox;
		this.apikey = apikey;
		this.secret = secret;
		this.clid = clid;		
		this.context = context;
	}
	
	public void setReceiver(ResultReceiver receiver) {
		this.receiver = receiver;
	}
	
	/*
	 * Pass in the Context which should start the service
	 */
	public void sendServiceIntent(Uri url, Bundle params, int type, String apiObject) {
        
		Log.println(Log.INFO, TAG, "Calling sendServiceIntent()..." + this.context + " " + this.receiver);
		
		if(this.context != null & this.receiver != null) {
			
			Intent intent = new Intent(this.context, SociocastService.class);
	        intent.setData(url);	        
	        intent.putExtra(SociocastConstants.EXTRA_PARAMS, params); //Put the params
	        intent.putExtra(SociocastConstants.EXTRA_RESULT_RECEIVER, this.receiver); //	        
	        intent.putExtra(SociocastConstants.EXTRA_HTTP_VERB, type);
	        intent.putExtra(SociocastConstants.EXTRA_API_OBJECT, apiObject);
	  	        
	        Log.d(TAG, "Starting SociocastService...");
	        
	        // Here we send our Intent from the activity
	        context.startService(intent);
	        	        	        
        } else {
        	Log.e(TAG, "The Context and the ResultReceiver cannot be null. Please make sure to set these.");
        }
	}

	@Override
	public void entityObserve(EntityObservation observation) {				

		Log.d(TAG, "Calling entityObserve()...");
		
		String url = SociocastService.getAPIUrl(this.sandbox) + SociocastConstants.ENTITY_OBSERVE_URL;
		long ts = observation.getTimestamp().getTime() / 1000L;
				
        try {
        	url = SociocastUtils.getSignedURL(url, apikey, ts, secret);
        } catch(SociocastException e) {
        	Log.e(TAG, e.getMessage());
        }		
		
        Uri uri = Uri.parse(url);
        Log.d(TAG, "Uri: " + uri.toString());
        						
		Bundle params = new Bundle();
        params.putString(SociocastConstants.EXTRA_PARAMS_JSON, observation.getJSON());
        params.putLong(SociocastConstants.EXTRA_TIMESTAMP, ts);
                
        Log.d(TAG, "Sending Service Intent...");        
        sendServiceIntent(uri, params, SociocastUtils.POST, EntityObservation.class.getSimpleName());        
	}
	
	@Override
	public void entityAttributes(EntityAttributes attributes) {
		
		Log.d(TAG, "Calling entityAttributes()...");
		
		String url = SociocastService.getAPIUrl(this.sandbox) + SociocastConstants.ENTITY_ATTRIB_URL;
		long ts = new Date().getTime() / 1000L;
		
        try {
        	url = SociocastUtils.getSignedURL(url, apikey, ts, secret);
        } catch(SociocastException e) {
        	Log.e(TAG, e.getMessage());
        }		
        
        Uri uri = Uri.parse(url);
        Log.d(TAG, "Uri: " + uri.toString());        
				
		Bundle params = new Bundle();
        try {
			params.putString(SociocastConstants.EXTRA_PARAMS_JSON, attributes.getJSON());	
			params.putLong(SociocastConstants.EXTRA_TIMESTAMP, ts);	
	        Log.d(TAG, "Sending Service Intent...");        
	        sendServiceIntent(uri, params, SociocastUtils.POST, EntityAttributes.class.getSimpleName());     			
		} catch (SociocastException e) {
			e.printStackTrace();
		}
        
	}	
	
	@Override
	public void contentProfile(String urlClassified, boolean humread) {
		
		Log.d(TAG, "Calling contentProfile()...");
		
		String url = SociocastService.getAPIUrl(this.sandbox) + SociocastConstants.CONTENT_PROFILE_URL;
		long ts = new Date().getTime() / 1000L;
		
        try {
        	url = SociocastUtils.getSignedURL(url, apikey, ts, secret);
        } catch(SociocastException e) {
        	Log.e(TAG, e.getMessage());
        }			
		
        Uri uri = Uri.parse(url);
        Log.d(TAG, "Uri: " + uri.toString());
		
		Bundle params = new Bundle();
		params.putString("url", urlClassified);
		params.putBoolean("humread", humread);
		
        Log.d(TAG, "Sending Service Intent...");        
        sendServiceIntent(uri, params, SociocastUtils.GET, ContentProfile.class.getSimpleName());                  
	}
		
	@Override
	public void entityProfile(String eid, boolean humread, List<String> attributes) {
		Log.d(TAG, "Calling entityProfile()...");
		
		String url = SociocastService.getAPIUrl(this.sandbox) + SociocastConstants.ENTITY_PROFILE_URL;
		long ts = new Date().getTime() / 1000L;
		
        try {
        	url = SociocastUtils.getSignedURL(url, apikey, ts, secret);
        } catch(SociocastException e) {
        	Log.e(TAG, e.getMessage());
        }	
        
        Uri uri = Uri.parse(url);
        Log.d(TAG, "Uri: " + uri.toString());  
        
		Bundle params = new Bundle();
        try {
			params.putString(SociocastConstants.EXTRA_PARAMS_JSON, SociocastUtils.attributesListToJSON(eid, this.clid, humread, attributes));
	        params.putLong(SociocastConstants.EXTRA_TIMESTAMP, ts);   
	        
	        Log.d(TAG, "Sending Service Intent...");        
	        sendServiceIntent(uri, params, SociocastUtils.POST, EntityProfile.class.getSimpleName());    		
		} catch (SociocastException e) {
			Log.e(TAG, "Error occured in sending entity profile request ", e); 
		}                   
	}
	
}
