Ext.define('app.view.history', {
    extend : 'Ext.Panel',
    xtype : 'history',
    requires : 'app.widget.triplist',
    config : {
	    fullscreen:	true,
	    layout:	{ type : 'vbox', align : 'center', animation: { type: 'slide' } },
	    items: [
			{ xtype : 'toolbar', docked : 'top', items : [
				{ xtype : 'button', id : 'historyBack', ui : 'back', text : 'Back' },
				{ xtype : 'container', html : '<img src="resources/images/toolbar-banner.jpg"></img>'},
			]},
			
			{ xtype : 'spacer', height : 8 },
			{xtype : 'container', width : '90%', layout : { type : 'hbox', align : 'justify' }, items : [
				{ cls : 'text', html: 'History of Trips' },
				{ xtype : 'spacer' },
				{ xtype : 'segmentedbutton', items : [
					{ text : 'Date', id : 'dateChoice', pressed : true },
					{ text : 'Miles', id : 'distChoice' },
				]},
			]},
			{ xtype : 'spacer', height : 8 },
			
			{ xtype : 'container', flex : 1, width: '100%', layout : 'fit', limit : 20, items : [
				{ xtype : 'triplist', id : 'historyList' },
			]}
		]
	},
	initialize: function() {
			
			app.view.mainmenu.superclass.initialize.call(this);
	},
	show: function() {
			var store = Ext.getStore('trips');
			
			if(store.count() > 0){
			
				var datefield = Ext.getCmp('lasttripDate');
				var timefield = Ext.getCmp('lasttripTime');
			
				store.sort('start', 'DESC');
				
				trip = store.first();
				
				var date = new Date((trip.data.start) * 1000);
				
				datefield.setHtml(date.getMonthDesc() + '. ' + date.getDay());
				timefield.setHtml(date.getUSTime());
			}
			
			Ext.getCmp('historyList').refresh();
			
			app.view.mainmenu.superclass.show.call(this);
	}
});
