var OneSpaceModel = new Array();



OneSpaceModel = function(controller) {
  this.controller = controller;
  this.http = new OneSpaceModel.Http(this.controller);
  this.xmpp = new OneSpaceModel.Xmpp(this.controller);
  this.windowHandler = new OneSpaceModel.WindowHandler(this);
  this.tabHandler = new OneSpaceModel.TabHandler(this);
  this.userId = -1;
};


OneSpaceModel.prototype = {

  initialize : function() {
    var that = this;
    this.http.initialize();
    this.windowHandler.initialize();
    this.tabHandler.initialize();
  },

  initializeLocations : function() {
    var that = this;
    chrome.windows.getAll({ populate: true}, function(windows) {
      for (var w = 0; w < windows.length; w++) {
	var win = windows[w];
	chrome.tabs.query({windowId : win.id}, function(tabs){
	  for (var i = 0; i < tabs.length; i++) {
	    var tab = tabs[i];
	    that.controller.onTabAdded(tab);
	  }
	});
      }
    });
    
  },
    
  


  
};




OneSpaceModel.WindowHandler = function(model) {
  this.model = model;
  this.currentFocusedWindowId = -1;

};


OneSpaceModel.WindowHandler.prototype = {
  
  initialize : function() {
    var that = this;
    
    chrome.windows.onCreated.addListener( function (window) {
      if ((window.type != "normal") || (window.incognito == true)) { return; }
      that.model.addOpenWindow(window.id);
    }); 

    chrome.windows.onRemoved.addListener( function (windowId) {
      that.model.controller.onWindowClosed(windowId);
    }); 

    
  },


};







OneSpaceModel.TabHandler = function(model) {
  this.model = model;
  this.prerendered = false;
  this.tabRemoved = false;
};


OneSpaceModel.TabHandler.prototype = {
  
  initialize : function() {
    var that = this;
    that.model = this.model;

    chrome.tabs.onCreated.addListener( function (tab) {
      that.model.controller.onTabAdded(tab);
    }); 
    
    chrome.tabs.onUpdated.addListener( function (tabId, changeInfo, tab) {
      if (changeInfo.status == 'complete') {
	that.model.controller.onNewPageLoaded(tab);
      }
    });



    chrome.tabs.onRemoved.addListener( function(tabId, removeInfo) {      
      that.model.controller.onTabRemoved(tabId);
    }); 

    chrome.tabs.onActivated.addListener( function(info) {
      if (that.prerendered == true) {
	// if prerendered, it's not "really" a onActivated event!!!
      } else {
	chrome.tabs.get(info.tabId, function (tab) {
	  that.model.controller.onTabSelectionChanged(tab);
	});
      }
      that.prerendered = false;
    }); 


    
    //
    // Repairs the prerendering issue
    //
    chrome.webNavigation.onTabReplaced.addListener( function (details) {
      that.model.controller.onTabReplaced(details);
    }); 
  },

  
  
};





OneSpaceModel.Http = function(controller) {
  this.controller = controller;
};



OneSpaceModel.Http.prototype = {
  
  initialize : function() {
    //
  },
  
  doAjaxPostRequest : function(serverUrl, method, data, callback) {
    $.ajax({
      url: serverUrl, type: method, data: data, dataType: "json",
      success: function(response, textStatus, jqXHR){ callback(response); },
      error: function(jqXHR, textStatus, errorThrown){ 
	
      },
    });
  },


  updateUserVloc : function(userId, action, vloc, doAsyncRequest) {
    var url = HTTP_SERVER_URL + "/user/vloc/?userid="+userId+"&action="+action+"&vloc="+vloc;
    this.doAjaxPostRequest(url, "put", { }, OneSpace.Controller.Utility.bind(this, this.onUserVlocUpdated));
  },
  
  onUserVlocUpdated : function(xmlResponse) {
    //alert("onUserVlocUpdated\n");
  },

};




//
// Crude work around:
// Just create new connection to cancel exiting onerro
// Needed when popup window is closed
//
OneSpaceModel.Xmpp = function(controller) {
  this.controller = controller;
  this.connection = null;
  this.resource = '';
  this.server = '';
  this.httpBind = '';
  this.user =  "";
  this.password = "";
};


OneSpaceModel.Xmpp.prototype = {

  initialize : function() {
    this.initializeXmppOptions();
  },
  
  initializeXmppOptions : function (){
    var that = this;
    chrome.storage.sync.get({
      xmppHost: '',
      xmppHttpBind : '',
      xmppResource : '',
      xmppUserName : '',
      xmppPassword : ''
    }, function(items) {
      that.server = items.xmppHost;
      that.httpBind = items.xmppHttpBind;
      that.resource = items.xmppResource;
      that.user = items.xmppUserName;
      that.password = items.xmppPassword;
      that.connect(false);
    });
  },
  
  

  connect : function(doRegister) {
    if (this.connection != null) {
      if (this.connection.connected()) {
	this.connection.disconnect();
      }
    }    
     
    try {
      this.userJid = this.user + "@" + this.server + "/" + this.resource;
      this.connection = new JSJaCHttpBindingConnection({ httpbase : this.httpBind });
      this.registerHandlers();
      // setup args for connect method
      oArgs = new Object();
      oArgs.domain = this.server;
      oArgs.username = this.user;
      oArgs.resource = this.resource;
      oArgs.pass = this.password;
      oArgs.register = doRegister;
      this.connection.connect(oArgs);
    } catch (e) {
      alert("[ERROR] " + e.toString());
    } finally {
      return false;
    }
  },
  
  
  registerHandlers : function() {
    this.connection.registerHandler('onconnect', OneSpace.Controller.Utility.bind(this, this.handleOnConnected));
    this.connection.registerHandler('onerror', OneSpace.Controller.Utility.bind(this, this.handleOnError));
    this.connection.registerHandler('ondisconnect', OneSpace.Controller.Utility.bind(this, this.handleDisconnected));
  },
  
  
  handleOnConnected : function () {
    if (this.connection != null) {
      if (this.connection.connected()) {
	this.connection.disconnect();
      }
    }   
  },

  
  handleOnError : function(e) {
    //alert(("An error occured; Code: "+ e.getAttribute("code")+", Type: "+ e.getAttribute("type") + "Condition: "+e.firstChild.nodeName).htmlEnc());
    code = e.getAttribute("code");
    if (code == 401) 
      alert("Error: wrong user name or password (code: " + code + ")!");
    else if (code == 409) 
      alert("Error: user name already exists (code: " + code + ")!");
    else
      alert("Error: unknown problem (code: " + code + ")!");

    if (this.connection.connected()) { this.connection.disconnect(); }
    
    //this.controller.handleOnXmppError(e);
  },

  
  handleDisconnected : function () {
    
  },
  
  
  
  
};


