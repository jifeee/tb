/*
 * Copyright (C) 2011 Sascha Klick
 * 
 * All rights reserved. Do not copy or use without the authors explicit written consent.
 * 
 */

/**
 * Phonegap plugin for activating Bluetooth and searching for devices
 * @author Sascha Klick <sascha.klick@toasternet.eu>
 */

package com.phonegap.plugin;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;
import com.phonegap.api.PhonegapActivity;
import org.json.JSONArray;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import android.util.Log;

import java.io.*;
import java.net.*;

public class Bluetooth extends Plugin {
	private static final String TAG = "BluetoothPlugin";
	
	private volatile Plugin plugin = null;
	private volatile String cbId = null;
	
	final BluetoothAdapter bluetoothAdp = BluetoothAdapter.getDefaultAdapter();
	
	/**
	 * Called by the Phonegap framework to handle a call to this plugin
	 */
	@Override
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		Log.d(TAG, "action " + action);	
	
		if(action.equals("on")){
			this.plugin = this;
			this.cbId = callbackId;
			return this.turnOn();
		}
		else if(action.equals("off"))
			return this.turnOff();
		else if(action.equals("state"))
			return new PluginResult(PluginResult.Status.OK, bluetoothAdp.isEnabled());
		else
			return new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION, "Illegal action '"+action+"'");
	}

	private PluginResult turnOn() {
		if(bluetoothAdp == null) {
			PluginResult result = new PluginResult(PluginResult.Status.OK, "nobt");
			result.setKeepCallback(true);
			return result;
		}else if (bluetoothAdp.isEnabled()){
			bluetoothAdp.startDiscovery();
			PluginResult result = new PluginResult(PluginResult.Status.OK, "scanstart");
			result.setKeepCallback(true);
			return result;
		}else{
			this.bluetoothAdp.enable();
			PluginResult result = new PluginResult(PluginResult.Status.OK, "btturnon");
			result.setKeepCallback(true);
			return result;
		}
	}
	
	private PluginResult turnOff() {
		this.bluetoothAdp.disable();
		return new PluginResult(PluginResult.Status.OK, "btturnoff");
	}

	// BroadcastReceiver for Bluetooth events
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        if(cbId == null || plugin == null)
				return;
	        
	        String action = intent.getAction();
	        PluginResult result;
	        
	        if(action.equals(BluetoothDevice.ACTION_FOUND)){
				BluetoothDevice deviceExtra = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String mac = deviceExtra.toString();
				
				result = new PluginResult(PluginResult.Status.OK, "mac " + mac);
				result.setKeepCallback(true);
				plugin.success(result, cbId);
				
				Log.d(TAG, action + " " + mac);
			}else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
				result = new PluginResult(PluginResult.Status.OK, "scanstop");
				result.setKeepCallback(true);
				plugin.success(result, cbId);
						
				Log.d(TAG, action);	
			}else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
				result = new PluginResult(PluginResult.Status.OK, "scanstart");
				result.setKeepCallback(true);
				plugin.success(result, cbId);
				
				Log.d(TAG, action);	
			}

			else if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
				int stateExtra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				switch(stateExtra){
					case BluetoothAdapter.STATE_OFF:
					case BluetoothAdapter.STATE_TURNING_OFF:
						
						result = new PluginResult(PluginResult.Status.OK, "btoff");
						result.setKeepCallback(true);
						plugin.success(result, cbId);
						
						Log.d(TAG, action + " off");
						break;
					case BluetoothAdapter.STATE_TURNING_ON:
						
						Log.d(TAG, action + " turning on");
						break;
					case BluetoothAdapter.STATE_ON:
						
						result = new PluginResult(PluginResult.Status.OK, "bton");
						result.setKeepCallback(true);
						plugin.success(result, cbId);
						
						bluetoothAdp.startDiscovery();
						
						Log.d(TAG, action + " on");
						break;
					default:
						
						Log.d(TAG, action + " " + stateExtra + "???");
						break;
				}	
	        }

	    }
	};
	
	public void setContext(PhonegapActivity ctx) {
		this.ctx = ctx;
		super.setContext(ctx);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

		ctx.registerReceiver(mReceiver, filter);
		
		if(bluetoothAdp != null && bluetoothAdp.isEnabled() && !bluetoothAdp.isDiscovering())
			bluetoothAdp.startDiscovery();
		
		Log.d(TAG, "init");
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "destroy");
		
		ctx.unregisterReceiver(mReceiver);
	}
}
