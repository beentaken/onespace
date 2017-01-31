OneSpacePopupModel =  new Array();


OneSpacePopupModel = function(controller) {
  this.controller = controller;
  this.openTabs = new Object();
  this.currentActiveTab = null;
  this.http = new OneSpacePopupModel.Http(this.controller);
  this.xmpp = new OneSpacePopupModel.Xmpp(this.controller);
  this.liveView = new OneSpacePopupModel.LiveView(this.controller);
  this.dummyTabId = 0;
  this.userId = -1;
};


OneSpacePopupModel.prototype = {

  initialize : function() {
    this.http.initialize();
    this.xmpp.initialize();
    this.initializeSettings();
    
    
    chrome.webRequest.onHeadersReceived.addListener(
      function(info) {
        var headers = info.responseHeaders;
        for (var i=headers.length-1; i>=0; --i) {
            var header = headers[i].name.toLowerCase();
            if (header == 'x-frame-options' || header == 'frame-options') {
          headers.splice(i, 1); // Remove header
            }
        }
        return {responseHeaders: headers};
      },
      {
        urls: [ '*://*/*' ], // Pattern to match all http(s) pages
        types: [ 'sub_frame' ]
      },
      ['blocking', 'responseHeaders']
    );
    
    
  },

  
  addTab : function(tab) {
    tab.vloc = '';
    tab.vlocSha1 = '';
    tab.vplaces = [];
    tab.previousVloc = ''
    this.openTabs[tab.id] = tab;
    //alert(JSON.stringify(this.openTabs, null, 4));
  },

  updateTab : function(tab) {
    var existingVloc = this.openTabs[tab.id].vloc;
    var existingVlocSha1 = this.openTabs[tab.id].vlocSha1;
    var existingVplaces = this.openTabs[tab.id].vplaces;
    var existingPreviousVloc = this.openTabs[tab.id].previousVloc;
    tab.vloc = existingVloc;
    tab.vlocSha1 = existingVlocSha1;
    tab.vplaces = existingVplaces;
    tab.previousVloc = '';
    this.openTabs[tab.id] = tab;
  },
  
  
  removeTab : function(tabId) {
    if (tabId in this.openTabs) {
      delete this.openTabs[tabId];
    }
  },

  
  onTabReplaced : function(tabDetails) {
    this.openTabs[tabDetails.tabId] = this.openTabs[tabDetails.replacedTabId];
    delete this.openTabs[tabDetails.replacedTabId];
    
    for (roomJid in this.xmpp.groupChatTabMap) {
      tabIds = this.xmpp.groupChatTabMap[roomJid];
      newTabIds = [];
      for (var i = 0; i < tabIds.length; i++) {
        var currentTabId = tabIds[i];
        if (currentTabId == tabDetails.replacedTabId) {
          newTabIds.push(tabDetails.tabId);
        } else {
          newTabIds.push(currentTabId);
        }
      }
      this.xmpp.groupChatTabMap[roomJid] = newTabIds;
    }
  },
  
  initializeSettings : function (){
    var that = this;
    chrome.storage.sync.get({
      multiloc: true,
      autotracking : true
    }, function(items) {
      that.controller.multipleLocations = items.multiloc;
      that.controller.autoTrackLocation = items.autotracking;
    });
  },
  

  getOpenTabIds : function() {
    var s = '';
    for (id in this.openTabs) {
      s = s + id.toString() + ', ';
    }
    return s;
  },
  
};






OneSpacePopupModel.Xmpp = function(controller) {
  this.controller = controller;
  this.connection = null;
  this.resource = '';
  this.server = '';
  this.httpBind = '';
  this.user =  "";
  this.password = "";
  this.groupChats = new Array();
  this.privateChats = new Array();
  this.groupChatTabMap = {};
  this.userGroupChat = null;
};


