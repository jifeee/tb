package com.access2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import classes.Logger;

public class RemoveReceiver extends BroadcastReceiver{
	
	String TAG="TEX";
	
	
	// Currently not used. An equivalent receiver is on TextbusterService
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "RemoveReceiver: tostring: " + intent.toString());
    	Log.i(TAG, "RemoveReceiver: getaction: " + intent.getAction());
    	
    	TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
    	
//    	if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED") && intent.toString().contains("eu.toasternet")) {
//    		Logger log = new Logger(imei);
//    		log.set("state", (byte)0, (byte)0, (byte)0, 
//    				(byte)0, (byte)4);
//    		log.write();
//    		log.send();
//    	}
    	
    }
}

