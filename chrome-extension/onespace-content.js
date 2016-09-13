OneSpace = function() { };
OneSpace.prototype = { };



OneSpace.Content =  function() { };

OneSpace.Content.prototype = { };



OneSpace.Content.Controller =  function() {
  this.messageHandler = new OneSpace.Content.Controller.MessageHandler(this);
  this.contentView = new OneSpace.Content.View(this);
};

OneSpace.Content.Controller.prototype = {

  initialize : function() {
    this.messageHandler.initialize();
    this.contentView.initialize();
    
    var tempController = this;
    window.addEventListener("mouseup", function(event) { tempController.contentView.snapSelectionToWord(); tempController.contentView.highlightSelection(); });

  },
  
  onMapIconClicked : function() {
    this.contentView.toggleContentBox();
  },


  onChatIconClicked : function() {
    this.contentView.toggleContentBox();
  },


  onSettingsIconClicked : function() {
    this.contentView.toggleContentBox();
  },
  
  onGoogleMapsScriptLoaded : function() {
    alert('onGoogleMapsScriptLoaded');
  },

  test : function() {
    alert('BLA');
  },
  
  handleDOM : function(domContent) {
    var treeWalker = document.createTreeWalker(domContent, NodeFilter.SHOW_TEXT, function(node) { return NodeFilter.FILTER_ACCEPT; }, false);
    while(treeWalker.nextNode()) {
      var textContent = treeWalker.currentNode.textContent;
      if (textContent.split(" ").length >= 6 ) {
	console.log(treeWalker.currentNode.textContent);
      }
    };
  },
    

};




OneSpace.Content.Controller.MessageHandler = function(controller) {
  this.controller = controller;
};

OneSpace.Content.Controller.MessageHandler.prototype = {

  initialize : function() {
   
//     var tmpController = this.controller
//     chrome.runtime.onMessage.addListener(function(message, sender, sendResponse) {
//       if (message.action == "append-info-window") {
// 	tmpController.contentView.appendContentBox();
//         tmpController.contentView.appendInfoBox();
// 	tmpController.contentView.asyncLoadGoogleMap();
//       } 
//     });
  },
  

};  
 


OneSpace.Content.View = function(controller) {
  this.controller = controller;
};

OneSpace.Content.View.prototype = {

  initialize : function() {
    //var s = document.createElement('script');
    // TODO: add "script.js" to web_accessible_resources in manifest.json
    //s.src = chrome.extension.getURL('vlimsy-content-page.js');
    //s.onload = function() {
    // this.parentNode.removeChild(this);
    //};
    //(document.head||document.documentElement).appendChild(s);
    //alert("Blubb");
  },
  
  test : function() {
    alert('BLA');
  },
  
  asyncLoadGoogleMap : function() {
    
    $.getScript("http://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&callback=initialize")
      .done(function (script, textStatus) {            
	  alert("Google map script loaded successfully");
      })
      .fail(function (jqxhr, settings, ex) {
	  alert("Could not load Google Map script: " + ex);
      });
      
  },
  
  appendInfoBox : function() {
    var tempController = this.controller;
    
    var infoBoxHtmlString = 
   ['<div id="vlimsy-infobox">',
    '<table id ="vlimsy-infobox-table">',
    '<tr>',
    '<td><a><img id="vlimsy-icon-map" src="' + chrome.extension.getURL('/images/icon-map.png') + '" /></a></td>',
    '<td><a><img id="vlimsy-icon-chat" src="' + chrome.extension.getURL('/images/icon-chat.png') + '" /></a></td>',
    '<td><a><img id="vlimsy-icon-settings" src="' + chrome.extension.getURL('/images/icon-settings.png') + '" /></a></td>',
    '</tr>',
    '</table>',
    '</div>'
   ].join('\n');
    $('body').append(infoBoxHtmlString);

    $('#vlimsy-icon-map').click(function(){ tempController.onMapIconClicked(); });
    $('#vlimsy-icon-chat').click(function(){ tempController.onChatIconClicked(); });
    $('#vlimsy-icon-settings').click(function(){ tempController.onSettingsIconClicked(); });
  },
  
  appendContentBox : function() {
    var contentBoxHtmlString = 
    ['<div id="vlimsy-contentbox">',
      '<div>',
      '<ul id="tabnav">',
      '<li class="tab1"><a>Tab One</a></li>',
      '<li class="tab2"><a>Tab Two</a></li>',
      '</ul>',
      '</div>',
      '<div id="vlimsy-map-canvas"></div>',
      '</div>',
    ].join('\n');
    $('body').append(contentBoxHtmlString);

  },
  
  toggleContentBox : function() {
    $('#vlimsy-contentbox').toggle();
  },
  
};  
  




oneSpaceContentController = new OneSpace.Content.Controller();
oneSpaceContentController.initialize();


// Listen for messages
chrome.runtime.onMessage.addListener(function (msg, sender, sendResponse) {

    // If the received message has the expected format...
    if (msg.text === 'request-dom') {
        // Call the specified callback, passing
        // the web-pages DOM content as argument
        //sendResponse(document.all[0]);
	//oneSpaceContentController.handleDOM(document.body)
    }
});
