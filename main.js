Ext.ns('InfoSite.thing.Manager');

Ext.require([
 //   'Ext.grid.*',
 //   'Ext.data.*',
  //  'Ext.dd.*'
]);

InfoSite.thing.Manager = function() {

	this.pageTitle = Ext.create('Ext.Img', {
		src: 'media/logo.png',
		width: 140,
		height: 50
	});

	this.searchBox = Ext.create('Ext.form.field.Text', {
        inputType: 'search',
        emptyText: 'Search for a course...',
        width: 300,
        scope: this,
        cls: 'rounded',
        fieldStyle: {
            'font-size':'18px',
            'padding':'15px 5px 0px 5px',
            'border-radius':'7px'
        },
        listeners: {
        },
        style: {
            "line-height":"40px",
        }
    });


    this.goButton = Ext.create('Ext.button.Button', {
		text: 'Go',
        scope: this,
        height: 37,
		handler: function() {
		}
	});


    this.courses = new InfoSite.thing.Courses();
    this.shoppingCart = new InfoSite.thing.ShoppingCart();

    this.toolbar = new Ext.Toolbar({
        dock: 'top',
        layout: {
            pack: 'center'
        },
        items: [
            '<-', this.pageTitle,  
            this.searchBox, 
            this.goButton
        ]
    });

    this.mainPanel =  Ext.create('Ext.Panel', {		
        layout: 'border' ,
        region: 'center' ,

        tbar: [
            this.pageTitle,
            '->',
            this.searchBox,
            this.goButton,
            '->',
            {xtype: 'button', width: 100}
        ],

        items: [
            this.courses.cPanel,
            this.shoppingCart.scPanel
        ]
	});
	
};


var infoSiteManager = null;


var docReadyFunction = function(){

	Ext.QuickTips.init();
	
	infoSiteManager = new InfoSite.thing.Manager();

	Ext.create('Ext.container.Viewport', {
		layout: 'border' ,
		items: 	[ infoSiteManager.mainPanel ] ,
		renderTo : Ext.getBody() ,
		id: 'infoSiteManagerViewPort' ,
		margins: '0 10 0 10' ,
		frame: true
	});

};

Ext.onReady(docReadyFunction);
