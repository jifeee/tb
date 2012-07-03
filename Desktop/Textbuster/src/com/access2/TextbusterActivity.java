package com.access2;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import classes.UserStatus;

public class TextbusterActivity extends Activity {
	private static final String TAG = "TEX";
	static final int DETECT=0;
	static final int TERMS=1;
	static final int SETUP=2;
	static final int LOGIN=3;
	static final int STARTLOCKING=4;
	static final int MENU=5;
	static final int TRIPS=6;
	static final int DETAIL=7;
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
	String MAC; 
	int currentActivity;
	String TextbusterMAC;
	
	

	
	UserStatus myUserStatus; 
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        Context ctx = this.getBaseContext();
        Intent service = new Intent(ctx, TextbusterService.class);
        ctx.startService(service);
        
        
        
//        SmsManager sm = SmsManager.getDefault();
//     String number = "01622896226";
//     sm.sendTextMessage(number, null, "Test SMS Message", null, null);
     
     
    	Intent loginIntent = new Intent(TextbusterActivity.this, LoginActivity.class);      
    	TextbusterActivity.this.startActivityForResult(loginIntent, LOGIN);  

    	currentActivity =0;
    } 
    

    
    protected void onActivityResult(int requestCode, int resultCode,
	            Intent resultIntent) {

    		
    		if (resultCode == RESULT_CANCELED ) {
    			if (requestCode == DETECT) {
//    				finish();
//    			}
//    			
//    			if (requestCode == TERMS) {
//    		    	Intent i = new Intent(TextbusterActivity.this, DetectActivity.class);      
//    		    	TextbusterActivity.this.startActivityForResult(i, DETECT);  
//    			}
//    			
//    			if (requestCode == SETUP) {
//	    	        Intent i = new Intent(TextbusterActivity.this, TermsActivity.class);      
//	    	    	TextbusterActivity.this.startActivityForResult(i, TERMS); 
//    			}
//    			
    			if (requestCode == LOGIN) {
    				finish(); 
    			}
//    			
//    			if (requestCode == STARTLOCKING) {
//	    	        Intent i = new Intent(TextbusterActivity.this, LoginActivity.class);      
//	    	    	TextbusterActivity.this.startActivityForResult(i, LOGIN); 
//        			
//    			}
//    			
//    			if (requestCode == MENU) {
//	    	        Intent i = new Intent(TextbusterActivity.this, LoginActivity.class);      
//	    	    	TextbusterActivity.this.startActivityForResult(i, LOGIN); 
//        			
//    			}
//    			
//    			if (requestCode == TRIPS) {
//	    	        Intent i = new Intent(TextbusterActivity.this, MenuActivity.class);      
//	    	    	TextbusterActivity.this.startActivityForResult(i, MENU); 
//        			
//    			}
//    			
//    			if (requestCode == DETAIL) {
//	    	        Intent i = new Intent(TextbusterActivity.this, TripsActivity.class);      
//	    	    	TextbusterActivity.this.startActivityForResult(i, TRIPS); 
//        			
    			}
    			
    			
    		}
    	
    		
	    	if (requestCode == DETECT) {
	    		if (resultCode == RESULT_OK ) {
	    			
	    			TextbusterMAC = resultIntent.getStringExtra("MAC");
	    			Log.i(TAG, "TB MAC: " + TextbusterMAC);
	    			
	    			Intent termsIntent = new Intent(TextbusterActivity.this, TermsActivity.class);      
	    	    	TextbusterActivity.this.startActivityForResult(termsIntent, TERMS); 
	    	    	currentActivity =1;
	    			
	    		}

	        }
	    	
	    	if (requestCode == TERMS) {
	    		if (resultCode == RESULT_OK ) {
	    	        Intent setupIntent = new Intent(TextbusterActivity.this, SetupPasswordActivity.class);      
	    	    	TextbusterActivity.this.startActivityForResult(setupIntent, SETUP); 
	    	    	currentActivity =2;
	    			
	    		}

	        }
	    	
	    	if (requestCode == SETUP) {
	    		if (resultCode == RESULT_OK ) {
	    	        Intent loginIntent = new Intent(TextbusterActivity.this, LoginActivity.class);      
	    	    	TextbusterActivity.this.startActivityForResult(loginIntent, LOGIN); 
	    	    	currentActivity =3;
	    			
	    		}

	        }
	    	
	    	if (requestCode == LOGIN) {
	    		if (resultCode == RESULT_OK ) {
//	    	        Intent startlockingIntent = new Intent(TextbusterActivity.this, StartLockingActivity.class);      
//	    	    	TextbusterActivity.this.startActivityForResult(startlockingIntent, STARTLOCKING); 
	    	    	currentActivity =4;
	    	    	
	    	        Intent menuIntent = new Intent(TextbusterActivity.this, MenuActivity.class);  
	    	    	TextbusterActivity.this.startActivityForResult(menuIntent, MENU); 
	    			
	    		}

	        }
	    	
	    	if (requestCode == MENU) {
	    		if (resultCode == RESULT_OK ) {
	    	    	
	    	        Intent tripsIntent = new Intent(TextbusterActivity.this, TripsActivity.class);  
	    	    	TextbusterActivity.this.startActivityForResult(tripsIntent, TRIPS); 
	    			
	    		}

	        }
	    	
	    	if (requestCode == TRIPS) {
	    		if (resultCode == RESULT_OK ) {
	    	    	
	    	        Intent detailIntent = new Intent(TextbusterActivity.this, TripDetailsActivity.class);  
	    	    	TextbusterActivity.this.startActivityForResult(detailIntent, DETAIL); 
	    			
	    		}

	        }
	    	
	    	if (requestCode == STARTLOCKING) {
	    		if (resultCode == RESULT_OK ) {
	    	        finish();
	    			
	    		}

	        }
	        
	       
        } 
	    
	    @Override public void onBackPressed() { 
	    	
	    	
	    }
	    
	    protected void onDestroy() 
		{
			Log.i(TAG, "TBA destroyed");	
			super.onDestroy();
		}

		public void onPause()
		{
			Log.i(TAG, "TBA pause");
			super.onPause();
		  }

		public void onResume()
		  {
			 Log.i(TAG, "TBA resume");	
		    super.onResume();
		  }



	    

}

