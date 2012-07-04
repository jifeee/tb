package com.access2;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import classes.AllowedPackages;
import classes.Reporter;
import classes.Writer;

public class TextbusterService extends Service{
	private static final String TAG = "SERV";
	private Context context;
	private String aclCallbackId = null;
	
	private volatile BluetoothAdapter bluetoothAdapter;
	
	final Handler nextAddressHandler = new Handler();
	final Handler closeRfcommHandler = new Handler();
	final Handler checkLockHandler = new Handler();
	
	BroadcastReceiver receiver;
	BroadcastReceiver btReceiver;
	BroadcastReceiver aclReceiver;
	
	private BluetoothSocket rfcommSocket = null; 
	BluetoothDevice bluetoothDevice = null; 
	
	private ArrayList<String> macAddresses = new ArrayList<String>();
	
	private boolean incomingCall; 
	
	private int lockCheckInterval = 1;
	private int lockInterval = 100;
	private int discoveryInterval= 120;
	private int discoveryDelay = 120;
	private int closeRfcommInterval = 15;

	private boolean locked = false; 
	private boolean lockedByBT = false;
	private boolean lockedByTB = false;
	
	private long lastACL = 0;
	
	private boolean active = true; 
	
	private String allowedCallPackage;
	private String allowedNavigationPackage;
	
	AllowedPackages myAllowedPacks;
	Set<String> myPacks;
	
	String lastMac=" ";
	
	PowerManager pm;
	
	Writer w;
	
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	
	public static final String SET_ACTIVE = "com.access2.textbuster.SET_ACTIVE";
	public static final String SET_INACTIVE = "com.access2.textbuster.SET_INACTIVE";
	public static final String SEND_MAC = "com.access2.textbuster.SEND_MAC";
	public static final String OUTGOING_CALL_START = "com.access2.textbuster.OUTGOING_CALL_START";
	public static final String OUTGOING_CALL_END = "com.access2.textbuster.OUTGOING_CALL_END";
	public static final String SEND_ALLOWED_CALL_PACKAGE = "com.access2.textbuster.SEND_ALLOWED_CALL_PACKAGE";
	public static final String NAVIGATION_START = "com.access2.textbuster.NAVIGATION_START";
	public static final String APP_START = "com.access2.textbuster.APP_START";
	
	int connectCount=0;
	int eventCount=0;
	int runCount;
	
	String imei;
	
	LocationManager locationManager;
	
	private Reporter reporter = null;
	
	
	//This is the main service. It check
	
	public void onCreate(){
		
		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		imei = tm.getDeviceId();
		
		
		
		w = new Writer();								//writes logs to a file on the sdcard
		reporter = new Reporter(this, w);				// class that prepares the events to be sent out to the server
		
		Log.i(TAG, "TB Service started");
		
		context=this; 

		setupReceivers();								//setting up a couple of broadcastreceivers
		
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		myAllowedPacks = new AllowedPackages();			//class that contains the names of packages of apps that are allowed when the phone is locked
		myPacks = myAllowedPacks.getAllowed();


    	settings = getSharedPreferences("textbuster", 0);
    	editor = settings.edit();
    	
    	macAddresses.add("00:12:A1:C8:00:06");			//hardcoded textbuster mac for tests
    	
    	
    	//getting mac addresses that are stored in the shared preferences
    	for (int i=0; i>-1; i++) {				
    		if (settings.getString(String.valueOf(i), "").equals("")) {
    			break;
    		}
    		else {
    			macAddresses.add(settings.getString(String.valueOf(i), ""));
    			Log.i(TAG, "added " + settings.getString(String.valueOf(i), "") + " to macs from settings");
    		}
    	}

    	//start the loop
		new Timer().scheduleAtFixedRate(mainloop, 0, lockCheckInterval*1000);	
	}
	
	
	
