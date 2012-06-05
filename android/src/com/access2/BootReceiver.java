package com.access2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver{
	
	String TAG="TEX";
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "received boot, t: textbuster");
//        Intent startServiceIntent = new Intent(context, SemperService.class);
//        context.startService(startServiceIntent);
    	Log.i(TAG, intent.getAction());
        
        Intent service = new Intent(context, TextbusterService.class);
        
        context.startService(service);
        
        if ("com.access2.textbuster.STOP_LOCK".equals(intent.getAction())) {
        	context.stopService(service);
        	
        	
        }
        
    }


}
