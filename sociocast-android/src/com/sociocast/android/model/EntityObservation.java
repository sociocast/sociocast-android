package com.sociocast.android.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class EntityObservation extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2060518228586912966L;

	/* 
	 * The Entity ID
	 */
	private String eid;
	
	/*
	 * The timestamp
	 */
	private Date timestamp;
	
	/*
	 * Event type
	 */
	private String evt;
	
	/*
	 * Client id
	 */
	private String clid;

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setAttribute(String key, Object... values) {
		if(values.length > 1) {
			put(key, new ArrayList<Object>(Arrays.asList(values)));
		} else {
			put(key, values[0]);
		}
	}
	
	public Object getAttribute(String key) {
		return get(key);
	}
	
	public Set<String> getAttributeKeys() {
		return keySet();
	}	
	
	public String getEvt() {
		return evt;
	}

	public void setEvt(String evt) {
		this.evt = evt;
	}
		
	public String getClid() {
		return clid;
	}

	public void setClid(String clid) {
		this.clid = clid;
	}

	public String getJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("ets", this.timestamp.getTime()/1000L);
			json.put("eid", this.eid);
			json.put("evt", this.evt);
			json.put("clid", this.clid);
			JSONObject obs = new JSONObject();
			for(String key : getAttributeKeys()) {	
				Object o = getAttribute(key);
				if(o instanceof ArrayList) {
					obs.put(key, new JSONArray((ArrayList<?>) o));
				} else {
					obs.put(key, o);
				}
			}	
			json.put("obs", obs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
}
