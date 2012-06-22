package com.access2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.access2.MenuActivity.TripListTask;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import classes.Constants;
import classes.CustomBaseAdapter;
import classes.TripListItem;
import classes.UserStatus;

public class TripsActivity extends Activity{
	
	Context ctx; 
	UserStatus myUserStatus; 
	final static int DETAIL =0; 
	ArrayList<TripListItem> items = new ArrayList<TripListItem>();
	String TAG="TEX";
	private ProgressDialog pd;
	TripListTask myTripListTask;
	int orderBy = 0;								// 0 by date 1 by miles 
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listlayout);
		ctx=this;
		
//		myUserStatus = (UserStatus)getIntent().getSerializableExtra("UserStatus");
		myUserStatus = Constants.myUserStatus;
		Log.i(TAG, "size in trips "  + myUserStatus.getTrips().size());
		
        final Button order = (Button) findViewById(R.id.button1);
    	if (orderBy == 0) {
    		order.setText("Sort by Miles");
    	}
    	
    	else {
    		order.setText("Sort by Date");
    	}
        order.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	pd = ProgressDialog.show(ctx, "Getting trips ...", "Please wait.           ", true,
                        false); 
            	myTripListTask = new TripListTask();
            	myTripListTask.execute(myUserStatus);
            	
            	if (orderBy == 0) {
            		orderBy = 1;
            		order.setText("Sort by Date");
            	}
            	
            	else {
            		orderBy=0;
            		order.setText("Sort by Miles");
            	}
            	
            }
        });

		makeList();
		
	}	
	
	public class TripListTask extends AsyncTask<UserStatus, String, UserStatus> {
		
		String TAG="TEX";
		String token = myUserStatus.getToken();
		
		
	    protected UserStatus doInBackground(UserStatus ... myUserStatus) {
	    	
	    	
	   
	        	
	          try {
	        	  
	            HttpClient client = new DefaultHttpClient();
	            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
	            HttpResponse response;	

	            String url = "http://textbuster.mobilezapp.de/api/trips";
	            url = url.concat("?token=");
	            url = url.concat(token);
	            if (orderBy==1) {
	            	url = url.concat("&order=miles");
	            }

	            HttpGet get = new HttpGet(url);

	            Log.i(TAG, "url" + url);
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
	                
//	                Trips myTrips = new Trips();
	                
	                JSONArray array = new JSONArray(builder.toString());
	                for (int i = 0; i < array.length(); i++) {
	                    JSONObject trip = array.getJSONObject(i);
	                    trips.add(trip.toString());
	                    
//	                    String startLocation = trip.getString("start");
//	                    String endLocation = trip.getString("end");
//	                    DateTime startTime = new DateTime(Long.parseLong(trip.getString("start_time")));
//	                    DateTime endTime = new DateTime(Long.parseLong(trip.getString("end_time")));
//	                    Double speed = Double.parseDouble(trip.getString("average_speed"));
//	                    Double distance = Double.parseDouble(trip.getString("distance"));

	                }

	                myUserStatus[0].setTrips(trips);
	                
	            }    
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
	
	
	public void makeList () {
		
		items.clear();
		for (int i=0; i<myUserStatus.getTrips().size(); i++) {
			
			JSONObject trip = null;

			try {
				trip = new JSONObject(myUserStatus.getTrips().get(i));
				items.add(new TripListItem(Integer.parseInt(trip.getString("trip_id")), trip.getString("start"), trip.getString("end"), new DateTime (Long.parseLong(trip.getString("end_time"))), 
						new DateTime (Long.parseLong(trip.getString("start_time"))), Double.parseDouble(trip.getString("distance"))));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		ListView listView = (ListView) findViewById(R.id.listView1);
	    listView.setAdapter(new CustomBaseAdapter(this, items));
	    
	    listView.setOnItemClickListener(new OnItemClickListener() {
	    	   @Override
	    	   public void onItemClick(AdapterView<?> a, View v, int position, long id) {
	    		   
	    	   		TripListItem item = (TripListItem)items.get(position);
	    	   		
	    	   		myUserStatus.setSelectedTripID(item.getId());
	    	   		
	    	   		Log.i(TAG, "position: " + position);

	    	   		Constants.myUserStatus = myUserStatus;
//	    		    Intent resultIntent = new Intent();
//	    		    setResult(RESULT_OK, resultIntent);
//	    	   		finish();
	    	   		
	    	        Intent i = new Intent(TripsActivity.this, TripDetailsActivity.class);  
	    	    	TripsActivity.this.startActivityForResult(i, DETAIL); 
	    	   	
	    	   }
	    	});
	    
	}
	
	public void epilog() {
		Constants.myUserStatus = myUserStatus;
		makeList();
	}

}
