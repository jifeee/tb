package com.access2;

import com.access2.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

public class LockActivity extends Activity {

	private static final String TAG = "TEX";
	
	public static final String BROADCAST_UNLOCK = "com.access2.textbuster.unlock";
	
	public static final int LOCK_TEXTBUSTER = 0;
	public static final int LOCK_BLUETOOTH = 1;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i(TAG, "created LockActivity");
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		int type = this.getIntent().getIntExtra("type", 0);
		
		this.setContentView((type == LOCK_BLUETOOTH) ? R.layout.lockbt : R.layout.locktb);
		
		// Make sure that only UNLOCK broadcasts are send to the receiver.
		IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_UNLOCK);
		
		registerReceiver(receiver, filter);
	}
	
	protected void onDestroy() {
		Log.i(TAG, "destroy");
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	public void enableBluetooth(View view) {
		Log.d(TAG, "Enabling Bluetooth");
		BluetoothAdapter.getDefaultAdapter().enable();
	}
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		 
		public void onReceive(Context context, Intent intent) {
		
			Log.d(TAG, "Unlocked");
			
			// Terminate the lock activity to allow normal usage of the phone again.
			finish();
				
		}
		
	};
	
	
	
	
	
}
