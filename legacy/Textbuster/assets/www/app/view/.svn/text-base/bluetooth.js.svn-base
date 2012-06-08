Ext.define('app.view.bluetooth', {
    extend : 'Ext.Panel',
    xtype : 'bluetoothsetup',
    config : {
	    fullscreen:		true,
	    layout:	{ type : 'vbox', pack : 'justify', align : 'center', animation: { type: 'slide' } },
	    items: [
			{ xtype : 'toolbar', docked : 'top', items : [
				{xtype : 'spacer'},
				{xtype : 'container', html : '<img src="resources/images/toolbar-banner.jpg"></img>', centered : true},
				{xtype : 'spacer'},
			]},
			
			{ xtype : 'spacer', height : '0.8em' },
			
			{ xtype : 'container', layout : 'hbox', items : [
				{ xtype : 'spacer' },
				{ html : '<img src="resources/images/state_on.png"></img>' },
				{ xtype : 'spacer', width : '0.4em' },
				{ html : '<img src="resources/images/state_off.png"></img>' },
				{ xtype : 'spacer', width : '0.4em' },
				{ html : '<img src="resources/images/state_off.png"></img>' },
				{ xtype : 'spacer' },
			]},
			
			{ xtype : 'spacer', height : '0.8em' },
			
			{ xtype : 'button', id : 'deviceScan', text : 'Scan for devices', width : '80%' },
			
			{ xtype : 'spacer', height : '0.8em' },
			
			{ xtype : 'panel', id : 'deviceList', flex : 4, width : '100%', layout: 'fit', items : [
				{ xtype : 'toolbar', docked: 'top', ui : 'inpage', items : [
					{ xtype : 'container', html : 'Bluetooth devices'},
					{ xtype : 'spacer'},
					{ xtype : 'container', id : 'scanIndicator'},
					{ xtype : 'container', id : 'scanIcon'},
				]},
				{ xtype : 'list', store: 'devices', itemTpl: '<div class="text"><tpl if="name.length &gt; 0">{name}<tpl else>{address}</tpl></div>' },
			]},
			{ id: 'versionString', html : '', style : 'font-size : 0.6em' },
			{ xtype : 'panel', id : 'deviceConfirm', flex : 4, hidden : true, layout : { type : 'vbox', pack : 'center' }, items : [
				{ xtype : 'toolbar', docked: 'top', ui : 'inpage', items : [
					{ xtype : 'container', html : 'Selected Bluetooth device'},
					{ xtype : 'spacer'},
				]},
				{ xtype : 'panel', ui : 'box', layout : { type : 'hbox', pack : 'justify' }, items : [
					{ xtype : 'spacer' },
					{ xtype : 'container', id : 'deviceConfirmLabel', cls : 'text' },
					{ xtype : 'spacer' },
					{ xtype : 'button', id : 'deviceConfirmButton', text : 'Confirm' },
				]},
				{ xtype : 'spacer', height : 20 },
			]},
	    ],
	},
	initialize: function() {
			navigator.bluetooth.state(function(running){
				if (running) Ext.getCmp('bluetoothOn').parent.setPressedButtons(Ext.getCmp('bluetoothOn')); else Ext.getCmp('bluetoothOff').parent.setPressedButtons(Ext.getCmp('bluetoothOff'));
			});
			
			console.log(Ext.getCmp('versionString'));
			Ext.getCmp('versionString').setHtml( 'Textbuster ' + app.version );
		
			
			app.view.bluetooth.superclass.initialize.call(this);
	}
});
