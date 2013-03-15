package com.sociocast.android.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class SociocastUtils {
	
	public static final String TAG = SociocastUtils.class.getName();
	
    public static final int GET = 0x1;
    public static final int POST = 0x2;		
	
    public static List<BasicNameValuePair> paramsToList(Bundle params) {
        ArrayList<BasicNameValuePair> formList = new ArrayList<BasicNameValuePair>(params.size());
        
        for (String key : params.keySet()) {
            Object value = params.get(key);
            
            // We can only put Strings in a form entity, so we call the toString()
            // method to enforce. We also probably don't need to check for null here
            // but we do anyway because Bundle.get() can return null.
            if (value != null) {
            	formList.add(new BasicNameValuePair(key, value.toString()));
            	Log.d(TAG, "Adding param: " + key + "=" + value.toString());
            }
        }
        
        return formList;
    }	
    
    public static String paramsToString(Bundle params) {
    	StringBuffer sb = new StringBuffer();
    	for(String key : params.keySet()) {
    		Object value = params.get(key);    		
    		if (value != null) {
    			BasicNameValuePair nvp = new BasicNameValuePair(key, value.toString());
    			sb.append(nvp.toString());
    		}    		
    	}
    	return sb.toString();
    }
    
    public static void attachUriWithQuery(HttpRequestBase request, Uri uri, Bundle params) {
        try {
            if (params == null) {
                // No params were given or they have already been
                // attached to the Uri.
                request.setURI(new URI(uri.toString()));
            }
            else {
                Uri.Builder uriBuilder = uri.buildUpon();
                
                // Loop through our params and append them to the Uri.
                for (BasicNameValuePair param : paramsToList(params)) {
                    uriBuilder.appendQueryParameter(param.getName(), param.getValue());
                }
                
                uri = uriBuilder.build();
                request.setURI(new URI(uri.toString()));
            }
        }
        catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect: "+ uri.toString(), e);
        }
    }  
    
    public static String verbToString(int verb) {
        switch (verb) {
            case GET:
                return "GET";
                
            case POST:
                return "POST";                
        }        
        return "";
    } 
    
	public static String generateAPISignature(long ts, String apikey, String secret) throws SociocastException {
		try {
			// Create a key sorted map (alphabetical order as required by Sociocast API authentication)
			Map<String, Object> params = new TreeMap<String, Object>(Collator.getInstance());			
			params.put("apikey", apikey);
			params.put("apisecret", secret);			
			params.put("ts", ts); //Make sure the timestamp is in EPOCH time			
						
			String sigParamString = "";
			for(String s : params.keySet()) {
				sigParamString += s + "=" + params.get(s) + "&";
			}
			// Get rid of trailing '&'
			sigParamString = sigParamString.substring(0, sigParamString.length() - 1);
			
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(sigParamString.getBytes());
			byte[] mdbytes = md.digest();
			
			// Output signature to Hex
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < mdbytes.length; i++) {
	          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
	        }			
		
	        return sb.toString();
			
		} catch (Exception uee) {					
			Log.e(TAG, uee.getMessage());
			throw new SociocastException(uee.getMessage());
		}
	}  
	
	public static String getSignedURL(String url, String apikey, long ts, String secret) throws SociocastException {
        
		try {
        	// Construct API call
        	url += "?apikey=" + apikey + "&";
        	Log.println(Log.INFO, TAG, "Generating API Signature");
        	url += "sig=" + generateAPISignature(ts, apikey, secret) + "&";
        	url += "ts=" + ts;
        } catch(SociocastException e) {
        	Log.e(TAG, e.getMessage());
        	throw e;
        }					
		return url;
	}
	
	public static String getUUID() {
		return UUID.randomUUID().toString();
	}	
	
	public static String encodeURL(String _url) throws SociocastException {
		try {
			String urlStr = _url;
			URL url = new URL(urlStr);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			//url = uri.toURL();	
			//return url.toString();
			return uri.toASCIIString();
		} catch(Exception e) {
			throw new SociocastException(e.getMessage());
		}
	}
	
}
