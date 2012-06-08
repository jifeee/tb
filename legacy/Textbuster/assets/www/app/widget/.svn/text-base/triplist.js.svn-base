Ext.define('app.widget.triplist', {
    extend : 'Ext.List',
    
    xtype : 'triplist',
	
	config : {
		store : 'trips',
		itemTpl : '<div class="triplistItem">'+
					'<span class="triplistDate">{[values.startDate.getUSDate()]},</span> ' +
					'<span class="triplistDist">{distance} miles</span><br>' +
					'<div class="triplistDesc triplistStart">{[values.startPos.toString()]}</div><br>' +
					'<div class="triplistDesc triplistEnd">{[values.endPos.toString()]}</div>' +
				  '</div>',
		locked : true,
	},
	
	pageupId : 'triplistUp',
	pagedownId : 'triplistDown',
	
	pageupText : 'Up',
	pagedownText : 'Down',
	
	currentIndex : 0,
	limit : 25,
	
	initialize : function() {
		this.currentIndex = 0;
		
		this.pagingButtonUp = Ext.widget('button');
		this.pagingButtonUp.setCls('x-button-pageup');
		this.pagingButtonUp.setHandler( function() {
			var list = this.parent;
			var store = list.getStore();
			
			store.clearFilter();
			if(store.sorter) store.sort(store.sorter);
			
			list.currentIndex -= list.limit;
			list.currentIndex = (list.currentIndex < 0) ? 0 : list.currentIndex;
			
			//~ var height = parseInt(window.getComputedStyle(list.innerElement.dom).getPropertyCSSValue("height").cssText) -
						 //~ parseInt(window.getComputedStyle(list.renderElement.dom).getPropertyCSSValue("height").cssText);
			//~ Ext.getDom(list.innerElement).setAttribute('style', '-webkit-transform: translate3d(0px, ' + height + 'px, 0px);');
			
			list.refresh();
		} );
		this.insert(0, this.pagingButtonUp);
		
		this.pagingButtonDown = Ext.widget('button');
		this.pagingButtonDown.setText(this.pagedownText);
		this.pagingButtonDown.setCls('x-button-pageDown');
		this.pagingButtonDown.setHandler( function() {
			var list = this.parent;
			var store = list.getStore();
			
			store.clearFilter();
			if(store.sorter) store.sort(store.sorter);
			
			list.currentIndex += list.limit;
			
			//~ Ext.getDom(list.innerElement).setAttribute('style', '-webkit-transform: translate3d(0px, 0px, 0px);');
			
			list.refresh();
		} );
		this.add(this.pagingButtonDown);
		
		app.controller.trips.setListToDate();
		
		this.refresh();
		
		app.widget.triplist.superclass.initialize.call(this);

	},
	
	refresh : function() {
		var store = this.getStore();
		
		store.clearFilter();
		if(store.sorter) store.sort(store.sorter);
		
		var first = store.first();
		var last = store.last();
		
		var limit = this.limit;
		var date = this.currentPagingDate;
		store.filterBy(function(record) {
			return (record.index >= this.currentIndex && record.index < this.currentIndex + this.limit);
		}, this );
		
		var but = this.pagingButtonUp;
		if(but){
			but.setText(this.pageupText);
			if(first != store.first()){
				but.setHidden(false);
			} else {
				but.setHidden(true);
			}
		}
		
		var but = this.pagingButtonDown;
		if(but){
			but.setText(this.pagedownText);
			if(last != store.last()){
				but.setHidden(false);
			} else {
				but.setHidden(true);
			}
		}
		
		app.widget.triplist.superclass.refresh.call(this);
	}

});

