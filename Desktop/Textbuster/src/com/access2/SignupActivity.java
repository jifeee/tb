package com.access2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import classes.Constants;
import classes.Converter;
import classes.UserStatus;

public class SignupActivity extends Activity {
	
	String TAG="TEX";
	Context ctx;
	
	String mail;
	String passw;
	String passwRep;
	String imei; 
	
	SignupTask mySignupTask;
	private ProgressDialog pd;
	UserStatus myUserStatus = new UserStatus(); 
	
	static final int MENU = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);
		ctx=this;
		
		
		//Sign up with the backend
		
		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		imei = tm.getDeviceId();
		
		
        final EditText email = (EditText) findViewById(R.id.editText1);
        email.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                 
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                   
                	findViewById(R.id.editText2).requestFocus();
                  return true;
                }
                return false;
            }
        });
		
        final EditText pass = (EditText) findViewById(R.id.editText2);
        pass.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                 
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                   
                	findViewById(R.id.editText3).requestFocus();
                  return true;
                }
                return false;
            }
        });
        
        final EditText passRep = (EditText) findViewById(R.id.editText3);
        passRep.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                 
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                   
                	findViewById(R.id.button1).requestFocus();
                  return true;
                }
                return false;
            }
        });
        
        final Button signup = (Button) findViewById(R.id.button1);
        signup.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	            	
	               	mail = email.getText().toString();
	               	passw = pass.getText().toString();
	               	passwRep = passRep.getText().toString();
	            	
	            	
	            	pd = ProgressDialog.show(ctx, "Signing up ...", "Please wait.           ", true,
	                        false); 
	            	mySignupTask = new SignupTask();
	            	mySignupTask.execute(myUserStatus);
            	
            }
        });
        
        final Button cancel = (Button) findViewById(R.id.button2);
        cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();

            }
        });

		
		
		
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

	    
		public class SignupTask extends AsyncTask<UserStatus, String, UserStatus> {
			
			String TAG="TEX";
			UserStatus myUserStatus = new UserStatus();
			
			
		    protected UserStatus doInBackground(UserStatus ... myUserStatus) {
		    	
		    	myUserStatus[0].setLastError("0");
		    	
		        HttpClient client = new DefaultHttpClient();
		        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
		        HttpResponse response;
		    	
		    	
		        try{
		        	

		        	JSONObject json = new JSONObject();
		            HttpPost post = new HttpPost("http://textbuster.mobilezapp.de/api/sign_up");
		            json.put("device", imei);
		            json.put("email", mail);
		            json.put("password", passw);
		            json.put("password_confirmation", passwRep);
		            StringEntity se = new StringEntity( json.toString());  
		            Log.i(TAG, "json to send: " + json.toString() + " to url http://textbuster.mobilezapp.de/api/sign_up" );
		            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		            post.setEntity(se);
		            response = client.execute(post);
		            /*Checking response */
		            if(response!=null){
		                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		                StringBuilder builder = new StringBuilder();
		                for (String line = null; (line = reader.readLine()) != null;) {
		                    builder.append(line).append("\n");
		                }
		                
		                Log.i(TAG, builder.toString());
		                
		                JSONObject responsejson = new JSONObject(builder.toString());
		                
		                if (responsejson.isNull("message")) {
			                
			                String token = responsejson.get("token").toString();
			                String userID = responsejson.get("user_id").toString();
			                
			                myUserStatus[0].setToken(token);
			                myUserStatus[0].setUserID(userID);
			                myUserStatus[0].logStatus();			                
		                }
		                
		                else {
		                	String error = responsejson.get("message").toString();
		                	myUserStatus[0].setLastError(error);
		                }

		                
		            }    
		
	        }
		        
		    catch (ConnectTimeoutException e) {
		    	pd.dismiss();
		    }
		        
	        catch(Exception e){
	            e.printStackTrace(); 
	            pd.dismiss();
	 
	        }
	
		    	return myUserStatus[0];
		    }
		
		    protected void onProgressUpdate(String... progress) {

		     }
		
		
		    protected void onPostExecute(UserStatus result) {
		    	Log.i(TAG, "post exec");
		        pd.dismiss();
		        afterSignup();
		    }
		}
		
		public void afterSignup () {
			
			myUserStatus.logStatus();
			
			if (!myUserStatus.getLastError().equals("0")) {
				final AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
		      	alertDialog.setTitle("Login error!");
		      	alertDialog.setMessage(myUserStatus.getLastError());
		      	
		      	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		    	      public void onClick(DialogInterface dialog, int which) {
		    	    	  alertDialog.dismiss();
		    	    	  
		    	    } }); 
		    	 
		    	alertDialog.show();
			}
			
			else {
				
				Constants.myUserStatus = myUserStatus; 
				final AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
		      	alertDialog.setTitle("Success.");
		      	alertDialog.setMessage("You signed up with TextBuster");
		      	
		      	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		    	      public void onClick(DialogInterface dialog, int which) {
		    	    	  alertDialog.dismiss();
		    	    	  finish();
		    	    	  
		    	    } }); 
		    	 
		    	alertDialog.show();
				

			}
			
		}

}
