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
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import classes.Constants;
import classes.UserStatus;

public class LoginActivity extends Activity {
	
	String TAG="TEX";
	Context ctx;
	
	
	
	LoginTask myLoginTask;
	private ProgressDialog pd;
	UserStatus myUserStatus = new UserStatus(); 
	
	static final int MENU = 0;
	static final int SIGNUP = 1;
	
    String mail;
    String passw;
	
    //Launcher activity, letting the user login
    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		ctx=this;
		
	
        Intent service = new Intent(ctx, TextbusterService.class);
        ctx.startService(service);
        
        GetMac gm = new GetMac();
        gm.getMac(ctx);
		
		
        final EditText email = (EditText) findViewById(R.id.editText2);
        email.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                 
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                   
                	findViewById(R.id.editText1).requestFocus();
                  return true;
                }
                return false;
            }
        });
		
        final EditText pass = (EditText) findViewById(R.id.editText1);
        pass.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                 
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                   
                	findViewById(R.id.button1).requestFocus();
                  return true;
                }
                return false;
            }
        });
        
        final Button login = (Button) findViewById(R.id.button1);
        login.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	            	
            		mail = email.getText().toString().trim();
            		passw = pass.getText().toString();
	            	
            		
            		if (mail.equals("") || passw.equals("")) {
        				final AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
        		      	alertDialog.setTitle("Ooops ...");
        		      	alertDialog.setMessage("Please enter Email and Password.");
        		      	
        		      	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
        		    	      public void onClick(DialogInterface dialog, int which) {
        		    	    	  alertDialog.dismiss();
        		    	    	  
        		    	    } }); 
        		    	 
        		    	alertDialog.show();
            			
            			
            		}
            		
            		else {
            		myUserStatus.setLastError("Unknown error");	
	            	pd = ProgressDialog.show(ctx, "Logging in ...", "Please wait.           ", true,
	                        false); 
	            	myLoginTask = new LoginTask();
	            	myLoginTask.execute(myUserStatus);
	            	}
            }
        });
        

        
        final Button forgot = (Button) findViewById(R.id.button3);
        forgot.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	Intent viewIntent = new Intent("android.intent.action.VIEW", 
            			Uri.parse("http://textbuster.mobilezapp.de/admin/login"));
            	startActivity(viewIntent);  

            }
        });
        
        final Button signup = (Button) findViewById(R.id.button4);
        signup.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
				Intent i = new Intent(LoginActivity.this, SignupActivity.class);  
    	    	LoginActivity.this.startActivityForResult(i, SIGNUP); 

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

	    
		public class LoginTask extends AsyncTask<UserStatus, String, UserStatus> {
			
			String TAG="TEX";
			UserStatus myUserStatus = new UserStatus();
			
			
		    protected UserStatus doInBackground(UserStatus ... myUserStatus) {
		    	
		    	
		        try{
		        	
		            HttpClient client = new DefaultHttpClient();
		            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit 
		            HttpConnectionParams.setSoTimeout(client.getParams(), 10000);
		            HttpResponse response;	
			        	
			        //Login
			        	
		        	JSONObject json = new JSONObject();
		            HttpPost post = new HttpPost("http://textbuster.mobilezapp.de/api/login");
		            json.put("email", mail);
		            json.put("password", passw);
		            StringEntity se = new StringEntity( json.toString());  
		            Log.i(TAG, "json to send: " + json.toString());
		            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		            post.setEntity(se);
		            response = client.execute(post);
		            
		            //Checking response
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
			                String devices = responsejson.get("devices").toString();
		
			                myUserStatus[0].setToken(token);
			                myUserStatus[0].setDevices(devices);
			                myUserStatus[0].setLoggedIn(true);
			                myUserStatus[0].logStatus();
			                
		                }
		                
		                else {
		                	String error = responsejson.get("message").toString();
		                	myUserStatus[0].setLastError(error);
		                }
		                this.myUserStatus = myUserStatus[0];
		                this.myUserStatus.logStatus();
		                
		                
		                // GET TRIPS
		                
		                json = new JSONObject();
			            String url = "http://textbuster.mobilezapp.de/api/trips";
			            url = url.concat("?token=");
			            url = url.concat(myUserStatus[0].getToken());
			            HttpGet get = new HttpGet(url);
			            
			            json.put("token", myUserStatus[0].getToken());           
			            Log.i(TAG, "json to send: " + json.toString());
			            get.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			            Log.i(TAG, get.toString());
			            
			            response = client.execute(get);
			            Log.i(TAG, "RESP" + response.toString());
			            
			            if(response!=null){
			                reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			                builder = new StringBuilder();
			                for (String line = null; (line = reader.readLine()) != null;) {
			                    builder.append(line).append("\n");
			                }
			                
			                Log.i(TAG, builder.toString()); 
			                
			                ArrayList<String> trips = new ArrayList<String>();
		  
			                JSONArray array = new JSONArray(builder.toString());
			                for (int i = 0; i < array.length(); i++) {
			                    JSONObject trip = array.getJSONObject(i);
			                    trips.add(trip.toString());


			                }

			                myUserStatus[0].setTrips(trips);
			                
			            }  
		                
		                
		            }
	        }
		        
		    catch (ConnectTimeoutException e) {
		    	Log.i(TAG, e.toString());
		    	pd.dismiss();
		    	myUserStatus[0].setLastError("Connection timed out");
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
			
			if (!myUserStatus.isLoggedIn()) {
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
				
				Intent i = new Intent(LoginActivity.this, MenuActivity.class);  
    	    	LoginActivity.this.startActivityForResult(i, MENU); 
			}
			
		}

}
