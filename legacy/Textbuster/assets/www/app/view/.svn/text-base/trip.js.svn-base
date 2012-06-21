Ext.define('app.view.trip', {
    extend : 'Ext.Panel',
    xtype : 'trip',
    config : {
	    fullscreen:	true,
	    layout:	{ type : 'vbox', align : 'center', animation: { type: 'slide' } },
	    items: [
			{ xtype : 'toolbar', docked : 'top', items : [
				{xtype : 'button', id : 'tripBack', ui : 'back', text : 'Back'},
				{xtype : 'container', html : '<img src="resources/images/toolbar-banner.jpg"></img>'},
			]},
			{ xtype : 'container', layout : { type : 'vbox', align : 'center' }, width : '100%', defaults : { width : '90%' }, items : [
				{ xtype : 'spacer', height : '0.4em' },
				
				{xtype : 'container', layout : { type : 'hbox', align : 'right' }, items : [
					{ cls : 'text', html: 'Trip Details', width : '100%' },
				]},
				{ xtype : 'spacer', height : '0.4em'  },
				{ xtype : 'container', id : 'tripDateBox', layout : { type : 'hbox', align : 'center' }, items : [
					{ cls : 'insetIcon', html : '<img height="16px" cls="icon" src="resources/images/calendar_dark.png"></img>' },
					{ cls : 'insetLabel', html : 'DATE:' },
					{ xtype : 'spacer' },
					{ cls : 'insetValue', id : 'tripDate' },
				]},
				
				{ xtype : 'spacer', height : '1em'  },
				
				{ xtype : 'container', cls : 'insetDetails', layout : 'vbox', items : [
					{ xtype : 'container', layout : 'hbox', items : [
						{xtype : 'container', flex : 1, cls : 'insetBox insetBorderRight insetBorderBottom', layout : 'vbox', items : [
							{xtype : 'container', layout : 'hbox', items : [
								{ cls : 'insetIcon', html : '<img height="16px" cls="icon" src="resources/images/clock.png"></img>' },
								{ cls : 'insetLabel', html : 'START TIME:' },
							]},
							{ cls : 'insetValueBig', id : 'tripTime' }
						]},
						{xtype : 'container', flex : 1, cls : 'insetBox insetBorderLeft insetBorderBottom', layout : 'vbox', items : [
							{xtype : 'container', layout : 'hbox', items : [
								{ cls : 'insetIcon', html : '<img height="16px" cls="icon" src="resources/images/clock.png"></img>' },
								{ cls : 'insetLabel', html : 'TRIP TIME:' },
							]},
							{ cls : 'insetValueBig', id : 'tripDur' }
						]},
					]},
					{ xtype : 'container', layout : 'hbox', items : [
						{xtype : 'container', flex : 1, cls : 'insetBox insetBorderRight insetBorderTop', layout : { type : 'vbox', align : 'center' }, items : [
							{ cls : 'insetLabel', html : 'AVERAGE SPEED:' },
							{ cls : 'insetValueBig', id : 'tripSpeed', html : '-' }
						]},
						{xtype : 'container', flex : 1, cls : 'insetBox insetBorderLeft  insetBorderTop', layout : { type : 'vbox', align : 'center' }, items : [
							{ cls : 'insetLabel', html : 'DISTANCE:' },
							{ cls : 'insetValueBig', id : 'tripDist', html : '-' }
						]},
					]},
				]},
				
				{ xtype : 'spacer', height : '1em'  },
			
				{ xtype : 'container', cls : 'insetDetails', layout : 'vbox', items : [
						{xtype : 'container', cls : 'insetBox insetBorderBottom', layout : 'hbox', items : [
							{ cls : 'insetLabel', html : 'FROM:'},
							{ xtype : 'spacer', width : 10 },
							{ xtype : 'spacer' },
							{ xtype : 'container', items : [ { cls : 'insetValue', id : 'tripFrom' } ] },
							{ xtype : 'spacer', width : 2 },
						]},
						{xtype : 'container', cls : 'insetBox insetBorderTop', layout : 'hbox', items : [
							{ cls : 'insetLabel', html : 'TO:'},
							{ xtype : 'spacer', width : 10 },
							{ xtype : 'spacer' },
							{ xtype : 'container', items : [ { cls : 'insetValue', id : 'tripTo' } ] },
							{ xtype : 'spacer', width : 2 },
						]}
					]},

			]},
			
			{ xtype : 'spacer', height : '1em'  },
			
			{ xtype : 'container', width : '90%', layout : 'hbox', items : [
				
				{ xtype : 'button', id : 'tripMap', text : 'See Map' },
				{ xtype : 'spacer' },
	
			] },
			
		]
	},
	initialize: function() {
			app.view.trip.superclass.initialize.call(this);
	},
	
	show: function() {		
		var trip = this.trip;
		var date = trip.get('startDate');
		var dur = new Date( (trip.get('end') - trip.get('start')) * 1000 );
		
		Ext.getCmp('tripDate').setHtml(date.getMonthDescription() + ' ' + date.getDate() + ', ' + date.getFullYear());
		
		Ext.getCmp('tripTime').setHtml(date.getUSTime());
		Ext.getCmp('tripDur').setHtml(dur.getMinutes() + ':' + ((dur.getSeconds() < 10) ? '0' : '') + dur.getSeconds() + '<small> min</small>');
		Ext.getCmp('tripSpeed').setHtml(trip.get('speedavg') +  '<small> mph</small>');
		Ext.getCmp('tripDist').setHtml(trip.get('distance') +  '<small> m</small>');
		
		Ext.getCmp('tripFrom').setHtml(trip.get('startPos').toString());
		Ext.getCmp('tripTo').setHtml(trip.get('endPos').toString());	
		
		app.view.trip.superclass.show.call(this);
	},
	
	setTrip : function(trip) {
		this.trip = trip;
	}
});