	private TimerTask mainloop = new TimerTask(){ 
		public void run(){

			runCount++;
			Log.i(TAG, "locked: " + locked + " locked by BT: " + lockedByBT + " locked by TB: " + lockedByTB);

			
			//collect data about the phone every 15 seconds	
			if (runCount==15) {
				runCount = 0;
				eventCount++;
				try {
					reporter.collectData(lockType(), lastMac);
					reporter.writeData();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i(TAG, e.toString());
				}

			}

			
			//send out package to the server every minute (4*15sec)
			if (eventCount==4) {
				 
				try {
					reporter.sendData();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, e.toString());
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, e.toString());
					e.printStackTrace();
				}
				eventCount = 0;
			}
				
	
			
			Log.i(TAG, "topact: " + reporter.getTopActivity() + " active: " + active + "mac size " + macAddresses.size() + " count " + connectCount);
			
			if (connectCount==macAddresses.size()) {
				connectCount=0;
			}
			
			//checking if the phones screen is on, set true for tests
//			boolean isScreenOn = pm.isScreenOn();
			boolean isScreenOn = true; 
			
			// No need to do all this if the screen is off anyways
			if (isScreenOn && active && macAddresses.size()!=0) {
				
				// check if the phone should be locked because Bluetooth is turned off
				checkBTlock();
				
				// check if the phone should be locked because theres a textbuster
				checkTBlock();
				
				// try to connect to known textbuster MAC Ids
				connect();
				
				connectCount++;

			}
			
				checkCalls();
			
		}
	};
	
	private void checkBTlock () {
		
		// Lock if Bluetooth is off
		if(reporter.getBluetoothState() < Reporter.BLUETOOTH_ON ){
			lockedByBT=true;  
			lock(LockActivity.LOCK_BLUETOOTH);
			
		}	
		
		// Unlock if phone isnt locked by Textbuster
		else {
			lockedByBT = false; 
			if (lockedByTB==false) {
				locked=false; 
				unlock();
			}
		}
	}
	
	
	private void checkTBlock() {
		
		long sinceLastACL = new Date().getTime() - lastACL;
	
		
		// If there was a Textbuster connected within the last x seconds, phone is locked
		if(sinceLastACL <= lockInterval * 1000){
			lockedByTB=true; 
			locked=true; 
			lock(LockActivity.LOCK_TEXTBUSTER);

		}
		
		// If no TB has been seen for a while and Bluetooth is on, unlock
		if(sinceLastACL > lockInterval * 1000) {
			lockedByTB=false; 
			if (lockedByBT==false) {
				locked=false; 
				unlock();
			}
			
			else {

			}
		}
		
		
		else if(sinceLastACL > lockInterval * 1000 && locked == true){
			
					unlock();
		}
		
		else {
		}
	}
	
	
	// If no TB was seen for while, try to connect to known TB MAC Ids
	public void connect () {
			long sinceLastACL = new Date().getTime() - lastACL;
			Log.i(TAG, sinceLastACL/1000 + " since last connection");
			if(sinceLastACL < discoveryDelay * 1000){
				
			}
			
			else {
				
	//			if(rfcommSocket == null){
				rfcommSocket = null; 
					try{
		
						String mac = macAddresses.get(connectCount); 
						Log.d(TAG, "Trying to connect to " + mac);
					
						
						if (!(macAddresses.get(connectCount).equals("null"))) {
						
							bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
							bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
							final BluetoothSocket bluetoothSocket = rfcommSocket = bluetoothDevice.
							
							createRfcommSocketToServiceRecord(new java.util.UUID(0x0000110100001000L, 0x800000805F9B34FBL));
							bluetoothSocket.connect();
						}
					}catch(IOException e){
	//					Log.d(TAG, "IO Exception: " + e.getMessage());
						
						rfcommSocket = null;
					}finally{
					}
	//			}
			}
	
		}

	private void lock(int type){ 
		
		Log.i(TAG, "lock, type: " + type);
		
		boolean allowedPackInFront = false; 
		

		//get the set of allowed packages and check if an allowed app is in front
		Iterator it = myPacks.iterator();
		while (it.hasNext()) {
		    // Get element
		    String a = it.next().toString();
			if (reporter.getTopActivity().contains(a)) {
			allowedPackInFront = true; 
		}
		}

		if(reporter.getTopActivity().equals(LockActivity.class.getName())) {

			Log.i(TAG, "Lock already engaged " + reporter.getTopActivity());
		}
		
		else if (allowedPackInFront) {
			Log.i(TAG, "allowed activity is in front, :" + reporter.getTopActivity());
		}
		

		
		else{
			
			Log.i(TAG, "new Lockscreen");
			
			Intent intent = new Intent(context, LockActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
							Intent.FLAG_ACTIVITY_NO_HISTORY |
							Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
							Intent.FLAG_ACTIVITY_SINGLE_TOP); 
			intent.putExtra("type", type);
		
			startActivity(intent);
		}
	}
	
	// Send the broadcast to unlock
	private void unlock(){
//		Log.i(TAG, "unlock");
		Intent i = new Intent();
		i.setAction(LockActivity.BROADCAST_UNLOCK);
        getApplicationContext().sendBroadcast(i);
		
	}
	
	public IBinder onBind(Intent intent) {
		
		return null;
	}
	
	
	//check if there is an incoming call and unlock if there is
	private void checkCalls(){
		
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
		if (tm.getCallState()==0) {
			incomingCall=false; 
		}
		
		else {
			incomingCall=true;
			unlock();
		}

	}
	

	
	// BroadcastReceivers that handle the Bluetooth events
	public void setupReceivers () {
		aclReceiver = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		    	Log.i(TAG, "onreceive aclReceiver");
		        String action = intent.getAction();
		        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String address = device.toString();
				
				String event = "ACUN";
				
				if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
					event = "ACCN";
					Log.d(TAG, "ACL connected from " + address);
					
					lastACL = new Date().getTime();
					
					lastMac=address;

				} else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
					event = "ACDC";
					Log.d(TAG, "ACL disconnected from " + address);
