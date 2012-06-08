package com.access2;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;

import android.os.IBinder;
import android.util.Log;
import android.os.Bundle;

/* Required for: Config file.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import android.os.Environment;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/* Required for: Log file
 */
import java.io.FileOutputStream;
import java.net.Socket;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/* Required for: Screen on/off check.
 */
import android.os.PowerManager;

/* Required for: Application whitelist check.
 */
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import java.util.List;

/* Required for: Textbuster device check.
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.bluetooth.BluetoothSocket;

/* Required for: Reaction to Bluetooth events.
 */
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/* Required for: GPS.
 */
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;

/* Required for: Looping through lists/arrays/hashtables
 */
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;

/* Required for: Hashing the IMEI for reporting.
 */
import android.telephony.TelephonyManager;
import java.security.MessageDigest;

public class BusterService extends Service implements LocationListener {
	private static final String TAG = "Textbuster";
	
	public static final String ACTION_ENABLE_GPS = "com.access2.Textbuster.enableGPS";
	public static final String ACTION_DISABLE_GPS = "com.access2.Textbuster.disableGPS";
	
	/* Bluetooth states.
	 */
	public static final int BT_NODEVICE = 0;
	public static final int BT_OFF 		= 1;
	public static final int BT_ENABLING	= 2;
	public static final int BT_ON 		= 3;
	public static final int BT_SCANNING	= 4;
	
	public volatile boolean locked = false;
	
	/* Drive the loops.
	 */
	private Timer lockTimer = new Timer();

	private Timer btTimer = new Timer();
	
	/* Respresents the state of the Bluetooth connection to the Textbuster device.
	 */
	private volatile int bluetoothState = -1;
	
	/* Holds the names of all processes that are whitelisted, e.g. allowed to be 
	 * accessed even when the service should lock the phone.
	 */
	private volatile ArrayList<String> processWhitelist = new ArrayList<String>();
	
	/* Holds the milliseconds interval that the loop is suspended after initialization.
	 */
	private long waitOnBoot = 3000;
	
	/* Holds the milliseconds interval after which GPS is no longer monitored when a Textbuster device is no longer present.
	 */
	private long disableGPSafter = 60000;
	
	/* Holds the timestamp after which the next lock checks are performed.
	 */
	private volatile long nextLockCall = 0;
	
	/* Holds the timestamp for the last time a Textbuster device 
	 */
	private volatile long lastTextbusterLock = 0;
	
	/* Represents the last time the config was updated.
	 */
	private volatile long lastConfigUpdate = 0;
	
	private volatile Hashtable btDetected = new Hashtable();
	private volatile ArrayList<String> btAddresses = new ArrayList<String>();
	private volatile PowerManager powerMgr;
	private volatile BluetoothAdapter bluetoothAdp;
	private volatile ActivityManager activityMgr;
	private volatile LocationManager locationMgr;
	private volatile TelephonyManager telephonyMgr;
	private volatile Location lastLocation;
	private volatile long gpsDiscrepancy = 0;
	private volatile Location lastReportedLocation;
	
