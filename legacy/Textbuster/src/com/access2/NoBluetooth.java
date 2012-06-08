package com.access2;

import android.util.Log;
import android.os.Bundle;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;

import android.view.View;
import android.view.Window;
import android.view.KeyEvent;

import android.bluetooth.BluetoothAdapter;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class NoBluetooth extends Activity{
		
	public void onCreate(Bundle b) {
        super.onCreate(b);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.nobluetooth);
		
		registerReceiver(mReceiver, new IntentFilter("com.access2.NoBluetooth.finish"));
	}
	
	public void onStart(){
		super.onStart();
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		 public void onReceive(Context context, Intent intent) {
			Log.d("Service", "Finishing NoBluetooth");
			finish();
		}
	};
	
	public void enableBluetooth(View view) {
		Log.d("Service", "Request bluetooth enable");
		BluetoothAdapter.getDefaultAdapter().enable();
		
		//Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		//startActivityForResult(enableBtIntent, 3);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  if (resultCode != Activity.RESULT_OK && requestCode == 3) {
		Log.d("Service", "Bluetooth enabling went wrong");
	  }
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
