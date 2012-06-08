navigator.map = new Object();

navigator.map.init = function(parent, lat, lng, zoom){
	navigator.map.el = parent;
	navigator.map.setZoom(zoom);
	navigator.map.panTo(lat,lng);
}

/*
 * Initialize OpenStreetMap layer
 */
navigator.map.initOpenStreetMap = function(){
	navigator.map.osmViewport = document.createElement('div');
	navigator.map.osmViewport.style.width = '100%';
	navigator.map.osmViewport.style.height = '100%';
	navigator.map.osmViewport.style.position = 'absolute';
	navigator.map.osmViewport.setAttribute('id', 'osm');
	navigator.map.el.appendChild(navigator.map.osmViewport);
	
	navigator.map.osm = new OpenLayers.Map('osm');
    navigator.map.osm.addLayer(new OpenLayers.Layer.OSM());
    //navigator.map.osm.zoomToMaxExtent(navigator.map.zoom);
    navigator.map.osm.panTo(new OpenLayers.LonLat(navigator.map.lng, navigator.map.lat), navigator.map.zoom);
}

/*
 * Initialize overlay layer on top of map layer
 */
navigator.map.initOverlay = function(){
	navigator.map.overlayViewport = document.createElement('div');
	navigator.map.overlayViewport.setAttribute('class', 'mark_overlay');
	navigator.map.overlayViewport.style.width = '100%';
	navigator.map.overlayViewport.style.height = '100%';
	navigator.map.overlayViewport.style.overflow = 'hidden';
	
	navigator.map.overlay = document.createElement('div');
	navigator.map.overlay.style.position = 'absolute';
	navigator.map.overlayViewport.appendChild(navigator.map.overlay);

	navigator.map.overlay.onmousedown = function(e){
		navigator.map.lastEvent = new Date().getTime();
		
		navigator.map.dragX = e.screenX;
		navigator.map.dragY = e.screenY;
		document.onmousemove = function(e){
			navigator.map.lastEvent = new Date().getTime();
			
			var rx = navigator.map.dragX - e.screenX;
			var ry = navigator.map.dragY - e.screenY;
			navigator.map.panBy(rx, ry);
			
			navigator.map.dragX = e.screenX;
			navigator.map.dragY = e.screenY;
		}
	};
	
	navigator.map.overlay.onmouseup = function(e){	
		document.onmousemove = null;
	};
	
	navigator.map.overlay.onmousewheel = function(e){
		if(e.wheelDeltaY > 0) navigator.map.setZoom(navigator.map.zoom + 1);
		if(e.wheelDeltaY < 0) navigator.map.setZoom(navigator.map.zoom - 1);
	};
	
	
	navigator.map.overlay.ontouchstart = function(e){	
		navigator.map.lastEvent = new Date().getTime();
		// TouchMove hack for Android
		if( navigator.userAgent.match(/Android/i) ) e.preventDefault();
		
		navigator.map.dragX = e.touches[0].screenX;
		navigator.map.dragY = e.touches[0].screenY;
		document.ontouchmove = function(e){
			navigator.map.lastEvent = new Date().getTime();
			
			var rx = navigator.map.dragX - e.touches[0].screenX;
			var ry = navigator.map.dragY - e.touches[0].screenY;
			navigator.map.panBy(rx, ry);
			
			navigator.map.dragX = e.touches[0].screenX;
			navigator.map.dragY = e.touches[0].screenY;
			
			if(e.touches[1]){
				app.infobox.innerHTML = "pinch";
			}
		}
	};
	
	navigator.map.overlay.ontouchend = function(e){	
		document.ontouchmove = null;
	};
	
	
	navigator.map.update();
	
	navigator.map.el.appendChild(navigator.map.overlayViewport);
}

/*
 * Marker class for maps overlay
 */
Marker = function(lat, lng, icon){
	this.lat = lat;
	this.lng = lng;
	this.icon = icon;
	
	this.el = document.createElement('div');
	this.el.style.position = 'absolute';
	this.el.marker = this;
	
	this.margin = (navigator.map.markerMargin) ? navigator.map.markerMargin : 0;
	
	if(typeof(this.icon) != 'undefined'){
		this.el.setAttribute('class', this.icon.className);
		//this.el.style.border = '1px dotted black';
	}else{
		this.el.style.width = (margin*2)+'px';
		this.el.style.height = (margin*2)+'px';
		this.el.style.border = '2px solid blue';
		this.el.style.borderRadius = '3px';
	}
	
	this.update();
	navigator.map.overlay.appendChild(this.el);
}

/*
 * Update marker's pixel coordinates in overlay container using  current latitude and longitude
 */
Marker.prototype.update = function(){
	this.y = navigator.map.latToPixel(this.lat);
	this.x = navigator.map.lngToPixel(this.lng);
	
	this.el.style.top = (this.y - this.icon.centerY + this.margin)+'px';
	this.el.style.left = (this.x - this.icon.centerX + this.margin)+'px';
	if(this.el.style.zIndex <= 999999999) this.el.style.zIndex = this.y;
};

