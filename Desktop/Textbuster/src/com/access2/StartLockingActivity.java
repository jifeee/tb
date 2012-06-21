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

public class StartLockingActivity extends Activity {
	private static final String TAG = "TEX"; 

	Button startButton;
	TextView tv; 

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.startlocking);
    	
    	startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(new OnClickListener() {
        	
            public void onClick(View v) {
            	
            	startLock();
            	
        	    Intent resultIntent = new Intent();
        	    setResult(RESULT_OK, resultIntent);
            	finish();
            	finish();
            	
        		}
            });
        
        tv = (TextView)findViewById(R.id.textView1);
        
        
    	
    }
    
    public void startLock () { 
		Intent i = new Intent();
		i.setAction(TextbusterService.SET_ACTIVE);
		getApplicationContext().sendBroadcast(i);
		tv.setText("Service started. ");
		startButton.setVisibility(8);
    }
    
    @Override public void onBackPressed() { 
	    Intent resultIntent = new Intent();
	    setResult(RESULT_CANCELED, resultIntent);
    	finish();
    	
    }
    
}