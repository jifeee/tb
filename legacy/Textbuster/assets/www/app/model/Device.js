Ext.define('app.model.Device', {
    extend : 'Ext.data.Model',
    
    fields: [
        {name: 'address',  type: 'string'},
        {name: 'name',     type: 'string'},
        {name: 'selected', type: 'boolean'},
    ],
} );
