package classes;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class Reporter implements LocationListener {

	String TAG="TEX";
	
	public static final int BLUETOOTH_NOTAVAILABLE	= 0;
	public static final int BLUETOOTH_OFF			= 1;
	public static final int BLUETOOTH_ON			= 2;
	public static final int BLUETOOTH_SCANNING		= 3;
	
	public static final int LOCATION_NOTAVAILABLE	= 0;
	public static final int LOCATION_OFF			= 1;
	public static final int LOCATION_ON				= 2;
	public static final int LOCATION_NEW			= 3;
	
	private BluetoothAdapter bluetoothAdapter = null;
	private ActivityManager activityManager = null;
	private LocationManager locationManager = null;
	private LocationListener locationListener;  
	
	private ArrayList <Integer> bluetoothState = new ArrayList <Integer> ();
	private ArrayList <Integer> locationState = new ArrayList <Integer> ();
	private ArrayList <Location> gpsLocation = new ArrayList <Location> ();
	private ArrayList <Boolean> isScreenOn = new ArrayList <Boolean> ();
	
	public Reporter(Service service){
		
		activityManager = (ActivityManager) service.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		locationManager = (LocationManager) service.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		
	}
	
	public String getTopActivity(){
		
		List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(4096);	
		return runningTasks.get(0).topActivity.getClassName();
		
	}
	
	public int getBluetoothState(){
		
		if(bluetoothAdapter == null){
			return BLUETOOTH_NOTAVAILABLE;
		} else
			
		if(bluetoothAdapter.isDiscovering()){
			return BLUETOOTH_SCANNING;
		} else	
		
		if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){
			return BLUETOOTH_ON;
		} else
		
		return BLUETOOTH_OFF;

	}
	
	public int getLocationState(){
		
		if(locationManager == null){
			return LOCATION_NOTAVAILABLE;
		} else
			
		{ }
		
		return LOCATION_OFF;

	}
	

	
	
	
	public String toString(){
		return "BT : " + new Integer(getBluetoothState()).toString() +
			   " | Front : " + getTopActivity();
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}
	
	public void checkForLocation() {
		
		boolean a = (locationManager==null);
		Log.i(TAG, String.valueOf(a));
		
		if (locationManager != null) {
			
			locationListener = new MyLocationListener();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 35000, 10, locationListener);
		}
	}
	
	public void getLocation () {
	
	}
	

	}

