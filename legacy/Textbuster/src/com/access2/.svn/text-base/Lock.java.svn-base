package com.access2;

import android.util.Log;
import android.os.Bundle;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;

import android.view.Window;
import android.view.KeyEvent;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class Lock extends Activity{
	String packageName;
	
	public void onCreate(Bundle b) {
        super.onCreate(b);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.lock);
		
		registerReceiver(mReceiver, new IntentFilter("com.access2.Lock.finish"));
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		 public void onReceive(Context context, Intent intent) {
			Log.d("Service", "Finishing Lock");
			finish();
		}
	};
	
	public void enableBluetooth(View view) {
		Log.d("Service", "Request bluetooth enable");
		BluetoothAdapter.getDefaultAdapter().enable();
		
		//Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		//startActivityForResult(enableBtIntent, 3);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
	}
	
	@Override
	public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
	}
}
