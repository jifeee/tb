package com.access2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TBReceiver extends BroadcastReceiver{
	
	String TAG="TEX";
	static String STOP_LOCK = "com.access2.textbuster.STOP_LOCK";
	static String START_LOCK = "com.access2.textbuster.START_LOCK";
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "Received in TB Bootreceiver");

    	Log.i(TAG, intent.getAction());

    	Intent service = new Intent(context, TextbusterService.class);
    	context.startService(service);
 
    }


}
