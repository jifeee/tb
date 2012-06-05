Ext.define('app.controller.trips', { 
	extend: 'Ext.app.Controller',
	
	views: ['mainmenu', 'history', 'trip', 'map'],
	
	stores: ['trips', 'positions'],
	
    init: function(){
		this.control({
			'#historyButton' : { tap : this.showHistory },
			'#moreButton' : { tap : function() { this.createDemo(); } },
			'#settingsButton' : { tap : function() { Ext.Viewport.setActiveItem(app.view.Bluetooth); } },
			'#historyBack' : { tap : this.backToMainmenu },
			'#dateChoice' : { tap : this.setListToDate },
			'#distChoice' : { tap : this.setListToMiles },
			'#historyList' : { itemtap : function(list, index, item) { this.showDetails(index); } },
			'#tripBack' : { tap : this.showHistory },
			'#tripMap' : { tap : this.showMap },
			'#trip'	   : { tap : function(e) { console.log(e) } },
			'#mapBack' : { tap : this.backToTrip },
		});

		var positions = Ext.getStore('positions');
		
		var trips = Ext.getStore('trips');
		
		trips.addListener('add', function(store, records) {

			for(i = 0; i < records.length; i++){
				var record = records[i];
				
				var start = (record.data.start + 60) * 1000;
				var end = (record.data.end + 60) * 1000;
				
				record.data.startDate = new Date(start);
				record.data.endDate = new Date(end);
				
				if(!record.data.startPos || !record.data.startPos){
					var pos = Ext.getStore('positions');
					
					pos.clearFilter(true);
				
					pos.filterBy( function(r) {
						return (r.data.time <= end && r.data.time >= start ); }
					);
				
					pos.sort('time', 'ASC');
					
					if(pos.count() > 0){
						record.data.startPos = pos.first();
						record.data.endPos = pos.last();
					} else {
						record.data.startPos = new Object( { fake : true, toString : function() { return '---'; }  } );
						record.data.endPos = new Object( { fake : true, toString : function() { return '---'; }  } );
					}
					
					var dist = 0;
					var latlng = pos.first();
					for(var i = 1; i < pos.count(); i++){
						dist += navigator.map.distance( latlng.data.lat, latlng.data.lng, pos.getAt(i).data.lat, pos.getAt(i).data.lng );
						
						latlng = pos.getAt(i);
					}
					
					if(dist > 0){
						record.data.distance = Math.floor(dist * 0.62137119 * 100) / 100;
						record.data.speedavg = Math.floor((dist * 0.62137119) / ((record.data.endDate.getTime() - record.data.startDate.getTime()) / 3600000));
					}
					
				}
			}
			
		});
		
		app.controller.trips = this;
	},
	
	showHistory : function() {
			Ext.Viewport.setActiveItem(app.view.History);
	},
	
	showMap : function() {
			if(!app.googlemapsLoaded){
				Ext.Msg.alert('Google Maps', 'The feature could not be initialized.');
				return;	
			}
			
			if(!app.googlemaps) {
				var myOptions = {
					zoom: 8,
					center: new google.maps.LatLng(-34.397, 150.644),
					mapTypeId: google.maps.MapTypeId.ROADMAP
				}
				app.googlemaps = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
			}
			
			if(app.googlemaps) {
				if(app.googlemapsPath){
					app.googlemapsPath.setPath([]);
					app.googlemapsPath.setMap(null);
				}
				
				var trip = app.view.Trip.trip;
				
				var pos = Ext.getStore('positions');
					
				pos.clearFilter(true);
				
				pos.sort('time', 'ASC');
				
				var start = (trip.data.start + 60) * 1000;
				var end = (trip.data.end + 60) * 1000;
				
				pos.filterBy( function(r) {	
					return (r.data.time <= end && r.data.time >= start ); }
				);
				
				var tripCoords = new Array();
				var bounds = new google.maps.LatLngBounds();
				
				pos.each( function(r) {
					var latlng = new google.maps.LatLng( r.get('lat'), r.get('lng') );
					
					tripCoords.push(latlng);
					bounds.extend(latlng);
				} );
				
				app.googlemaps.panTo(tripCoords[0]);
				
				app.googlemapsPath = new google.maps.Polyline({
					path: tripCoords,
					strokeColor: "#FF0000",
					strokeOpacity: 1.0,
					strokeWeight: 2
				});
				
				app.googlemapsPath.setMap(app.googlemaps);
				
				window.setTimeout(function() { app.googlemaps.fitBounds(bounds); }, 1);
			}
			
			Ext.Viewport.setActiveItem(app.view.Map);
	},
	
	showDetails : function(index) {
			app.view.Trip.setTrip(Ext.getStore('trips').getAt(index));
			Ext.Viewport.setActiveItem(app.view.Trip);
	},
	
	backToMainmenu : function() {
			Ext.Viewport.setActiveItem(app.view.Mainmenu);
	},
	
	backToTrip : function() {
			Ext.Viewport.setActiveItem(app.view.Trip);
	},
	
	setListToDate : function() {
		Ext.getStore('trips').sorter = { property : 'start', direction : 'DESC' };
		var list = Ext.getCmp('historyList');
		list.pageupText = 'Show newer trips';
		list.pagedownText = 'Show older trips';
		list.currentIndex = 0;
		list.refresh();
	},
	
	setListToMiles : function() {
		Ext.getStore('trips').sorter = { property : 'distance', direction : 'DESC' };
		var list = Ext.getCmp('historyList');
		list.pageupText = 'Show longer trips';
		list.pagedownText = 'Show shorter trips';
		list.currentIndex = 0;
		list.refresh();
	},
	
	createDemo : function() {
		var trips = Ext.getStore('trips');
		var positions = Ext.getStore('positions');
		
		var time = 1318553758;
		var vlat = 41.4205;
		var vlng = -74.5249;
		
		var randtrips = new Array();
		
		while( time < new Date().getTime() / 1000 ){
			randtrips.push( new Object( { start : time, end : Math.floor(time + Math.random() * 2000 + 500), distance : Math.random() * 50 } ) );
			time += Math.floor(Math.random() * 3600 * 24);
		}
		
		for(var i = 0; i < randtrips.length; i++){
			var trip = randtrips[i];
		
			var stime = trip.start;
			
			var vvlat = Math.random() - 0.5;
			var vvlng = Math.random() - 0.5;
			
			while(stime < trip.end) {
				vlat += vvlat * (Math.random() * 0.01);
				vlng += vvlng * (Math.random() * 0.01);
			
				positions.add( { lat : vlat, lng : vlng , time : stime * 1000 } );
				
				//break;
				stime += 30;
			}
		
			trips.add( randtrips[i] );
		}
		
		app.view.Mainmenu.updateLasttrip();
		Ext.getCmp('historyList').refresh();
	}
});