	private volatile byte[] deviceId;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}		
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		/* Update config.
		 */
		readConfig();
		
		processWhitelist.add("com.access2.Textbuster");
		processWhitelist.add("com.android.launcher2.Launcher");
		
		/* Create bluetooth adapter which is used in the loop.
		 */
		bluetoothAdp = BluetoothAdapter.getDefaultAdapter();
	
		/* Create activity manager which is used in the main loop.
		 */
		activityMgr = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		
		/* Create power manager which is used in the loop to determine if the screen is off.
		*/
		powerMgr = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		
		/* Create location manager which is used to receive the current GPS status and location.
		*/
		locationMgr = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		
		lastLocation = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		enableGPS();
		
		telephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		telephonyMgr.getDeviceId();

		MessageDigest md;
		try{
			md = MessageDigest.getInstance("SHA-256");
			md.update(telephonyMgr.getDeviceId().getBytes());
			deviceId = md.digest();
		} catch(java.security.NoSuchAlgorithmException e) {
			deviceId = null;
		}
		
		/* Initial assessment of Bluetooth state.
		 */
		if (bluetoothAdp == null) {
			Log.d(TAG, "No Bluetooth found");
			bluetoothState = BT_NODEVICE;
		} else if (!bluetoothAdp.isEnabled()) {
			Log.d(TAG, "Bluetooth off");
			bluetoothState = BT_OFF;
		} else {
			Log.d(TAG, "Bluetooth on");
			bluetoothState = BT_ON;
			if(bluetoothAdp.isDiscovering()) {
				Log.d(TAG, "Bluetooth scanning");
				bluetoothState = BT_SCANNING;
			}
		}
	
		/* Wait for a given number of milliseconds after create to give the system enough time to boot before starting the lock checks.
		 */
		Log.d(TAG + "Loop", "Creation: loop suspended for " + waitOnBoot + " milliseconds");
		this.nextLockCall = new Date().getTime() + waitOnBoot;
		
		/* TextBuster service lock loop (timer-based)
		 */
		TimerTask lockLoop = new TimerTask() {
			public void run() {
				////////////////////////////////////////////////////////////
				// BIG OVERRIDE : disable all locking checks! Debugging only!!!
				////////////////////////////////////////////////////////////
				// if(true) return;
				////////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////////
				
				/* Get the current time in milliseconds.
				 */
				long currentTime = new Date().getTime();
				
				/* 
				 * Delay the loop in case a lock intent was called only a given number of milliseconds ago.
				 * Some processes like the home screen launchers do not allow the intent to start up right away,
				 * so we prevent calling startIntent multiple times. The lock intent will start eventually.
				 */
				if(nextLockCall > currentTime){
					return;
				}
				
				Status status = new Status();
				status.writeLog();
				
				
				/* Disable GPS listener when no Textbuster device was found for a given period of time.
				 */
				if(lastTextbusterLock + disableGPSafter < currentTime){
					Intent i = new Intent();
					i.setAction(ACTION_DISABLE_GPS);
					//getApplicationContext().sendBroadcast(i);	
				}
				
				/* Update config.
				 */
				readConfig();
				
				/* When the screen is off, no further locking checks are required.
				 */

				if(status.getScreenState() == 0){
					Log.d(TAG + "Loop", "Screen is off");
					
					nextLockCall = currentTime + 1000;
					return;
				}
				
				/* Check if top process is on the whitelist.
				 */
				boolean frontPorcessOnWhitelist = false;
				for(String process : processWhitelist) {
					if(process.startsWith(status.frontProcess)) {
						frontPorcessOnWhitelist = true;
					}
				}
				
				/* If the process is on the whitelist, there is no reason to progress further with any locking attempts.
				 */
				if(frontPorcessOnWhitelist) {
					Log.d(TAG + "Loop", "Whitelisted application");
					
					nextLockCall = currentTime + 1000;
					return;
				}
				
				/* Counteract when Bluetooth is off.
				 */
				if(status.getBluetoothState() == 1) {					
					/* Do not start lock activity twice.
					 */	
					if(status.frontProcess.equals("com.access2.NoBluetooth")) {
						Log.d(TAG + "Loop", "Bluetooth lock already engaged");
						
						nextLockCall = currentTime + 1000;
						return;
					}
		
					/* Call the Bluetooth lock activity and bring it to front.
					 */
					Intent intent = new Intent(getApplicationContext(), NoBluetooth.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
									Intent.FLAG_ACTIVITY_NO_HISTORY |
									Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
									Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);

					Log.d(TAG + "Loop", "Bluetooth off LOCK");
					
					nextLockCall = currentTime + 1000;
					return;
				}
				
				/* Do not continue unless Bluetooth is operational.
				 */
				if(status.getBluetoothState() < 2){
					Log.d(TAG + "Loop", "Bluetooth not on");
					
					nextLockCall = currentTime + 1000;
					return;
				}
				
				/* At this point, we can close the Bluetooth lock screen
				 */
				Intent i = new Intent();
				i.setAction("com.access2.NoBluetooth.finish");
		        getApplicationContext().sendBroadcast(i);
		        
				/* Check all Bluetooth address on lock list against the hashmap of devices currently in the vicinity.
				 */
				locked = false;
				Enumeration e = btDetected.keys();
 
				while(e.hasMoreElements()) {
					String mac = (String)e.nextElement();
					Long lastSeen = (Long)btDetected.get(mac);
					
					long lastSeenDiff = (currentTime - lastSeen.longValue()) / 1000;
					
					for(String btAddress : btAddresses){
						if(btAddress.equals(mac) && lastSeenDiff < 30){
							Log.d(TAG + "Loop", "Locked by " + mac + " (" + lastSeenDiff + " secs ago)");
							
							locked = true;
							lastTextbusterLock = currentTime;
						}
					}
				}
		
				/* If no device was found, there is no reason to lock anymore. Tell lock screen to close.
				 */
				if(!locked){
					Log.d(TAG + "Loop", "No Textbuster device");
					
					i = new Intent();
					i.setAction("com.access2.Lock.finish");
					getApplicationContext().sendBroadcast(i);
					
					nextLockCall = currentTime + 1000;
					return;
				}
		
				/* If we got this far, Bluetooth is working and a device was found.
				 * We bring the lock screen to the front now.
				 */
				if(!frontPorcessOnWhitelist) {	
					
					/* Do not start lock activity twice.
					 */
					if(status.frontProcess.equals("com.access2.Lock")) {
						Log.d(TAG + "Loop", "Application '" + status.frontProcess + "' lock already engaged");
						nextLockCall = currentTime + 1000;
						return;
					}
					
					/* Call the application lock activity and bring it to front.
					 */
					Intent intent = new Intent(getApplicationContext(), Lock.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
									Intent.FLAG_ACTIVITY_NO_HISTORY |
									Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
									Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					
					Log.d(TAG + "Loop", "Application '" + status.frontProcess + "' LOCK");
					
					nextLockCall = currentTime + 1000;
					return;
				}
				
			}
		};
						
		/* Start the timers.
		 */
		lockTimer.scheduleAtFixedRate(lockLoop, 0, 100);
	
		/* TextBuster service lock loop (timer-based)
		 */
		TimerTask bluetoothClientLoop = new TimerTask() {
			public void run() {
					for (String address : btAddresses){
						BluetoothDevice device = bluetoothAdp.getRemoteDevice(address);
										
						try{
							BluetoothSocket socket = device.createRfcommSocketToServiceRecord(new java.util.UUID( 0x0000110100001000L, 0x800000805F9B34FBL));
							socket.connect();
							//socket.close();
						}catch(java.io.IOException e) {
							Log.d( TAG, device.toString() + " " + e.toString() );
						}

					}
				
			}
		};
		lockTimer.scheduleAtFixedRate(bluetoothClientLoop, 0, 5000);
	
		/* TextBuster send-reports-to-server loop (timer-based)
		 */
		TimerTask sendReportsLoop = new TimerTask() {
			public void run() {
				File file = new File(getFilesDir() + "/1");

				int length = (int)file.length();
				int header_length = 2 + 2 + 32 + 4;
			
				Log.d(TAG, "Report length: " + new Long(length).toString());
				
				byte[] bytes = new byte[header_length + length];
				ByteBuffer buf = ByteBuffer.wrap(bytes);
				buf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
				buf.clear();
				
				buf.putShort((short)0x6254);
				buf.putShort((short)1);
				buf.put(deviceId);
				buf.putInt(length);
				
				try{
					FileInputStream fis = new FileInputStream(file);
					fis.read(bytes, buf.position(), length);
				}
				catch(java.io.FileNotFoundException e){ Log.d(TAG, "Event file not found"); }
				catch(java.io.IOException e){ Log.d(TAG, "Event file: IOExecption"); }
			
				try {
					Socket clientSocket = new Socket("192.168.0.46", 1234);
					OutputStream out = clientSocket.getOutputStream();
					InputStreamReader in = new InputStreamReader(clientSocket.getInputStream());
					out.write(bytes);
					
					char[] response = new char[4];
					
					in.read(response, 0, 4);
					
					Log.d(TAG, "Send result: " + response[0] + response[1] + response[2] + response[3]) ;
				}	
				catch(IOException ioe) {
					Log.d(TAG, "Sending status failed");
				}

			};
		};
		lockTimer.scheduleAtFixedRate(sendReportsLoop, 0, 10000);
	
		/* Register the BroadcastReceiver
		 */
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(ACTION_ENABLE_GPS);
		filter.addAction(ACTION_DISABLE_GPS);

		registerReceiver(mReceiver, filter);
		
		filter = new IntentFilter();
		
		String[] actions={ BluetoothDevice.ACTION_ACL_CONNECTED };
		
		for(int i = 0; i < actions.length; i++){
				filter.addAction(actions[i]);
		}
		
		registerReceiver(aclConnectedReceiver, filter);
		//registerReceiver(aclDisconnectedReceiver, filter);
		
	}
	
	class Status{
		private Byte screenState = null;
		private Byte bluetoothState = null;
		private Byte gpsState = null;
		private String frontProcess = null;
		private Location location = null;
		
		private Long locationAge = null;
		
		public Status(){ 
			/* Get current running task and determine process on top.
			*/
			List<ActivityManager.RunningTaskInfo> runningTasks = activityMgr.getRunningTasks(4096);	
			this.frontProcess = runningTasks.get(0).topActivity.getClassName();
		};
		
		/* Get current screen state.
		 * 
		 *  0 = screen off, 1 = screen on
		 */
		public byte getScreenState(){
			if(this.screenState == null){
				this.screenState = new Byte((powerMgr != null && powerMgr.isScreenOn()) ? (byte)1 : (byte)0);
			}
			
			return this.screenState.byteValue();
		};
		
		/* Get current Bluetooth state.
		 * 
		 * 0 = Bluetooth unavailable, 1 = Bluetooth disabled, 2 = Bluetooth enabled, 3 = Bluetooth discovering
		 */
		public byte getBluetoothState(){
			if (this.bluetoothState == null){
				this.bluetoothState = new Byte(( bluetoothAdp == null) ? (byte)0 :
								      (!bluetoothAdp.isEnabled()) ? (byte)1 : 
								      ( bluetoothAdp.isDiscovering()) ? (byte)3 : 2);
			}
			
			return this.bluetoothState.byteValue();
		};
		
		/* Get current GPS state.
		 * 
		 * 0 = GPS unavailable, 1 = GPS off, 2 = GPS on, 3 = new location available
		 */
		public byte getGPSState(){
			if (this.gpsState == null){
				this.gpsState = new Byte( (locationMgr.getProvider(LocationManager.GPS_PROVIDER) == null) ? (byte)0 :
				( locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) ? (byte)2 : (byte)1);
				
				if(this.gpsState.byteValue() > 0 && lastLocation != null){
					this.location = lastLocation;
					
					this.locationAge = new Long(System.currentTimeMillis() - this.location.getTime() - gpsDiscrepancy);
					
					Log.d("Funk", this.locationAge.toString());
					
					if(this.locationAge <= 5000){
						
						if(lastReportedLocation == null){
							this.gpsState = new Byte((byte)3);
						}else if(lastReportedLocation.getTime() != this.location.getTime()){
						
							Log.d("Funk", " " + new Long(lastReportedLocation.getTime()).toString() + " : " + new Long(this.location.getTime()).toString() + " : " + this.locationAge.toString());
						
							this.gpsState = new Byte((byte)3);
							
						} 
						
					}
				}
			}
			
			return this.gpsState.byteValue();
		};
		
		/* Get current loation provider.
		 */
		public Location getLocation(){
			if (this.location == null){
				 this.location = location;
			}
			
			return this.location;
		};
	
		public void writeReadableLog(){
			String LOGFILENAME = "1.log";
		
			String log = "" + this + "\n";
		
			try{
				FileOutputStream fos = openFileOutput(LOGFILENAME, Context.MODE_PRIVATE | Context.MODE_APPEND);
				try{
					fos.write(log.getBytes());
					fos.close();
				}catch(java.io.IOException e){};
			}catch(java.io.FileNotFoundException e){};
		};
		
		public void writeLog(){
			String LOGFILENAME = "1";
		
			try{
				FileOutputStream fos = openFileOutput(LOGFILENAME, Context.MODE_PRIVATE | Context.MODE_APPEND);
				try{
					fos.write(this.getBytes());
					fos.close();
				}catch(java.io.IOException e){};
			}catch(java.io.FileNotFoundException e){};
		};
		
		public byte[] getBytes(){
			byte[] bytes = new byte[512];
			ByteBuffer buf = ByteBuffer.wrap(bytes);
			buf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
			buf.clear();
			
			buf.putLong(System.currentTimeMillis());
			buf.put((byte)((this.getScreenState() << 6) + (this.getBluetoothState() << 4) + (this.getGPSState() << 2)));
			
			byte[] ret = new byte[buf.position()];
			
			System.arraycopy(bytes, 0, ret, 0, buf.position());
			
			return ret;
		};
		
		public String toString(){
			String string = new String();
			string += "[Time: " + new Long(System.currentTimeMillis()).toString();
			string += " Scrn: " + new Byte(this.getScreenState()).toString();
			string += " Blue: " + new Byte(this.getBluetoothState()).toString();
			string += " GPS: "  + new Byte(this.getGPSState()).toString();

			if(this.gpsState.byteValue() == 3){
				string += " POS: " + new Double(this.location.getLatitude()).toString();
				
				lastReportedLocation = this.location;
			}
		
			string += " Frnt: " + this.frontProcess;
			return string + "]";
		};
	};
	
	public Status createStatus(){
		
		Status status = new Status();
		
		return status;
	};
	
	/* Create the ACL BroadcastReceiver.
	 */
	private final BroadcastReceiver aclConnectedReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String address = device.toString();
				
			long currentTime = new Date().getTime();
			
			Long lastSeen = (Long)btDetected.put(address, new Long(currentTime));
				
			Log.d(TAG + "ACL", "+ " + address);
		}
	};
	
	/* Create the ACL disconnect BroadcastReceiver.
	 */
	private final BroadcastReceiver aclDisconnectedReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String address = device.toString();
				
			long currentTime = new Date().getTime();
			
			Long lastSeen = (Long)btDetected.put(address, new Long(currentTime));
				
			Log.d(TAG + "ACL", "- " + address);
		}
	};
	
	/* Create the BroadcastReceiver.
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
			if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
				int stateExtra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				switch(stateExtra){
					case BluetoothAdapter.STATE_OFF:
					case BluetoothAdapter.STATE_TURNING_OFF:
						bluetoothState = BT_OFF;
						Log.d("ActionService", action + " off");
						break;
					case BluetoothAdapter.STATE_TURNING_ON:
						bluetoothState = BT_ENABLING;
						Log.d("ActionService", action + " enabling");
						break;
					case BluetoothAdapter.STATE_ON:
						bluetoothState = BT_ON;
						Log.d("ActionService", action + " on");
						break;
					default:
						bluetoothState = BT_OFF;
						Log.d("ActionService", action + " " + stateExtra + "???");
						break;
				}	
	        }
	        else if(action.equals(ACTION_ENABLE_GPS)){
				enableGPS();
			}else if(action.equals(ACTION_DISABLE_GPS)){
				disableGPS();
			}
		}
	};
	
	/* Read in a config file if one exists.
	 */
	private void readConfig(){
		
		JSONObject config = null;
		try{
			File file = new File(Environment.getExternalStorageDirectory() + "/config");
			
			/* Do not update unless a more recent config is found.
			 */
			if(file.lastModified() < lastConfigUpdate){
				return;
			}
			
			Log.d(TAG + "Config", "Config file : " + file.getAbsolutePath());
			
			FileInputStream is = new FileInputStream(file);
			
			BufferedReader buf = new BufferedReader( new InputStreamReader(is) );
			
			String content = new String();
			String line = new String();
			
			while( (line = buf.readLine() ) != null){
				content += line;
			}
		
			config = new JSONObject(content);
		
			Log.d(TAG + "Config", config.toString());
		
		} catch (FileNotFoundException e) {
			Log.d(TAG + "Config", "No config on external storage");
			return;
		} catch (IOException e){
			Log.d(TAG + "Config", "Error reading config from external storage");
			return;
		} catch (JSONException e){
			Log.d(TAG + "Config", "Malformed JSON config");
			return;
		}
		
		try{
			String app = config.getString("app");
			if(!app.equals("Textbuster")){
				Log.d(TAG + "Config", "Wrong app (" + app + ")");
				return;
			}
		}catch (JSONException e){
			Log.d(TAG + "Config", "Malformed JSON config : app");
		}
		
		try{
			JSONArray devices = config.getJSONArray("devices");
			
			btAddresses.clear();
			
			for(int i = 0; i < devices.length(); i++){
				JSONObject device = devices.getJSONObject(i);
				
				btAddresses.add(device.getString("address"));
				
				Log.d(TAG + "Config", "Addresses : " + btAddresses.toString());
			}
			
			lastConfigUpdate = new Date().getTime();
			Log.d(TAG + "Config", "Loaded " + new Integer(btAddresses.size()).toString() + " device(s) [" + config.getString("app") + " " + config.getString("version") + "]" );
		}catch (JSONException e){
			Log.d(TAG + "Config", "Malformed JSON config : devices");
			return;
		}
	}
	
	public void enableGPS() {
		if(locationMgr == null){
			locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
		}
		locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, this);
		
		Log.d(TAG + "GPS", "Start monitoring");
	}
	
	public void disableGPS() {
		if(locationMgr != null){
			locationMgr.removeUpdates(this);
			locationMgr = null;
		}
		
		Log.d(TAG + "GPS", "Stop monitoring");
	}
	
	@Override
	public void onLocationChanged(Location loc) {
		lastLocation = loc;
		
		Log.d(TAG + "GPS", "Location changed: " + lastLocation.toString());
		gpsDiscrepancy = System.currentTimeMillis() - loc.getTime();
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG + "GPS", "Provider disabled: " + provider);
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG + "GPS", "Provider enabled: " + provider);
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG + "GPS", "Status changed: " + provider + " " + status);
	}
	
	/* Called when the service is destroyed.
	 */
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		if (lockTimer != null){
			lockTimer.cancel();
		}
		if (btTimer != null){
			btTimer.cancel();
		}
		unregisterReceiver(aclConnectedReceiver);
		unregisterReceiver(mReceiver);
	}
	
	/* Called when the service is started.
	 */
	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(TAG, "onStart (" + new Integer(startid) + ")");	
	}
}


		//final BluetoothDevice bd = bluetoothAdp.getRemoteDevice("00:07:61:BF:C5:80");
		//final BluetoothDevice bd = bluetoothAdp.getRemoteDevice("C8:89:79:F8:28:CC");

