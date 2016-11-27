//const HTTP_SERVER_URL = "http://172.29.33.45:11090";
//const HTTP_SERVER_URL = "http://172.18.101.112:8091";
//const HTTP_SERVER_URL = "http://127.0.0.1:8888";
const HTTP_SERVER_URL = "http://sesame.comp.nus.edu.sg/app/onespace/api";



var OneSpace = new Array();




OneSpace.Controller = function() {
  this.messageHandler = new OneSpace.Controller.MessageHandler(this);
  this.model = new OneSpaceModel(this);
  this.view = new OneSpaceView(this);

};

OneSpace.Controller.prototype = {

  initialize : function() {
    this.messageHandler.initialize();
    this.model.initialize();
    this.view.initialize();
    
    var that = this;

    chrome.browserAction.onClicked.addListener(function() {
      if (that.view.popupWindow.window == null) {
	chrome.windows.create({'url': 'popup.html', 'type': 'popup', 'width': 600, 'height': 500 }, function(window) { that.onPopupWindowOpened(window) } );
      } else {
	var updateInfo = {};
	updateInfo.focused = true;
	chrome.windows.update(that.view.popupWindow.window.id, updateInfo, function(window) { });
      }
    });
  },
  

  onPopupWindowOpened : function(window) {
    this.view.popupWindow.setWindow(window);
  },

  onPopupWindowInitialized : function() {
    this.view.updatePopupWindow();
    this.model.initializeLocations();
  },
  
  onTabSelectionChanged : function(tab) {
    this.view.popupWindow.onTabSelectionChanged(tab);
  },
  
  onTabAdded : function(tab) {
    this.view.popupWindow.onTabAdded(tab); // Perhaps not needed since it is covered by page loads
  },

  onTabRemoved : function(tabId) {
    this.view.popupWindow.onTabRemoved(tabId);
  },

  onTabReplaced : function(details) {
    
    this.view.popupWindow.onTabReplaced(details);
  },
  
  onWindowClosed : function(windowId) {
    if (this.view.popupWindow.isPopupWindow(windowId)) {
      this.view.popupWindow.unsetWindow();
      this.model.http.updateUserVloc(this.model.userId, 'remove-all', '', true);
      this.model.xmpp.initialize(); // Just create a pointless connection to leave all group chats
    }
  },
  
  onNewPageLoaded : function(tab) {
    this.view.popupWindow.onNewPageLoaded(tab);
  },
    
  onUserIdReceived : function(userId) {
    this.model.userId = userId;
    this.onPopupWindowInitialized();
  },
  
  textNodesUnder : function(el){
    var n, a=[]
    var walk=document.createTreeWalker(el,NodeFilter.SHOW_TEXT,null,false);
    while(n=walk.nextNode()) {
      alert(n.textContent);
    };
  },
  
};




OneSpace.Controller.Utility = {
  
  bind : function(context, method, arguments) {
    if (!arguments) { arguments = new Array(); }
    return function() { return method.apply(context, arguments); };
  },

};






OneSpace.Controller.MessageHandler = function(controller) {
  this.controller = controller;
};

OneSpace.Controller.MessageHandler.prototype = {

  initialize : function() {
    var that = this;
    chrome.runtime.onMessage.addListener(function(message, sender, sendResponse) {
      //alert(JSON.stringify(message, null, 4)); 
      if (message.details.event == 'on-popup-window-initialized') {
        that.controller.onPopupWindowInitialized();
      } else if (message.details.event == 'on-user-id-received') {
       that.controller.onUserIdReceived(message.details.userId);
      } else if (message.details.event == 'reinitialize-locations') {
       that.controller.model.initializeLocations();
      } 
    });
  },
  

};  
  




oneSpaceController = new OneSpace.Controller();
oneSpaceController.initialize();


