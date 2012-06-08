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

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;
import com.phonegap.api.PhonegapActivity;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.content.Context;

import android.util.Log;

public class Pinch extends Plugin {
	private String pinchCallbackId;
	private PhonegapActivity ctx;

	
	private class PinchListener extends View implements View.OnTouchListener{
		private Plugin plugin;
		private String cbId;
		private double stepDistance;
		private double lastStep;
		
		private double initialDistance;
			
		public PinchListener(Context context, Plugin plugin, String cbId){
			super(context);
			this.plugin = plugin;
			this.cbId = cbId;
			this.initialDistance = -1;
			this.stepDistance = 100;
			this.lastStep = 0;
		}
		
		public boolean onTouch(View v, MotionEvent e){
			if(e.getPointerCount() == 2 || (e.getPointerCount() == 1 && this.initialDistance != -1)){
				double rx = (e.getX(0) - e.getX(1));
				double ry = (e.getY(0) - e.getY(1));
				double cx = e.getX(1) + (rx / 2);
				double cy = e.getY(1) + (ry / 2);
				double dist = java.lang.Math.sqrt( (rx * rx) + (ry * ry) );
				
				int type = -1;
				
				String touches = "\"touches\":[";
				for(int i = 0; i < e.getPointerCount(); i++)
					touches += "{\"x\":" + (int)e.getX(i) + ",\"y\":" + (int)e.getY(i) + ",\"force\":" + e.getPressure(i) + "},";
				if(e.getPointerCount() > 0) touches = touches.substring(0, touches.length() - 1);
				touches += "],\"middle\":{\"x\":" + (int)cx + ",\"y\":" + (int)cy + ",\"distance\":" + (int)dist + "}";

				if(e.getPointerCount() == 1 && this.initialDistance != -1){
					this.initialDistance = -1;
					type = 2;
					Log.d("Pinch", "Stop");
				}else if(e.getPointerCount() == 2){
						if(this.initialDistance == -1) {
							this.initialDistance = dist;
							type = 0;
							this.lastStep = dist;
							Log.d("Pinch", "Start");
						}else{
							type = 1;
							if(dist - this.lastStep >= this.stepDistance){
								String event = "{\"type\":3,\"event\":{" + touches + "}}";
								PluginResult result = new PluginResult(PluginResult.Status.OK, event);
								result.setKeepCallback(true);
								this.plugin.success(result, this.cbId);
								this.lastStep = dist;
								Log.d("Pinch", "Step Increase");
							}else if(dist - this.lastStep <= -this.stepDistance){
								String event = "{\"type\":4,\"event\":{" + touches + "}}";
								PluginResult result = new PluginResult(PluginResult.Status.OK, event);
								result.setKeepCallback(true);
								this.plugin.success(result, this.cbId);
								this.lastStep = dist;
								Log.d("Pinch", "Step Decrease");
							}
							Log.d("Pinch", "Move");
						}
						
				}
				
				String event = "{\"type\":" + type + ",\"event\":{" + touches + "}}";
				PluginResult result = new PluginResult(PluginResult.Status.OK, event);
				result.setKeepCallback(true);
				this.plugin.success(result, this.cbId);
				return true;
			
			}
				
			return false;			
/*
					"[ { 'identifier' : '0', " +
					"'screenX' : " + (int)e.getX(0) + ", 'screenY' : " + (int)e.getY(0) + ", " +
					"'clientX' : " + (int)e.getX(0) + ", 'clientY' : " + (int)e.getY(0) + ", " + 
					"'pageX' : " + (int)e.getX(0) + ", 'pageY' :" + (int)e.getY(0) + ", " +
					"'radiusX' : 1, 'radiusY' : 1, 'rotationAngle' : 0, " +
					"'force' : " + e.getPressure(0) + " } " + 
					"{ 'identifier' : '1', " +
					"'screenX' : " + (int)e.getX(1) + ", 'screenY' : " + (int)e.getY(1) + ", " +
					"'clientX' : " + (int)e.getX(1) + ", 'clientY' : " + (int)e.getY(1) + ", " + 
					"'pageX' : " + (int)e.getX(1) + ", 'pageY' :" + (int)e.getY(1) + ", " +
					"'radiusX' : 1, 'radiusY' : 1, 'rotationAngle' : 0, " +
					"'force' : " + e.getPressure(1) + " } ]";
*/
		}
	}
	
	/**
	 * Called by the Phonegap framework to handle a call to this plugin
	 */
	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		switch(Integer.parseInt(action)){
			case 0:
				if(this.pinchCallbackId	!= null){
					return new PluginResult(PluginResult.Status.ERROR, "Pinch listener already running.");
				}else{
					this.pinchCallbackId = callbackId;
					PinchListener listener = new PinchListener(ctx, this, this.pinchCallbackId);
					webView.setOnTouchListener(listener);
					PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
					result.setKeepCallback(true);
					Log.d("Pinch", "Init");
					return result;
				}
			default:
				return new PluginResult(PluginResult.Status.INVALID_ACTION);
		}
		
	}
	
	public void setContext(PhonegapActivity ctx) {
		this.ctx = ctx;
		this.pinchCallbackId = null;
		super.setContext(ctx);
	}
		
	/**
	 * Do something before pause
	 */
	@Override
	public void onPause(boolean multitask) {
		super.onPause(multitask);
		Log.d("Pinch", "Paused");
	}
	
	/**
	 * Do something on resume
	 */
	@Override
	public void onResume(boolean multitask) {
		super.onResume(multitask);
		Log.d("Pinch", "Resumed");
	}
}
