Ext.define('app.view.btpin', {
    extend : 'Ext.Panel',
    xtype : 'bluetoothpin',
    config : {
	    fullscreen:		true,
	    layout:	{ type : 'vbox', animation: { type: 'slide' } },
	    items: [
			{ xtype : 'toolbar', docked : 'top', items : [
				{xtype : 'spacer'},
				{xtype : 'container', html : '<img src="resources/images/toolbar-banner.jpg"></img>', centered : true},
				{xtype : 'spacer'},
			]},
			{ xtype : 'spacer', height : 20 },
			{ xtype : 'container', flex : 1, width : '90%', left : '5%', items : [
				{ xtype : 'container', cls : 'text', html : 'Enter Bluetooth security PIN' },
				{ xtype : 'spacer', height : 20 },
				{ xtype : 'fieldset', items : { xtype : 'textfield', useClearIcon : false } },
				{ xtype : 'button', id : 'btpinOK', width : '20%', text : 'OK' },
				{ xtype : 'spacer' }
			]},
			{ xtype : 'container', flex : 1 }
	    ]
	},
	initialize: function() {
			app.view.btpin.superclass.initialize.call(this);
	}
});