/*		
		long uuid0 = 0x0000000000001000L;
		long uuid1 = 0x8000006057028A06L;
					   
		try {
			bluetoothSocket = bd.createRfcommSocketToServiceRecord(new UUID(uuid0, uuid1));
		}catch(java.io.IOException e) {}
*/

		//Log.d(TAG, bd.fetchUuidsWithSdp().toString());
		
					
/*
					Log.d(TAG, "Retrieve UUIDs from " + bd.getAddress());
					ParcelUuid[] uuids = null;
					try{
						Class cl = Class.forName("android.bluetooth.BluetoothDevice");
						Class[] par = {};
						Method method = cl.getMethod("getUuids", par);
						Object[] args = {};
						uuids = (ParcelUuid[])method.invoke(bd, args);
					}
					catch (ClassNotFoundException e) { e.printStackTrace(); }
					catch (NoSuchMethodException e) { e.printStackTrace(); }
					catch (IllegalAccessException e) { e.printStackTrace(); }
					catch (InvocationTargetException e) { e.printStackTrace(); }
					
					Log.d(TAG, uuids.toString());
*/
					

/*
					Log.d(TAG, "Try to connect");
					try {
						bluetoothSocket.connect();
						Log.d(TAG, "BT yes");
					}catch(java.io.IOException e) {
						Log.d(TAG, "BT error " + e.toString() );
					}
*/

