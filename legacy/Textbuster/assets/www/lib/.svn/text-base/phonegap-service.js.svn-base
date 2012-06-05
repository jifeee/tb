/*
 * Copyright (C) 2011 Sascha Klick
 * 
 * All rights reserved. Do not copy or use without the authors explicit written consent.
 * 
 */

if( !PhoneGap.hasResource("Service") ) {
	PhoneGap.addResource("Service");

	/**
	 * @returns instance of Service
	 */
	var Service = function() {
	}
	
	Service.prototype.stopService = function(e){
		return PhoneGap.exec(this.ServiceListener, function(err){ 'Service stop plugin error: ' + err }, 'Service', '1', []);
	};
	
	Service.prototype.ServiceListener = function(json){
		try{
			var res = JSON.parse(json);
		}catch(err) { alert(err); }
		
		switch(res.type){
			case 0: if(navigator.Service.onServiceStart) navigator.Service.onServiceStart.call(document.body); break;
			case 1: if(navigator.Service.onServiceStop) navigator.Service.onServiceStop.call(document.body); break;
			default: console.log('Illegal response type'); break;
		}
	}
	
	Service.prototype.start = function(failure){
		PhoneGap.exec(this.ServiceListener, failure, 'Service', '0', []);
	};
	
	Service.prototype.stop = function(failure){
		PhoneGap.exec(function(){}, failure, 'Service', '1', []);
	}
	
	Service.prototype.check = function(handler, failure){
		PhoneGap.exec(handler, failure, 'Service', '2', []);
	}
	
	Service.prototype.send = function(message, handler, failure){
		PhoneGap.exec(handler, failure, 'Service', '3', [message]);
	}
	
	Service.prototype.onServiceStart = function(e){
	};
	
	Service.prototype.onServiceMove = function(e){
	};
	
	Service.prototype.onServiceEnd = function(e){
	};
	
	/**
	 * Register the plugin with PhoneGap
	 */
	PhoneGap.addConstructor(function() {
		// Register the Service plugin with PhoneGap
		PhoneGap.addPlugin('Service', new Service());
	});
}
