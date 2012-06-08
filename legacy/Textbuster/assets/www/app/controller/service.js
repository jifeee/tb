Ext.define('app.controller.service', { 
	extend: 'Ext.app.Controller',
	
	views: ['service'],
    
    refs: [
        {
            ref: 'serviceControls',
            selector: 'servicecontrols'
        }
    ],
    
    init: function(){
		this.control({
			'servicecontrols #startService' : { tap : this.startService },
			'servicecontrols #stopService' : { tap : this.stopService },
		});
	},
	
	startService: function() {
		navigator.service.start();
		this.checkService();
    },
    
    stopService: function() {
		navigator.service.stop();
		this.checkService();
    },
    
    checkService: function() {
		navigator.service.check(function(running){
			if (running) Ext.getCmp('serviceStatus').setHtml('on'); else Ext.getCmp('serviceStatus').setHtml('off');
		});
    }
    
});
