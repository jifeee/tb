Ext.define('app.controller.setup', { 
	extend: 'Ext.app.Controller',
	
	views: ['bluetooth', 'btpin', 'terms'],
	
	stores: ['devices'],
	
    init: function(){
		this.control({
			//'#bluetoothOn' : { tap : this.startBluetooth },
			//'#bluetoothOff' : { tap : this.stopBluetooth },
			'#deviceList list' : { itemtap : this.selectDevice },
			'#deviceScan' : { tap : this.startScan },
			//'#deviceConfirm' : { tap : this.unselectDevice },
			//'#deviceConfirmButton' : { tap : this.confirmDevice },
			//'#btpinOK' : { tap : this.pairDevice },
			'#termsAccept' : { tap : this.showMainmenu },
		});
		
		navigator.bluetooth.stateCallback = function(running) {
			
		};
		
		navigator.bluetooth.onCallback = function() { app.controller.setup.checkBluetooth(); };
		navigator.bluetooth.offCallback = function() { app.controller.setup.checkBluetooth(); };
		navigator.bluetooth.scanStartCallback = function() { Ext.getCmp('scanIndicator').setHtml('Scanning'); Ext.getCmp('scanIcon').setHtml('<img src="resources/images/loader.gif"></img>'); };
		navigator.bluetooth.scanStopCallback = function() { Ext.getCmp('scanIndicator').setHtml( Ext.getStore('devices').count() + ' found' ); Ext.getCmp('scanIcon').setHtml(''); if(Ext.getStore('devices').count() < 1) { navigator.bluetooth.start(); } };
		navigator.bluetooth.foundCallback = function(devs) { app.controller.setup.foundBluetooth(devs); };
		
		app.controller.setup.checkBluetooth = function() {
			navigator.bluetooth.state();
		};
		
		app.controller.setup.foundBluetooth = function(devs) {
			var store = Ext.getStore('devices');
			
			for(var i = 0; i < devs.length; i++){
				if(store.find('address', devs[i][0]) == -1)
					store.add( { address : devs[i][0], name : devs[i][1] } );							
			}
		};
		
		app.controller.Setup = this;
	
	},
	
	startScan : function() {
		navigator.bluetooth.start();
	},
	
    selectDevice : function(list, item) {
		var device = Ext.getStore('devices').getAt(item).data;
		
		navigator.service.send(device.address);
    
		var conf = new Object();
		conf.app = 'Textbuster';
		conf.version = app.version;
		conf.devices = [];
		conf.devices[0] = {};
		conf.devices[0].address = device.address;
		
		window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function (fileSystem) {
			fileSystem.root.getFile("config", {create: true}, function(fileEntry) {
			//window.resolveLocalFileSystemURI("file:///data/data/com.access2/address.txt", function(fileEntry) {
				fileEntry.createWriter(function(writer) {
					writer.onwrite = function(evt) {
						Ext.Msg.alert('Bluetooth Setup', 'Device <br>"' + device.address + '"<br> successfully saved.');
					};
					writer.write(JSON.stringify(conf));
				}, function() { console.log('File: Create failed'); } ); 
			}, function() { console.log('File: Resolve failed'); } ); 
		}, function() { console.log('File: File system failed'); } );
		
		Ext.Viewport.setActiveItem(app.view.Terms);
		
		//~ Ext.getCmp('deviceList').hide();
		//~ 
		//~ if(device.name.length > 0)
			//~ Ext.getCmp('deviceConfirmLabel').setHtml(device.name);
		//~ else
			//~ Ext.getCmp('deviceConfirmLabel').setHtml(device.address);
		//~ Ext.getCmp('deviceConfirm').show();
	},
	
	startBluetooth : function() {
		Ext.getCmp('deviceConfirm').hide();
		Ext.getCmp('deviceList').show();
		navigator.bluetooth.start();
    },
    
    stopBluetooth : function() {
		navigator.bluetooth.stop();
    },
	
	unselectDevice : function(list, item) {
		Ext.getCmp('deviceConfirm').hide();
		Ext.getCmp('deviceList').show();
	},
	
	confirmDevice : function() {
		Ext.Viewport.setActiveItem(app.view.Btpin);
	},
	
	pairDevice : function() {
		Ext.Viewport.setActiveItem(app.view.Terms);
	},
	
	showMainmenu : function() {
		Ext.Viewport.setActiveItem(app.view.Mainmenu);
	},
});
