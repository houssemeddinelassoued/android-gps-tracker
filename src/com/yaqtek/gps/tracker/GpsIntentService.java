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
    
    public String provider;
    String TAG = "GpsIntentService";
        
    public GpsIntentService() {
    	super("GpsIntentService");
    	Log.d(TAG,"[GpsIntentService consturctor]");
    }
 
    /**
     * This is invoked on the worker thread once its created and startService is called. 
     */
    @Override
    protected void onHandleIntent(Intent intent) {
    	Log.d(TAG,"[onHandleIntent()]");
    	int gps_dt = intent.getIntExtra(PARAM_GPS_DT, 2*60*1000);
    	
    	int count = 0;
        while (count < 20*48) {
        	Log.d(TAG,"[onHandleIntent()] In while loop");
        	count++;
        	
	        String resultTxt = (String) DateFormat.format("MM/dd/yy h:mmaa - ", System.currentTimeMillis());
	        
	        
	        // Get the location
	        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        provider  = locationManager.GPS_PROVIDER;
	        Location location = locationManager.getLastKnownLocation(provider);
	        
	        // Create retrun text
	        double lat = location.getLatitude();
	        double lon = location.getLongitude();
	        resultTxt = resultTxt + "\nLat: "+lat+"\nLon: "+lon;
	        Log.d(TAG,"resultTxt: "+resultTxt);
	        
	        // Create return braodcastIntent and give it the message
	        // then send the broadcast.
	        Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(GpsReceiver.ACTION_RESP);
	        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	        broadcastIntent.putExtra(PARAM_OUT_MSG, resultTxt);
	        sendBroadcast(broadcastIntent);
	        SystemClock.sleep(30*1000); // 30 seconds
        }
    }
}
