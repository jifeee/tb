Ext.define('app.view.terms', {
    extend : 'Ext.Panel',
    xtype : 'terms',
    config : {
	    fullscreen:		true,
	    layout:	{ type : 'vbox', pack : 'justify', animation: { type: 'slide' } },
	    items: [
			{ xtype : 'toolbar', docked : 'top', items : [
				{xtype : 'spacer'},
				{xtype : 'container', html : '<img src="resources/images/toolbar-banner.jpg"></img>', centered : true},
				{xtype : 'spacer'},
			]},
			{ xtype : 'toolbar', docked : 'bottom', items : [
				{xtype : 'spacer'},
				{xtype : 'button', id : 'termsAccept', text : 'I accept'},
				{xtype : 'spacer'},
			]},
			
			{ xtype : 'spacer', height : '0.8em' },
			
			{ xtype : 'container', layout : 'hbox', items : [
				{ xtype : 'spacer' },
				{ html : '<img src="resources/images/state_on.png"></img>' },
				{ xtype : 'spacer', width : '0.4em' },
				{ html : '<img src="resources/images/state_on.png"></img>' },
				{ xtype : 'spacer', width : '0.4em' },
				{ html : '<img src="resources/images/state_off.png"></img>' },
				{ xtype : 'spacer' },
			]},
			
			{ xtype : 'spacer', height : '0.8em' },
			
			{ xtype : 'container', flex : 10, style : 'padding-left : 5%; padding-right : 5%', scrollable : true, items : [
				{ cls : 'text', html : 'Terms of use' },
				{ xtype : 'spacer', height : 10 },
				{ cls : 'lines', html : '<p>Lorem ipsum dolor sit amet, no eum exerci phaedrum, vel ei veritus disputando, pri appareat appellantur id. Cibo omittam vulputate no sed, ius dicta iuvaret nusquam eu. Ut vim explicari quaerendum. Ea cum utamur nominavi percipit. Pri nostro everti cu. Eirmod iuvaret vis eu, usu ne nobis numquam. Ut idque everti dissentias mea, no mel posse graeci concludaturque, in vis mazim dicunt.</p><p>Ea duo feugiat moderatius, ne mel cibo verear numquam. Ubique everti nam no, eu eos dolorem corpora. Et sea soluta minimum, id nostrum fierent mel. Pri ei volutpat temporibus complectitur. Te vix nihil iudico, cum ex facer lucilius scripserit.</p><p>Et eum omnes epicuri elaboraret. Cum ferri simul scaevola in. Ius iuvaret epicurei neglegentur id, quod feugiat et sea, ad affert feugiat fabellas quo. Duo stet ferri consequuntur in. Ad eum probo aliquid pertinacia, ut pertinax prodesset per.</p><p>Offendit platonem mei id, id putant moderatius assueverit cum, pro semper reprimique percipitur ex. Sit scaevola moderatius cu. Quis expetendis duo at, his et scripta dolorum accumsan, nisl laoreet ius eu. Vel reque quando facilisi ei. Ignota option hendrerit ne est, his an suas altera apeirian, illud clita no quo.</p><p>Has eu mucius ornatus, has te impetus perpetua pertinacia. Est quis tractatos an, eu est ullum aliquam. Eum blandit laboramus ad, mei et oportere posidonium. Summo tincidunt persequeris eum et, eos case magna tempor cu. Pro percipit constituam ut.</p>' }
			]},
			
	    ]
	},
	initialize: function() {
			app.view.btpin.superclass.initialize.call(this);
	}
});
