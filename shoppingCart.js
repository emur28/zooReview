Ext.ns('InfoSite.thing.ShoppingCart');

// This is a panel that will contain a table of elements
InfoSite.thing.ShoppingCart = function() {

    //used to call helper functions from within listener functions
    var sc = this;


    /*       *
     * Store *
     *       */

    //generic model used to instantiate the store
    this.model = Ext.define('scModel',{
        extend: 'Ext.data.Model',
    });

    this.store = Ext.create('Ext.data.Store', {

        pageSize: 0,
        storeId: 'scStore',
        model: 'scModel' ,
        remoteSort: false,

        proxy: { 
            type: 'ajax' ,
            reader: {
                type: 'json',
                rootProperty : 'data',
                idProperty: 'id',
                totalProperty: 'total'
            }
        },

        listeners: {
        },
    });

    /*      *
     * Grid *
     *      */


    this.grid  = Ext.create( 'Ext.grid.Panel', {
        
        store : sc.store,
        id: 'scGrid',
        region: 'center',
        bufferedRenderer: true,
        emptyText: 'no records to display',
        
        columns:[
            {text: 'Subject', dataIndex: 'subject', width: 70},
            {text: '#', dataIndex: 'courseNum', width: 70},
            {text: 'Name', dataIndex: 'courseName', flex: 1},
        ],

        viewConfig: {
            plugins: {
                ptype: 'gridviewdragdrop',
                enableDrag: false,
                dropGroup: 'firstGridDDGroup'
            },
            listeners: {
                beforedrop: function(node, data) {
                    data.copy = true;
                }
            }
        },

        listeners: {
        }
    });



    /*            *
     * Main panel *
     *            */


    this.scPanel =  Ext.create('Ext.Panel', {     
        layout: 'border' ,
        region: 'east' ,
        margin: '5 5 5 0',
        width: 400,
        title: 'Shopping Cart',
        items: [ sc.grid ],
    });

};

