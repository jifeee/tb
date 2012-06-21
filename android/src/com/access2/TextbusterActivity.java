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
	public static final String STOP_LOCK = "com.access2.textbuster.STOP_LOCK";
	public static final String START_LOCK = "com.access2.textbuster.START_LOCK";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
//        Intent service = null;
//        
//        Context ctx = this.getBaseContext();
//        
//        service = new Intent(ctx, TextbusterService.class);
//        
//        ctx.startService(service);
        

        // Intent to start the detecting of new textbuster devices; need to disable TextbusterService while this runs
        
        Intent detectIntent = new Intent(TextbusterActivity.this, DetectActivity.class);      
    	TextbusterActivity.this.startActivityForResult(detectIntent, DETECT);  

 
    }
    
    public void stopLock () {
		Intent i = new Intent();
		i.setAction(TextbusterService.STOP_LOCK);
		getApplicationContext().sendBroadcast(i);
    }
    
    protected void onDestroy() 
    {
		super.onDestroy();
	}
	public void onPause()
	{
		super.onPause();
	  }

	 public void onResume()
	  {
	    super.onResume();
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
	    

}

