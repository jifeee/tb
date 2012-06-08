/*
 * Copyright (C) 2011 Sascha Klick
 * 
 * All rights reserved. Do not copy or use without the authors explicit written consent.
 * 
 */

/**
 * Phonegap plugin for setting and releasing wake locks on Android devices
 * @author Sascha Klick <sascha.klick@toasternet.eu>
 */

package com.phonegap.plugin;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.net.*;

public class HttpRequest extends Plugin {
	/**
	 * Called by the Phonegap framework to handle a call to this plugin
	 */
	@Override
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		String url = null;
		String postData = null;
		try {
			url = args.getString(0);
			if (action.equals("post"))
				postData = args.getString(1);
		}
		catch (JSONException e) {
			return new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION, "Malformed JSON args");
		}
		
		if(action.equals("post"))
			return this.postRequest(url, postData);
		else if(action.equals("get"))
			return new PluginResult(PluginResult.Status.OK, "getStuff");
		else
			return new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION, "Illegal action '"+action+"'");
	}


	private PluginResult getRequest( String location, String data ) {
		String buffer = "";
		
		URL url = null;
		try{ url = new URL(location); }
		catch(java.net.MalformedURLException e){ return new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION, "Malformed URL '"+url+"'"); }
		
		try{
			HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
			ucon.setRequestMethod("GET");
			ucon.setDoOutput(true);
			ucon.connect();

			InputStream is = ucon.getInputStream();
			
			byte[] bbuffer = new byte[1024];
			int len;
			while ((len = is.read(bbuffer)) > 0)
				buffer += new String(bbuffer, 0, len);
			
		}catch(Exception e){
			return new PluginResult(PluginResult.Status.OK, "Request failed during transfer");
		}
   
		return new PluginResult(PluginResult.Status.OK, buffer);
	}
	
	private PluginResult postRequest( String location, String data ) {
		String buffer = "";
		
		URL url = null;
		try{ url = new URL(location); }
		catch(java.net.MalformedURLException e){ return new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION, "Malformed URL '"+url+"'"); }
		
		try{
			HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
			ucon.setRequestMethod("POST");
			ucon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			ucon.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
			ucon.setRequestProperty("Content-Language", "en-US");  
			ucon.setUseCaches (false);
			ucon.setDoInput(true);
			ucon.setDoOutput(true);
			
			DataOutputStream wr = new DataOutputStream(ucon.getOutputStream());
			wr.writeBytes(data);
			wr.flush();
			wr.close();
		
			InputStream is = ucon.getInputStream();
			
			byte[] bbuffer = new byte[1024];
			int len;
			while ((len = is.read(bbuffer)) > 0)
				buffer += new String(bbuffer, 0, len);
			
		}catch(Exception e){
			return new PluginResult(PluginResult.Status.OK, "Request failed during transfer");
		}
   
		return new PluginResult(PluginResult.Status.OK, buffer);
	}
}
