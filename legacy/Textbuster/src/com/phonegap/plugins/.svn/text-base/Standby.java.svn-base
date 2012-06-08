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

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;


public class Standby extends Plugin {
	private PowerManager.WakeLock lock = null;

	/**
	 * Called by the Phonegap framework to handle a call to this plugin
	 */
	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		switch(Integer.parseInt(action)){
			case 0:
				return this.setStandbyType(-1);
			case 1:
				return this.setStandbyType(PowerManager.FULL_WAKE_LOCK);
			case 2:
				return this.setStandbyType(PowerManager.SCREEN_DIM_WAKE_LOCK);
			case 3:
				return this.setStandbyType(PowerManager.SCREEN_BRIGHT_WAKE_LOCK);
			case 4:
				return this.setStandbyType(PowerManager.PARTIAL_WAKE_LOCK);
			case 5:
				PowerManager pm = (PowerManager)ctx.getSystemService(Context.POWER_SERVICE);
				return new PluginResult(PluginResult.Status.OK, pm.isScreenOn());
			default:
				return new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION, "Illegal action '"+action+"'");
		}
	}
	
	/**
	 * Create a wakelock
	 */
	private PluginResult setStandbyType( int standbyType ) {
		if(this.lock != null){
			this.lock.release();
			this.lock = null;
		}
		
		if(standbyType != -1){
			PowerManager pm = (PowerManager)ctx.getSystemService(Context.POWER_SERVICE);
			this.lock = pm.newWakeLock(standbyType, "StandbyPlugin");
			this.lock.acquire();
		}
		
		return new PluginResult(PluginResult.Status.OK);
	}
	
	/**
	 * Release wakelock on standby
	 */
	@Override
	public void onPause(boolean multitask) {
		if( this.lock != null ) this.lock.release();
		super.onPause(multitask);
	}
	
	/**
	 * Reaquire wakelock on resume
	 */
	@Override
	public void onResume(boolean multitask) {
		if( this.lock != null ) this.lock.acquire();
		super.onResume(multitask);
	}
}
