package com.sociocast.android.util;

public class SociocastException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7009997153735540237L;
	
	private String message;
	
	public SociocastException(String message) {
		this.message = message;
	}
	
	@Override 
	public String getMessage() {
		return "SociocastException: " + message + " " + getMessage();
	}
	
}
