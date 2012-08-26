package com.yaqtek.gps.tracker;

import java.text.DateFormat;
import java.util.Date;

import com.yaqtek.gps.tracker.R;

import com.yaqtek.gps.tracker.GpsIntentService;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity implements LocationListener {
	public String TAG = "GPS Tracker Main"; 
	
	private LocationManager locationManager;
	private ToggleButton gpsToggleBtn;
	private String provider;
	private GpsReceiver receiver;
	
	protected TextView latField;
	protected TextView lonField;
	protected TextView gpsTimestampField;
	protected TextView providerField;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //check to make sure GPS is enabled
        Log.d(TAG,"Checking GPS status");
        gpsCheckStatus();
        
        // Instantiate GPS Toggle Button
        Log.d(TAG,"Instantiating ToggleButton");
        gpsToggleBtn = (ToggleButton)findViewById(R.id.gpsToggleBtn);
        
        // Registers the Broadcast receiver for the IntentService
        IntentFilter filter = new IntentFilter(GpsReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new GpsReceiver();
        registerReceiver(receiver, filter);
        
        
        // Set latFeild on lonField
        latField = (TextView)findViewById(R.id.lat);
        lonField = (TextView)findViewById(R.id.lon);
        gpsTimestampField = (TextView)findViewById(R.id.gpsTimestampField);
        providerField = (TextView)findViewById(R.id.provider);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void gpsToggleClicked(View view) {
    	
    	String result = "GPS Track ";
    	boolean on = ((ToggleButton) view).isChecked();
 		   	
		
		if (on){
			result = result+"started.";
			gpsStart();
		}else {
			result = result+"stopped.";
			gpsStop();
		}
		 		   	
	   	Toast.makeText(MainActivity.this, result.toString(),Toast.LENGTH_SHORT).show();
   	}
    
    public void gpsCheckStatus(){
    	// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to 
		// go to the settings
    	Log.d(TAG,"in gpsCheckStatus()");
    	locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabled = locationManager
			.isProviderEnabled(LocationManager.GPS_PROVIDER);
		Log.d(TAG,"enabled: "+enabled);
		
		if (!enabled) {
		  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		} 
    }
    
    public void gpsStart(){
    	/**
    	 * Starts a GPS IntentService. Also begins updating the GPS location in 
    	 * the main UI. The GPS 
    	 * 
    	 */
    	// This starts a LocationManager and gets location SystemService
    	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	
    	//Criteria criteria = new Criteria();  	
    	//provider = locationManager.getBestProvider(criteria, false);
    	provider = locationManager.GPS_PROVIDER;
    	   	
    	Log.d(TAG,"Provider: " + provider);
        Location location = locationManager.getLastKnownLocation(provider);
        gpsUpdateLatLon(location);
        
        // Start GPS logging
        gpsStartLog();
        
        // This causes this activity to listen for the onLocationChange broadcast.
        locationManager.requestLocationUpdates(provider, 1000, 1, this);
        
    }
           
    public void gpsStop(){
    	
    }
    
    public void gpsStartLog(){
    	Log.d(TAG,"[gpsStartLog()]");
    	Intent gpsIntent = new Intent(MainActivity.this, GpsIntentService.class);
		gpsIntent.putExtra(GpsIntentService.PARAM_IN_MSG, "30*1000");
		gpsIntent.putExtra(GpsIntentService.PARAM_GPS_DT, 30*1000);
		
		Log.d(TAG,"[gpsStartLog()] Starting gpsIntentService");
		startService(gpsIntent);
    }
    
    public void gpsUpdateLatLon(Location location){
    	if (location != null) {
	      System.out.println("Provider has been selected.");
	      
	      
	      onLocationChanged(location);
	      
	    } else {
	      providerField.setText("not available");
	      latField.setText("not available");
	      lonField.setText("not available");
	      gpsTimestampField.setText("not available");
	      
	    }
    	
    }
    @Override
    public void onLocationChanged(Location location) {
      // This is automatically called because this activity has registered
	  // for requestLocationUpdates.
      double lat = (double) (location.getLatitude());
      double lon = (double) (location.getLongitude());
      Date date = new Date(location.getTime());
      DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
      DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
      
      String slat = Double.toString(lat);
      String slon = Double.toString(lon);
      
      providerField.setText(location.getProvider());
      latField.setText(slat);
      lonField.setText(slon);
      gpsTimestampField.setText(dateFormat.format(date) + " - " + timeFormat.format(date) );
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
     // TODO Auto-generated method stub
     // I had to put this in so the main activity could implement LocationListener	
    }
    
    /* Request updates at startup */
    /*
    @Override
    protected void onResume() {
      Log.d(TAG,"In onResume()");
      super.onResume();
      locationManager.requestLocationUpdates(provider, 400, 1, this);
      Log.d(TAG,"Leaving onResume()");
    }
    */
    
    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
      super.onPause();
      locationManager.removeUpdates(this);
    }
   
    
    @Override
    public void onProviderEnabled(String provider) {
      Toast.makeText(this, "Enabled new provider " + provider,
          Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
      Toast.makeText(this, "Disabled provider " + provider,
          Toast.LENGTH_SHORT).show();
    }
    
    /**
     * @author wilblack
     * Receives the GpsIntentService broadcast 
     */
    public class GpsReceiver extends BroadcastReceiver {
 		public static final String ACTION_RESP = "com.yaqtek.gps.tracker";
 		
 		@Override
 		public void onReceive(Context context, Intent intent){
 			String text = intent.getStringExtra(GpsIntentService.PARAM_OUT_MSG);
 			Log.d(TAG, "text: ");
 			Toast toast = Toast.makeText(MainActivity.this,
					text,
					Toast.LENGTH_LONG);
 			toast.show();
 		}
 	}
    
 }
    
    

