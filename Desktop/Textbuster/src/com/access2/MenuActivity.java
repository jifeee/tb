package com.access2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import classes.Constants;
import classes.Trips;
import classes.UserStatus;


public class MenuActivity extends Activity {
	
	String TAG="TEX";
	final static int TRIPS =0;
	final static int MORE =1;
	final static int SETTINGS =2;
	final static int DETECT =3;
	Context ctx;

	private ProgressDialog pd;
	TripListTask myTripListTask;
	UserStatus myUserStatus; 
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		ctx=this;
		
		myUserStatus = Constants.myUserStatus;
		
		if (myUserStatus.getTrips().size()>0) {
		
			JSONObject trip = null;
			
			try {
				trip = new JSONObject(myUserStatus.getTrips().get(0));
				String date = new DateTime (Long.parseLong(trip.getString("start_time"))).toString("MM/dd/yy");
				String startTime = new DateTime (Long.parseLong(trip.getString("start_time"))).toString("hh:mm");
				String from = String.valueOf(trip.getString("start"));
				String to = String.valueOf(trip.getString("end"));
				
				TextView dateV = (TextView)findViewById(R.id.date);
				dateV.setText(date);
				TextView startV = (TextView)findViewById(R.id.tripstarttime);
				startV.setText(startTime);
				TextView fromV = (TextView)findViewById(R.id.from);
				fromV.setText(from);
				TextView toV = (TextView)findViewById(R.id.to);
				toV.setText(to);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			
	        
		
		
		}

        final Button trips = (Button) findViewById(R.id.button1);
        trips.setText("History of Trips");
        trips.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	pd = ProgressDialog.show(ctx, "Getting trips ...", "Please wait.           ", true,
                        false); 
            	myTripListTask = new TripListTask();
            	myTripListTask.execute(myUserStatus);
            	

            }
        });
        
        final Button settings = (Button) findViewById(R.id.button2);
        settings.setText("Settings");
        settings.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

    	        Intent i = new Intent(MenuActivity.this, SettingsActivity.class);      
    	    	MenuActivity.this.startActivityForResult(i, SETTINGS); 
            	
            	
            }
        });
        
        final Button more = (Button) findViewById(R.id.button3);
        more.setText("More");
        more.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
    	        Intent i = new Intent(MenuActivity.this, MoreActivity.class);      
    	    	MenuActivity.this.startActivityForResult(i, MORE); 
            	
            	
            }
        });
        
        final Button pair = (Button) findViewById(R.id.button4);
        pair.setText("Pair with TextBuster");
        pair.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
    	        Intent i = new Intent(MenuActivity.this, DetectActivity.class);      
    	    	MenuActivity.this.startActivityForResult(i, DETECT); 
            	
            	
            }
        });
        
        final Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
    	        myUserStatus = null;
    	        Constants.myUserStatus = null;
            	finish();
            	
            }
        });
		
		
		
	}	
	
	public class TripListTask extends AsyncTask<UserStatus, String, UserStatus> {
		
		String TAG="TEX";
		String token = myUserStatus.getToken();
		
		
	    protected UserStatus doInBackground(UserStatus ... myUserStatus) {
	    	
	    	
	   
	        	
	          try{
	        	  
	            HttpClient client = new DefaultHttpClient();
	            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit 
	            HttpConnectionParams.setSoTimeout(client.getParams(), 10000);
	            HttpResponse response;	
		            
		            
	        	JSONObject json = new JSONObject();
	            String url = "http://textbuster.mobilezapp.de/api/trips";
	            url = url.concat("?token=");
	            url = url.concat(token);
	            HttpGet get = new HttpGet(url);
	            
	            json.put("token", token);           
	            Log.i(TAG, "json to send: " + json.toString());
	            get.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	            Log.i(TAG, get.toString());
	            
	            response = client.execute(get);
	            Log.i(TAG, "RESP" + response.toString());
	            /*Checking response */
	            if(response!=null){
	                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
	                StringBuilder builder = new StringBuilder();
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
	          
		    catch (ConnectTimeoutException e) {
			    	pd.dismiss();
			    	final AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
			      	alertDialog.setTitle("Connection error.");
			      	alertDialog.setMessage("Server unreachable. Please check your internet settings.");
			      	
			      	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			    	      public void onClick(DialogInterface dialog, int which) {
			    	    	  alertDialog.dismiss();
			    	    	  
			    	    } }); 
			    	 
			    	alertDialog.show();
		    }
	          
	        catch(Exception e){
	            e.printStackTrace();
	            
	 
	        }
	    	
	    	
	    	
	    	return myUserStatus[0];
	    }
	
	    protected void onProgressUpdate(String... progress) {

	     }
	
	
	    protected void onPostExecute(UserStatus result) {
	        pd.dismiss();
	        myUserStatus = result; 
	        epilog();
	    }
	}
	
	public void epilog() {
		
		Constants.myUserStatus = myUserStatus;
		
        Intent tripsIntent = new Intent(MenuActivity.this, TripsActivity.class);  
    	MenuActivity.this.startActivityForResult(tripsIntent, TRIPS); 
	}
	
    protected void onActivityResult(int requestCode, int resultCode,
            Intent resultIntent) {
if (requestCode == DETECT) {
    		if (resultCode == RESULT_OK ) {
    			
    			String mac = resultIntent.getStringExtra("MAC");
    			
				Toast toast = Toast.makeText(ctx, "Paired successfully with TB device " + mac, Toast.LENGTH_LONG);
				toast.show();

    			
    		}

        }
    }

}
