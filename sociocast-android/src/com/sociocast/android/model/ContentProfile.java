package com.sociocast.android.model;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

public class ContentProfile implements JSONParseable {
	
	public static String PARAM_URL = "url";
	public static String PARAM_HUMREAD = "humread";
	public static String PARAM_CLASSIFICATION = "classification";
	
	/*
	 * The url to be/that has been classified
	 */
	private String url;
	
	/*
	 * The classifications returned from Sociocast
	 */
	private Map<String, Double> classifications = new TreeMap<String, Double>();;

	//private ContentProfile(){}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, Double> getClassifications() {
		return classifications;
	}

	public void setClassifications(Map<String, Double> classifications) {
		this.classifications = classifications;
	}
	
	@Override
	public void parseJSON(String json) {		
		try {
			JSONObject jObject = new JSONObject(json);
			setUrl(jObject.getString(PARAM_URL));
			JSONObject cls = jObject.getJSONObject(PARAM_CLASSIFICATION);			
			@SuppressWarnings("rawtypes")
			Iterator it = cls.keys();
			while(it.hasNext()) {
				String key = (String) it.next();
				classifications.put(key, cls.getDouble(key));
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}
	
			
}
