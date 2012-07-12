package com.guardianapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver{
	
	String TAG="SEM";
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "received boot, GuardianApp");
        Intent i = new Intent(context, GuardianService.class);
        context.startService(i);
    }


}