/*
					Log.d(TAG, "Try getUuids");
					try{
						Class cl = Class.forName("android.bluetooth.BluetoothDevice");
						Class[] par = {};
						Method method = cl.getMethod("getUuids", par);
						Object[] args = {};
						ParcelUuid[] retval = (ParcelUuid[])method.invoke(bd, args);
						
						if(retval == null) Log.d(TAG, "NO UUIDS");
						else Log.d(TAG, "UUIDS: "+new Integer(retval.length).toString());
					}catch(Throwable t) {
						Log.e(TAG, "Unexception exception", t);
					}
*/

/*
	
	private static final int ACL_READINGS = 5;
	private volatile float[] aclReadings = new float[(ACL_READINGS) * 3];
	private volatile int aclReadingsIndex = 0;
	
	
	final SensorManager sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
	sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

	
	public void onAccuracyChanged(Sensor sensor,int accuracy){
	}

	public void onSensorChanged(SensorEvent event){
		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){

			aclReadings[aclReadingsIndex * 3 + 0] = event.values[0];
			aclReadings[aclReadingsIndex * 3 + 1] = event.values[1];
			aclReadings[aclReadingsIndex * 3 + 2] = event.values[2];

			aclReadingsIndex++;
			if(aclReadingsIndex >= ACL_READINGS){
				aclReadingsIndex = 0;
			}

			float x = 0, y = 0, z = 0;
			for(int i = 0; i < ACL_READINGS * 3; i += 3){
				x += aclReadings[i + 0];
				y += aclReadings[i + 1];
				z += aclReadings[i + 2];
			}
			x /= ACL_READINGS;
			y /= ACL_READINGS;
			z /= ACL_READINGS;
			
			float jx = 0, jy = 0, jz = 0;
			for(int i = 0; i < (ACL_READINGS - 1) * 3; i += 3){
				jx += Math.abs(aclReadings[i + 0] - aclReadings[i + 3]);
				jy += Math.abs(aclReadings[i + 1] - aclReadings[i + 4]);
				jz += Math.abs(aclReadings[i + 2] - aclReadings[i + 5]);
			}
			
			double speed = Math.sqrt( (x * x) + (y * y) + (z * z) );
			double jitter = jx + jy + jz;
			
			//if(jitter < 1)
				Log.d(TAG + "Speed", "speed:" + new Double(speed).toString() + " j:" +new Double(jitter).toString());
				//Log.d(TAG + "Speed", "speed:" + new Double(speed).toString() + " x:" +new Float(jx).toString() + " y:" +new Float(jy).toString() + " z:" +new Float(jz).toString());
			//Log.d(TAG + "Speed", new Float(x).toString() + " " + new Float(y).toString() + " " + new Float(z).toString());
		}
	}
*/

/*
			if(action.equals("android.bluetooth.device.action.PAIRING_REQUEST")){
				BluetoothDevice device = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");

				try{
					device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
					device.getClass().getMethod("cancelPairingUserInput").invoke(device);
				}catch(java.lang.NoSuchMethodException e){
					Log.d(TAG + "Actions", e.toString());
				}catch(java.lang.IllegalAccessException e){
					Log.d(TAG + "Actions", e.toString());
				}catch(java.lang.reflect.InvocationTargetException e){
					Log.d(TAG + "Actions", e.toString());
				}

			}
*/
