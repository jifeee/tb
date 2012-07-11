package classes;


import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;


// putting to together the events that are being sent out to the servcie


public class Reporter implements LocationListener {

	String TAG="TEX"; 
	
	public static final int BLUETOOTH_NOTAVAILABLE	= 0;
	public static final int BLUETOOTH_OFF				= 1;
	public static final int BLUETOOTH_ON				= 2;
	public static final int BLUETOOTH_SCANNING		= 3;
	
	public static final int LOCATION_NOTAVAILABLE		= 0;
	public static final int LOCATION_OFF				= 1;
	public static final int LOCATION_ON				= 2;
	public static final int LOCATION_NEW				= 3;
	
	private BluetoothAdapter bluetoothAdapter = null;
	private ActivityManager activityManager = null;
	private LocationManager locationManager = null;
	private PowerManager pm = null;
	private TelephonyManager tm = null;
	

	private int locked;
	private int alert;
	int eventCount = 0;
	String imei;
	final Handler handler;
	boolean gpsstarted = false;
	boolean locIsNew = false;
	String currentMac=" ";
	File f;
	public Logger log;
	Writer w;
	String textbusterMac;
	Service service;
	Location lastLocation = new Location("x");;
	Location newLocation = new Location("x");
	Location startLocation = new Location("x");
	Location endLocation = new Location("x");
	boolean tripRunning = false; 
	DateTime lastLock = new DateTime();
	

	
	public Reporter(Service service, Writer wr){
		
		this.service = service;
		activityManager = (ActivityManager) service.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		locationManager = (LocationManager) service.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		
		
		tm = (TelephonyManager)service.getSystemService(Context.TELEPHONY_SERVICE);
		pm = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
		
		imei = tm.getDeviceId();
		
		
		handler = new Handler();
		
		w = new Writer();
		log = new Logger(imei);
		
		//just for test purposes we always start GPS; in real use gps is started and stopped according to if the phone is locked via handler.post(gpsRun);
		//TODO : remove
//		startGPS();



	}
	
	
	public void collectData (int lockType, String mac) throws IOException  {
		
		//start or stop gps according to lock status of phone
		handler.post(gpsRun);
		
		locked = lockType;
		currentMac = mac;

		
		int sc = getScreenState();
		int bt = getBluetoothState();
		int lc = getLocationState();
		int lt = lockType;
		int al = getAlert();
		

		log.set("state", (byte)sc, (byte)bt, (byte)lc, 
				(byte)lt, (byte)al, currentMac);
		
//		  `screen` enum('OFF','ON','IL1','IL2') DEFAULT NULL,
//		  `bluetooth` enum('NA','OFF','ON','SCN') DEFAULT NULL,
//		  `gps` enum('NA','OFF','ON','NPOS') DEFAULT NULL,
//		  `locked` enum('NO','BT','TB','IL1') DEFAULT NULL,
//		  `alert` enum('NO','TBN','TBU','GUN','GUU') DEFAULT NULL,

		
		
		//if we have a new location
		if (lc==3) {
			
			Log.i(TAG, "Location LOGSET location: " + newLocation.toString());
//			w.appendLog("Location LOGSET\n");
			
			
			log.set("gps", newLocation.getTime(), newLocation.getLatitude(), newLocation.getLongitude(), 
					newLocation.getAltitude(), (double)newLocation.getSpeed(), (double)newLocation.getAccuracy(), 
					(double)newLocation.getBearing());

			
			locIsNew = false; 
			
		}
			

		

		eventCount++;

		
		
	}
	
	public void writeData () throws IOException {
		
		Log.i(TAG, "writeData");

		log.write();
	   

	
	}
	
	public void sendData () throws UnknownHostException, IOException {
		
		Log.i(TAG, "sendData");
		log.send();

	}
	
	
	
	public String getTopActivity(){
		
		List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(4096);	
		return runningTasks.get(0).topActivity.getClassName();
		
	}
	
	public int getBluetoothState(){
		
		int bt = 0;
		if(bluetoothAdapter == null){
			
		} else
			
		if(bluetoothAdapter.isDiscovering()){
			bt=3;
		} else	
		
		if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){
			bt=2;
		} else 
			
		if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
			bt=1;	
		}
		
		return bt;

	}
	
	public int getLocationState(){
		
		int state = 0;

		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false){
			state=1;
		}
		
		else {
			state=2;
			float dist = lastLocation.distanceTo(newLocation);
			
			Log.i(TAG, "getLocationState, newLocLat: " + newLocation.getLatitude() + " isNew: " + locIsNew 
				+ " dist: " + dist + "isBetter: " + isBetterLocation(newLocation, lastLocation));
			
			//dont send out location all the time, only when we have a new & gopod location
			if (newLocation.getLatitude()!=0  && dist > 1 && locIsNew  && isBetterLocation(newLocation, lastLocation) && locked!=0) { //&& locked!=0,&& locIsNew
//			if (newLocation.getLatitude()!=0) {
				
				state = 3;
			}

		}	
		Log.i(TAG, "LOCATIONSTATE: (na, off, on, new)" + state);
		return state;
//		return 3; 
	}
	
	public int getScreenState () {
		int on=0;
		
		if (pm==null) { 
			on=2;
		}
		
		else if (pm.isScreenOn()) {
			on = 1;
			}
		
		return on;
	}
	

	
	
	//currently not used
	public Location getLocation() {
			
			Location l = null;
			Location l2 = null;
			
			locationManager = (LocationManager) service.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
			if (locationManager != null) {
	
				
				Criteria criteria = new Criteria();
		
				
				
//				l = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
				l = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
			}
			
			else {
				Log.i(TAG, "LocationManager null");
			}
			return l;
		}

	
	//check if something is wrong, eg the guardian app is not running on the system 
	public int getAlert () {
			alert=0;
			
			boolean serviceRunning = false; 
		    ActivityManager manager = (ActivityManager) service.getSystemService(service.getApplicationContext().ACTIVITY_SERVICE);
		    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		        if ("eu.toasternet.SemperService".equals(service.service.getClassName())) {
		           Log.i(TAG, "Found running service " + service.service.getClassName());
		           serviceRunning = true; 
		        }
		        
		
		    }
		    
		    if (!serviceRunning) {
		    	Log.i(TAG, "Guardian Service not running");
		    	alert=3;
		    }
		    return alert;
		}


	
	@Override
	public void onLocationChanged(Location arg0) {

		lastLocation = newLocation;
		newLocation = arg0;
		locIsNew=true;
		
		
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
	
	public LocationManager getLocMan () {
		return locationManager;
	}
	
	private final Runnable gpsRun = new Runnable() {
        public void run() {
//        	Toast.makeText(service, "Location: " + locIsNew + " "+  newLocation.getLatitude() + " " 
//        + newLocation.getLongitude(), Toast.LENGTH_LONG).show();
            setGPS();      
        }
    };

    private void setGPS() {


    		if (locked==2 && !gpsstarted) {
	            startGPS();
	            gpsstarted = true; 
	    	}      
	    	
	    	else if (locked !=2 && gpsstarted) {
	    		stopGPS();
    			gpsstarted = false; 
	    	}
	    
    }


	public void startGPS()
    {

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);

        Log.i(TAG, "gps start");
        handler.removeCallbacks(gpsRun);
    }

    public void stopGPS()
    {

        locationManager.removeUpdates(this);
        Log.i(TAG, "gps stop");
        handler.removeCallbacks(gpsRun);
    }
    
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }


	

	}

