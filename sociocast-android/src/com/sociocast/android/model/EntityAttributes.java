package com.sociocast.android.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.sociocast.android.util.SociocastConstants;
import com.sociocast.android.util.SociocastException;

public class EntityAttributes {

	
	public static final String ATTRIB_SET = "set";
	public static final String ATTRIB_ADD = "add";
	public static final String ATTRIB_DELETE = "del";
	
	/*
	 * Entity ID to be modified
	 */
	private String eid;
	
	private String clid;
	
	private Date timestamp;
	
	/*
	 * Map to maintain the attributes to be set, delete, added
	 */
	private Map<String, Map<String, Object>> setMap = 
			new HashMap<String, Map<String,Object>>();
	

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public String getClid() {
		return clid;
	}

	public void setClid(String clid) {
		this.clid = clid;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.setMap.put(ATTRIB_SET, attributes);
	}
	
	public void addAttributes(Map<String, Object> attributes) {
		this.setMap.put(ATTRIB_ADD, attributes);
	}
	
	public void deleteAttributes(Map<String, Object> attributes) {
		this.setMap.put(ATTRIB_DELETE, attributes);
	}
			
	public String getJSON() throws SociocastException {
		JSONObject json = new JSONObject();
		try {
			json.put(SociocastConstants.JSON_ATTRIBUTES_EID, this.eid);
			json.put(SociocastConstants.JSON_ATTRIBUTES_CLID, this.clid);
			for(String type : this.setMap.keySet()) {
				JSONObject jsonAttributes = new JSONObject();
				Map<String, Object> attributes = this.setMap.get(type);
				for(String attribute : attributes.keySet()) {
					jsonAttributes.put(attribute, attributes.get(attribute));
				}
				json.put(type, jsonAttributes);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}							
		return json.toString();
	}

}
