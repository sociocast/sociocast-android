package com.sociocast.android.client;

import java.util.ArrayList;
import java.util.Date;

import com.sociocast.android.Sociocast;
import com.sociocast.android.model.EntityObservation;
import com.sociocast.android.util.SociocastConstants;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;

public class SociocastClientActivity extends ListActivity {

	public static final String TAG = SociocastClientActivity.class.getName();
	
	private Sociocast sociocast;
			
	private ArrayAdapter<String> mAdapter;
	
	private boolean loading = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sociocast);
		
		mAdapter = new ArrayAdapter<String>(this, R.layout.item_label_list);                   
		setListAdapter(mAdapter);
		
		mAdapter.add("Loading...");
		                
		String clid = "c85";
		String apikey = "testclient";
		String secret = "testclient";
		
		// Create instance of Sociocast Android Library
		this.sociocast = Sociocast.newInstance(this, apikey, secret, clid, false);
		
		// Create the ResultReceiver
        ResultReceiver receiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
            	loading = false;
                if (resultData != null && resultData.containsKey(SociocastConstants.REST_RESULT)) {
                    onRESTResult(resultCode, resultData);
                }
                else {
                    onRESTResult(resultCode, null);
                }
            }            
        };
        
        this.sociocast.setReceiver(receiver);
        
		Log.println(Log.INFO, TAG, "Loading testEntityObservation");
		EntityObservation obs = new EntityObservation();
		obs.setEid("eid_test_1234");
		obs.setEvt("view");
		obs.setAttribute("url","http://www.sociocast.com");	
		obs.setClid(clid);
		obs.setTimestamp(new Date());
		
		Log.println(Log.INFO, TAG, "Observing Entity...");
		this.sociocast.entityObserve(obs);
		
		Log.println(Log.INFO, TAG, "Getting Content Profile...");
		this.sociocast.contentProfile("http://www.cnn.com", true);
		
		
		Log.println(Log.INFO, TAG, "Getting Entity Profile...");
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("cls.ctx");
		this.sociocast.entityProfile("eid_test_1234", true, attributes);		
	}
	
	private void onRESTResult(int code, Bundle result) {
		if(!loading) mAdapter.remove("Loading...");
		Log.d(TAG, "Received Result: Code (" + code + ") " + result);
		if(result != null) {
			String apiObject = result.getString(SociocastConstants.RESULT_API_OBJECT);
			String json = result.getString(SociocastConstants.REST_RESULT);			
			mAdapter.add(apiObject + "\n" + json);
		} else {
			mAdapter.add("Connection Failed...");
		}
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sociocast, menu);
		return true;
	}

}
