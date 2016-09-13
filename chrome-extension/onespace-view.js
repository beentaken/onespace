var OneSpaceView = new Array();



OneSpaceView = function(controller) {
  this.controller = controller;
  this.popupWindow = new OneSpaceView.PopupWindow(controller);
  
};

OneSpaceView.prototype = {

  initialize : function() {
    this.popupWindow.initialize();
  },
    
  onWindowClosed : function(windowId) {

  },
  
  updatePopupWindow : function() {
    var details = {};
      details.event = 'onespace-popup-opened';
      details.window = this.popupWindow.window;
      chrome.runtime.sendMessage({ details: details }, function(response) {});
  },
  
  
};  
  



OneSpaceView.PopupWindow = function(controller) {
  this.controller = controller;
  this.window = null;
};

OneSpaceView.PopupWindow.prototype = {
  
  initialize : function() {
    
  },
  
  setWindow : function(window) {
    this.window = window;
  },
 
  unsetWindow : function() {
    this.window = null;
  },

  isPopupWindow : function(windowId) {
    if (this.window.id == windowId) {
      return true;
    }
    return false;
  },
  
  onTabSelectionChanged : function(tab) {
    if (this.isPopupWindow(tab.windowId)) { return; }
    
    var details = {};
    details.event = 'onespace-new-tab-selected';
    details.tab = tab;
    chrome.runtime.sendMessage({ details: details }, function(response) {})
  },

  
  onTabAdded : function(tab) {
    if (this.isPopupWindow(tab.windowId)) { return; }
    
    var details = {};
    details.event = 'onespace-tab-added';
    details.tab = tab
    chrome.runtime.sendMessage({ details: details }, function(response) {})
  },
  
  onTabRemoved : function(tabId) {
    var details = {};
    details.event = 'onespace-tab-removed';
    details.tabId = tabId;
    chrome.runtime.sendMessage({ details: details }, function(response) {})
  },
  
  
  onTabReplaced : function(tabDetails) {
    var details = {};
    details.event = 'onespace-tab-replaced';
    details.tabDetails = tabDetails;
    chrome.runtime.sendMessage({ details: details }, function(response) {})
  },
  
  onNewPageLoaded : function(tab) {
    if (this.isPopupWindow(tab.windowId)) { return; }
    
    var details = {};
    details.event = 'onespace-new-page-loaded';
    details.tab = tab;
    chrome.runtime.sendMessage({ details: details }, function(response) {})
  },


  

  
};


