app.cameraIds = new Object();

/*
 * Start updating the camera list and repeat update after given interval
 */
function updateCameraListStart(interval){
	if(!interval) interval = app.cameraInterval;
	app.gpsInterval = interval;
	updateCameraList(app.pos.lat, app.pos.lng, app.cameraDist);
}

/*
 * [DEBUG] Update status of server transmission
 */
function updateCamerasInfo(state){
	state = (state) ? state : 0;
	var out = app.statusbar;
	switch(state){
		case 0: out.innerHTML = 'Uninitialized'; break;
		case 1: out.innerHTML = 'Loading'; break;
		case 2: out.innerHTML = app.camerasCount + ' cameras'; break;
		case 3: out.innerHTML = 'Transmission Failure'; break;
		case 4: out.innerHTML = 'Response Failure'; break;
		
		case 10: out.innerHTML = 'Sending'; break;
	}
}

/*
 * Retrieve list of cameras from server
 */
function updateCameraList(lat, lng, dist){
	updateCamerasInfo(1);
	
	var json = JSON.stringify({"request" : { "lat": lat, "lng": lng, "distance" : dist}  });
	
	navigator.httprequest.post(app.serverURL, json,
		function(ret) { parseCameraList(ret); },
		function(err) { updateCamerasInfo(3); alert("Error: "+err); }
	);
	
	setTimeout(function(){ updateCameraList(app.pos.lat, app.pos.lng, app.cameraDist) }, app.cameraInterval);
}

/*
 * Parse JSON list of cameras received from server
 */
function parseCameraList(input){
	var json;
	
	try
		{ json = JSON.parse(input); }
	catch(err)
		{ updateCamerasInfo(4); }
	
	var c = -1;
	for(i in json.responce.cameras){
		addCameraToList(json.responce.cameras[i].camera);	
		c = i;
	}
	app.camerasCount = parseInt(c) + 1;
	
	updateMarkers();
	
	updateCamerasInfo(2);
}

/*
 * Create markers for all cameras in the list who do not have one yet
 */
function updateMarkers(){
	for(id in app.cameraIds){
		var cam = app.cameraIds[id];
		if(!(cam.marker instanceof Marker)){
			cam.marker = new Marker(cam.lat, cam.lng, (cam.type == 2) ? app.icons.pin2 : app.icons.pin);
			cam.marker.el.cam = cam;
			cam.marker.el.onclick = function() { setCameraPopup(this.cam); };
			cam.marker.el.ontouchstart = function() { setCameraPopup(this.cam); };
			//cam.marker.el.innerHTML = cam.id;
		//~ cam.line = new google.maps.Polyline({
			//~ path: new Array(new google.maps.LatLng(cam.lat, cam.lng), app.pos.latlng),
			//~ strokeColor: "#FF0000",
			//~ strokeOpacity: 1.0,
			//~ strokeWeight: 1,
			//~ map: app.map
		//~ });
		}
	}
}

/*
 * Add a single camera in server JSON format to internal list
 */
function addCameraToList(cam){
	if(app.cameraIds[cam.id])
		cam.marker = app.cameraIds[cam.id].marker;
	app.cameraIds[cam.id] = cam;
}

/*
 * Delete a single camera provided as reference from internal lists
 */
function deleteCameraFromList(cam){
	cam.marker.remove();
	delete(app.cameraIds[cam.id]);
}

/*
 * Send new camera at current position to server
 */
function sendCamera(type){
	if(app.pos.source != 2 || app.pos.time < new Date().getTime() - 45000)
		alert("Keine aktuelle GPS Position vorhanden.");
	else{
		updateCamerasInfo(10);
		
		
		
		app.cameraTemp = { "lat": Math.round(app.pos.lat * 10000) / 10000, "lng": Math.round(app.pos.lng * 10000) / 10000, "type" : type, "name" : ((type = 2) ? 'm' : 's')+'Blitz', "notes" : "" };
		
		var json = JSON.stringify({ "request" :  app.cameraTemp });
		
		navigator.httprequest.post(app.serverURL+'/add', json,
			function(ret) {
				if (parseCameraAdd(ret)){
					updateCameraList(app.pos.lat, app.pos.lng, app.cameraDist);
					delete(app.cameraTemp);
				}
			},
			function(err){
				updateCamerasInfo(3);
				alert('Blitzer konnte nicht gesendet werden.');
			}
		);
	}
}

/*
 * Parse server response after trying to add camera to server
 */
function parseCameraAdd(input){
	var json;
		
	try
		{ json = JSON.parse(input); }
	catch(err){
		updateCamerasInfo(4);
		alert('Blitzer konnte nicht gesendet werden.');
		return false;
	}
	
	if(json.responce.success == 1){
		alert('Blitzer gesendet!');
		return true;
	}else{
		alert('Blitzer konnte nicht gesendet werden.');
		return false;
	}
}

/*
 * Send a request for removal of a specific camera to the server
 */
function deleteCamera(cam){
	if(!cam.id)
		alert("Kann Blitzer noch nicht loeschen!");
	else{
		updateCamerasInfo(10);
		
		var json = JSON.stringify({ "request" : { "id" : cam.id } });
		
		navigator.httprequest.post(app.serverURL+'/delete', json,
			function(ret) {
				if(parseCameraDelete(ret))
					deleteCameraFromList(cam);
			},
			function(err){
				updateCamerasInfo(3);
				alert('Blitzer konnte nicht gesendet werden.');
			}
		);
	}
}

/*
 * Start updating the camera list and repeat update after given interval
 */
function parseCameraDelete(input){
	var json;
		
	try
		{ json = JSON.parse(input); }
	catch(err){
		updateCamerasInfo(4);
		alert('Blitzer konnte nicht geloescht werden.');
		return false;
	}
	
	if(json.responce.success == 1){
		alert('Blitzer geloescht!');
		return true;
	}else{
		alert('Blitzer konnte nicht gesendet werden.');
		return false;
	}
}
