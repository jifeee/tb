/*
 * Copyright (C) 2011 Sascha Klick
 * 
 * All rights reserved. Do not copy or use without the authors explicit written consent.
 * 
 */

if( !PhoneGap.hasResource("Bluetooth") ) {
	PhoneGap.addResource("Bluetooth");

	/**
	 * @returns instance of Bluetooth
	 */
	var Bluetooth = function() {	
	}
	
	Bluetooth.prototype.callback = function(res) {
		console.log('ACTION RECEIVED: ' + res);
		
		if(res == 'bton'){
			if(navigator.bluetooth.onCallback) navigator.bluetooth.onCallback(document.body);
			if(navigator.bluetooth.stateCallback) navigator.bluetooth.stateCallback(true);
		}
		else if(res == 'btoff'){
			if(navigator.bluetooth.offCallback) navigator.bluetooth.offCallback(document.body);
			if(navigator.bluetooth.stateCallback) navigator.bluetooth.stateCallback(false);
			if(navigator.bluetooth.scanStopCallback) navigator.bluetooth.scanStopCallback();
		}
		else if(res == 'scanstart'){ if(navigator.bluetooth.scanStartCallback) navigator.bluetooth.scanStartCallback(); }
		else if(res == 'scanstop') { if(navigator.bluetooth.scanStopCallback) navigator.bluetooth.scanStopCallback(); }
		else if(res.substring(0, 4) == 'mac ') {
			var add = res.substr(4);
			console.log(add);
			if(navigator.bluetooth.foundCallback) navigator.bluetooth.foundCallback([[add, ""]]);
		}
	}
	
	/**
	 * Activate Bluetooth and begin discovering devices
	 */
	Bluetooth.prototype.start = function() {
	    PhoneGap.exec(Bluetooth.prototype.callback, function() {}, 'Bluetooth', 'on', []);
	}
	
	/**
	 * Deactivate Bluetooth
	 */
	Bluetooth.prototype.stop = function() {
	    PhoneGap.exec(function() {}, function() {}, 'Bluetooth', 'off', []);
	}
	
	/**
	 * Get state of Bluetooth device
	 */
	Bluetooth.prototype.state = function() {
	    PhoneGap.exec(navigator.bluetooth.stateCallback, function() {}, 'Bluetooth', 'state', []);
	}
	
	/**
	 * Register the plugin with PhoneGap
	 */
	PhoneGap.addConstructor(function() {
		// Register the Bluetooth plugin with Phonegap
		PhoneGap.addPlugin('Bluetooth', new Bluetooth());
	});
}
