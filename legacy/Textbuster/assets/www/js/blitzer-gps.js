/*
 * Check for cameras in the current vicinity, producing an alarm depending on the proximity of the nearest camera.
 */
function checkCameras(){
	var clDist = Math.pow(2, 28);
	var clCam;
	for(i in app.cameraIds){
		var cam = app.cameraIds[i];
		var dist = Math.sqrt(Math.pow(app.pos.x - cam.marker.x, 2) + Math.pow(app.pos.y - cam.marker.y, 2));
		if(dist < clDist){
			clDist = dist;
			clCam = cam;
		}
	}
	
	clDist = Math.round(clDist * (40008000 / navigator.map.fullPixels));
	
	if(clDist < 200){
			warning.style.display = "block";
			warning.style.background = "red";
			warning.innerHTML = Math.floor(clDist)+' m';
			navigator.notification.vibrate(100);
			app.lastCameraId = cam.id;
	}else if(clDist < 2000){
			warning.style.display = "block";
			warning.style.background = "yellow";
			warning.innerHTML = (Math.floor(clDist / 100) / 10)+' km';
			if(app.lastCameraId != cam.id){
				navigator.notification.vibrate(500);
				app.lastCameraId = cam.id;
			}
	}else{
		app.lastCameraId = null;
		warning.style.display = "none";
	}
	
	setTimeout(function(){ checkCameras() }, app.proximityCheckInterval);
}

/*
 * Start updating the position and repeat update after given interval.
 */
function updatePositionStart(interval){
	app.gpsTimeDiff = 0;
	
	if(!interval) interval = app.gpsInterval;
	app.gpsInterval = interval;
	navigator.geolocation.watchPosition(updatePositionSuccess, updatePositionError, {
			frequency: app.gpsInterval,
			timeout: app.gpsTimeout,
			maximumAge: 10000,
			enableHighAccuracy: true
	} );
}

/*
 * On receiving new position
 */
function updatePositionSuccess(position){
	var lat = position.coords.latitude;
	var lng = position.coords.longitude;

	app.marker.lat = lat;
	app.marker.lng = lng;
  
	app.marker.update();
  
	var gpsTime = new Date(position.timestamp).getTime();
	
	app.pos.state = "OK";
	app.pos.source = 2;
	app.pos.lat = lat;
	app.pos.lng = lng;
	app.pos.acc = position.coords.accuracy;
	app.pos.alt = position.coords.altitude;
	app.pos.y = navigator.map.latToPixel(lat);
	app.pos.x = navigator.map.lngToPixel(lng);
	app.pos.aacc = position.coords.altitudeAccuracy;
	app.pos.head = position.coords.heading;
	app.pos.spd = position.coords.speed;
	app.pos.time = gpsTime;
	app.pos.aqc = position.acquracy
	app.gpsTimeDiff = new Date().getTime() - gpsTime;
	
	window.localStorage.setItem("app.pos.lat", lat);
	window.localStorage.setItem("app.pos.lng", lng);
	window.localStorage.setItem("app.pos.time", gpsTime);
	
	updateInfobox();
}

function centerPosition(){
	if(new Date().getTime() > navigator.map.lastEvent + app.recenterDelay){
		var offCenterY =  navigator.map.latToPixel(app.pos.lat) - navigator.map.y;
		var offCenterX =  navigator.map.lngToPixel(app.pos.lng) - navigator.map.x;
		if ((Math.abs(offCenterX) > navigator.map.el.offsetWidth / app.recenterTolerance) || (Math.abs(offCenterY) > navigator.map.el.offsetHeight / app.recenterTolerance)){
				navigator.map.panTo(app.pos.lat, app.pos.lng);
		}	
		navigator.map.lastEvent = new Date().getTime();
	}
	setTimeout(centerPosition, 1000);
}

function updatePositionError(error) {
	app.pos.state = "ERR "+error.code+" ("+error.message+")";					
	updateInfobox();
}

/*
 * On receiving new position
 */
function updateInfobox(){
	var i = "State: "+app.pos.state+"<br>"+
			"Source:"+((app.pos.source == 2) ? 'Live' : (app.pos.source == 1) ? 'Storage' : 'Default')+"<br>"+
			"Lat:   "+app.pos.lat+"<br>"+
			"Lng:   "+app.pos.lng+"<br>"+
			"Alt:   "+app.pos.alt+"<br>"+
			"Speed: "+app.pos.spd+"<br>"+
			"Head:  "+app.pos.head+"<br>"+
			"Acc:   "+app.pos.acc+", "+app.pos.aacc+"<br>"+
			"Time:  "+Math.floor(app.pos.time / 1000)+"<br>"+
			"Last:  "+Math.ceil((new Date().getTime() - app.pos.time - app.gpsTimeDiff) / 1000)+" secs<br>";						
	infobox.innerHTML = i.replace(/\s/g, '&nbsp;');
	setTimeout(updateInfobox, 1000);
}
