package com.access2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import classes.Reporter;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class TextbusterService extends Service{
	private static final String TAG = "TEX";
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
	
	private ArrayList<String> macAddresses = new ArrayList();
	
	private int lockCheckInterval = 1;
	private int lockInterval = 40;
	private int discoveryInterval = 120;
	private int discoveryDelay = 60;
	private int closeRfcommInterval = 15;

	private boolean locked = false; 
	private boolean lockedByBT = false;
	private boolean lockedByTB = false;
	
	private long lastACL = 0;
	
	private boolean active = false; 
	
	PowerManager pm;
	
	public static final String STOP_LOCK = "com.access2.textbuster.stoplock";
	public static final String START_LOCK = "com.access2.textbuster.startlock";
	
	
	private Reporter reporter = null;
	
	public void onCreate(){
		
		Log.i(TAG, "TB Service started");
		
		context=this; 
		
		reporter = new Reporter(this);
		
		setupReceivers();
		
		new Timer().scheduleAtFixedRate(mainloop, 0, lockCheckInterval*1000);
		
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(STOP_LOCK);
		filter.addAction(START_LOCK);
		
//		reporter.checkForLocation();
//		reporter.getLocation();

		
	}
	
	private TimerTask mainloop = new TimerTask(){ 
		public void run(){
			
			// No need to do all this if the screen is off anyways
			
//			boolean isScreenOn = pm.isScreenOn();
			boolean isScreenOn = true; 

			
			if (isScreenOn==true && active == true) {
				
				// check if the phone should be locked because Bluetooth is turned off
				checkBTlock();
				
				// check if the phone should be locked because theres a textbuster
				checkTBlock();
				
				// try to connect to known textbuster MAC Ids
				connect();

			}

			
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
						
						// Hardcoded MAC ID here, should be a couple of them coming from the backend
						String mac = "00:12:A1:C8:00:06";
						Log.d(TAG, "Trying to connect to " + mac);
						
						bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
						final BluetoothSocket bluetoothSocket = rfcommSocket = bluetoothDevice.
						
						createRfcommSocketToServiceRecord(new java.util.UUID(0x0000110100001000L, 0x800000805F9B34FBL));
						bluetoothSocket.connect();
						
					}catch(IOException e){
	//					Log.d(TAG, "IO Exception: " + e.getMessage());
						
						rfcommSocket = null;
					}finally{
					}
	//			}
			}
	
		}

	private void lock(int type){
		
//		Log.i(TAG, "lock, type: " + type);
		
		if(reporter.getTopActivity().equals(LockActivity.class.getName())) {

//			Log.i(TAG, "Lock already engaged");
		}else{
			
//			Log.i(TAG, "new Lockscreen");
			
			Intent intent = new Intent(getApplicationContext(), LockActivity.class);
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
//					Log.d(TAG, "ACL disconnect requested from " + address);
				}

			}
		};

		/* Reveives events related to the Android's Bluetooth device.
		 */
		btReceiver = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		    	Log.i(TAG, "onreceive btReceiver");
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
			
				String action = intent.getAction();
				
				if (TextbusterActivity.STOP_LOCK.equals(action)) {
					active = false; 
					Log.i(TAG, "received STOP_LOCK");
				}
				
					
			}
			
		};
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		IntentFilter filter = new IntentFilter();
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
	}


}