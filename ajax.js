Ext.ns("InfoSite.common.ajax");

//
// submit
//
// Simplifies invocation of AJAX request.  
//
InfoSite.common.ajax.submit = function (actionURL, scopeObj, cbsFn, cbfFn, params) {

    Ext.Ajax.request({    	
    	url:     actionURL, 
        scope:   scopeObj,
        params:  params,
    	success: cbsFn,
    	failure: cbfFn,
    	timeout: 180000
    });
};


//
// responseCallback()
//
// A default implementation of a callback handler suitable to be passed to
// submit() function above.  Expects "this" to be in scope of containing object
// (e.g. assign scope:this before AJAX call.  Also assumes response
// (responseText) from server will be {failure:<boolean>,
// message:<string-message>}
//
InfoSite.common.ajax.responseCallback = function(response) {

    var jsonResponse = Ext.JSON.decode(response.responseText);

    if (jsonResponse.failure) {
        Ext.MessageBox.alert('Error', jsonResponse.message);              
        this.refreshSubscribers;
    } 
}


//
// failureCallback()
//
// A default implementation of a callback handler suitable to be passed to
// submit() function above.  Expects "this" to be in scope of containing object
// (e.g. assign scope:this before AJAX call.  Also assumes response 
// (responseText) from server will contain meaningful message.
//
InfoSite.common.ajax.failureCallback = function(response) {

    Ext.MessageBox.alert('Error', 'Unable to process request: ' 
                         + response.responseText); 
    this.refreshSubscribers;
}
