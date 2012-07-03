package com.access2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import classes.Constants;
import classes.UserStatus;

import com.access2.MenuActivity.TripListTask;

public class SettingsActivity extends Activity {

	
		String TAG="TEX";
		Context ctx;

		private ProgressDialog pd;
		TripListTask myTripListTask;
		UserStatus myUserStatus; 
		
		static final int CHANGE = 0;
		
		
		// Enter settings, send them to web api and check the response; 
		
		//TODO: API calls on the backend
		//TODO: Actually send out the call, have to wait for the backend to have it implemented
		
		
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.settings);
			ctx=this;
			
			myUserStatus = Constants.myUserStatus;
			
	        final Button save = (Button) findViewById(R.id.button1);
	        save.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	


	            }
	        });
	        
	        final Button change = (Button) findViewById(R.id.button2);
	        change.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	
	    	        Intent i = new Intent(SettingsActivity.this, ChangePwActivity.class);      
	    	    	SettingsActivity.this.startActivityForResult(i, CHANGE); 

	            }
	        });
	        
	        final EditText email = (EditText) findViewById(R.id.editText1);
	        email.setOnEditorActionListener(new OnEditorActionListener() {        
	            @Override
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	                if(actionId==EditorInfo.IME_ACTION_DONE){
	                    findViewById(R.id.editText2).requestFocus();
	                    
	                    
	                }
	            return false;
	            }
	        });
	        
	        final EditText sms = (EditText) findViewById(R.id.editText2);
	        sms.setOnEditorActionListener(new OnEditorActionListener() {        
	            @Override
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	                if(actionId==EditorInfo.IME_ACTION_DONE){
	                    findViewById(R.id.editText3).requestFocus();
	                }
	            return false;
	            }
	        });
	        
	        final EditText message = (EditText) findViewById(R.id.editText3);
	        message.setOnEditorActionListener(new OnEditorActionListener() {        
	            @Override
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	                if(actionId==EditorInfo.IME_ACTION_DONE){
	                    findViewById(R.id.button1).requestFocus();
	                }
	            return false;
	            }
	        });
	        
	        
	        
	        
		}
		
		
		
}		