OneSpacePopupModel.Xmpp.prototype = {

  initialize : function() {
    var that = this;
    this.initializeXmppOptions();
    
    setInterval(OneSpacePopup.Controller.Utility.bind(this, this.sendProofOfLive), 60000);
  },
  
  sendProofOfLive : function() {
    if (this.connection != null) {
      if (this.connection.connected()) { 
        this.connection.send(new JSJaCPresence());
      }
    }
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
  
  
  unload : function() {
    this.leaveAllWebGroupChats();
    this.groupChats = new Array(); this.privateChats = new Array(); this.userGroupChat = null;
    if (this.connection != null) {
      if (this.connection.connected()) { 
        this.connection.disconnect(); 
      }	
    }
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
    this.connection.registerHandler('message', OneSpacePopup.Controller.Utility.bind(this, this.handleMessage));
    this.connection.registerHandler('presence', OneSpacePopup.Controller.Utility.bind(this, this.handlePresence));
    this.connection.registerHandler('iq', OneSpacePopup.Controller.Utility.bind(this, this.handleIQ));
    this.connection.registerHandler('onconnect', OneSpacePopup.Controller.Utility.bind(this, this.handleOnConnected));
    this.connection.registerHandler('onerror', OneSpacePopup.Controller.Utility.bind(this, this.handleOnError));
    this.connection.registerHandler('status_changed', OneSpacePopup.Controller.Utility.bind(this, this.handleStatusChanged));
    this.connection.registerHandler('ondisconnect', OneSpacePopup.Controller.Utility.bind(this, this.handleDisconnected));
    this.connection.registerIQGet('query', NS_VERSION, OneSpacePopup.Controller.Utility.bind(this, this.handleIqVersion));
    this.connection.registerIQGet('query', NS_TIME, OneSpacePopup.Controller.Utility.bind(this, this.handleIqTime));
  },
  
  
  handleOnConnected : function () {
    this.connection.send(new JSJaCPresence());
    //this.createUserGroupChat();
    this.controller.handleOnConnected();
  },

  
  handleMessage : function(oJSJaCPacket) {
    jid = oJSJaCPacket.getFromJID().toString();
    if (jid.indexOf("@"+this.resource+"."+this.server) >= 0) {
      this.handleGroupChatMessage(oJSJaCPacket);
    } else {
      this.handlePrivateChatMessage(oJSJaCPacket);
    }
  },

  
  handlePresence : function(oJSJaCPacket) {
    var jid = oJSJaCPacket.getFromJID().toString();
    if (jid.indexOf("@"+this.resource+"."+this.server) >= 0) {
      this.handleGroupChatPresence(oJSJaCPacket);
    } 
  },

  
  handleIQ : function(oIQ) {
    
  },

  
  handleOnError : function(e) {
    code = e.getAttribute("code");
    if (code == 401) 
      alert("Error: wrong user name or password (code: " + code + ")!");
    else if (code == 409) 
      alert("Error: user name already exists (code: " + code + ")!");
    else
      alert("Error: unknown problem (code: " + code + ")!");

    if (this.connection.connected()) { this.connection.disconnect(); }
  },

  
  handleStatusChanged : function(status) {
    
  },
  
  handleDisconnected : function () {
    
  },
  
  
  handleIqVersion : function (iq) {
    this.connection.send(
      iq.reply([
      iq.buildNode('name', 'jsjac simpleclient'),
      iq.buildNode('version', JSJaC.Version),
      iq.buildNode('os', navigator.userAgent)
      ])
    );
    return true;
  },
  
  
  handleIqTime : function (iq) {
    var now = new Date();
    this.connection.send(iq.reply([iq.buildNode('display',
          now.toLocaleString()),
          iq.buildNode('utc',
          now.jabberDate()),
          iq.buildNode('tz',
          now.toLocaleString().substring(now.toLocaleString().lastIndexOf(' ')+1))
      ]));
    return true;
  },
  

  handleGroupChatPresence : function(oJSJaCPacket) {
    var jid = oJSJaCPacket.getFromJID().toString();
    var elements = jid.split("/");
    var roomJid = elements[0]; 
    var user = elements[1];
    var userJid = user + '@' + this.server + '/' + this.resource;
    try {
      status = "unavailable";
      if (oJSJaCPacket.getType() != null) {
        if (oJSJaCPacket.getType().toString().toLowerCase() == "unavailable") {
          this.groupChats[roomJid].removeParticipant(userJid);
        }
      } else {
        this.groupChats[roomJid].addNewParticipant(userJid, user, "available");
        status = "available";
      }
      this.controller.handleGroupChatPresence(roomJid, user, status);
    } catch(e) {
      // this error is caused when leaving a group chat -> just ignore it
    }
  },
  
  handleGroupChatMessage : function(oJSJaCPacket) {
    from = oJSJaCPacket.getFromJID().toString();
    elements = from.split("/"); roomJid = elements[0]; user = elements[1];
    body = oJSJaCPacket.getBody().htmlEnc();
    if (body == "") { return; }
    try { 
      this.groupChats[roomJid].addNewMessage(from, body);
    } catch(e) { 
      
    }
    this.controller.handleIncomingMessage(roomJid, user, body, true);
  },
  

  joinWebGroupChat : function(tabId, url, vloc, vlocSha1, room, name, show) {
    
    if (tabId > 0) { // "Normally" called by main browser window
      this.joinTabGroupChat(tabId, vloc, vlocSha1, room, name, show, false);
    } else { // Called due to click on "Join Group Chat" on map => no TAB ID associated
      roomJid = room + "@" + this.resource + "." + this.server;
      if (roomJid in this.groupChats) { // Group Chat already open
        //tabId = this.groupChatTabs[roomJid][0];
        this.joinTabGroupChat(tabId, vloc, vlocSha1, room, name, show, true);
      } else { // Group chat does not yet exist => open new tab (is the more consistent solution than, e.g., dummy tabs)
        this.controller.openUrlInNewTab(url);
      }
    }
  },
  
   
  joinTabGroupChat : function(tabId, vloc, vlocSha1, room, name, show, forceTracking) {
    roomJid = room + "@" + this.resource + "." + this.server;
    // Check if joined this Group Chat using a different tab, if so "link" together
    if (roomJid in this.groupChats) {
      tabs = this.groupChatTabMap[roomJid];
      if(typeof tabs !== 'undefined') { 
        if (tabs.indexOf(tabId) < 0) { tabs.push(tabId); }
        this.controller.onWebGroupChatEntered(vloc, vlocSha1, roomJid, name, false, forceTracking);
        return;
      }
    }
    
    if (this.groupChats[roomJid] == null) {
      var str = room + "@" + this.resource + "." + this.server + "/" + this.user;
      var presence = new JSJaCPresence();
      presence.setTo(str);
      inode = presence.buildNode("item");
      inode.setAttribute("affiliation", "none");
      inode.setAttribute("jid", this.user + "@" + this.server + "/" + this.resource);
      inode.setAttribute("role", "participant");

      var xnode = presence.buildNode("x", [inode]);
      xnode.setAttribute("xmlns", "http://jabber.org/protocol/muc#user");
      
      presence.appendNode(xnode);
      presence.setStatus("available");
      presence.setShow(show);
      
      var success = false;
      if (this.connection != null) {
        if (this.connection.connected()) {
          success = this.connection.send(presence);
	  if (success == true) {
	    this.groupChats[roomJid] = new OneSpacePopupModel.Xmpp.GroupChat(this.controller, roomJid, name, GROUP_CHAT_TYPE_WEB);
	    var tabs = new Array();
	    tabs.push(tabId);
	    this.groupChatTabMap[roomJid] = tabs;
	    
	  }
        }
      }
      
      this.controller.onWebGroupChatEntered(vloc, vlocSha1, roomJid, name, true, forceTracking);
      
    } else {
      
    }

  },
  
  
  joinGroupChat : function(roomJid, roomName, userJid, show, forceTracking) {
    if (roomJid in this.groupChats) { return; }
    
    var str = roomJid + "/" + this.user;
    var presence = new JSJaCPresence();
    presence.setTo(str);
    inode = presence.buildNode("item");
    inode.setAttribute("affiliation", "none");
    inode.setAttribute("jid", this.user + "@" + this.server + "/" + this.resource);
    inode.setAttribute("role", "participant");

    var xnode = presence.buildNode("x", [inode]);
    xnode.setAttribute("xmlns", "http://jabber.org/protocol/muc#user");
    
    presence.appendNode(xnode);
    presence.setStatus("available");
    presence.setShow(show);
   
    var success = false;
    if (this.connection != null) {
      if (this.connection.connected()) {
        success = this.connection.send(presence);
        if (success == true) {
          this.groupChats[roomJid] = new OneSpacePopupModel.Xmpp.GroupChat(this.controller, roomJid, roomName, GROUP_CHAT_TYPE_DEFAULT);
        }
      }
    }
      
    this.controller.onGroupChatEntered(roomJid, roomName, true, forceTracking);

  },
  

  leaveGroupChat : function(roomJid) {
    var str = roomJid + "/" + this.user;
    var presence = new JSJaCPresence();
    presence.setTo(str);
    presence.setType("unavailable");
    presence.setStatus("unavailable");
    if (this.connection != null) {
      if (this.connection.connected()) {
        var success = this.connection.send(presence);
        if (success == true) {
          this.controller.onGroupChatLeft(roomJid);
        }
      }
    }    
    try { delete this.groupChats[roomJid]; } catch(e) {  }
  },

  
  
  leaveWebGroupChats : function(tab) {
    var tabId = tab.id;
    for (var i = 0; i < tab.vplaces.length; i++) {
      var vplaceId = tab.vplaces[i]['vplace_id'];
      var roomJid = vplaceId + "@" + this.resource + "." + this.server;
      
      tabs = this.groupChatTabMap[roomJid];
      if(typeof tabs === 'undefined'){ continue; }
      
      var index = tabs.indexOf(tabId);
      if (index > -1) { tabs.splice(index, 1); }
      
      if (tabs.length == 0) {
        this.leaveWebGroupChat(tabId, roomJid);
        try { delete this.groupChatTabMap[roomJid]; } catch(e) {  }
      }
      
    }
    
  },


  leaveWebGroupChat : function(tabId, roomJid) {
    var str = roomJid + "/" + this.user;
    var presence = new JSJaCPresence();
    presence.setTo(str);
    presence.setType("unavailable");
    presence.setStatus("unavailable");
    if (this.connection != null) {
      if (this.connection.connected()) {
        var success = this.connection.send(presence);
        if (success == true) {
          var tab = this.controller.model.openTabs[tabId]
          this.controller.onWebGroupChatLeft(tabId, tab.previousVloc ,roomJid);
        }
      }
    }    
    try { delete this.groupChats[roomJid]; } catch(e) {  }
  },

  

  
  leaveAllWebGroupChats : function(exemptTabIds) {
    for (tabId in this.controller.model.openTabs) {
      if (!(tabId in exemptTabIds)) {
        this.leaveWebGroupChats(this.controller.model.openTabs[parseInt(tabId)]);
      }
    }
  },
  
  
  handlePrivateChatMessage : function(oJSJaCPacket) {
    
    from = oJSJaCPacket.getFromJID().toString();
    user = from.split("@")[0];
    body = oJSJaCPacket.getBody().htmlEnc();
    console.log('handlePrivateChatMessage: ' + from);
    if (body == "") { return; }
    //this.startPrivateChat(from);
    this.controller.handleStartPrivateChatClick(from, false);
    this.privateChats[from].addNewMessage(from, body);
    
    this.controller.handleIncomingMessage(from, user, body, true);
  },
  
  startPrivateChat : function(from) {
    if (this.privateChats[from] == null) {
      this.privateChats[from] = new OneSpacePopupModel.Xmpp.PrivateChat(this.controller, from);
    }
  },
  
  closePrivateChat : function(from) {
    try { delete this.privateChats[from]; } catch(e) { }
  },
  
  sendMessage : function(jid, body) {
    if (body.toString() == "") return false;
    type = "";
    if (jid.indexOf("@"+this.resource+"."+this.server) >= 0) {
      this.sendGroupChatMessage(jid, body);
    } else {
      this.sendPrivateChatMessage(jid, body);
    }
  },
  
  
  sendGroupChatMessage : function(roomJid, body) {
    try {
      var aMsg = new JSJaCMessage();
      aMsg.setType("groupchat");
      aMsg.setTo(new JSJaCJID(roomJid));
      aMsg.setBody(body.toString());
      this.connection.send(aMsg);
      return true;
    } catch (e) {
      return false;
    }
  },
  
  sendPrivateChatMessage : function(toJid, body) {
    try {
      var aMsg = new JSJaCMessage();
      aMsg.setType("chat");
      aMsg.setTo(new JSJaCJID(toJid));
      aMsg.setBody(body.toString());
      this.connection.send(aMsg);
      this.privateChats[toJid].addNewMessage(this.userJid, body);
      var user = toJid.split('@')[0];
      this.controller.handleIncomingMessage(toJid, this.controller.model.xmpp.user, body, false);
      return true;
    } catch (e) {
      return false;
    }
  },
  
};



OneSpacePopupModel.Xmpp.GroupChat = function(controller, roomJid, roomName, type) {
  this.controller = controller; 
  this.roomName = roomName;
  this.roomJid = roomJid;
  this.room = roomJid.split("@")[0];
  this.participants = {};
  this.messages = new Array();
  this.type = type;
};


OneSpacePopupModel.Xmpp.GroupChat.prototype =  {
  
  addNewParticipant : function(jid, name, status) { 
    this.participants[jid] = new OneSpacePopupModel.Data.GroupChatParticipant(jid, name, status)
  },
  
  removeParticipant : function(jid) {
    try { 
      delete this.participants[jid]; 
    } catch(e) { 
    } 
  },
  
  addNewMessage : function(jid, text) { this.messages.push(new OneSpacePopupModel.Data.GroupChatMessage(jid, text)); },
  
  showHistory : function() {
    for(i = 0; i < this.messages.length; i++) {
      if (this.messages[i].user) {
        this.controller.handleIncomingMessage(this.roomJid, this.messages[i].user, this.messages[i].text, false);
      }
    }
  }
};


OneSpacePopupModel.Xmpp.PrivateChat = function(controller, contactJid) {
  this.controller = controller; 
  this.contactJid = contactJid;
  this.participants = {};
  this.user = contactJid.split("@")[0];
  this.participants[contactJid] = new OneSpacePopupModel.Data.GroupChatParticipant(this.contactJid, this.user, 'available');
  this.messages = new Array();
};

OneSpacePopupModel.Xmpp.PrivateChat.prototype =  {
  
  addNewMessage : function(from, text) { 
    this.messages.push(new OneSpacePopupModel.Data.PrivateChatMessage(from, text)); 
  },
  
  showHistory : function() {
    for(i = 0; i < this.messages.length; i++) {
      if (this.messages[i].user) {
        this.controller.handleIncomingMessage(this.contactJid, this.messages[i].user, this.messages[i].text, false);
      }
    }
  }
};


















OneSpacePopupModel.Http = function(controller) {
  this.controller = controller;
  this.locations = new Array();;
};



OneSpacePopupModel.Http.prototype = {
  
  initialize : function() {
    this.initializeApiOptions();
  },
  
  
  initializeApiOptions : function (){
    var that = this;
    chrome.storage.sync.get({
      apiBaseUrl: '',
      mapMaxPlaces: 100,
      mapMaxCorners: 100,
      mapMaxSurfers: 10,
      mapMaxWalkers: 10,
      dashboardMaxItems: 10
    }, function(items) {
      that.apiBaseUrl = items.apiBaseUrl;
      that.mapMaxPlaces = items.mapMaxPlaces;
      that.mapMaxCorners = items.mapMaxCorners;
      that.mapMaxSurfers = items.mapMaxSurfers;
      that.mapMaxWalkers = items.mapMaxWalkers;
      that.dashboardMaxItems = items.dashboardMaxItems;
    });
  },  
  
  
  unload : function() {
    //this.updateUserPloc(0, 0);
  },
  
  doAjaxPostRequest : function(serverUrl, method, data, dataType, processData, callback) {
    $.ajax({
      url: serverUrl, type: method, data: data, dataType: dataType, cache: false, contentType: false, processData: processData,
      success: function(response, textStatus, jqXHR){ callback(response); },
      error: function(jqXHR, textStatus, errorThrown){ 
	
      },
    });
  },

  
  handleUserLogin : function (userName, password, xmppHost, xmppResource) {
    //"http://172.29.32.195:11090/user/login/?name=homer&password=pwd&jid=barney@172.29.32.195&jidresource=conference"
    var jid = userName + "@" + xmppHost;
    var url = this.apiBaseUrl + "/user/login/?name="+userName+"&password="+password+"&jid="+jid+"&jidresource="+xmppResource;
    this.doAjaxPostRequest(url, "post", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onUserIDReceived));
  },
  
  onUserIDReceived : function(response) {
    var id = response["id"];
    this.controller.onUserIDReceived(id);
  },
  
  requestVloc : function(tab) {
    var url = this.apiBaseUrl + "/url/map/?tabid="+tab.id+"&url="+encodeURIComponent(tab.url)+"&unshorten=0";
    this.doAjaxPostRequest(url, "get", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onVlocReceived));
  },
  
  onVlocReceived : function(response) {
    //{"url":"http://www.marinabaysands.com/index.html","vloc":"www.marinabaysands.com","vloc-sha1":"9ae3562a174ccf1de97ad7939d39b505075bdc7a"}
    //alert(JSON.stringify(response, null, 4));
    this.controller.onVlocReceived(response);
  },
  
  requestLocations : function(mapBounds) {
    var ne = mapBounds.getNorthEast(); var sw = mapBounds.getSouthWest();
    var url = this.apiBaseUrl + "/places/box/?lat1="+ne.lat()+"&lng1="+ne.lng()+"&lat2="+sw.lat()+"&lng2="+sw.lng()+"&limit="+this.mapMaxPlaces;
    this.doAjaxPostRequest(url, "get", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onLocationsReceived));
  },

  onLocationsReceived : function(response) {
    this.locations = new Array();
    for (var i = 0; i < response.length; i++) {
      var location = new OneSpacePopupModel.Data.Location(response[i].id.toString(), response[i].name.toString(), response[i].vloc.toString(), response[i].website.toString(), response[i].lat.toString(), response[i].lng.toString(), response[i]["category_class"]);
      this.locations.push(location);
    }
    this.controller.onLocationsReceived(this.locations);
  },


  requestUserCorners : function(mapBounds) {
    var ne = mapBounds.getNorthEast(); var sw = mapBounds.getSouthWest();
    var url = this.apiBaseUrl + "/corners/box/?lat1="+ne.lat()+"&lng1="+ne.lng()+"&lat2="+sw.lat()+"&lng2="+sw.lng()+"&limit="+this.mapMaxCorners;
    this.doAjaxPostRequest(url, "get", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onUserCornersReceived));
  },

  onUserCornersReceived : function(response) {
    var userCorners = new Array();
    for (var i = 0; i < response.length; i++) {
      var userCorner = new OneSpacePopupModel.Data.UserCorner(response[i].id.toString(), response[i]['creator_id'].toString(), response[i]['creator_jid'].toString(), response[i]['creator_jid_resource'].toString(), response[i]['room_jid'].toString(), response[i]['room_jid_resource'].toString(), response[i]['name'].toString(), response[i]['description'].toString(), response[i]['lat'].toString(), response[i]['lng'].toString(), response[i]['created'].toString());
      userCorners.push(userCorner);
    }
    this.controller.onUserCornersReceived(userCorners);
  },

  deleteUserCorner : function(creatorId, roomJid) {
    var url = this.apiBaseUrl + "/corners/delete/?creatorid="+creatorId+"&roomjid="+roomJid;
    this.doAjaxPostRequest(url, "post", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onUserCornerDeleted));    
  },
  
  onUserCornerDeleted : function(response) {
    this.controller.onUserCornerDeleted(response);
  },
  
  requestNearSites : function(lat, lng, radius, limit) {
    var url = this.apiBaseUrl + "/places/near/?lat="+lat+"&lng="+lng+"&radius="+radius+"&limit="+limit;
    this.doAjaxPostRequest(url, "get", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onNearSitesReceived));
  },

  onNearSitesReceived : function(response) {
    var sites = new Array();
    for (var i = 0; i < response.length; i++) {
      var site = new OneSpacePopupModel.Data.Site(response[i].name.toString(), response[i].vloc.toString(), response[i].website.toString(), response[i].ploc[1].toString(), response[i].ploc[0].toString());
      sites.push(site);
    }
  },

  requestSurfers : function(mapBounds) {
    var ne = mapBounds.getNorthEast(); var sw = mapBounds.getSouthWest();
    var url = this.apiBaseUrl + "/surfers/box/?lat1="+ne.lat()+"&lng1="+ne.lng()+"&lat2="+sw.lat()+"&lng2="+sw.lng()+"&limit="+this.mapMaxSurfers;
    this.doAjaxPostRequest(url, "get", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onSurfersReceived));
  },
  
  onSurfersReceived : function(response) {
    var surfers = new Array();
    for (var i = 0; i < response.length; i++) {
      var surfer = new OneSpacePopupModel.Data.Surfer(response[i]["user_id"].toString(), response[i]["user_name"].toString(), response[i]["jid"].toString(), response[i]["resource"].toString(), response[i]["vloc"], response[i]["website"], response[i]["lat"], response[i]["lng"], response[i]["place_name"]);
      surfers.push(surfer);
    }
    this.controller.onSurfersReceived(surfers, response.locations);
  },
  
  requestWalkers : function(mapBounds) {
    var ne = mapBounds.getNorthEast(); var sw = mapBounds.getSouthWest();
    var url = this.apiBaseUrl + "/walkers/box/?lat1="+ne.lat()+"&lng1="+ne.lng()+"&lat2="+sw.lat()+"&lng2="+sw.lng()+"&limit="+this.mapMaxWalkers;
    this.doAjaxPostRequest(url, "get", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onWalkersReceived));
  },
  
  onWalkersReceived : function(response) {
    var walkers = new Array();
    for (var i = 0; i < response.length; i++) {
      var walker = new OneSpacePopupModel.Data.Walker(response[i]["user_id"].toString(), response[i]['user_name'].toString(), response[i]["jid"].toString(), response[i]["resource"].toString(), response[i]["lat"], response[i]["lng"]);
      walkers.push(walker);
    }
    this.controller.onWalkersReceived(walkers);
  },
  
  updateUserPloc : function(lat, lng) {
    var url = this.apiBaseUrl + "/user/ploc/?userid="+this.model.userId+"&lat="+lat+"&lng="+lng;
    this.doAjaxPostRequest(url, "put", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onUserPlocUpdated));
    this.requestNearSites(lat, lng, 100, 10);
  },
  
  onUserPlocUpdated : function(xmlResponse) {
    //dump("onGeoLocationUpdated\n");
  },
  
  updateUserVloc : function(userId, action, vloc, doAsyncRequest) {
    var url = this.apiBaseUrl + "/user/vloc/?userid="+userId.toString()+"&action="+action+"&vloc="+vloc;
    this.doAjaxPostRequest(url, "put", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onUserVlocUpdated));
  },
  
  onUserVlocUpdated : function(response) {
    //alert("onUserVlocUpdated\n");
    //alert(JSON.stringify(response, null, 4));
  },

  
  requestPloc : function(vloc, limit) {
    var url = this.apiBaseUrl + "/places/ploc/?vloc="+vloc+"&limit="+limit;
    this.doAjaxPostRequest(url, "get", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onPlocReceived));
  },
   
  onPlocReceived : function(response) {
    //alert(JSON.stringify(response, null, 4));
  },  
  
  requestData : function(tabId, type, vloc, vlocSha1 ) {
    var url = this.apiBaseUrl + "/data/?tabid="+tabId+"&type="+type+"&vloc="+vloc+"&vlocsha1="+vlocSha1+"&limit="+this.dashboardMaxItems;
    this.doAjaxPostRequest(url, "get", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onDataReceived));
  },
  
  onDataReceived : function(response) {
    var type = response.type;
    var data = response.data;
    var tabId = response.tabid;
    this.controller.onDataReceived(tabId, type, data);
  },
  
  
  uploadMediaFile : function(fromJid, fromJidResource, toJid, toJidResource, formData) {
    //"http://172.29.33.45:11090/media/upload/?fromjid=homer@172.29.33.45&fromjidresource=conference&tojid=carl@172.29.33.45&tojidresource=conference"
    var url = this.apiBaseUrl + "/media/upload/?fromjid="+fromJid+"&fromjidresource="+fromJidResource+"&tojid="+toJid+"&tojidresource="+toJidResource;
    this.doAjaxPostRequest(url, "post", formData, "json", false, OneSpacePopup.Controller.Utility.bind(this, this.onUploadResponseReceived));
  },
  
  onUploadResponseReceived : function(response) {
    this.controller.onUploadResponseReceived(response)
  },
  
  createNewUserCorner : function(userId, userName, userJid, userJidResource, name, description, lat, lng) {
    var url = this.apiBaseUrl + "/corners/add/?creatorid="+userId+"&creatorname="+userName+"&creatorjid="+userJid+"&creatorjidresource="+userJidResource+"&name="+name+"&description="+description+"&lat="+lat+"&lng="+lng;
    this.doAjaxPostRequest(url, "post", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onCreateUserCornerResponseReceived));
  },
  
  
  onCreateUserCornerResponseReceived : function(response) {
    if ('errorcode' in response) {
      alert(response['errormsg']);
    } else {
      this.controller.onCreateUserCornerResponseReceived(response);
    }
  },

  
  requestWalkersAroundVloc(vloc, maxDistance) {
    var url = this.apiBaseUrl + "/walkers/vloc/?vloc="+vloc+"&maxdistance="+maxDistance;
    console.log(url);
    this.doAjaxPostRequest(url, "get", { }, "json", true, OneSpacePopup.Controller.Utility.bind(this, this.onWalkersAroundVlocReceived));
  },
  
  
  onWalkersAroundVlocReceived : function(response) {
    this.controller.onWalkersAroundVlocReceived(response);
  },
};





