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
	
	/**
	 * Get the document at the URL via a POST request
	 */
	Bluetooth.prototype.post = function(url, data, successCallback,failureCallback) {
	    return PhoneGap.exec(successCallback, failureCallback, 'Bluetooth', 'on', []);
	}
	
	/**
	 * Get the document at the URL via a GET request
	 */
	Bluetooth.prototype.get = function(url, successCallback,failureCallback) {
	    return PhoneGap.exec(successCallback, failureCallback, 'Bluetooth', 'state', []);
	}
	
	/**
	 * Register the plugin with PhoneGap
	 */
	PhoneGap.addConstructor(function() {
		// Register the Bluetooth plugin with Phonegap
		PhoneGap.addPlugin('Bluetooth', new Bluetooth());
	});
}
