var distanceWidgets = new Array;
var map;

﻿/**
 * A distance widget that will display a circle that can be resized and will
 * provide the radius in km.
 *
 * @param {google.maps.Map} map The map to attach to.
 *
 * @constructor
 */
function DistanceWidget(map,latlng,rad,id) {
  this.set('map', map);
  this.set('position', latlng);
  this.set('id', id);
  
  var marker = new google.maps.Marker({
  	icon: new google.maps.MarkerImage("/images/pin.png", null, null, new google.maps.Point(15, 28)),
    draggable: true,
    title: 'Move me!'
  });
  
  // make marker accessible from the outside
  this.set('marker', marker);
  google.maps.event.addListener(marker, 'click', function() {
    setActiveWidget(id);
  });
  // Bind the marker map property to the DistanceWidget map property
  marker.bindTo('map', this);

  // Bind the marker position property to the DistanceWidget position property
  marker.bindTo('position', this);

  // Create a new radius widget
  var radiusWidget = new RadiusWidget(rad, id, marker);

  // Bind the radiusWidget map to the DistanceWidget map
  radiusWidget.bindTo('map', this);

  // Bind the radiusWidget center to the DistanceWidget position
  radiusWidget.bindTo('center', this, 'position');

  // Bind to the radiusWidgets' distance property
  this.bindTo('distance', radiusWidget);

  // Bind to the radiusWidgets' bounds property
  this.bindTo('bounds', radiusWidget);

  this.remove = function() { radiusWidget.remove(); }
}

DistanceWidget.prototype = new google.maps.MVCObject();

DistanceWidget.prototype.setAsActive = function(set) {
  if (set) 
    this.get('marker').setIcon(new google.maps.MarkerImage("/images/pin-green.png", null, null, new google.maps.Point(15, 28)));
  else 
    this.get('marker').setIcon(new google.maps.MarkerImage("/images/pin.png", null, null, new google.maps.Point(15, 28)));
}

/**
 * A radius widget that add a circle to a map and centers on a marker.
 *
 * @constructor
 */
function RadiusWidget(rad, id) {
  var circle = new google.maps.Circle({
    fillColor: '#ffffff',
    fillOpacity: 0.5,
    strokeColor: '#0090ff',
    strokeOpacity: 1,
    strokeWeight: 1
  });

  // Set the distance property value, default to 50km.
  this.set('distance', rad?rad:50);

  // Bind the RadiusWidget bounds property to the circle bounds property.
  this.bindTo('bounds', circle);
  
  // Bind DistanceWidget id to RadiusWidget id 
  this.set('id', id);
  
  // Bind the circle center to the RadiusWidget center property
  circle.bindTo('center', this);
  
  // Bind the circle map to the RadiusWidget map
  circle.bindTo('map', this);

  // Bind the circle radius property to the RadiusWidget radius property
  circle.bindTo('radius', this);

  // Add the sizer marker
  this.addSizer_();
  
  var me = this;
  google.maps.event.addListener(circle, 'click', function() {
    setActiveWidget(me.get('id'));
  });
  
  this.remove = function() { circle.setMap(null); }
}
RadiusWidget.prototype = new google.maps.MVCObject();

/**
 * Update the radius when the distance has changed.
 */
RadiusWidget.prototype.distance_changed = function() {
  this.set('radius', this.get('distance') * 1000);
};


/**
 * Add the sizer marker to the map.
 *
 * @private
 */
RadiusWidget.prototype.addSizer_ = function() {
  var sizer = new google.maps.Marker({
  	icon:  new google.maps.MarkerImage("/images/resize.png", null, null, new google.maps.Point(8, 8)),
    draggable: true,
    raiseOnDrag: false,
    title: 'Drag me!'
  });

  sizer.bindTo('map', this);
  sizer.bindTo('position', this, 'sizer_position');
  
  sizer.test = "test"

  var me = this;
  google.maps.event.addListener(sizer, 'drag', function() {
    // Set the circle distance (radius)
    stopPos = me.setDistance(sizer);
  });
  google.maps.event.addListener(sizer, 'dragend', function() {
    
  	sizer.setPosition( new google.maps.LatLng(document.getElementById('sizerLat_' + me.get('id')).value,
  	                                          document.getElementById('sizerLng_' + me.get('id')).value) )
  });
};


