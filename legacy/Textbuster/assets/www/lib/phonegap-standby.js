/*
 * Copyright (C) 2011 Sascha Klick
 * 
 * All rights reserved. Do not copy or use without the authors explicit written consent.
 * 
 */

if( !PhoneGap.hasResource("Standby") ) {
	PhoneGap.addResource("Standby");

	/**
	 * @returns instance of Standby
	 */
	var Standby = function() {	
	}
	
	/**
	 * Prevent standby. Allow system to turn off screen and keyboard.
	 *
	 * @param successCallback function to be called when standby status is set successfully
	 * @param errorCallback function to be called when there was a problem setting the standby status
	 */
	Standby.prototype.allowStandby = function(successCallback,failureCallback) {
	    return PhoneGap.exec(successCallback, failureCallback, 'Standby', '0', []);
	}
	
	/**
	 * Prevent standby and keep screen and keyboard lit
	 * 
	 * @param successCallback function to be called when standby status is set successfully
	 * @param errorCallback function to be called when there was a problem setting the standby status
	 */
	Standby.prototype.alwaysOn = function(successCallback,failureCallback) {
	    return PhoneGap.exec(successCallback, failureCallback, 'Standby', '1', []);
	}
	
	/**
	 * Prevent standby and allow system to dim screen. The system can still turn the keyboard light off.
	 * 
	 * @param successCallback function to be called when standby status is set successfully
	 * @param errorCallback function to be called when there was a problem setting the standby status
	 */
	Standby.prototype.dimScreen = function(successCallback,failureCallback) {
	    return PhoneGap.exec(successCallback, failureCallback, 'Standby', '2', []);
	}
	
	/**
	 * Prevent standby and keep screen at full brightness. The system can still turn the keyboard light off.
	 *
	 * @param successCallback function to be called when standby status is set successfully
	 * @param errorCallback function to be called when there was a problem setting the standby status
	 */
	Standby.prototype.brightScreen = function(successCallback,failureCallback) {
	    return PhoneGap.exec(successCallback, failureCallback, 'Standby', '3', []);
	}
	
	/**
	 * Prevent standby. Allow system to turn off screen and keyboard.
	 *
	 * @param successCallback function to be called when standby status is set successfully
	 * @param errorCallback function to be called when there was a problem setting the standby status
	 */
	Standby.prototype.cpuOnly = function(successCallback,failureCallback) {
	    return PhoneGap.exec(successCallback, failureCallback, 'Standby', '4', []);
	}
	
	/**
	 * Prevent standby. Allow system to turn off screen and keyboard.
	 *
	 * @param successCallback function to be called when standby status is set successfully
	 * @param errorCallback function to be called when there was a problem setting the standby status
	 */
	Standby.prototype.isScreenOn = function(successCallback,failureCallback) {
	    return PhoneGap.exec(successCallback, failureCallback, 'Standby', '5', []);
	}
	
	/**
	 * Register the plugin with PhoneGap
	 */
	PhoneGap.addConstructor(function() {
		// Register the Standby plugin with PhoneGap
		PhoneGap.addPlugin('Standby', new Standby());
	});
}
