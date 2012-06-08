package com.access2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RemoveReceiver extends BroadcastReceiver{
	
	String TAG="TEX";
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, intent.toString());
    }
}
