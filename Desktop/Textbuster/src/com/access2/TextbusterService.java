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
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import classes.AllowedPackages;
import classes.Reporter;

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
	Set myPacks;
	
	PowerManager pm;
	
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	
	public static final String SET_ACTIVE = "com.access2.textbuster.SET_ACTIVE";
	public static final String SET_INACTIVE = "com.access2.textbuster.SET_INACTIVE";
	public static final String SEND_MAC = "com.access2.textbuster.SEND_MAC";
	public static final String OUTGOING_CALL_START = "com.access2.textbuster.OUTGOING_CALL_START";
	public static final String OUTGOING_CALL_END = "com.access2.textbuster.OUTGOING_CALL_END";
	public static final String SEND_ALLOWED_CALL_PACKAGE = "com.access2.textbuster.SEND_ALLOWED_CALL_PACKAGE";
	public static final String NAVIGATION_START = "com.access2.textbuster.NAVIGATION_START";
	
	int connectCount=0;
	int eventCount=0;
	int runCount;
	
	private Reporter reporter = null;
	
	public void onCreate(){
		
		
		
		reporter = new Reporter(this);

		
		Log.i(TAG, "TB Service started");
		
		context=this; 
		
		
		
		setupReceivers();
		

		
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		myAllowedPacks = new AllowedPackages();
		myPacks = myAllowedPacks.getAllowed();
		
		
    	
    	settings = getSharedPreferences("textbuster", 0);
    	editor = settings.edit();
    	
    	for (int i=0; i>-1; i++) {
    		if (settings.getString(String.valueOf(i), "").equals("")) {
    			break;
    		}
    		else {
    			macAddresses.add(settings.getString(String.valueOf(i), ""));
    			Log.i(TAG, "added " + settings.getString(String.valueOf(i), "") + " to macs from settings");
    		}
    	}
    	


		new Timer().scheduleAtFixedRate(mainloop, 0, lockCheckInterval*1000);
	}
	
	private TimerTask mainloop = new TimerTask(){ 
		public void run(){

			runCount++;
			
			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			Log.i(TAG, "" + telephonyManager.getDeviceId());
			
			Log.i(TAG, "runC" + runCount  +" eventC" + eventCount);

			if (runCount==20) {
				try {
					reporter.collectData();
					reporter.writeData();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				runCount = 0;
				eventCount++;
			}
			
			
		
			Log.i(TAG, "try to write & send");

			if (eventCount==10) {
				
				try {
					reporter.sendData();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				eventCount = 0;
			}
				
	
			
			Log.i(TAG, "topact: " + reporter.getTopActivity() + " active: " + active + "mac size " + macAddresses.size() + " count " + connectCount);
			
			if (connectCount==macAddresses.size()) {
				connectCount=0;
			}
			
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
			if (locked==false) {
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
						
						Log.i(TAG, "size: " + macAddresses.size() + "cc: " + connectCount);
						
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
		
		
		
//		for (int i=0; i<myPacks.size(); i++) {
//			if (reporter.getTopActivity().contains(myPacks.get(i))) {
//				allowedPackInFront = true; 
//			}
//		}
		
		Iterator it = myPacks.iterator();
		while (it.hasNext()) {
		    // Get element
		    String a = it.next().toString();
		    Log.i(TAG, "set of allowed: " + a);
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
		
		// Is unlocked anyway because com.android.phone is on top
//		else if (incomingCall) {
//			Log.i(TAG, "call in process");
//		}
		
		else{
			
			Log.i(TAG, "new Lockscreen");

			
			Intent intent = new Intent(context, LockActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
							Intent.FLAG_ACTIVITY_NO_HISTORY |
							Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
							Intent.FLAG_ACTIVITY_SINGLE_TOP); 
//							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
	
	
	private void checkCalls(){
		
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Log.i(TAG, "Call state: " + tm.getCallState());
		
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
//					lock(LockActivity.LOCK_TEXTBUSTER);
				} else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
					event = "ACDC";
					Log.d(TAG, "ACL disconnected from " + address);
					lastACL = new Date().getTime();
					
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
				
				if (intent.getAction().equals(OUTGOING_CALL_START)) {
					
	            	unlock();
	            	Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"));
	            	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            	startActivity(i);
	        		PackageManager pm = getPackageManager();
	        		allowedCallPackage=i.resolveActivity(pm).getPackageName();
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
	        		allowedNavigationPackage=i.resolveActivity(pm).getPackageName();
	        		myAllowedPacks.addPackage(allowedNavigationPackage);
	        		Log.i(TAG, "adding " + i.resolveActivity(pm).getPackageName());
	        		myPacks=myAllowedPacks.getAllowed();
	        		Log.i(TAG, "here");
				}
					
			}
			
		};
		
		IntentFilter filter = new IntentFilter();
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
	}



}