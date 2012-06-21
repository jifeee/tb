Ext.define('app.view.mainmenu', {
    extend : 'Ext.Panel',
    xtype : 'mainmenu',
    config : {
	    fullscreen:	true,
	    layout:	{ type : 'vbox', align : 'center', animation: { type: 'slide' } },
	    items: [
			{ xtype : 'toolbar', docked : 'top', items : [
				{xtype : 'spacer'},
				{xtype : 'container', html : '<img src="resources/images/toolbar-banner.jpg"></img>' },
				{xtype : 'spacer'},
				//{xtype : 'button', id : 'logoutButton', text : 'Close'}
			]},
			{ xtype : 'container', id : "mainmenuTrip", width : '90%', layout : { type : 'vbox', align : 'center' }, defaults : { width : '100%' }, items : [
				{ xtype : 'spacer', height : 12 },
				{ cls : 'text', html: 'LAST TRIP DETAILS' },
				{ xtype : 'spacer', height : 4 },
				{ xtype : 'container', cls : 'insetDetails', layout : 'hbox', items : [
					{xtype : 'container', flex : 1, cls : 'insetBox insetBorderRight', layout : 'vbox', items : [
						{xtype : 'container', layout : 'hbox', items : [
							{ cls : 'insetIcon', html : '<img height="16px" cls="icon" src="resources/images/calendar.gif"></img>' },
							{ cls : 'insetLabel', html : 'DATE:' },
						]},
						{ cls : 'insetValueBig', id : 'lasttripDate', html : '---' }
					]},
					{xtype : 'container', flex : 1, cls : 'insetBox insetBorderLeft', layout : 'vbox', items : [
						{xtype : 'container', layout : 'hbox', items : [
							{ cls : 'insetIcon', html : '<img height="16px" cls="icon" src="resources/images/clock.png"></img>' },
							{ cls : 'insetLabel', html : 'START TIME:' },
						]},
						{ cls : 'insetValueBig', id : 'lasttripTime',  html : '---'  }
					]}
				]},
				{ xtype : 'spacer', height : 4 },
				{ xtype : 'container', cls : 'insetDetails', layout : 'vbox', items : [
					{xtype : 'container', cls : 'insetBox insetBorderBottom', layout : 'hbox', items : [
						{ cls : 'insetLabel', html : 'FROM:'},
						{ xtype : 'spacer', width : 10 },
						{ xtype : 'spacer' },
						{ xtype : 'container', items : [ { cls : 'insetValue', id : 'lasttripFrom', html : '---' } ] },
						{ xtype : 'spacer', width : 2 },
					]},
					{xtype : 'container', cls : 'insetBox insetBorderTop', layout : 'hbox', items : [
						{ cls : 'insetLabel', html : 'TO:'},
						{ xtype : 'spacer', width : 10 },
						{ xtype : 'spacer' },
						{ xtype : 'container', items : [ { cls : 'insetValue', id : 'lasttripTo',  html : '---' } ] },
						{ xtype : 'spacer', width : 2 },
					]}
				]},
			]},
			{ xtype : 'spacer' },
			{ xtype : 'container', width : '100%', id : "mainmenuButtons", layout : { type : 'vbox', align : 'center' }, defaults : { width : '80%' },items : [
					{ html : '<center><img src="resources/images/mainmenuarrow.png"></img></center>' },
					{ xtype : 'button', id : 'historyButton', ui : 'menu', text : 'History of Trips', icon : 'resources/images/menuarrow.png' },
					{ xtype : 'button', id : 'settingsButton', ui : 'menu', text : 'Settings', icon : 'resources/images/menuarrow.png' },
					{ xtype : 'button', id : 'moreButton', ui : 'menu', text : 'More', icon : 'resources/images/menuarrow.png' },
			]},
		]
	},
	initialize: function() {
			
			app.view.mainmenu.superclass.initialize.call(this);
	},
	
	show: function() {		
			this.updateLasttrip();
	
			app.view.mainmenu.superclass.show.call(this);
	},
	
	updateLasttrip : function() {
			var store = Ext.getStore('trips');
			
			if(store.count() > 0){
				store.sort('start', 'DESC');
				
				trip = store.first();
				
				var date = trip.data.startDate;
		
				Ext.getCmp('lasttripDate').setHtml(date.getMonthDesc() + '. ' + date.getDate());
				Ext.getCmp('lasttripTime').setHtml(date.getUSTime());
				Ext.getCmp('lasttripFrom').setHtml(trip.get('startPos').toString());
				Ext.getCmp('lasttripTo').setHtml(trip.get('endPos').toString());
			}
	}
});