//					w.appendLog("\nacldis" + address);
					lastACL = new Date().getTime();
					
					
					lastMac=address;
					
					if(rfcommSocket != null && rfcommSocket.getRemoteDevice().getAddress().equals(address)){
						rfcommSocket = null;
					
					}
					
					lastACL = new Date().getTime();
				} else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)){
					event = "ACDR";
					Log.d(TAG, "ACL disconnect requested from " + address);
				}

			}
		};

		/* Reveives events related to the Android's Bluetooth device.
		 */
		btReceiver = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		    	Log.i(TAG, "onreceive btReceiver " + intent.getAction());
		        String action = intent.getAction();
		        String event = "UN";
				
				if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
					event = "BTDS";
//					Log.d(TAG, "Discovery started");
				} else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
					event = "BTDF";
//					Log.d(TAG, "Discovery finished");
				}

			}
		};
		
		
		receiver = new BroadcastReceiver() {
			 
			public void onReceive(Context context, Intent intent) {
				Log.i(TAG, "received in service");
				Log.i(TAG, intent.getAction());
				
				
				if (intent.getAction().equals(SET_ACTIVE)) {
					Log.i(TAG, "service set active");
					active = true; 
				}
				
				if (intent.getAction().equals(SET_INACTIVE)) {
					Log.i(TAG, "service set INactive");
					active = false; 
				}
				
				//receive mac address from DetectActivity
				if (intent.getAction().equals(SEND_MAC)) {
					Log.i(TAG, "service received mac adress: " + intent.getStringExtra("MAC"));
					
					if (intent.getStringExtra("MAC")!=null) {
						if (!(intent.getStringExtra("MAC").equals("null"))) {
						Log.i(TAG, "added to macadresses: " + intent.getStringExtra("MAC"));
						macAddresses.add(0, intent.getStringExtra("MAC"));

						// removing duplicates
						HashSet h = new HashSet(macAddresses);
						macAddresses.clear();
						macAddresses.addAll(h);
						
						}
					}	
					
					
					for (int i=0; i<macAddresses.size(); i++) {
						
						// removing duplicates
						HashSet h = new HashSet(macAddresses);
						macAddresses.clear();
						macAddresses.addAll(h);
						
						editor.putString(String.valueOf(i), macAddresses.get(i));
						Log.i(TAG, "wrote " + macAddresses.get(i) + " to settings");
			        	editor.commit();
					} 
					 
				}
				
				
				//receive broadcasts from LockActivity when user wants to make a call or navigate
				if (intent.getAction().equals(OUTGOING_CALL_START)) {
					
	            	unlock();
	            	Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"));
	            	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            	startActivity(i);
	        		PackageManager pm = getPackageManager();
	        		
	        		//resolve the app that is going to handle the call intent & add it to allowed
	        		allowedCallPackage=i.resolveActivity(pm).getClassName();
	        		Log.i(TAG, "allowed for call: " + i.resolveActivity(pm).getClassName());
	        		myAllowedPacks.addPackage(allowedCallPackage);
	        		myPacks=myAllowedPacks.getAllowed();
	        		
				}
				
				if (intent.getAction().equals(OUTGOING_CALL_END)) {
					Log.i(TAG, "callout end");
				}
				
				if (intent.getAction().equals(NAVIGATION_START)) {
					
					unlock();
	            	Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"));
	            	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            	startActivity(i);
	            	PackageManager pm = getPackageManager();
	        		allowedNavigationPackage=i.resolveActivity(pm).getClassName();
	        		myAllowedPacks.addPackage(allowedNavigationPackage);
	        		Log.i(TAG, "Allowed for Navigation: " + i.resolveActivity(pm).getClassName());
	        		myPacks=myAllowedPacks.getAllowed();

				}

					
			}
			
		};
		
		
		//Receive intents when the guardian app is uninstalled and write out an event immediately
		BroadcastReceiver uninstallReceiver = new BroadcastReceiver() {
//			Logger log = new Logger(imei, w);
		    public void onReceive(Context context, Intent intent) {
//		    	Log.i(TAG, "onreceive textbuster uninstallReceiver " + intent.getAction());
//		        
//		        if(intent.toString().contains("eu.toasternet")){
//					log.set("state", (byte)0, (byte)0, (byte)0, 
//							(byte)0, (byte)4);						
//					log.write();
//
//				} 

			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addDataScheme("package");
		context.registerReceiver(uninstallReceiver, filter); 
		
		filter = new IntentFilter();
		filter.addAction(SET_ACTIVE);
		filter.addAction(SET_INACTIVE);
		filter.addAction(SEND_MAC);
		filter.addAction(OUTGOING_CALL_START);
		filter.addAction(OUTGOING_CALL_END);
		filter.addAction(Intent.ACTION_DIAL);
		filter.addAction(NAVIGATION_START);
		context.registerReceiver(receiver, filter); 
		
		
		
		filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
		context.registerReceiver(aclReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		context.registerReceiver(btReceiver, filter);
	}
	
	public void onDestroy() {
	    super.onDestroy();
	    unregisterReceiver(btReceiver);
	    unregisterReceiver(aclReceiver);
	    unregisterReceiver(receiver);
	    Log.i(TAG, "Service destroyed");
//	    w.appendLog("\ntbservdestr");
	}
	
	public int lockType () {
		int type=0;					//0=NO, 1=BT, 2=TB
		
		if (lockedByBT) {
			type=1;
		}
		
		else if (lockedByTB){
			type=2;
		}
		
		Log.i(TAG, "lockType: " + type);
		return type;
	}



}