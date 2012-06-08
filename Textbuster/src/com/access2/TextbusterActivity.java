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

public class TextbusterActivity extends Activity {
	private static final String TAG = "TEX";
	static final int DETECT=0;
	static final int TERMS=1;
	static final int SETUP=2;
	IntentFilter filter = new IntentFilter();
	BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
	BluetoothDevice textbuster;
	ProgressBar pb;
	ToggleButton toggle;
	Button pairButton;
	TextView tv1;
	TextView tv2;
	public static final String SET_ACTIVE = "com.access2.textbuster.SET_ACTIVE";
	public static final String SET_INACTIVE = "com.access2.textbuster.SET_INACTIVE";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
    	Intent service = new Intent(getApplicationContext(), TextbusterService.class);
    	getApplicationContext().startService(service);

        // Intent to start the detecting of new textbuster devices; need to disable TextbusterService while this runs
    	Intent detectIntent = new Intent(TextbusterActivity.this, DetectActivity.class);      
    	TextbusterActivity.this.startActivityForResult(detectIntent, DETECT);  

    	
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
    
    protected void onActivityResult(int requestCode, int resultCode,
	            Intent resultIntent) {

	    	if (requestCode == DETECT) {
	    		if (resultCode == RESULT_OK ) {
	    	        Intent termsIntent = new Intent(TextbusterActivity.this, TermsActivity.class);      
	    	    	TextbusterActivity.this.startActivityForResult(termsIntent, TERMS); 
	    			
	    		}

	        }
	    	
	    	if (requestCode == TERMS) {
	    		if (resultCode == RESULT_OK ) {
	    	        Intent setupIntent = new Intent(TextbusterActivity.this, SetupPasswordActivity.class);      
	    	    	TextbusterActivity.this.startActivityForResult(setupIntent, SETUP); 
	    			
	    		}

	        }
	    	
	    	if (requestCode == SETUP) {
	    		if (resultCode == RESULT_OK ) {
	    	        Intent loginIntent = new Intent(TextbusterActivity.this, LoginActivity.class);      
	    	    	TextbusterActivity.this.startActivityForResult(loginIntent, SETUP); 
	    			
	    		}

	        }
	        
	       
        } 
	    
	    @Override public void onBackPressed() { 
	    	
	    	
	    }
	    
//	    protected void onDestroy() 
//		{
//			Log.i(TAG, "TBA destroyed");	
//			super.onDestroy();
//		}
//
//		public void onPause()
//		{
//			Log.i(TAG, "TBA pause");
//			super.onPause();
//		  }
//
//		public void onResume()
//		  {
//			 Log.i(TAG, "TBA resume");	
//		    super.onResume();
//		  }
//
//		@Override
//	    public void onSaveInstanceState(Bundle savedInstanceState) {
//	      Log.i(TAG, "OSIS tbact");
//	    }
//	    
//	    @Override
//	    public void onRestoreInstanceState(Bundle savedInstanceState) {
//	    	Log.i(TAG, "ORIS tbact");
//	      
//	    }


	    

}