/**
 * Update the center of the circle and position the sizer back on the line.
 *
 * Position is bound to the DistanceWidget so this is expected to change when
 * the position of the distance widget is changed.
 */
RadiusWidget.prototype.center_changed = function() {
  var bounds = this.get('bounds');

  // Bounds might not always be set so check that it exists first.
  if (bounds) {
    var lng = bounds.getNorthEast().lng();

    // Put the sizer at center, right on the circle.
    var position = new google.maps.LatLng(this.get('center').lat(), lng);
    this.set('sizer_position', position);
  	document.getElementById('sizerLat_' + this.get('id')).value = position.lat()
  	document.getElementById('sizerLng_' + this.get('id')).value = position.lng()
  }
};


/**
 * Calculates the distance between two latlng points in km.
 * @see http://www.movable-type.co.uk/scripts/latlong.html
 *
 * @param {google.maps.LatLng} p1 The first lat lng point.
 * @param {google.maps.LatLng} p2 The second lat lng point.
 * @return {number} The distance between the two points in km.
 * @private
 */
RadiusWidget.prototype.distanceBetweenPoints_ = function(p1, p2) {
  if (!p1 || !p2) {
    return 0;
  }

  var R = 6371; // Radius of the Earth in km
  var dLat = (p2.lat() - p1.lat()) * Math.PI / 180;
  var dLon = (p2.lng() - p1.lng()) * Math.PI / 180;
  var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(p1.lat() * Math.PI / 180) * Math.cos(p2.lat() * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  var d = R * c;
  
  return d;
};


/**
 * Set the distance of the circle based on the position of the sizer.
 */
RadiusWidget.prototype.setDistance = function(sizer) {
  // As the sizer is being dragged, its position changes.  Because the
  // RadiusWidget's sizer_position is bound to the sizer's position, it will
  // change as well.
  var pos = this.get('sizer_position');
  var center = this.get('center');
  var distance = this.distanceBetweenPoints_(center, pos);

  if ( distance<0.1 ){
  	distance=0.1;
  }else if(distance>500){
  	distance=500;
  }
  
  if ( distance>0.1 && distance<500 ){
  	document.getElementById('sizerLat_' + this.get('id')).value = pos.lat()
  	document.getElementById('sizerLng_' + this.get('id')).value = pos.lng()
  }

  // Set the distance property for any objects that are bound to it
  this.set('distance', distance);
};

function init() {
  var mapDiv = document.getElementById('map-canvas');
  var initialLocation;
  var initZoom;
  
  map = new google.maps.Map(mapDiv, {
  	center: new google.maps.LatLng(0,0),
  	zoom: 0,
    disableDefaultUI: true,
    zoomControl: true,
    mapTypeId: google.maps.MapTypeId.ROADMAP
    });
  
  var divs = document.getElementsByClassName('data_container');
  
  for(var i=0; i<divs.length; i++)  {
    var id = /controls_(.+)/.exec(divs[i].id);
    if (id) createNewWidget(id[1]);
  }
  	
  if (distanceWidgets.length == 0) {
  	
	  if(navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(function(position) {
			  map.setCenter( new google.maps.LatLng(position.coords.latitude,position.coords.longitude) );
			  map.setZoom(8);
			});
		}
  } else {
    fitCirclesBounds(map);
  }
  
  addNewButton();
}
  
function distanceLog(widget){
  google.maps.event.addListener(widget, 'distance_changed', function() {
    displayInfo(widget);
  });
  google.maps.event.addListener(widget, 'position_changed', function() {
    displayInfo(widget);
  });
}


function displayInfo(widget) {
  document.getElementById('placeLat_' + widget.get('id')).value = widget.get('position').lat();
  document.getElementById('placeLng_' + widget.get('id')).value = widget.get('position').lng();
  document.getElementById('placeRad_' + widget.get('id')).value = widget.get('distance');
}

function setActiveWidget(id) {
  for (var i = 0; i < distanceWidgets.length; i++)
    distanceWidgets[i].setAsActive(distanceWidgets[i].get('id') == id);
  document.getElementById('active_widget').setAttribute('value',id);
  if (id) addRemoveButton();
  else {
    var button = document.getElementById('remove_btn');
    if(button) button.parentNode.removeChild(button); }
}

function fitCirclesBounds(map) {
  var bounds = new google.maps.LatLngBounds();
  for(var i=0; i<distanceWidgets.length; i++) {
    var b = distanceWidgets[i].get('bounds')
    // Bounds might not always be set so check that it exists first.
    if(b) {
      bounds.extend(b.getNorthEast());
      bounds.extend(b.getSouthWest());
    }
  }
  map.fitBounds(bounds);
}

function addRemoveButton() {
  if (document.getElementById('remove_btn')) return;
  var button = document.createElement("a"); 
    button.setAttribute('id','remove_btn');
    button.setAttribute('href','#');
    button.innerHTML = 'Remove active point';
    button.onclick = function() { removeActiveWidget(); return false; }
    document.getElementById('map-input').appendChild(button);
    
  function removeActiveWidget() {
    var id = document.getElementById('active_widget').getAttribute('value');

    // remove the circle
    for(var i = 0; i<distanceWidgets.length; i++) {
      if(distanceWidgets[i].get('id') == id) {
        distanceWidgets[i].remove();
        distanceWidgets.splice (i, 1);
        break
      }
    }
    // check _destroy checkbox
    document.getElementById('destroy_' + id).setAttribute("value", true);
    
    setActiveWidget();    
  }
}

function addNewButton() {
  var button = document.createElement("a"); 
    button.setAttribute('id','add_btn');
    button.setAttribute('href','#');
    button.setAttribute('style','margin-right:10px;');
    button.innerHTML = 'Add new point';
    button.onclick = function() { createNewPoint(); return false; }
    // document.getElementById('map-input').appendChild(button);    
    document.getElementById('map-actions').appendChild(button);    
    
    function createNewPoint() {
      var div = document.getElementById('controls_').cloneNode(true);
      var children = div.getElementsByTagName('input');
      var num = document.getElementsByClassName('data_container').length;
      
      div.setAttribute('id', div.id + num);
      for (var i = 0; i<children.length; i++) {
        children[i].setAttribute('id', children[i].id.replace(/\d+$/, num));
        children[i].setAttribute('name', children[i].name.replace(/attributes\]\[\d+/, 'attributes]['+num));
      }
      $('form #alert_submit').before($(div));

      document.getElementById('placeLat_' + num).setAttribute('value', map.getCenter().lat());
      document.getElementById('placeLng_' + num).setAttribute('value', map.getCenter().lng());
      document.getElementById('placeRad_' + num).setAttribute('value', 1);
      
      createNewWidget(num);
    }
}

function createNewWidget(id) {
  var saveRad = document.getElementById('placeRad_' + id).value;
  var saveLat = document.getElementById('placeLat_' + id).value;
  var saveLng = document.getElementById('placeLng_' + id).value;

  if( saveRad && saveLat && saveLng ){
 	
    var savedLL = new google.maps.LatLng(saveLat,saveLng);

    distanceWidgets.push(new DistanceWidget(map,savedLL,saveRad,id));
    distanceLog(distanceWidgets[distanceWidgets.length-1]);
  }
}

google.maps.event.addDomListener(window, 'load', init);
