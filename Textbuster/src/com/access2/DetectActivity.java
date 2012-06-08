package com.access2;

import java.util.Iterator;

import com.access2.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DetectActivity extends Activity {
	private static final String TAG = "TEX"; 
	IntentFilter filter = new IntentFilter();
	BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
	BluetoothDevice textbuster;
	ProgressBar pb;
	ToggleButton toggle;
	Button pairButton;
	TextView tv1;
	TextView tv2; 
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detect);
        
        // dont lock the phone right away
        stopLock();
        
        toggle = (ToggleButton)findViewById(R.id.bluetoothButton);
        pairButton = (Button)findViewById(R.id.pairButton);
        pb = (ProgressBar) findViewById(R.id.progressBar1);
        tv1 = (TextView) findViewById(R.id.textView1);  
        tv2 = (TextView) findViewById(R.id.textView3);
        
        pairButton.setVisibility(8);
        
        if (bt.getState()==12) {
        	toggle.setChecked(true);
        	startDiscovery();
        }
         
        else {
        	toggle.setChecked(false);
        	tv2.setText("Bluetooth is not enabled.");
        	noDiscovery();
        }
        
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	
                if (isChecked == true) {
                	bt.enable();
//                	startDiscovery();
            
                }
            	
                else {
                	bt.disable();
//                	noDiscovery();
                }
            	
            }
        });
        
        pairButton.setOnClickListener(new OnClickListener() {
        	
            public void onClick(View v) {   

        	    Intent resultIntent = new Intent();
        		setResult(RESULT_OK, resultIntent);
            	finish();
        		}
            });

        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver, filter);
        
    }
    
    public void startDiscovery () {
		bt.startDiscovery();
		tv2.setText("Scanning for devices ... ");
		pb.setVisibility(0);
		pairButton.setVisibility(8);
	}
	
	public void noDiscovery () {
		tv1.setText("Turn Bluetooth on to connect your Textbuster device. ");
		tv2.setText("Bluetooth is disabled.");
		pb.setVisibility(8);
		pairButton.setVisibility(8);
	}
	
	public void foundDevice () {
		pb.setVisibility(8);
		tv1.setText("Setup the connection with your Textbuster device. ");
		tv2.setText("Found device " + textbuster.getName() + " (" + textbuster.getAddress() + ")\n" 
		+ "Press Button to pair. " );
		pairButton.setVisibility(0);
	}
	
	protected void onDestroy() { 
		Log.i(TAG, "DET destroy");
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	public void onPause()
	  {
		Log.i(TAG, "DET pause");
//		unregisterReceiver(receiver);
	    super.onPause();
	  }

	 public void onResume()
	  {
		 Log.i(TAG, "DET resume");
	    super.registerReceiver(receiver, filter);
	    super.onResume();
	  } 

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		 
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
	
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Log.i(TAG, "Found");
				Log.i(TAG, "Intent: " + intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE).toString());
				
				BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				Log.i(TAG, "NAME: " + d.getName());
				
				if (d.getName() != null) { 
					if (d.getName().equals("TEXTBUSTER")) {
						textbuster = d;
						foundDevice();
					}
				}
				
				
			} 
			 
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Log.i(TAG, "Disc started");
			}
	
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				if (textbuster==null) {
					Log.i(TAG, "go again");
					startDiscovery();
				}
			}
			
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				Log.i(TAG, "State changed");
				
				int state = bt.getState();
				
				switch (state) {
				case 11: {												// turning on
					tv2.setText("Bluetooth is being turned on");
					pb.setVisibility(0);
					break;
				}
				
				case 13: {												// turning off
					tv2.setText("Bluetooth is being turned off");
					pb.setVisibility(0);
					pairButton.setVisibility(8);
					break; 
				}
				
				case 10: {												// off
					noDiscovery();
					break; 
				} 
				
				case 12: {												// on
					startDiscovery();
					break; 
				}
				
				
				
				}
				
				
				
			}
				 
		}
		
	};
	
    @Override public void onBackPressed() { 
    	stopLock();
    	startLock();
    	
    }
    
    public void startLock () { 
		Intent i = new Intent();
		i.setAction(TextbusterService.SET_ACTIVE);
		getApplicationContext().sendBroadcast(i);
    }
    
    public void stopLock () {
		Intent i = new Intent();
		i.setAction(TextbusterService.SET_INACTIVE);
		getApplicationContext().sendBroadcast(i);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      Log.i(TAG, "OSIS detact");
      super.onSaveInstanceState(savedInstanceState);

    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	Log.i(TAG, "ORIS detact");
      
    }


}
        

        
//        
    
