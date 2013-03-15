package com.sociocast.android;

import com.sociocast.android.model.EntityAttributes;
import com.sociocast.android.model.EntityObservation;
import com.sociocast.android.util.SociocastException;

public interface SociocastAPI {

	/*
	 * Register an entity observation
	 */
	public void entityObserve(EntityObservation observation);
	
	/*
	 * Retrieve a content profile
	 */
	public void contentProfile(String url, boolean humread) throws SociocastException;
	
	/*
	 * Retrieve an entity profile
	 */
	public void entityProfile(String eid);
	
	/*
	 * Modify entity attributes
	 */
	public void entityAttributes(EntityAttributes attributes);
	
}
