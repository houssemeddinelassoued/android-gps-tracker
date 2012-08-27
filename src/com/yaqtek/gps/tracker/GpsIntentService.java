package com.yaqtek.gps.tracker;

import com.yaqtek.gps.tracker.MainActivity.GpsReceiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * @author wilblack, 08.26.2012
 * This extends the IntentService adding optional parameters. Don't forget to 
 * add this to the activities in the Manifest file.
 * @PARAM_IN_MSG
 * @PARAM_OUT_MSG 
 */

public class GpsIntentService extends IntentService{
	public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";
    public static final String PARAM_GPS_DT = "gps_dt";
    
    
    boolean kill = false;
    long gpsDt = 5*60*1000;
    
    public String provider;
    String TAG = "GpsIntentService";
        
    public GpsIntentService() {
    	super("GpsIntentService");
    }
 
    /**
     * This is invoked on the worker thread once its created and startService is called. 
     */
    @Override
    protected void onHandleIntent(Intent intent) {
    	
    	gpsDt = intent.getLongExtra(PARAM_GPS_DT, 2*60*1000);
    	
    	int count = 0;
        while (count < 20*48 && !kill) {
        	count++;
	        String resultTxt = (String) DateFormat.format("MM/dd/yy h:mmaa - ", System.currentTimeMillis());
	        	        
	        // Get the location
	        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        provider  = locationManager.GPS_PROVIDER;
	        Location location = locationManager.getLastKnownLocation(provider);
	        
	        // Create result text
	        double lat = location.getLatitude();
	        double lon = location.getLongitude();
	        resultTxt = resultTxt + "\nLat: "+lat+"\nLon: "+lon;
	        	        
	        // Create return braodcastIntent and give it the message
	        // then send the broadcast.
	        Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(GpsReceiver.ACTION_RESP);
	        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	        broadcastIntent.putExtra(PARAM_OUT_MSG, resultTxt);
	        sendBroadcast(broadcastIntent);
	        SystemClock.sleep(gpsDt); // 30 seconds
        }
    }
    
    @Override
     public void onDestroy() {
    	kill = true;
    	super.onDestroy();
    	Log.d(TAG,"[onDestroy()]");
    	
    }
}
