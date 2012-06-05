Ext.Loader.setConfig({ enabled: true });

Ext.application({
    name: 'app',
    controllers: ['service', 'setup', 'trips'],
    
    
    launch: function() {
		
		app.view.Bluetooth = Ext.create('app.view.bluetooth');
		app.view.History = Ext.create('app.view.history');
		app.view.Mainmenu = Ext.create('app.view.mainmenu');
		app.view.Trip = Ext.create('app.view.trip');
		app.view.Map = Ext.create('app.view.map');
		app.view.Btpin = Ext.create('app.view.btpin');
		app.view.Terms = Ext.create('app.view.terms');
		
		//Ext.Viewport.setActiveItem(app.view.History);
		Ext.Viewport.setActiveItem(app.view.Mainmenu);
		//Ext.Viewport.setActiveItem(app.view.Bluetooth);
		
   },
   init: function() {
		app.version = 'PROTO 11/01/11';
		
		navigator.service.start();
		
		Date.prototype.months = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
		
		Date.prototype.monthsLong = new Array("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
		
		Date.prototype.getMonthDesc = function(){
			return this.months[this.getMonth()];
		};
		
		Date.prototype.getMonthDescription = function(){
			return this.monthsLong[this.getMonth()];
		};
		
		Date.prototype.getUSTime = function(){
			return Math.floor(this.getHours() % 12) + ':' + ((this.getMinutes() < 10)  ? '0' : '') + this.getMinutes() + '<small> ' + ((this.getHours() / 12 < 0) ? 'AM' : 'PM') + '</small>';
		};
		
		Date.prototype.getUSDate = function(){
			return ((this.getMonth() < 10) ? '0' : '') + this.getMonth() + '/' + ((this.getDate() < 10)  ? '0' : '') + this.getDate() + '/' + this.getFullYear();
		};	
   }
});

Ext.onReady(function() {
	function loadScript() {
		var script = document.createElement("script");
		script.type = "text/javascript";
		script.src = "http://maps.googleapis.com/maps/api/js?sensor=false&callback=initialize";
		script.onload = function() { app.googlemapsLoaded = true; };
		document.body.appendChild(script);
	}

	window.setTimeout( function() {
		loadScript();
	}, 1000 );
	
});

function initialize() {
};

