package com.guardianapp;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class GuardianService extends Service {

	String TAG = "SEM";
	boolean alarm;
	String imei="";
	Logger log;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Get phone's IMEI
		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		imei = tm.getDeviceId();
		
		log = new Logger(imei);
		Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
		

		
		
		// Receiver that detect uninstall of TextBuster app
		BroadcastReceiver uninstallReceiver = new BroadcastReceiver() {
			
		    public void onReceive(Context context, Intent intent) {
		    	Log.i(TAG, "onreceive semper uninstallReceiver " + intent.getAction());
		        
		        if(intent.toString().contains("com.access2")){
		        	
		        	call911 ();

				} 

			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addDataScheme("package");
		this.registerReceiver(uninstallReceiver, filter); 
		
		// Start the Service
		new Timer().scheduleAtFixedRate(mainloop, 0, 60000);
		


	}
	
	private TimerTask mainloop = new TimerTask(){ 
		public void run(){

			checkService();
			
		}
	};	
	
	
	
	// Ccheck if textbuster service is running
	private void checkService() {
		
		Logger log = new Logger(imei);
		
		boolean serviceRunning = false; 
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//	    	Log.i(TAG, "" + service.service.getClassName());
	        if ("com.access2.TextbusterService".equals(service.service.getClassName())) {
	           Log.i(TAG, "Found running service " + service.service.getClassName());
	           serviceRunning = true; 
	        }

	    }
	    
	    // If service isnt running, write 'alert' event
	    if (!serviceRunning) {
	    	alarm = true; 
	    	
			log.set("state", (byte)0, (byte)0, (byte)0, 
					(byte)0, (byte)1, " ");						
			log.write();
	    }
	    
	    // If we have an alert, send out event(s)
	    // do nothing is everything is fine
	    if (alarm) {
	    	if (log.send()==true) {
	    		alarm = false; 
	    	}
	    }
	  
	}
	
	// Called when TextBuster Uninstall is detected, write 'alert' event and send out sms  
	private void call911 () {
		
		Logger log = new Logger(imei);
		alarm = true; 
		log.set("state", (byte)0, (byte)0, (byte)0, 
				(byte)0, (byte)2, " ");							
		log.write();
		
		 SmsManager sm = SmsManager.getDefault();
	     String number = "somephonenumber";   // has to be set from the backend
	     sm.sendTextMessage(number, null, "TextBuster Main App has been uninstalled on phone " + imei, null, null);
		
		
	}



	@Override
	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

	}

	@Override
	public void onStart(Intent intent, int startId) {

		super.onStart(intent, startId);

		Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

	}

	@Override
	public IBinder onBind(Intent intent) {
	
		return null;
	
	}
	

	

}