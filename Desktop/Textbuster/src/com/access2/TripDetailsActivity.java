package com.access2;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.joda.time.DateTime;
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
import classes.Constants;
import classes.UserStatus;

public class TripDetailsActivity extends Activity {
	
	UserStatus myUserStatus;
	static final int MAP = 0;
	ProgressDialog pd; 
	Context ctx; 
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tripdetails);
        
        myUserStatus = Constants.myUserStatus;
        ctx = this;
        JSONObject trip=null;
        String date=null;
        String startTime=null;
        String tripTime=null;
        String speed=null;
        String miles=null;
        String from=null;
        String to=null;
        
        try {
			trip = new JSONObject(myUserStatus.getTripByID(myUserStatus.getSelectedTripID()));
			
			date = new DateTime (Long.parseLong(trip.getString("start_time"))).toString("MM/dd/yy");
			startTime = new DateTime (Long.parseLong(trip.getString("start_time"))).toString("hh:mm");
			
			long diff = Long.parseLong(trip.getString("end_time")) - Long.parseLong(trip.getString("start_time"));
			int minutes = (int) ((diff / (1000*60)) % 60);
			int hours   = (int) ((diff / (1000*60*60)) % 24);
			tripTime = String.valueOf(hours) + ":" + String.valueOf(minutes);
			speed = String.valueOf(trip.getString("average_speed")) + " mph";
			miles = String.valueOf(trip.getString("distance"));
			from = String.valueOf(trip.getString("start"));
			to = String.valueOf(trip.getString("end"));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        TextView dateV = (TextView)findViewById(R.id.date);
        dateV.setText(date);
        TextView starttimeV = (TextView)findViewById(R.id.starttime);
        starttimeV.setText(startTime);

        TextView triptimeV = (TextView)findViewById(R.id.triptime);
        triptimeV.setText(tripTime);
        TextView speedV = (TextView)findViewById(R.id.speed);
        speedV.setText(speed);
        TextView milesV = (TextView)findViewById(R.id.miles);
        milesV.setText(miles);
        TextView fromV = (TextView)findViewById(R.id.from);
        fromV.setText(from);
        TextView toV = (TextView)findViewById(R.id.to);
        toV.setText(to);
        
        final Button map = (Button)findViewById(R.id.button1);
        map.setText("See Map");
        map.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	pd = ProgressDialog.show(ctx, "Getting Map ...", "Please wait.           ", true,
                        false); 
            	
            	TripDetailTask myTripDetailTask = new TripDetailTask();
            	myTripDetailTask.execute(myUserStatus);

            }
        });
        
    }
    
	public class TripDetailTask extends AsyncTask<UserStatus, String, UserStatus> {
		
		String TAG="TEX";
		String token = myUserStatus.getToken();
		int selectedTripID = myUserStatus.getSelectedTripID();
		
		
	    protected UserStatus doInBackground(UserStatus ... myUserStatus) {
	    	
	    	
	   
	        	
	          try{
	        	  
	            HttpClient client = new DefaultHttpClient();
	            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit 
	            HttpConnectionParams.setSoTimeout(client.getParams(), 10000);
	            HttpResponse response;	
		            
		            
	        	JSONObject json = new JSONObject();
	            String url = "http://textbuster.mobilezapp.de/api/trips";
	            url = url.concat("/" + selectedTripID);
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
	                
	                JSONObject details = new JSONObject(builder.toString());
	                myUserStatus[0].addDetails(details.toString(), Integer.parseInt(details.getString("trip_id")));
	                	               
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
	
	public void epilog () {
		
		Constants.myUserStatus = myUserStatus;
		
        Intent i = new Intent(TripDetailsActivity.this, TripMapActivity.class);  
    	TripDetailsActivity.this.startActivityForResult(i, MAP); 
		
	}
}
