package com.sociocast.android.model;

import java.util.Map;
import java.util.TreeMap;

public class ContentProfile {
	
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
				
}
