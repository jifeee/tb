package com.access2;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LockActivity extends Activity {

	private static final String TAG = "TEX";
	
	public static final String BROADCAST_UNLOCK = "com.access2.textbuster.unlock";
	public static final String MOVED_AWAY_FROM_CALL = "com.access2.textbuster.MOVED_AWAY_FROM_CALL";
	public static final String MOVED_AWAY_FROM_NAVI = "com.access2.textbuster.MOVED_AWAY_FROM_NAVI";
	
	public static final int LOCK_TEXTBUSTER = 0;
	public static final int LOCK_BLUETOOTH = 1;
	public static final int CALL = 2;
	public static final int NAVI = 3;
	
	Intent i1; 
	Intent i2; 
	
	Button callButton;
	Button naviButton;
	Button appButton;
	ImageView tbLogo;
	
	//Locking the Phone
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		Log.i(TAG, "created LockActivity");
		
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		int type = this.getIntent().getIntExtra("type", 0);
		
		this.setContentView((type == LOCK_BLUETOOTH) ? R.layout.lockbt : R.layout.locktb);
		
		//Listen for UNLOCK from the service
		IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_UNLOCK);

		
		registerReceiver(receiver, filter);
		
		TextView tv1 = (TextView) findViewById(R.id.textView1);
		tv1.setText("TextBuster\u2122");
		
		//Check if the Phone is locked because theres a Textbuster near or because Bluetooth has been turned off
		if (type == LOCK_TEXTBUSTER) {
		
		    	callButton = (Button)findViewById(R.id.button1);
		        callButton.setOnClickListener(new OnClickListener() {
		        	
		            public void onClick(View v) {
		            	
		            	//Let user make a phonecall, Broadcast will be received in TextbusterService
		            	Intent i = new Intent();
		        		i.setAction(TextbusterService.OUTGOING_CALL_START);
		                getApplicationContext().sendBroadcast(i);
		            	
		            	
		            	
		        		}
		            });
		        
		    	naviButton = (Button)findViewById(R.id.button2);
		        naviButton.setOnClickListener(new OnClickListener() {
		        	
		            public void onClick(View v) {
		            	
		            	//Let user use navigation, Broadcast will be received in TextbusterService
		            	Intent i = new Intent();
		        		i.setAction(TextbusterService.NAVIGATION_START);
		                getApplicationContext().sendBroadcast(i);
		                
		        		}
		            });
 
			}
		
		else {
	        
	        
	    	tbLogo = (ImageView)findViewById(R.id.bluetoothlogo);
	    	tbLogo.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	enableBluetooth(v);
	            }
	        });
		}
			
	}	
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	public void enableBluetooth(View view) {
		Log.d(TAG, "Enabling Bluetooth");
		BluetoothAdapter.getDefaultAdapter().enable();
	}
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		 
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_UNLOCK)) {
				Log.d(TAG, "Unlocked");
				finish();
			}	

		}
		
	};

	
}
