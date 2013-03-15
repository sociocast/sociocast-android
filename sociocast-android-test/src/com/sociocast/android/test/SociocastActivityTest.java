package com.sociocast.android.test;

import com.sociocast.android.client.SociocastClientActivity;

import android.test.ActivityInstrumentationTestCase2;

public class SociocastActivityTest extends ActivityInstrumentationTestCase2<SociocastClientActivity> {

	public SociocastActivityTest() {
		super(SociocastClientActivity.class);
		// TODO Auto-generated constructor stub
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testOnCreate() {	
		assertTrue(this.getActivity() != null);
	}

}
