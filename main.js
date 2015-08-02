Ext.ns('ZooReview.Manager');

Ext.require([
  //  'Ext.dd.*'
]);

ZooReview.Manager = function() {

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


    this.classShopper = new ZooReview.ClassShopper();


    this.mainPanel =  Ext.create('Ext.Panel', {		
        layout: 'border',

        id: 'test',
        tbar: [
            this.pageTitle,
            '->',
            this.searchBox,
            this.goButton,
            '->',
            {xtype: 'button', width: 100}
        ],

        items: [
            this.classShopper.classShopperPanel,
        ]
	});
	
};


var zooReviewManager = null;


var docReadyFunction = function(){

	Ext.QuickTips.init();
	
	zooReviewManager = new ZooReview.Manager();

	Ext.create('Ext.container.Viewport', {
		layout: 'fit' ,
		items: 	[ zooReviewManager.mainPanel ] ,
		renderTo : Ext.getBody() ,
		id: 'zooReviewManagerViewPort' ,
		margins: '0 10 0 10' ,
		frame: true
	});

};

Ext.onReady(docReadyFunction);
