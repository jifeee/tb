/*
 * Copyright (C) 2011 Sascha Klick
 * 
 * All rights reserved. Do not copy or use without the authors explicit written consent.
 * 
 */

if( !PhoneGap.hasResource("HttpRequest") ) {
	PhoneGap.addResource("HttpRequest");

	/**
	 * @returns instance of HttpRequest
	 */
	var HttpRequest = function() {	
	}
	
	/**
	 * Get the document at the URL via a POST request
	 */
	HttpRequest.prototype.post = function(url, data, successCallback,failureCallback) {
	    return PhoneGap.exec(successCallback, failureCallback, 'HttpRequest', 'post', [url, data]);
	}
	
	/**
	 * Get the document at the URL via a GET request
	 */
	HttpRequest.prototype.get = function(url, successCallback,failureCallback) {
	    return PhoneGap.exec(successCallback, failureCallback, 'HttpRequest', 'get', [url]);
	}
	
	/**
	 * Register the plugin with PhoneGap
	 */
	PhoneGap.addConstructor(function() {
		// Register the HttpRequest plugin with Phonegap
		PhoneGap.addPlugin('HttpRequest', new HttpRequest());
	});
}
