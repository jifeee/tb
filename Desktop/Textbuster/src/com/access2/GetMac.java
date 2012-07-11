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

import com.access2.LoginActivity.LoginTask;


import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import classes.Constants;
import classes.UserStatus;

public class GetMac  {
	
	UserStatus myUserStatus;
	String imei;
	Context ctx;

	
	// Class to retrieve Textbuster MACs that are associated with this IMEI from the backend
	// Lots TODO
	
	public void getMac (Context ctx) {

		this.ctx = ctx;
		
		myUserStatus = Constants.myUserStatus;
		imei = Constants.imei;
		
		TelephonyManager tm = (TelephonyManager)ctx.getSystemService(ctx.TELEPHONY_SERVICE);
		imei = tm.getDeviceId();
		
		GetMacTask gmt = new GetMacTask();
    	
    	gmt.execute(myUserStatus);
		
	}

	
	public class GetMacTask extends AsyncTask<UserStatus, String, UserStatus> {
		
		String TAG="TEX";

		
		
	    protected UserStatus doInBackground(UserStatus ... myUserStatus) {

	          try{
	        	  
	            HttpClient client = new DefaultHttpClient();
	            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit 
	            HttpConnectionParams.setSoTimeout(client.getParams(), 10000);
	            HttpResponse response;	
		            

	        	JSONObject json = new JSONObject();
	        	String url = "http://textbuster.mobilezapp.de/api/get_textbusters/";
	        	url = url.concat(imei);
	            HttpPost post = new HttpPost(url);
	            

	            response = client.execute(post);
	            
	            
	            /*Checking response */
	            if(response!=null){
	                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
	                StringBuilder builder = new StringBuilder();
	                for (String line = null; (line = reader.readLine()) != null;) {
	                    builder.append(line).append("\n");
	                }
	                
	                Log.d(TAG, builder.toString()); 
	                
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

		    }
	          
	        catch(Exception e){
	            e.printStackTrace();
	            
	 
	        }
	    	
	    	
	    	
	    	return myUserStatus[0];
	    }
	
	    protected void onProgressUpdate(String... progress) {

	     }
	
	
	    protected void onPostExecute(UserStatus result) {
	        myUserStatus = result; 
	        epilog();
	    }
	}
	
	public void epilog() {
		
	}

}
