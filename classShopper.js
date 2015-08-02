Ext.ns('InfoSite.thing.ClassShopper');

// This is a panel that will contain a table of elements
InfoSite.thing.ClassShopper = function() {

    //used to call helper functions from within listener functions
    var classShopper = this;

    this.model = Ext.define('classShopperModel',{
        extend: 'Ext.data.Model',
        fields: [
            {name: 'subject'},
            {name: 'courseNum'},
            {name: 'courseName'},
            {name: 'prof'},
            {name: 'genEd'},
            {name: 'quality'},
            {name: 'difficulty'},
            {name: 'cost'}
        ]
    });


    this.coursesGrid  = Ext.create( 'Ext.grid.Panel', {


        id: 'coursesGrid',
        multiSelect: true,
        margin: '5 5 5 5',
        title: 'Browse Courses',
        flex: 2,
        bufferedRenderer: true,
        emptyText: 'no records to display',

        requires: [
            'Ext.ux.RowExpander',
        ],

        columns:[
            {text: 'Subject', dataIndex: 'subject', width: 70},
            {text: '#', dataIndex: 'courseNum', width: 70},
            {text: 'Name', dataIndex: 'courseName', width: 120, flex: 1},
            {text: 'Professor', dataIndex: 'prof', width: 120},
            {text: 'Gen Ed', dataIndex: 'genEd', width: 120},
            {text: 'Quality', dataIndex: 'quality', width: 100},
            {text: 'Difficulty', dataIndex: 'difficulty', width: 100},
            {text: 'Cost', dataIndex: 'cost', width: 100}
        ],

        store: new Ext.data.Store({
            model: 'classShopperModel' ,
            autoLoad: true,

            proxy: { 
                type: 'ajax' ,
                url: 'dad.json',
                reader: {
                    type: 'json',
                    rootProperty : 'data',
                    idProperty: 'id',
                    totalProperty: 'total'
                }
            }
        }),

        plugins: [{
            ptype: 'rowexpanderplus',
            selectRowOnExpand: true,
            expandOnlyOne: true,
            rowBodyTpl: new Ext.XTemplate(
                '<p>Welcome to my corny template</p>',
                '<br />',
                '<hr />',
                '<br />',
                '<table id={table_id}>',
                '<tr>',
                '<td><div id=leftdiv></div></td>',
                '<td><div id=rightdiv></div></td>',
                '</tr>',
                '<tr><td><hr /></td><td><hr />',
                '</table>',
                '<p>So ends my corny template</p>',
            {}),
        }],

        viewConfig: {
            plugins: {
                ptype: 'gridviewdragdrop',
                containerScroll: true,
                dragGroup: 'coursesGrid',
            },
            listeners: {
                drop: function(node, data, dropRec, dropPos) {
                }
            }
        },

        style: {
            "border":"3px solid #f5f5f5"
        }
    });


    this.shoppingCartGrid  = Ext.create( 'Ext.grid.Panel', {
        
        id: 'shoppingCartGrid',
        flex: 1, 
        bufferedRenderer: true,
        emptyText: 'no records to display',
        margin: '5 5 5 5',
        title: 'Shopping Cart',

        store: new Ext.data.Store({
            model: 'classShopperModel'
        }),
        
        columns:[
            {text: 'Subject', dataIndex: 'subject', width: 70},
            {text: '#', dataIndex: 'courseNum', width: 70},
            {text: 'Name', dataIndex: 'courseName', flex: 1},
        ],

        viewConfig: {
            plugins: {
                ptype: 'gridviewdragdrop',
                enableDrag: false,
                containerScroll: true,
                dropGroup: 'coursesGrid'
            },
            listeners: {
                beforedrop: function(node, data) {
                console.log('here');
                    data.copy = true;
                }
            }
        },

        style: {
            "border":"3px solid #f5f5f5"
        },

        listeners: {
        }
    });



    /*            *
     * Main panel *
     *            */


    this.classShopperPanel = Ext.create('Ext.Panel', {
        region: 'center',
        layout: {
            type: 'hbox',
            align: 'stretch'
        },

        items: [
            classShopper.coursesGrid,
            classShopper.shoppingCartGrid
        ]
    });

};

