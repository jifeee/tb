Ext.define('app.model.Position', {
    extend : 'Ext.data.Model',
    
    fields: [
        {name: 'time', type: 'int'},
        {name: 'lat',  type: 'float'},
        {name: 'lng',  type: 'float'},
    ],
    
    toString : function(){
		if (this.data.description){
			return this.data.description;
		} else if (this.data.string){
			this.descDecoded();
			return this.data.string;
		} else {
			this.descDecoded();
			this.data.string = '<span class="latlng">' + Math.abs(this.get('lat')).toFixed(4) + '&deg;' + ((this.get('lat') > 0) ? 'N ' : 'S ') + Math.abs(this.get('lng')).toFixed(4) + '&deg;' + ((this.get('lat') > 0) ? 'E' : 'W') + '</span>';
			return this.data.string;
		}
	},
	
	descDecoded : function(){
		if(app.geocoderQueueOverLimit)
			return;
		
		if(app.geocoderQueueBusy > 5)
			window.setTimeout( function() { if(app.geocoderWorker) app.geocoderWorker(); }, 1000);
			
		if(!app.geocoderQueue)
			app.geocoderQueue = new Array(this);
		else 
			if (app.geocoderQueue.indexOf(this) == -1)
				app.geocoderQueue.push(this);
		
		if(!app.geocoderQueueWorker){
			app.geocoderQueueWorker = function() {
				
				var pos = app.geocoderQueue[app.geocoderQueue.length - 1];
				
				if(!pos) {
					app.geocoderQueueWorker = null;
				}else{
					req = new XMLHttpRequest();
					req.abort();
					req.onreadystatechange = function() {
						if (req.readyState == 4){						
							app.geocoderQueueBusy--;
						
							var resp = JSON.parse(req.responseText);
							var addr = resp.address;
							
							var desc = '';
							
							if(addr.road) desc += addr.road + '.';
							if(addr.house_number) desc += ' ' + addr.house_number;
							if(desc.length == 0 && addr.hamlet) desc += addr.hamlet;
							if(desc.length > 0) desc += ', ';
							if(addr.postcode) desc += addr.postcode;
							if(addr.city) desc += ' ' + addr.city;
							else if(addr.county) desc += ' ' + addr.county;
							
							//if(desc.length > 30) desc = desc.substring(0, 32) + '...';
							
							app.geocoderQueue[app.geocoderQueue.length - 1].set('description', desc);
							
							app.geocoderQueue.pop();
							if (app.geocoderQueueWorker) app.geocoderQueueWorker();
							
							//Ext.getCmp('historyList').refresh();
							app.view.Mainmenu.updateLasttrip();
						}
					};
					
					app.geocoderQueueBusy++;
					//req.open('GET', 'http://maps.googleapis.com/maps/api/geocode/json?latlng=' + pos.get('lat') + ',' + pos.get('lng') + '&sensor=false', true);
					//req.open('GET', 'http://nominatim.openstreetmap.org/reverse?format=xml&lat=' + pos.get('lat') + '&lon=' + pos.get('lng') + '&zoom=16&addressdetails=1&format=json', true);
					req.open('GET', 'http://open.mapquestapi.com/nominatim/v1/reverse?format=xml&lat=' + pos.get('lat') + '&lon=' + pos.get('lng') + '&zoom=18&addressdetails=1&format=json', true);
					req.send();
				}
				
			};
			app.geocoderQueueWorker();
		}
	}
	
} );

	// Google Maps Geocoding
	// ---------------------------------------------------------------------
	//~ if(resp.status == 'OK'){
		//~ var addr = new Object();
	//~ 
		//~ for(var i = 0; i < resp.results[0].address_components.length; i++){
			//~ var acmp = resp.results[0].address_components[i];
			//~ addr[acmp.types[0]] = acmp.short_name;
		//~ }
		//~ 
		//~ administrative_area_level_1: "BY"
		//~ administrative_area_level_2: "BT"
		//~ country: "DE"
		//~ locality: "Seybothenreuth"
		//~ postal_code: "95517"
		//~ route: "Wallenbrunn"
		//~ street_number: "1"
		//~ sublocality: "Wallenbrunn"
		//~ 
		//~ var desc = '';
		//~ 
		//~ if(addr.route) desc += addr.route;
		//~ if(addr.street_number) desc += ' ' + addr.street_number;
		//~ if(desc.length > 0) desc += ', ';
		//~ if(addr.administrative_area_level_1) desc += addr.administrative_area_level_1 + ' ';
		//~ if(addr.postal_code) desc += addr.postal_code;
		//~ 
		//~ //console.log(desc + ' - ' + resp.results[0].formatted_address);
	//~ 
		//~ if(desc.length < 30) 
			//~ app.geocoderQueue[0].set('description', desc);
		//~ else
			//~ app.geocoderQueue[0].set('description', desc.substring(0, 27) + '...');
		//~ app.geocoderQueue[0].set('description', desc);
	//~ }else if(resp.status == 'OVER_QUERY_LIMIT'){
		//~ app.geocoderQueueOverLimit = true;
	//~ }
