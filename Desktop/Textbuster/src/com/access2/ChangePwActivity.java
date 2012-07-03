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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import com.access2.LoginActivity.LoginTask;
import com.access2.MenuActivity.TripListTask;

public class ChangePwActivity extends Activity {

	
		String TAG="TEX";
		Context ctx;

		private ProgressDialog pd;
		TripListTask myTripListTask;
		UserStatus myUserStatus; 
		EditText oldPw = null;
		EditText newPw = null;
		EditText newPwRep = null;
		LoginTask myLoginTask;
		String oldPass;
		String newPass;
		String newPassRep;
		
		
		
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.changepw);
			ctx=this;
			
			myUserStatus = Constants.myUserStatus;
			

	        
	        final EditText oldPw = (EditText) findViewById(R.id.editText1);
	        oldPw.setOnEditorActionListener(new OnEditorActionListener() {        
	            @Override
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	                if(actionId==EditorInfo.IME_ACTION_DONE){
	                    findViewById(R.id.editText2).requestFocus();
	                    
	                    
	                }
	            return false;
	            }
	        });
	        
	        newPw = (EditText) findViewById(R.id.editText2);
	        newPw.setOnEditorActionListener(new OnEditorActionListener() {        
	            @Override
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	                if(actionId==EditorInfo.IME_ACTION_DONE){
	                    findViewById(R.id.editText3).requestFocus();
	                }
	            return false;
	            }
	        });
	        
	        final EditText newPwRep = (EditText) findViewById(R.id.editText3);
	        newPwRep.setOnEditorActionListener(new OnEditorActionListener() {        
	            @Override
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	                if(actionId==EditorInfo.IME_ACTION_DONE){
	                    findViewById(R.id.button1).requestFocus();
	                }
	            return false;
	            }
	        });
	        
	        
	        final Button save = (Button) findViewById(R.id.button1);
	        save.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	
	            	oldPass = oldPw.getText().toString();
	            	newPass = newPw.getText().toString();
	            	newPassRep = newPwRep.getText().toString();
	            	
	            	pd = ProgressDialog.show(ctx, "Logging in ...", "Please wait.           ", true,
	                        false); 
	            	myLoginTask = new LoginTask();
	            	myLoginTask.execute(myUserStatus);

	            }
	        });
	        
	        final Button cancel = (Button) findViewById(R.id.button2);
	        cancel.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	finish();

	            }
	        });
	        
	        
	        
		}
		
		public class LoginTask extends AsyncTask<UserStatus, String, UserStatus> {
			
			String TAG="TEX";
			
		    protected UserStatus doInBackground(UserStatus ... myUserStatus) {
		    	
		    	myUserStatus[0].setLastError("0");
		    	
		        try{
		        	
		            HttpClient client = new DefaultHttpClient();
		            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit 
		            HttpConnectionParams.setSoTimeout(client.getParams(), 10000);
		            HttpResponse response;	
			        	
			        // LOGIN	
			        	
		        	JSONObject json = new JSONObject();
		            HttpPost post = new HttpPost("http://textbuster.mobilezapp.de/api/change_password");
	//	            json.put("email", myUserStatus[0].getEmail());
	//	            json.put("password", myUserStatus[0].getPassword());
		            json.put("token", myUserStatus[0].getToken());
		            json.put("current_password", oldPass);
		            json.put("new_password", newPass);
		            json.put("confirm_new_password", newPassRep);
		            StringEntity se = new StringEntity( json.toString());  
		            Log.i(TAG, "json to send: " + json.toString());
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
			                Log.i(TAG, "message null");
			                
			                String token = responsejson.get("token").toString();
			                
		
			                myUserStatus[0].setToken(token);
			                myUserStatus[0].setPassword(newPw.getText().toString());
			                myUserStatus[0].setLoggedIn(true);
			                myUserStatus[0].logStatus();
			                
		                }
		                
		                else {
		                	String error = responsejson.get("message").toString();
		                	myUserStatus[0].setLastError(error);
		                }

		                
		            }
	        }
		        
		    catch (ConnectTimeoutException e) {
		    	Log.i(TAG, e.toString());
		    	pd.dismiss();
		    }
		        
	        catch(Exception e){
	            e.printStackTrace(); 
	            Log.i(TAG, e.toString());
	            pd.dismiss();
	 
	        }
		    	
		    	
		    	
		    	return myUserStatus[0];
		    }
		
		    protected void onProgressUpdate(String... progress) {

		     }
		
		
		    protected void onPostExecute(UserStatus result) {
		    	Log.i(TAG, "post exec");
		        pd.dismiss();
		        afterLogin();
		    }
		}
		
		public void afterLogin () {
			
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
				final AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
		      	alertDialog.setTitle("Success");
		      	alertDialog.setMessage("Your password has been changed.");
		      	
		      	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		    	      public void onClick(DialogInterface dialog, int which) {
		    	    	  alertDialog.dismiss();
		    	    		finish();
		    	    	  
		    	    } }); 
		    	 
		    	alertDialog.show();
		    	
		    	myUserStatus.setPassword(newPass);
		    	Constants.myUserStatus = myUserStatus;
		    	
			
			}
		
		}	
		
}		