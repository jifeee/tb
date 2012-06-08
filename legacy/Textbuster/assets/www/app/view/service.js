Ext.define('app.view.service', {
    extend : 'Ext.Panel',
    xtype : 'servicecontrols',
    config : {
	    fullscreen:		true,
	    layout:		'vbox',
	    items: [
			{	
				title : 'Service',
				xtype : 'panel',
				layout: 'vbox',
				items : [
					{ xtype	: 'container',
					  id	: 'serviceStatus',
					  width	: '100%',
					  height: '40px',
					},
					{ xtype	: 'button',
					  text	: 'Start',
					  itemId: 'startService',
					},
					{ xtype	: 'button',
					  text	: 'Stop',
					  itemId: 'stopService',
					}
				]
			}
	    ],
	},
	initialize: function() {
			navigator.service.check(function(running){
				if (running) Ext.getCmp('serviceStatus').setHtml('on'); else Ext.getCmp('serviceStatus').setHtml('off');
			});
			
			app.view.service.superclass.initialize.call(this);
	}
});
