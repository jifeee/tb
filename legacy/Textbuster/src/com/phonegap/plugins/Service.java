/*
 * Copyright (C) 2011 Sascha Klick
 * 
 * All rights reserved. Do not copy or use without the authors explicit written consent.
 * 
 */

/**
 * Phonegap plugin for setting a listner for touch screen pinch events
 * @author Sascha Klick <sascha.klick@toasternet.eu>
 */

package com.phonegap.plugin;

import com.access2.BusterService;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;
import com.phonegap.api.PhonegapActivity;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Service extends Plugin {	
	private static final String TAG = "Service";
	
	private PhonegapActivity ctx;
	private Intent busterService;
	
	public Service(){
		this.busterService = null;
	}
	
	/**
	 * Called by the Phonegap framework to handle a call to this plugin
	 */
	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		switch(Integer.parseInt(action)){
			case 0:
				this.busterService = new Intent(this.ctx, BusterService.class);
				this.ctx.startService(this.busterService);
				PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
				result.setKeepCallback(true);
				return result;
			case 1:
				this.ctx.stopService(this.busterService);
				return new PluginResult(PluginResult.Status.OK);
			case 2:
				return new PluginResult(PluginResult.Status.OK, this.isServiceRunning());
			case 3:
				Log.d(TAG, "Sending " + data.toString());
				return new PluginResult(PluginResult.Status.OK);
			default:
				return new PluginResult(PluginResult.Status.INVALID_ACTION);
		}
		
	}
	
	public void setContext(PhonegapActivity ctx) {
		this.ctx = ctx;
		super.setContext(ctx);
	}
	
	private boolean isServiceRunning(){
		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		Log.d(TAG, "Looking for running services");
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			//Log.d("Service", service.service.getClassName());
			if ("com.access2.BusterService".equals(service.service.getClassName())) {
				Log.d(TAG, "Found our service");
				return true;
			}
		}
		return false;
	}
}
