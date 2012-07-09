var directionsDisplay;
var directionsService = new google.maps.DirectionsService();
var map;

function initMap() {
	
  var mapDiv = document.getElementById('map-canvas');
  var circles = new Array;
  
  map = new google.maps.Map(mapDiv, {
  	center: new google.maps.LatLng(0,0),
  	zoom: 1,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  });

  var divs = document.getElementsByClassName('data_container');
  for(var i=0; i<divs.length; i++)  {
    var id = /controls_(.+)/.exec(divs[i].id);
    if (id) createNewCircle(id[1]);
  }
  
  fitCirclesBounds();
  
  function createNewCircle(id) {
    var saveRad = document.getElementById('placeRad_' + id).value;
    var saveLat = document.getElementById('placeLat_' + id).value;
    var saveLng = document.getElementById('placeLng_' + id).value;

    if( saveRad && saveLat && saveLng ){
     	
      var savedLL = new google.maps.LatLng(saveLat,saveLng);

      circles.push(new google.maps.Circle({
        fillColor: '#ffffff',
        fillOpacity: 0.5,
        strokeColor: '#0090ff',
        strokeOpacity: 1,
        strokeWeight: 1,
        map: map,
        center: savedLL,
        radius: (parseFloat(saveRad) * 1000)
      }));
    }
  }
  
  function fitCirclesBounds() {
    var bounds = new google.maps.LatLngBounds();
    for(var i=0; i<circles.length; i++) {
      var b = circles[i].getBounds();
      bounds.extend(b.getNorthEast());
      bounds.extend(b.getSouthWest());
    }
    map.fitBounds(bounds);
  }

}

function calcRoute(start, end, stops) {
  directionsDisplay = new google.maps.DirectionsRenderer();
  directionsDisplay.setMap(map);
  directionsDisplay.setPanel(document.getElementById("directionsPanel"));

  var waypts = [];

  for (var i = 0; i < stops.length; i++) {
    waypts.push({
      location: new google.maps.LatLng(stops[i]['latitude'], stops[i]['longitude']),
      stopover:false
    });
  }

  var request = {
      origin: new google.maps.LatLng(start['latitude'], start['longitude']),
      destination: new google.maps.LatLng(end['latitude'], end['longitude']),
      waypoints: waypts,
      travelMode: google.maps.TravelMode.DRIVING
  };

  directionsService.route(request, function(response, status) {
    if (status == google.maps.DirectionsStatus.OK) {
      directionsDisplay.setDirections(response);
    } else {
      $('.google-map').html(status);
    }
  });
}

function showPoint(lat, lng) {
  var point = new google.maps.LatLng(lat, lng);
  new google.maps.Marker({
  	icon: new google.maps.MarkerImage("/images/pin.png", null, null, new google.maps.Point(15, 28)),
  	position: point,
    map: map
  });
  var bounds = new google.maps.LatLngBounds(point, point);
  map.fitBounds(bounds);
  
  var listener = google.maps.event.addListener(map, "idle", function() { 
    if (map.getZoom() > 10) map.setZoom(12); 
    google.maps.event.removeListener(listener); 
  });
}

google.maps.event.addDomListener(window, 'load', initMap);
