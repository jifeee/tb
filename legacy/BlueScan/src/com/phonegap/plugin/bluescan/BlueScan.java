package com.phonegap.plugin.bluescan;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

public class BlueScan extends Plugin {
	public static final String TAG = "BlueScan";
	
	private Context context;
	private String aclCallbackId = null;
	
	private volatile BluetoothAdapter bluetoothAdapter;
	
	final Handler nextAddressHandler = new Handler();
	final Handler closeRfcommHandler = new Handler();
	final Handler checkLockHandler = new Handler();
	
	private BluetoothSocket rfcommSocket = null;
	
	private ArrayList<String> macAddresses = new ArrayList();
	
	private int lockCheckInterval = 1;
	private int lockInterval = 40;
	private int discoveryInterval = 120;
	private int discoveryDelay = 60;
	private int closeRfcommInterval = 15;
	
	private boolean locked = false;
	
	private long lastACL = 0;
	
	public BlueScan(){
		Log.d(TAG, "Plugin initialized");
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
	}
	
	@Override
	public PluginResult execute(String action, JSONArray arg1, String callbackId) {
		Log.d(TAG, "Plugin called with action \"" + action + "\"");
		PluginResult result = new PluginResult(Status.OK);
		
		context = this.ctx;
		
		if(action.equals("registerACL")){
			
			aclCallbackId = callbackId;
			
			IntentFilter filter = new IntentFilter();
			filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
			filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
			
			context.registerReceiver(aclReceiver, filter);
			
			filter = new IntentFilter();
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			
			context.registerReceiver(btReceiver, filter);
			
			nextAddressHandler.postDelayed(contactNextAddress, 30000);
			
			checkLockHandler.postDelayed(checkLock, lockCheckInterval * 1000);
			
			result = new PluginResult(Status.OK, "{\"action\":\"INIT\"}");
			result.setKeepCallback(true);
			
		}else if(action.equals("setVariables")){
			
			try{
				macAddresses.add(arg1.getString(0));
				discoveryInterval = arg1.getInt(1);
				discoveryDelay = arg1.getInt(2);
				
				Log.d(TAG, "New settings: " + macAddresses.get(0) + " Int1: " + new Integer(discoveryInterval).toString() + " Int2: " + new Integer(discoveryDelay).toString());
			}catch(JSONException e){
				Log.d(TAG, "JSON Exception");
			}
			
		}else if(action.equals("startDiscovery")){
			
			bluetoothAdapter.startDiscovery();
		
		}else if(action.equals("createRfcomm")){
		
			try{
				final String mac = arg1.getString(0);
				
				contactMACAddress rfcomm = new contactMACAddress(mac);
				rfcomm.run();
				
			}catch(JSONException e){
				Log.d(TAG, "JSON Exception");
			}
			
		}
	
		return result;
	}
    
    /* Try to make contact to the next MAC address on the list.
	 */
	private class contactMACAddress implements Runnable{
		private String mac;
		
		public contactMACAddress(String mac){
			this.mac = mac;
		}
		
		public void run() {
			
			long sinceLastACL = new Date().getTime() - lastACL;
			
			if(sinceLastACL < discoveryDelay * 1000){
					Log.d(TAG, "Skipping connection to " + mac + ". Only " + new Long(sinceLastACL).toString() + "ms since last ACL.");
					jslog("{\"action\":\"RFSK\",\"address\":\"" + mac + "\"}");
					return;
			}
			
			if(rfcommSocket == null){
				try{
					Log.d(TAG, "Trying to connect to " + mac);
					jslog("{\"action\":\"RFTR\",\"address\":\"" + mac + "\"}");
					
					//closeRfcommHandler.postDelayed(closeRfcomm, closeRfcommInterval * 1000);	
					
					BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
					final BluetoothSocket bluetoothSocket = rfcommSocket = bluetoothDevice.createRfcommSocketToServiceRecord(new java.util.UUID(0x0000110100001000L, 0x800000805F9B34FBL));
					bluetoothSocket.connect();
					
					jslog("{\"action\":\"RFCN\"}");
				}catch(IOException e){
					Log.d(TAG, "IO Exception: " + e.getMessage());
					jslog("{\"action\":\"RFAB\"}");
					
					rfcommSocket = null;
				}finally{
				}
			}else{
				Log.d(TAG, "Rfcomm in progress");
				
				jslog("{\"action\":\"RFIP\"}");
			}
			
		}
	}
	
