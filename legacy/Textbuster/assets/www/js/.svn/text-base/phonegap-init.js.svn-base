/*
 * Setup phonegap; wait for phonegap to initialize on real device, load dummy in desktop browser
 */
function initPhonegap(){
	if( navigator.userAgent.match(/Android/) )
		document.addEventListener("deviceready", function() { setupApp(); console.log('Phonegap ready'); } , false);
	else
		setupPhonegapDummy();
}
    
/*
 * Create 'Phonegap' dummy object 
 */
function setupPhonegapDummy(){
	device = new Object();
	
	device.name 	= 'Phonegap Dummy';
    device.platform = 'Browser';
    device.uuid 	= '123456789';
    device.version 	= '0.1';
    device.phonegap = device.name + ' ' + device.version;
    device.emu 		= true;
	window.device = device;
	
	// Create 'navigator.camera' dummy system
	var can = document.createElement('canvas');
	var ctx = can.getContext('2d');
	
	var imgs = new Object();
	imgs.items = new Array();
	imgs.add = function (url){ imgs.items.push(new Image()); imgs.items[imgs.items.length - 1].src = url; }
	
	imgs.add('http://farm2.static.flickr.com/1399/1084445061_993666e8a8.jpg');
	imgs.add('http://www.mijnalbum.nl/Foto-WVL3J3YY.jpg');
	imgs.add('http://www.ibiblio.org/wm/paint/auth/gogh/starry-night/gogh.starry-night.jpg');
	imgs.add('http://www.blogcdn.com/de.engadget.com/media/2008/08/phone-box-to-play2.jpg');
	imgs.add('http://www.e-leseratte.de/wp-content/uploads/2009/04/braille_book2.jpg');

	navigator.camera = new Object();
	navigator.camera.getPicture = function (success, error, options){
		var img = imgs.items[parseInt(Math.random()*imgs.items.length)];
		var width = (options.targetWidth) ? options.targetWidth : img.width;
		var height = (options.targetHeight) ? options.targetHeight : img.height;
		can.width = width;
		can.height = height;
		ctx.drawImage(img, 0, 0, width, height);
		
		success(can.toDataURL().substring(22));
	}
	
	// Create 'navigator.geolocating' dummy system
	navigator.geolocation = new Object();
	
	var pos = { timestamp : new Date().getTime(), coords : {
		latitude : 49.62, longitude : 11.01, altitude : 400,
		accuracy : 10, altitudeAccuracy : 2, heading : 0, speed : 0 } };
	
	navigator.geolocation.dummySpeed = 0.0003;
	navigator.geolocation.dummyHeading = Math.round(Math.random() * 360);
	navigator.geolocation.dummyChangeHeadingInterval = 5000;
	navigator.geolocation.dummyChangeHeading = function(){
		navigator.geolocation.dummyHeading = Math.round(Math.random() * 360);
		setTimeout("navigator.geolocation.dummyChangeHeading()", navigator.geolocation.dummyChangeHeadingInterval);
	}
	navigator.geolocation.dummyChangeHeading();
	
	navigator.geolocation.getCurrentPosition = function(onsuccess, onerror, options){
		pos.coords.latitude -= Math.random() * 0.001;
		pos.coords.longitude -= Math.random() * 0.001;
		onsuccess(pos);
	};	
	
	navigator.geolocation.watchPosition = function(onsuccess, onerror, options){
		navigator.geolocation.watcher(onsuccess, onerror, options);
		return Math.floor(Math.random() * 1000);
	};
	
	navigator.geolocation.watcher = function(onsuccess, onerror, options){
		pos.coords.latitude -= Math.sin(navigator.geolocation.dummyHeading / (180 / Math.PI)) * navigator.geolocation.dummySpeed;
		pos.coords.longitude += Math.cos(navigator.geolocation.dummyHeading / (180 / Math.PI)) * navigator.geolocation.dummySpeed;
		pos.coords.heading = navigator.geolocation.dummyHeading;
		pos.timestamp = new Date().getTime();
		onsuccess(pos);
		
		var freq = (options && options.frequency) ? options.frequency : 3000;
		
		setTimeout(function(){ navigator.geolocation.watcher(onsuccess, onerror, options); }, freq);
	};
	
	navigator.standby = new Object();
	
	// Create 'navigator.standby' dummy system
	document.title = "NO_WAKELOCK";
	navigator.standby.alwaysOn = function(onsuccess, onerror) { document.title = "FULL_WAKELOCK"; onsuccess.call(); }
	navigator.standby.dimScreen = function(onsuccess, onerror) { document.title = "DIM_WAKELOCK"; onsuccess.call(); }
	navigator.standby.brightScreen = function(onsuccess, onerror) { document.title = "BRIGHT_WAKELOCK"; onsuccess.call(); }
	navigator.standby.cpuOnly = function(onsuccess, onerror) { document.title = "PARTIAL_WAKELOCK"; onsuccess.call(); }
	navigator.standby.allowStandby = function(onsuccess, onerror) { document.title = "NO_WAKELOCK"; onsuccess.call(); }
	navigator.standby.isScreenOn = function(onsuccess, onerror) { onsuccess.call(true); }
	
	// Create 'navigator.httprequest' dummy system
	navigator.httprequest = new Object();
	
	navigator.httprequest.post = function (url, data, onsuccess, onerror){
		var http = new XMLHttpRequest();
		http.open("POST", url, true);
		http.setRequestHeader("Content-length", data.length);
		http.setRequestHeader("Connection", "close");
		http.onreadystatechange = function() {
			if(http.readyState == 4) {
				if(http.status == 200)
					onsuccess(http.responseText);
				else
					onerror(http.status);
				
			}
		}
		http.send(data);
	}
	
	navigator.httprequest.get = function (url, onsuccess, onerror){
		var http = new XMLHttpRequest();
		http.open("GET", url, true);
		http.onreadystatechange = function() {
			if(http.readyState == 4) {
				if(http.status == 200)
					onsuccess(http.responseText);
				else
					onerror(http.status);
				
			}
		}
		http.send();
	}
	
	navigator.pinch = new Object();
	navigator.pinch.setListener = function(onsuccess, onerror){
	}
	
	// Create 'navigator.notification' dummy system
	navigator.notification = new Object();
	
	navigator.notification.vibrate = function(dur){
		var oldTitle = document.title;
		document.title = 'VIBRATE';
		setTimeout(function(){ document.title = oldTitle; }, dur);
	}
	
	navigator.notification.confirm = function(mess, callback, title, options){
		alert("Title: "+title+"\nMessage: "+mess);
		callback.call(0);
	}
	
	// Create 'navigator.bluetooth' dummy system
	navigator.bluetooth = new Object();
	navigator.bluetooth.isOn = false;
	
	navigator.bluetooth.start = function(onsuccess, onerror){
		navigator.bluetooth.isOn = true;
		onsuccess.call();
	}
	
	navigator.bluetooth.stop = function(onsuccess, onerror){
		navigator.bluetooth.isOn = false;
		onsuccess.call();
	}
	
	navigator.bluetooth.state = function(onsuccess, onerror){
		onsuccess.call(navigator.bluetooth.isOn);
	}
	
	
	
	console.log('Phonegap dummy ready');
	setupApp();
}
