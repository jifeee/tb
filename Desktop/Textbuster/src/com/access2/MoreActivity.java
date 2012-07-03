package com.access2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import classes.Constants;
import classes.UserStatus;

import com.access2.MenuActivity.TripListTask;

public class MoreActivity extends Activity {

	
		String TAG="TEX";
		Context ctx;
		static final int PRIVACY =0;
		
		private ProgressDialog pd;
		TripListTask myTripListTask;
		UserStatus myUserStatus; 
		
		
		
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.more);
			ctx=this;
			
			myUserStatus = Constants.myUserStatus;
			
			SharedPreferences sp;
	    	sp = getSharedPreferences("textbuster", 0);
	    	
	    	int i;
	    	for (i=0; i>-1; i++) {
	    		if (sp.getString(String.valueOf(i), "").equals("")) {
	    			break;
	    		}
	    		else {
	    			Log.i(TAG, "tb from settings: " + sp.getString(String.valueOf(i), ""));
	    		}
	    	}
	    	
	    	Log.i(TAG, "i: " + i);
	    	
	    	final TextView tv = (TextView) findViewById(R.id.textView2);
	    	tv.setText("This phone is currently paired with " + i + " TextBuster device(s). ");
			
	        final Button call = (Button) findViewById(R.id.button1);
	        call.setText("Call us");
	        call.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	
	            	Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:12345"));
	            	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            	startActivity(i);

	            }
	        });
	        
	        final Button settings = (Button) findViewById(R.id.button2);
	        settings.setText("Email us");
	        settings.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {

		            final Intent i = new Intent(android.content.Intent.ACTION_SEND);
		            i.setType("plain/text");
		            String[] recipients = new String[]{"info@mobilezapp.de"};
		            i.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
		            i.putExtra(android.content.Intent.EXTRA_SUBJECT, "TextBuster Email Support Request " + myUserStatus.getEmail());
		            
		            
		            ctx.startActivity(Intent.createChooser(i, "Send mail."));
	            	
	            	
	            }
	        });
			
	        
	        final Button privacy = (Button) findViewById(R.id.button3);
	        privacy.setText("Privacy Policy");
	        privacy.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {

	    	        Intent i = new Intent(MoreActivity.this, PrivacyActivity.class);      
	    	    	MoreActivity.this.startActivityForResult(i, PRIVACY); 
	            	
	            }
	        });
			
		}

}