	/* Try to make contat with the next MAC address on the list.
	 */
	private Runnable contactNextAddress = new Runnable() {
		public void run() {
			
			// Skip if list of mac addresses is empty.
			if(macAddresses.isEmpty()){
				jslog("{\"action\":\"LSEM\"}");
			}else{
				String mac = macAddresses.get(0);
				jslog("{\"action\":\"LSTR\",\"address\":\"" + mac + "\"}");
				new contactMACAddress(mac).run();
			}
			
			long sinceLastACL = new Date().getTime() - lastACL;
			nextAddressHandler.postDelayed(this,  discoveryInterval * 1000 );	
		}
	};
	
	/* Cancel rfcommCreate should one be in progress.
	 */
	private Runnable closeRfcomm = new Runnable() {
		public void run() {
			if(rfcommSocket != null){
				try{
					rfcommSocket.close();
				}catch(IOException e){
				}
				jslog("{\"action\":\"RFCL\"}");
			}else{
				jslog("{\"action\":\"RFNO\"}");
			}
		}
	};
	
	/* Check lock.
	 */
	private Runnable checkLock = new Runnable() {
		public void run() {
			long sinceLastACL = new Date().getTime() - lastACL;
			
			if(sinceLastACL <= lockInterval * 1000 && locked == false){
				locked = true;
				jslog("{\"action\":\"LOCK\"}");
			}else if(sinceLastACL > lockInterval * 1000 && locked == true){
					locked = false;
					jslog("{\"action\":\"UNLO\"}");
			}
			
			checkLockHandler.postDelayed(checkLock, lockCheckInterval * 1000);
		}
	};

    /* Reveives events related an external device making contact to the Android using a low-level ACL connection.
	 */
	private final BroadcastReceiver aclReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String address = device.toString();
			
			String event = "ACUN";
			
			if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
				event = "ACCN";
				Log.d(TAG, "ACL connected from " + address);
				
				//if(rfcommSocket != null && rfcommSocket.getRemoteDevice().getAddress().equals(address)){
					//try{
						//Log.d(TAG, "Closing Rfcomm");
						//rfcommSocket.close();
					//}catch(IOException e){
						//Log.d(TAG, "Closing Rfcomm failed");
					//}
				//}
				
				lastACL = new Date().getTime();
			} else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
				event = "ACDC";
				Log.d(TAG, "ACL disconnected from " + address);
				
				if(rfcommSocket != null && rfcommSocket.getRemoteDevice().getAddress().equals(address)){
					rfcommSocket = null;
				}
				
				lastACL = new Date().getTime();
			} else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)){
				event = "ACDR";
				Log.d(TAG, "ACL disconnect requested from " + address);
			}
			
			jslog("{\"action\":\"" + event + "\",\"address\":\"" + address + "\"}");
		}
	};

	/* Reveives events related to the Android's Bluetooth device.
	 */
	private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
			String event = "UN";
			
			if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
				event = "BTDS";
				Log.d(TAG, "Discovery started");
			} else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
				event = "BTDF";
				Log.d(TAG, "Discovery finished");
			}
			
			jslog("{\"action\":\"" + event + "\"}");
		}
	};

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
    	Log.i(TAG, "Plugin destroyed");
    	context.unregisterReceiver(aclReceiver);
    	super.onDestroy();
	}
	
	private void jslog(String message){
		if(aclCallbackId != null){
			PluginResult result = new PluginResult(Status.OK, message);
			result.setKeepCallback(true);
			success(result, aclCallbackId);
		}else{
			Log.d(TAG, "Listener not yet registered");
		}
	}
}
