package com.access2;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ConnectActivity extends Activity {
	
	String TAG="TEX";
	String token;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detect);

        
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
        HttpResponse response;
        
        
//      try{
//    	JSONObject json = new JSONObject();
//        HttpPost post = new HttpPost("http://textbuster.mobilezapp.de/api/sign_up");
//        json.put("device", "1234");
//        json.put("email", "test@example.com");
//        json.put("password", "test123");
//        json.put("password_confirmation", "test123");
//        StringEntity se = new StringEntity( json.toString());  
//        Log.i(TAG, "json to send: " + json.toString() + " to url http://textbuster.mobilezapp.de/api/sign_up" );
//        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//        post.setEntity(se);
//        response = client.execute(post);
//        /*Checking response */
//        if(response!=null){
//            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
//            StringBuilder builder = new StringBuilder();
//            for (String line = null; (line = reader.readLine()) != null;) {
//                builder.append(line).append("\n");
//            }
//            
//            Log.i(TAG, builder.toString());
//            
//            JSONObject responsejson = new JSONObject(builder.toString());
////            token = responsejson.get("token").toString();
//            
//        }    
//    }
//    catch(Exception e){
//        e.printStackTrace(); 
//
//    }
        
 
        
        try{
        	JSONObject json = new JSONObject();
            HttpPost post = new HttpPost("http://textbuster.mobilezapp.de/api/login");
            json.put("email", "michael@pp.com");
            json.put("password", "qwe123");
            StringEntity se = new StringEntity( json.toString());  
            Log.i(TAG, "json to send: " + json.toString() + " to url http://textbuster.mobilezapp.de/api/login");
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
                token = responsejson.get("token").toString();
                
            }    
        }
        catch(Exception e){
            e.printStackTrace(); 
 
        } 
        
        try{
        	JSONObject json = new JSONObject();
            String url = "http://textbuster.mobilezapp.de/api/trips";
            url = url.concat("?token=");
            url = url.concat(token);
            HttpGet get = new HttpGet(url);
             
            json.put("token", token);           
            StringEntity se = new StringEntity( json.toString());  
            Log.i(TAG, "json to send: " + json.toString() + " to url " + url);
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

                
            }    
        }
        catch(Exception e){
            e.printStackTrace();
 
        }
        
//        try{
//        	JSONObject json = new JSONObject();
////            HttpGet get = new HttpGet("http://textbuster.mobilezapp.de/api/trips");
//            String url = "http://textbuster.mobilezapp.de/api/trips/1";
//            
//            url = url.concat("?token=");
//            url = url.concat(token);
//            HttpGet get = new HttpGet(url);
//            
//            json.put("token", token);           
//            StringEntity se = new StringEntity( json.toString());  
//            Log.i(TAG, "json to send: " + json.toString() + " to url " + url);
//            get.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
////            get.setEntity(se);
//            Log.i(TAG, get.toString());
//            
//            response = client.execute(get);
//            Log.i(TAG, "RESP" + response.toString());
//            /*Checking response */
//            if(response!=null){
//                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
//                StringBuilder builder = new StringBuilder();
//                for (String line = null; (line = reader.readLine()) != null;) {
//                    builder.append(line).append("\n");
//                }
//                
//                Log.i(TAG, builder.toString()); 
//
//                
//            }    
//        }
//        catch(Exception e){
//            e.printStackTrace();
// 
//        }


      
        

        
        
        
     
    

    	

  	  
    }
    

}

   