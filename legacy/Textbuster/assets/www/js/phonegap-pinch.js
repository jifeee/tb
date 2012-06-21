/*
 * Copyright (C) 2011 Sascha Klick
 * 
 * All rights reserved. Do not copy or use without the authors explicit written consent.
 * 
 */

if( !PhoneGap.hasResource("Pinch") ) {
	PhoneGap.addResource("Pinch");

	/**
	 * @returns instance of Pinch
	 */
	var Pinch = function() {
		return PhoneGap.exec(this.pinchListener, function(err){ 'Pinch plugin error: ' + err }, 'Pinch', '0', []);
	}
	
	Pinch.prototype.pinchListener = function(json){
		try{
			var res = JSON.parse(json);
		}catch(err) { alert(err); }
		
		switch(res.type){
			case 0: if(navigator.pinch.onPinchStart) navigator.pinch.onPinchStart.call(document.body, res.event); break;
			case 1: if(navigator.pinch.onPinchMove)  navigator.pinch.onPinchMove.call(document.body, res.event); break;
			case 2: if(navigator.pinch.onPinchEnd)   navigator.pinch.onPinchEnd.call(document.body, res.event); break;
			case 3: if(navigator.pinch.onPinchIncrease)   navigator.pinch.onPinchIncrease.call(document.body, res.event); break;
			case 4: if(navigator.pinch.onPinchDecrease)   navigator.pinch.onPinchDecrease.call(document.body, res.event); break;
			default: console.log('Illegal touch event type'); break;
		}
	}
	
	Pinch.prototype.onPinchStart = function(e){
	}
	
	Pinch.prototype.onPinchMove = function(e){
	}
	
	Pinch.prototype.onPinchEnd = function(e){
	}
	
	/**
	 * Register the plugin with PhoneGap
	 */
	PhoneGap.addConstructor(function() {
		// Register the Pinch plugin with PhoneGap
		PhoneGap.addPlugin('Pinch', new Pinch());
	});
}
