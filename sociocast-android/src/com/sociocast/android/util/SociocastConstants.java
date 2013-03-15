package com.sociocast.android.util;

public class SociocastConstants {
	
	public static final String EXTRA_CONNECTIVITY_CHANGE = "connectivity";
	public static final Boolean CONNECTIVITY_IS_NOW_ON = true;

	public static final String RETRY_QUEUED_ACTION = "com.sociocast.android.retry_queued_items";	
	public static final String EXTRA_KEY_TIME_STAMP = "time_stamp";
	public static final String EXTRA_KEY_REFERENCE = "reference";
	public static final String EXTRA_KEY_ID = "id";
	
    public static final String EXTRA_PARAMS = "sociocast.android.EXTRA_PARAMS";
    public static final String EXTRA_PARAMS_JSON = "json";
    public static final String EXTRA_RESULT_RECEIVER = "sociocast.android.EXTRA_RESULT_RECEIVER";	
    public static final String EXTRA_HTTP_VERB = "sociocast.android.EXTRA_HTTP_VERB";
    public static final String EXTRA_API_OBJECT = "sociocast.android.EXTRA_API_OBJECT";
    public static final String EXTRA_UUID = "sociocast.android.EXTRA_UUID";
    public static final String EXTRA_TIMESTAMP = "sociocast.android.EXTRA_TIMESTAMP";
        
    /*
     * API Requests
     */
    //public static final String REST_API
    public static final String REST_RESULT = "sociocast.android.REST_RESULT";
    public static final String RESULT_API_OBJECT = "sociocast.android.RESULT_API_OBJECT";
    public static final String REST_API_ENTITY_OBSERVATION = "sociocast.android.REST_API_ENTITY_OBSERVATION";

    public static String RESPONSE_OK_STATUS = "OK";
    public static int RESPONSE_OK_STATUS_CODE = 200;    
    
    public static String CONTENT_PROFILE_URL = "/content/profile";
    public static String ENTITY_PROFILE_URL = "/entity/profile";
    public static String ENTITY_ATTRIB_URL = "/entity/attributes";
    public static String ENTITY_OBSERVE_URL = "/entity/observe";    
            
}