OneSpacePopupModel.LiveView = function(controller) {
  this.controller = controller;
  this.surferFollowers = {};
  this.surferGuides = {};
  this.walkerFollowers = {};
  this.walkerGuides = {};

};



OneSpacePopupModel.LiveView.prototype = {
  
  initialize : function() {
    //
  },

  
  addFollower : function(source) {
    if (source.role == 'surfer') {
      this.surferFollowers[source.jid] = new OneSpacePopupModel.Data.LiveViewSurfer(source.jid, source.user);
    } else if (source.role == 'walker') {
      this.walkerFollowers[source.jid] = new OneSpacePopupModel.Data.LiveViewWalker(source.jid, source.user);
    }
  },
  
  removeFollower : function(source) {
    try {
      if (source.role == 'surfer') {
        delete this.surferFollowers[source.jid]; 
      } else if (source.role == 'walker') {
        delete this.walkerFollowers[source.jid]; 
      }
    } catch(e) { } 
  },

  addGuide : function(source) {
    if (source.role == 'surfer') {
      var url = source.command['url'];
      this.surferGuides[source.jid] = new OneSpacePopupModel.Data.LiveViewSurfer(source.jid, source.user, url);
    } else if (source.role == 'walker') {
      this.walkerGuides[source.jid] = new OneSpacePopupModel.Data.LiveViewWalker(source.jid, source.user);
    }
  },
  
  removeGuide : function(source) {
    try {
      if (source.role == 'surfer') {
        delete this.surferGuides[source.jid]; 
      } else if (source.role == 'walker') {
        delete this.walkerGuides[source.jid]; 
      }
    } catch(e) { } 
  },



  
};





