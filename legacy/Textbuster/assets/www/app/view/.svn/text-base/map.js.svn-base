Ext.define('app.view.map', {
    extend : 'Ext.Panel',
    xtype : 'tripmap',
    
    config : {
	    fullscreen:	true,
	    layout:	{ type : 'vbox', align : 'center', animation: { type: 'slide' } },
	    items: [
			{ xtype : 'toolbar', docked : 'top', items : [
				{xtype : 'button', id : 'mapBack', ui : 'back', text : 'Back'},
				{xtype : 'container', html : '<img src="resources/images/toolbar-banner.jpg"></img>'},
			]},
			
			{ xtype : 'spacer', height : '0.4em' },
			
			{ xtype : 'container', width : '90%', layout : 'hbox', items : [
				{ cls : 'text', html : 'Trip Details' },
				{ xtype : 'spacer' },
			]},
			
			{ xtype : 'spacer', height : '0.4em' },
			
			{ flex : 1, width: '100%', id : "map_canvas" }
		]
	}
});