/*
 * Remove marker and delete div
 */
Marker.prototype.remove = function(){
	this.el.parentNode.removeChild(this.el);
	delete(this);
};

/*
 * Icon class for maps overlay
 */
Icon = function(className, cy, cx){
	this.className = 'marker' + className;
	this.centerY = (cy) ? cy : 0;
	this.centerX = (cx) ? cx : 0;
}

/*
 * Convert latitude to y-coordinate in overlay container. Takes current zoom into account.
 */
navigator.map.latToPixel = function(lat){
	return Math.round(navigator.map.fullPixels / 2 - (navigator.map.fullPixels / 2 / Math.PI) * Math.log((1 + Math.sin(lat * Math.PI / 180)) / (1 - Math.sin(lat * Math.PI / 180))) / 2);
}

/*
 * Convert longitude to x-coordinate in overlay container. Takes current zoom into account.
 */
navigator.map.lngToPixel = function(lng){
	return Math.round(navigator.map.fullPixels / 360 * lng);
}

/*
 * Distance between two points
 */
navigator.map.distance = function(lat1 , lng1, lat2, lng2){
	var km = 40000;
	lat1 = Math.round(km / 2 - (km / 2 / Math.PI) * Math.log((1 + Math.sin(lat1 * Math.PI / 180)) / (1 - Math.sin(lat1 * Math.PI / 180))) / 2);
	lng1 = km / 360 * lng1;
	lat2 = Math.round(km / 2 - (km / 2 / Math.PI) * Math.log((1 + Math.sin(lat2 * Math.PI / 180)) / (1 - Math.sin(lat2 * Math.PI / 180))) / 2);
	lng2 = km / 360 * lng2;
	return Math.sqrt( ((lat1 - lat2) * (lat1 - lat2)) + ((lng1 - lng2) * (lng1 - lng2)) );
}

/*
 * Set zoom for map and overlay
 */
navigator.map.setZoom = function(zoom){
	if(zoom < 1 || zoom > 18 || (navigator.map.gmap && (zoom > navigator.map.gmap.maxZoom || zoom < navigator.map.gmap.minZoom))) return;
	if(zoom) navigator.map.zoom = zoom;
	
	navigator.map.update();
	
	if(navigator.map.gmap)
		navigator.map.gmap.setZoom(zoom);
		
	if (typeof(navigator.map.onzoom) == 'function')
		navigator.map.onzoom.call(navigator.map, zoom);
}

/*
 * Pan map and overlay by given number of pixels
 */
navigator.map.panBy = function(rx, ry){
	if(navigator.map.overlay) {
		navigator.map.x += rx;
		navigator.map.y += ry;
		navigator.map.overlay.style.top = (-navigator.map.y + (navigator.map.el.offsetHeight / 2))+'px';
		navigator.map.overlay.style.left = (-navigator.map.x + (navigator.map.el.offsetWidth / 2))+'px';
	}
	if(navigator.map.gmap)
		navigator.map.gmap.panBy(rx, ry);
}

/*
 * Pan map and overlay to the given coordinates
 */
navigator.map.panTo = function(lat, lng){
	navigator.map.lat = lat;
	navigator.map.lng = lng;
	
	navigator.map.y = navigator.map.latToPixel(lat);
	navigator.map.x = navigator.map.lngToPixel(lng);

	if(navigator.map.overlay) {	
		navigator.map.overlay.style.top = (-navigator.map.y + (navigator.map.el.offsetHeight / 2))+'px';
		navigator.map.overlay.style.left = (-navigator.map.x + (navigator.map.el.offsetWidth / 2))+'px';
	}
	if(navigator.map.gmap)
		navigator.map.gmap.panTo(new google.maps.LatLng(lat, lng));
}

/*
 * 
 */
navigator.map.update = function(){
	navigator.map.fullPixels = Math.pow(2, navigator.map.zoom + 8);
	
	if(navigator.map.overlay) {
		//console.log(navigator.map.overlayViewport);
		//navigator.map.overlayViewport.style.width = navigator.map.overlayViewport.parentNode.offsetWidth+'px';
		//navigator.map.overlayViewport.style.height = navigator.map.overlayViewport.parentNode.offsetHeight+'px';	
		
		navigator.map.overlay.style.width = navigator.map.fullPixels+'px';
		navigator.map.overlay.style.height = navigator.map.fullPixels+'px';
	
		for(var i = 0; i < navigator.map.overlay.childNodes.length; i++){
			if(navigator.map.overlay.childNodes[i].marker)
				navigator.map.overlay.childNodes[i].marker.update();
		}
	}
	
	navigator.map.panTo(navigator.map.lat, navigator.map.lng);
}