OneSpacePopupModel.Data = function() { };

OneSpacePopupModel.Data.Location = function(id, name, vloc, url, lat, lng, category) { this.id = id, this.name = name; this.vloc = vloc; this.url = url; this.lat = lat; this.lng = lng; this.category = category; };
OneSpacePopupModel.Data.UserCorner = function(id, creatorId, creatorJid, creatorJidResource, roomJid, roomJidResource, name, description, lat, lng, created) { this.id = id, this.creatorId = creatorId, this.creatorJid = creatorJid; this.creatorJidResource = creatorJidResource; this.roomJid = roomJid; this.roomJidResource = roomJidResource; this.name = name; this.description = description; this.lat = lat; this.lng = lng; this.created = created; };
OneSpacePopupModel.Data.Surfer = function(id, name, jid, jidResource, vloc, url, lat, lng, placeName) { this.id = id; this.name = name; this.jid = jid; this.jidResource = jidResource; this.vloc = vloc; this.url = url; this.lat = lat; this.lng = lng; this.placeName = placeName; this.fullJid = jid+"/"+jidResource};

OneSpacePopupModel.Data.Walker = function(id, name, jid, jidResource, lat, lng) { this.id = id; this.name = name; this.jid = jid; this.jidResource = jidResource; this.lat = lat; this.lng = lng; this.fullJid = jid+"/"+jidResource};

OneSpacePopupModel.Data.GroupChatMessage = function(from, text) { this.from = from; this.text = text; this.room = this.from.split("@")[0]; this.user = this.from.split("/")[1]; };
OneSpacePopupModel.Data.PrivateChatMessage = function(from, text) { this.from = from; this.text = text; this.user = this.from.split("@")[0]; };

OneSpacePopupModel.Data.GroupChatParticipant = function(jid, name, status) { this.jid = jid; this.name = name; this.status = status; };

OneSpacePopupModel.Data.LiveViewSurfer = function(jid, name, url) { this.jid = jid; this.user = user; this.url = url; this.imageBase64 = ''};
OneSpacePopupModel.Data.LiveViewWalker = function(jid, name) { this.jid = jid; this.user = user; ; this.imageBase64 = '' };