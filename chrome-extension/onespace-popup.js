
const CSS_CLASS_CHAT_PRESENCE = "chat-presence";
const CSS_CLASS_CHAT_PRESENCE_USER = "chat-presence-user";
const CSS_CLASS_CHAT_PRESENCE_INFO = "chat-presence-info";
const CSS_CLASS_CHAT_PARTICIPANTS = "chat-participants";
const CSS_CLASS_CHAT_PARTICIPANTS_HEAD = "chat-participants-head";
const CSS_CLASS_CHAT_PARTICIPANTS_USER = "chat-participants-user";
const CSS_CLASS_CHAT_MESSAGE = "chat-message";
const CSS_CLASS_CHAT_MESSAGE_SENDER = "chat-message-sender";
const CSS_CLASS_CHAT_MESSAGE_TEXT = "chat-message-text";

const CSS_CLASS_RADAR_USER = "radar-user";

const CSS_CLASS_LIVEVIEW_USERS = "liveview-users";
const CSS_CLASS_LIVEVIEW_GUIDES = "liveview-guides";
const CSS_CLASS_LIVEVIEW_FOLLOWERS = "liveview-followers";

const CSS_CLASS_DASHBOARD_DATA_TWEET = "dashboard-data-tweet";
const CSS_CLASS_DASHBOARD_DATA_YOUTUBE_VIDEO = "dashboard-data-youtube-video";
const CSS_CLASS_DASHBOARD_DATA_FLICKR_IMAGES = "dashboard-data-flickr-images";
const CSS_CLASS_DASHBOARD_DATA_INSTAGRAM_IMAGES = "dashboard-data-instagram-images";
const CSS_CLASS_DASHBOARD_DATA_NEA = "dashboard-data-nea";
const CSS_CLASS_DASHBOARD_DATA_LTA = "dashboard-data-lta";

const GROUP_CHAT_TYPE_WEB = 1;
const GROUP_CHAT_TYPE_DEFAULT = 0;

const XMPP_CHAT_COMMAND_PREFIX = "[CMD]";

const IMAGE_MEDIA_FILE_SUFFIXES = {'jpg' : 1, 'jpeg' : 1, 'png' : 1, 'gif' : 1, 'gifv' : 1};




String.prototype.capitalizeFirstLetter = function() {
  return this.charAt(0).toUpperCase() + this.slice(1);
}

String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};



OneSpacePopup =  function() { };

OneSpacePopup.prototype = { };



OneSpacePopup.Controller =  function() {
  this.messageHandler = new OneSpacePopup.Controller.MessageHandler(this);
  this.model = new OneSpacePopupModel(this);
  this.view = new OneSpacePopupView(this);
  this.multipleLocations = true;
  this.autoTrackLocation = true;
  this.windowId = -1;
  
  this.prerendered = false;
};

OneSpacePopup.Controller.prototype = {
  
  initialize : function() {
    this.messageHandler.initialize();
    this.model.initialize();
    this.view.initialize();
  },
    
  
  
  onWindowClosed : function(windowId) {
    alert(windowId);
  },
  
  setOption : function(option, value) {
    var o = new Object()
    o[option] = value
    chrome.storage.sync.set(o);
  },

  getOption : function(option, callback) {
    chrome.storage.sync.get(option, function(items) {
      callback(items);
    });
  },
  
  handleMapLegendCheckboxClick : function (action, target, isChecked) {
    switch (action) {

      case 'show':
        
        switch (target) {
          case 'locations':
            this.toggleLocations(isChecked);
            break;
            case 'corners':
            this.toggleUserCorners(isChecked);
            break;
          case 'surfers':
            this.toggleSurfers(isChecked);
            break;
          case 'walkers':
            this.toggleWalkers(isChecked);
            break;
        } 
      
        break;
    } 
    
  },
  
  
  handleSettingsCheckboxClick : function (target, isChecked) {
    switch (target) {
      case 'multiloc':
        this.multipleLocations = isChecked;
        this.setOption(target, isChecked);
        var exemptGroupChatJids = new Object();
        if (isChecked == false) {
          exemptGroupChatJids[this.model.currentActiveTab.id] = 1;
          this.model.xmpp.leaveAllWebGroupChats(exemptGroupChatJids);
        } else {
          // First, leave all group chats (should only be one anyway)
          this.model.xmpp.leaveAllWebGroupChats(exemptGroupChatJids);
          // Re-initialize all locations from scratch
          var details = {};
          details.event = 'reinitialize-locations';
          chrome.runtime.sendMessage({ details: details }, function(response) {});
        }
        break;
      case 'autotracking':
        this.autoTrackLocation = isChecked;
        this.setOption(target, isChecked);
        if (isChecked == true) {
          var tab = this.model.currentActiveTab;
        }
        break;
    } 
  },
  
  
  
  
  handleOnConnected : function () {
    this.model.http.handleUserLogin(this.model.xmpp.user, this.model.xmpp.password, this.model.xmpp.server, this.model.xmpp.resource);
  },
  
  onUserIDReceived : function(id) { 
    this.model.userId = id; 
    var details = {};
    details.event = 'on-user-id-received';
    details.userId = id;
    chrome.runtime.sendMessage({ details: details }, function(response) {});
  },
  
  requestLocations : function(mapBounds) { this.model.http.requestLocations(mapBounds); },
  
  requestUserCorners : function(mapBounds) { this.model.http.requestUserCorners(mapBounds); },
  
  requestSurfers : function(mapBounds) { this.model.http.requestSurfers(mapBounds); },
  
  requestWalkers : function(mapBounds) { this.model.http.requestWalkers(mapBounds); },
  
  onLocationsReceived : function(locations) { this.view.map.onLocationsReceived(locations); },
  
  onUserCornersReceived : function(locations) { this.view.map.onUserCornersReceived(locations); },
  
  onSurfersReceived : function(surfers, locationsMap) { this.view.map.onSurfersReceived(surfers, locationsMap); },
 
  onWalkersReceived : function(walkers) { this.view.map.onWalkersReceived(walkers); },
  
  
  toggleLocations : function(isChecked) {
    this.view.map.toggleLocationFilterLegend(isChecked);
    if (isChecked == false) {
      this.view.map.clearLocationsOverlay();
      this.view.map.showLocations = false;
    } else {
      this.requestLocations(this.view.map.map.getBounds());
      this.view.map.showLocations = true;
    }
  },
  
  toggleUserCorners : function(isChecked) {
    if (isChecked == false) {
      this.view.map.clearUserCornersOverlay();
      this.view.map.showUserCorners = false;
    } else {
      this.requestUserCorners(this.view.map.map.getBounds());
      this.view.map.showUserCorners = true;
    }
  },

  toggleSurfers : function(isChecked) {
    if (isChecked == false) {
      this.view.map.clearSurfersOverlay();
      this.view.map.showSurfers = false;
    } else {
      this.requestSurfers(this.view.map.map.getBounds());
      this.view.map.showSurfers = true;
    }    
  },

  toggleWalkers : function(isChecked) {
    if (isChecked == false) {
      this.view.map.clearWalkersOverlay();
      this.view.map.showWalkers = false;
    } else {
      this.requestWalkers(this.view.map.map.getBounds());
      this.view.map.showWalkers = true;
    }    
  },
  
  onTabAdded : function(tab) {
    this.model.addTab(tab);
    
    if (tab.active) { 
      this.model.currentActiveTab = tab; 
    }
    
    if (OneSpacePopup.Controller.Utility.isValidUrl(tab.url) == true) {
      this.model.http.requestVloc(tab);
    }
    
    this.prerendered = false;
  },
  
  
  onNewPageLoaded : function(tab) {
    var that = this;
    var oldTab = this.model.openTabs[tab.id];
    var prevVloc =  oldTab.vloc;

    this.model.updateTab(tab); // It's actually more overwriting the tab here (but needed!)

    tab.previousVloc = prevVloc
    
    if (tab.active) { 
      this.model.currentActiveTab = tab; 
      this.onNewPageOnDisplay(tab.url);
    }
    
    if (OneSpacePopup.Controller.Utility.isValidUrl(tab.url) == true) {
      this.model.http.requestVloc(tab);
    }

    this.prerendered = false;
  },
  

  onNewTabSelected : function(tab) {
    if (this.prerendered == true) {
      this.prerendered = false;
      return;
    }
    this.prerendered = false;
    
    var previousActiveTab = this.model.currentActiveTab;
    this.model.currentActiveTab = tab;
    this.model.openTabs[tab.id].active = true;
    try { this.model.openTabs[previousActiveTab.id].active = false; } catch(e) {  } // Needed if tab activated due to closed active tab
    var joinGroupChat = true;
    if (OneSpacePopup.Controller.Utility.isValidUrl(tab.url) == false) { joinGroupChat = false; }

    if (joinGroupChat == true) {
      if (this.multipleLocations == false) {
        try { this.model.xmpp.leaveWebGroupChats(previousActiveTab); } catch(e) {  } // Needed if tab activated due to closed active tab
      }
      var openTab = this.model.openTabs[tab.id];
      this.joinAllVPlacesGroupChats(openTab);
    }
    
    this.onNewPageOnDisplay(tab.url);
  },


  onTabRemoved : function(tabId) {
    var tab = this.model.openTabs[tabId];
    this.model.xmpp.leaveWebGroupChats(tab);
    this.model.removeTab(tabId);
  },
  
  onTabReplaced : function(tabDetails) {
    this.prerendered = true;
    this.model.onTabReplaced(tabDetails);
  },
  
  onVlocReceived : function(response) {
    // If tab id < 0, the "Join Group Chat" on the map has been clicked
    if (response.tabid < 0) {
      this.joinAllVPlacesGroupChats(response);
      return;
    }
    
    var tab = this.model.openTabs[response.tabid];
    if(typeof tab === 'undefined'){ return; }
    
    //alert(JSON.stringify(response, null, 4));
    var joinGroupChat = true;
    // Check if multiple locations are supported -- if not, only switch if the tab is the active one
    if ((this.multipleLocations == false) && (tab.active == false)) { joinGroupChat = false; }
    // Check if the URL is a valid one
    if (OneSpacePopup.Controller.Utility.isValidUrl(tab.url) == false) { joinGroupChat = false; }
    // Check if the new location -> jid <- is the same -- if so, nothing to do

    if (joinGroupChat == true) {
      if (tab.active == true) {
        this.model.xmpp.leaveWebGroupChats(tab);
      }
    }

    tab.vloc = response['vloc'];
    tab.vlocSha1 = response['vloc-sha1'];
    tab.vplaces = response['vplaces'];
    
    if (tab.vplaces.length == 0) {
      var vplace = {};
      vplace['vplace_id'] = tab.vlocSha1;
      vplace['name'] = tab.title;
      tab.vplaces.push(vplace);
    }

    if (joinGroupChat == true) {
      this.joinAllVPlacesGroupChats(tab);
    }

  },
  
  
  joinAllVPlacesGroupChats : function(tab) {
    //alert(JSON.stringify(tab, null, 4));
    for (var i = 0; i < tab.vplaces.length; i++) {
      var vplaceId = tab.vplaces[i]['vplace_id'];
      var vplaceName = tab.vplaces[i]['name'];
      if(vplaceName == ''){
	vplaceName = tab.title + ' (' + vplaceId + ')';
      } else {
	vplaceName = vplaceName + ' (' + vplaceId + ')';
      }  
      this.model.xmpp.joinWebGroupChat(tab.id, tab.url, tab.vloc, tab.vlocSha1, vplaceId, vplaceName, 'chat');
    }
    
  },
  
  handleAutoTracking : function(roomJid, forceTracking) {
    if (this.autoTrackLocation == true) {
      var tabs = this.model.xmpp.groupChatTabMap[roomJid];
      if(typeof tabs === 'undefined'){ return; }
      for (var i = 0; i < tabs.length; i++) {
	var tab = this.model.openTabs[tabs[i]];
	if (typeof tab === 'undefined') { continue; }
	if ((tab.active == true) || (forceTracking == true)){
	  this.view.chat.onNewLocationSelected(roomJid);
	  this.view.chat.updateParticipantsList(roomJid);
	  this.view.radar.updateSurfersList(roomJid);
      this.onRadarViewSelected();
	  return;
	}
      }
    }
    
  },
  
    
  
  onWebGroupChatEntered : function(vloc, vlocSha1, roomJid, pageTitle, initialEntering, forceTracking) {
    if (initialEntering == true) {
      //var vloc = roomJid.split('@')[0];
      this.view.chat.addGroupChatToList(roomJid, pageTitle, 'WGC');
      this.model.http.updateUserVloc(this.model.userId, 'add', vloc, true);
      this.model.http.requestPloc(vloc, 1);
    }
    this.handleAutoTracking(roomJid, forceTracking);
  },

  
  onGroupChatEntered : function(roomJid, roomName, initialEntering, forceTracking) {
    if (initialEntering == true) {
      //var vloc = roomJid.split('@')[0];
      this.view.chat.addGroupChatToList(roomJid, roomName, 'CGC');
      this.view.chat.onNewLocationSelected(roomJid);
      this.view.chat.updateParticipantsList(roomJid);
      this.view.radar.updateSurfersList(roomJid);
    }
    this.handleAutoTracking(roomJid, forceTracking);
  },
  
  
  onWebGroupChatLeft : function(tabId, vloc, roomJid) {
    var tab = this.model.openTabs[tabId];
    this.view.chat.removeGroupChatFromList(roomJid);
    this.model.http.updateUserVloc(this.model.userId, 'remove', tab.vloc, true);
    this.model.http.requestPloc(vloc, 1);
    var selectedJid = $("#chat-select").val();
    this.view.chat.switchToChat(selectedJid);
    tab.previousVloc = vloc;
  },
  
  
  onGroupChatLeft : function(roomJid) {
    this.view.chat.removeGroupChatFromList(roomJid);
    var selectedJid = $("#chat-select").val();
    this.view.chat.switchToChat(selectedJid);
  },

  
  handleGroupChatPresence : function(roomJid, user, status) {
    if (roomJid == this.view.chat.currentDisplayedChat) {
      this.view.chat.updateParticipantsList(roomJid);
      this.view.radar.updateSurfersList(roomJid);
      this.view.chat.displayNewGroupChatPresence(user, status);
    }
  },
  
  handleIncomingMessage : function(jid, user, body, processCommands) {    
    body = OneSpacePopup.Controller.Utility.unescapeHtml(body);
    try {
      bodyJson = jQuery.parseJSON(body);
    } catch (e) {
      if (jid == this.view.chat.currentDisplayedChat) {
        this.view.chat.displayNewMessage(jid, user, body);
      }
    }
    
    //console.log(bodyJson);
    
    var messageType = bodyJson["message-type"];
    switch (messageType) {
      case 'chat':
        this.handleChatMessage(jid, user, bodyJson)
        break;
      case 'cmd':
        if (processCommands == true) {
          this.handleCommandMessage(jid, user, bodyJson);
        }
        break;
    }
  },
  

  handleChatMessage : function(jid, user, bodyJson) {
    var media = bodyJson["media"];
    switch (media) {
      case 'text':
	this.handleChatTextMessage(jid, user, bodyJson);
        break;
      case 'image':
	this.handleChatImageMessage(jid, user, bodyJson);
        break;
    }
  },
  


  handleChatTextMessage : function(jid, user, bodyJson){
    if (jid == this.view.chat.currentDisplayedChat) {
      msg = bodyJson["content"];
      this.view.chat.displayNewMessage(jid, user, msg);
    }
  },
  
  handleChatImageMessage : function(jid, user, bodyJson){
    if (jid == this.view.chat.currentDisplayedChat) {
      msg = '<a href="' + bodyJson["image-link"] + '" target="_blank"><img src="' + bodyJson["thumbnail-link"] + '" /></a>';
      this.view.chat.displayNewMessage(jid, user, msg);
    }
  },

  
  handleSendMessageClick : function() {
    var message = $('#chat-send-message-text').val();
    var body = { "message-type" : "chat", "media" : "text" , "content" : message};
    this.model.xmpp.sendMessage(this.view.chat.currentDisplayedChat, JSON.stringify(body));
    $('#chat-send-message-text').val('');
  },
  
  

  
  handleStartPrivateChatClick : function(userJid, forceTracking) {
    // Open Chat (Check if already exists)
    var isNewPrivateChat = false;
    if (this.model.xmpp.privateChats[userJid] == null) {
      this.model.xmpp.startPrivateChat(userJid);
      this.view.chat.addPrivateChatToList(userJid);
      isNewPrivateChat = true;
    }
    if ((forceTracking == true) || (isNewPrivateChat == true)) {
      // Clear received messages
      this.view.chat.switchToChat(userJid);
      this.view.showContentDiv('chat');
    }
  },
  
  handleCloseChatClick : function() {
    var jid = this.view.chat.currentDisplayedChat;
    if (jid in this.model.xmpp.groupChats) {
      this.model.xmpp.leaveGroupChat(jid);
    } else {
      this.view.chat.removeGroupChatFromList(jid);
      this.model.xmpp.closePrivateChat(jid);
      var selectedJid = $("#chat-select").val();
      this.view.chat.switchToChat(selectedJid);
    }
  },


  handleShareMediaClick : function(event) {
    alert("handleShareMediaClick");
  },

  handleShareMediaFileSelected : function(formData){
    var fromJid = this.model.xmpp.user + "@" + this.model.xmpp.server;
    var fromJidResource = this.model.xmpp.resource;
    var toFullJid = this.view.chat.currentDisplayedChat;
    if (toFullJid.indexOf("@"+this.model.xmpp.resource+"."+this.model.xmpp.server) >= 0) {
      var toJid = toFullJid;
      var toJidResource = '';
    } else {
      var toJid = toFullJid.split('/')[0];
      var toJidResource = toFullJid.split('/')[1];
    }
    this.model.http.uploadMediaFile(fromJid, fromJidResource, toJid, toJidResource, formData);
  },
  
  
  onUploadResponseReceived : function(response) {
    var that = this;
    $.each(response, function(key, value) {
      var thumbnailLink = value['thumbnail-link'];
      var imageLink = value['image-link'];
      var toJid = value["to-jid"].trim()
      var toJidResource = value["to-jid-resource"].trim()
      if (toJidResource != '') {
	toJid = toJid + '/' + toJidResource;
      }
      var body = "<a href='"+imageLink+"'><img src='"+thumbnailLink+"' />";
      var body = { "message-type" : "chat", "media" : "image", "thumbnail-link" : thumbnailLink, "image-link" : imageLink, "caption" : ''}
      that.model.xmpp.sendMessage(toJid, JSON.stringify(body));
    });
  },
  
  
  handleJoinWebGroupChatClick : function(url) {
    // DO NOT JOIN DIRECTLY THE GROUP CHAT!!!
    // You have to fetch the vplace_ids first. The location.vloc is NOT the name of the group chat!!!
    var dummyTab = {};
    dummyTab['id'] = -1;
    dummyTab['url'] = url;
    this.model.http.requestVloc(dummyTab);
    //switch to chat view
    this.view.showContentDiv('chat');
  },
  
  handleOpenWebsiteClick : function(url) {
    var win = window.open(url, '_blank');
    win.focus();
  },

  
  handleJoinUserCornerGroupChatClick : function(roomJid, roomName, userJid) {
    this.model.xmpp.joinGroupChat(roomJid, roomName, userJid, true, true)
    //switch to chat view
    this.view.showContentDiv('chat');
  },
  
  handleDeleteUserCornerClick : function(userId, roomJid) {
    this.model.http.deleteUserCorner(userId, roomJid);
  },
  
  
  onUserCornerDeleted : function(response) {
    this.view.map.infoPanel.close();
    this.requestUserCorners(this.view.map.map.getBounds());
  },
  
  
  openUrlInNewTab : function(url) {
    var that = this;
    var winId = -1;
    chrome.windows.getAll({ populate: true}, function(windows) {
      for (var w = 0; w < windows.length; w++) {
        var win = windows[w];
        if (win.id != that.windowId) {
          winId = win.id;
        }
        chrome.tabs.create({ windowId : winId,  url: url });
        return;
      }
    });
    
  },
  
 
  requestData : function(target) {
    var currentTabId = this.model.currentActiveTab.id;
    var vloc = this.model.openTabs[currentTabId].vloc;
    var vlocSha1 = this.model.openTabs[currentTabId].vlocSha1;
    if (vlocSha1 != '') {
      this.model.http.requestData(currentTabId, target, vloc, vlocSha1 );
    } else{
      alert('Not a valid website');
    }
  },
  
  
  onDataReceived : function(tabId, type, data) {
    switch (type) {
      case 'twitter':
        this.view.dashboard.setHeading('Latest Tweets for "' + this.model.openTabs[tabId].title + '"');
        this.view.dashboard.displayLatestTweets(data);
        break;
      case 'youtube':
        this.view.dashboard.setHeading('YouTube videos for "' + this.model.openTabs[tabId].title + '"');
        this.view.dashboard.displayYoutubeVideos(data);
        break;
      case 'flickr':
        this.view.dashboard.setHeading('Flickr images for "' + this.model.openTabs[tabId].title + '"');
        this.view.dashboard.displayFlickrImages(data);
        break;
      case 'instagram':
        this.view.dashboard.setHeading('Instagram images for "' + this.model.openTabs[tabId].title + '"');
        this.view.dashboard.displayInstagramImages(data);
        break;
      case 'nea':
        this.view.dashboard.setHeading('2h Nowcast for "' + this.model.openTabs[tabId].title + '"');
        this.view.dashboard.displayNeaNowcast(data);
        break
      case 'lta':
        this.view.dashboard.setHeading('Carpark Availability for "' + this.model.openTabs[tabId].title + '"');
        this.view.dashboard.displaycarparkAvailability(data);
        break;
      case 'ltabuses':
        this.view.dashboard.setHeading('Bus Stop Information for "' + this.model.openTabs[tabId].title + '"');
        this.view.dashboard.displayBusStopInformation(data);
        break;
      case 'products':
        this.view.dashboard.setHeading('Local Products for "' + this.model.openTabs[tabId].title + '"');
        this.view.dashboard.displayLocalProducts(data);
        break;
    } 
    
  },
  
  
  handleStopFollowingUserClick : function(jid, role) {
    var source = {}
    source.jid = jid;
    source.role = role
    
    this.model.liveView.removeGuide(source);
    if (jid == this.view.liveView.currentDisplayedGuideJid) { 
      this.view.liveView.currentDisplayedGuideJid = ''; 
      this.view.liveView.clear();
    }
    this.view.liveView.updateGuidesList();

    this.model.xmpp.sendMessage(jid, '{ "message-type" : "cmd", "context" : "liveview", "role" : "surfer", "action" : "handle-following-stopped" }');
  },

  handleStopSharingClick : function(jid, role) {
    var source = {}
    source.jid = jid;
    source.role = role
    
    this.model.liveView.removeFollower(source);
    this.view.liveView.updateFollowersList();
    
    this.model.xmpp.sendMessage(jid, '{ "message-type" : "cmd", "context" : "liveview", "role" : "surfer", "action" : "handle-guiding-stopped" }');
  },
    
 
  onSnapshotButtonClick : function(imageBase64) {
    this.model.xmpp.sendMessage(jid, '{ "message-type" : "cmd", "context" : "liveview", "role" : "surfer", "action" : "send-image-base64", "base64" : "'+imageBase64+'" }');
  },
  
  
  onNewPageOnDisplay : function(url) {
    if (OneSpacePopup.Controller.Utility.isValidUrl(url) == false) { return; }
    
    for (var jid in this.model.liveView.surferFollowers) {
      this.model.liveView.surferFollowers[jid].url = url;
      this.model.xmpp.sendMessage(jid, '{ "message-type" : "cmd", "context" : "liveview", "role" : "surfer", "action" : "send-url", "url" : "'+url+'" }');
    }
  },
  

  handleStartShareSessionClick : function(jid) {
    this.model.xmpp.sendMessage(jid, '{ "message-type" : "cmd", "context" : "liveview", "role" : "surfer", "action" : "send-share-request", "url" : "'+this.model.currentActiveTab.url+'" }');
  },
  
  
  
  
  handleAcceptLiveViewShareRequest : function(source) {
    if (this.model.liveView.surferGuides[source.jid] === undefined) {
      //this.view.liveView.addGuide(source);
    } else {
      return;
    }
    this.model.liveView.addGuide(source);
    this.model.xmpp.sendMessage(source.jid, '{ "message-type" : "cmd", "context" : "liveview", "role" : "surfer", "action" : "handle-share-request-accepted" }');
  },

  handleRejectLiveViewShareRequest : function(source) {
    this.model.xmpp.sendMessage(source.jid, '{ "message-type" : "cmd", "context" : "liveview", "role" : "surfer", "action" : "handle-share-request-rejected" }');
  },
  
  
  
  handleCommandMessage : function(jid, user, command) {
    var context = command['context'];
    if (typeof context === 'undefined'){ return; } 
    
    switch (context) {
      case 'liveview':
	this.handleLiveViewCommand(jid, user, command);
	break;
    } 
    
  },
  
  
  handleLiveViewCommand : function(jid, user, command) {
    var action = command['action'];
    var role = command['role'];
    if (typeof action === 'undefined' || typeof role === 'undefined'){ return; } 
    
    var source = {};
    source.jid = jid;
    source.user = user;
    source.role = role;
    source.action = action;
    source.command = command;

    switch (action) {
      case 'send-share-request':
        this.view.notificationPanel.handleLiveViewShareRequestSent(source);
        break;
      case 'handle-share-request-accepted':
        this.model.liveView.addFollower(source);
        this.view.notificationPanel.handleLiveViewShareRequestAccepted(source);
        break;
      case 'handle-share-request-rejected':
        this.view.notificationPanel.handleLiveViewShareRequestRejected(source);
        break;
      case 'handle-following-stopped':
        this.model.liveView.removeFollower(source);
        this.view.liveView.updateFollowersList();
        this.view.notificationPanel.handleLiveViewFollowingStopped(source);
        break;
      case 'handle-guiding-stopped':
        this.model.liveView.removeGuide(source);
        this.view.liveView.updateGuidesList();
        this.view.notificationPanel.handleLiveViewShareGuidingStopped(source);
        break;
      case 'send-url':
        if (jid in this.model.liveView.surferGuides) { 
          this.model.liveView.surferGuides[jid].url = command['url'];
          this.view.liveView.updateLiveView(jid, command); 
        } 
        break;
      case 'send-image-base64':
        if (jid in this.model.liveView.surferGuides) { 
          this.model.liveView.surferGuides[jid].imageBase64 = command['base64'];
          this.view.liveView.updateLiveView(jid, command); 
        } else if (jid in this.model.liveView.walkerGuides) { 
          this.model.liveView.walkerGuides[jid].imageBase64 = command['base64'];
          this.view.liveView.updateLiveView(jid, command); 
        }
        break;
    } 

  },
  
  
  handleMapContextMenuClick : function(action) {
    switch (action) {
      case 'createcorner':
	this.view.dialogWindowManager.openCreateCornerDialog();
	this.view.map.contextMenu.close();
        break;
    }  
  },
  
  handleCreateNewCornerClick : function(name, description) {
    var lat = this.view.map.contextMenu.lastRightClickLat;
    var lng = this.view.map.contextMenu.lastRightClickLng;
    var userId = this.model.userId;
    var userName = this.model.xmpp.user;
    var userJid = this.model.xmpp.user+"@"+this.model.xmpp.server;
    var userJidResource = this.model.xmpp.resource;
    this.model.http.createNewUserCorner(userId, userName, userJid, userJidResource, name, description, lat, lng);
  },
  
  onCreateUserCornerResponseReceived : function(response) {
    this.view.dialogWindowManager.close();
    if (this.view.map.showUserCorners == true) {
      this.requestUserCorners(this.view.map.map.getBounds());
    }
  },
  
  onShownLocationUpdated : function() {
    this.onLocationsReceived(this.model.http.locations);
  },
  
  
  
  onRadarViewSelected : function() {
    var currentTabId = this.model.currentActiveTab.id;
    var vloc = this.model.openTabs[currentTabId].vloc;
    
    if (vloc != null) {
      this.model.http.requestWalkersAroundVloc(vloc, 10000);
    }
  },
  
  onWalkersAroundVlocReceived : function(response) {
    //alert(JSON.stringify(response, null, 4));
    this.view.radar.updateWalkersList(response);
  },
  
};


OneSpacePopup.Controller.Utility = {
  
  bind : function(context, method, arguments) {
    if (!arguments) { arguments = new Array(); }
    return function() { return method.apply(context, arguments); };
  },

  xmlToString : function(xmlData) { 
    var xmlString;
    if (window.ActiveXObject){ xmlString = xmlData.xml; }
    else{ xmlString = (new XMLSerializer()).serializeToString(xmlData); }
    return xmlString;
  },   
  
  size : function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
  },

  isValidUrl : function(str) {
    var pattern = new RegExp('^(https?:\/\/)');
    if(!pattern.test(str)) {
      return false;
    } else {
      return true;
    }
  },
  
  extractDomain : function(url) {
    var a = document.createElement('a');
    a.href = url;
    return a.hostname.replace('www.', '');
  },
  
  deleteFromArray : function(array, element) {
    var index = array.indexOf(element);
    if (index > -1) {
      array.splice(index, 1);
    }
  },
  
  convertToLinks : function(text) {
    var replaceText;
    var replacePattern1;
    var replacePattern2;
 
    //URLs starting with http://, https://
    replacePattern1 = /(\b(https?):\/\/[-A-Z0-9+&amp;@#\/%?=~_|!:,.;]*[-A-Z0-9+&amp;@#\/%=~_|])/ig;
    replacedText = text.replace(replacePattern1, '<a href="$1" target="_blank">$1</a>');
 
    //URLs starting with "www."
    replacePattern2 = /(^|[^\/])(www\.[\S]+(\b|$))/gim;
    replacedText = replacedText.replace(replacePattern2, '$1<a href="http://$2" target="_blank">$2</a>');
 
    return replacedText;
  },
  
  cleanRawString : function(s) {
    s = s.replace(/\\U([0-9a-f]{8})/gi, "&#x$1;");
    s = s.replace(/\\U([0-9a-f]{4})/gi, "&#x$1;");
    s = s.replace(/\\X([0-9a-f]{2})/gi, "&#x$1;");
    s = s.replace(/\\n/gi, " ");
    return s;
  },
  
  escapeHtml : function(unsafe) {
      return $('<div />').text(unsafe).html()
  },

  unescapeHtml : function(safe) {
      return $('<div />').html(safe).text();
  },
  
  getFileExtension : function(file) {
    var re = /(?:\.([^.]+))?$/;
    ext = re.exec(file)[1];
    if (typeof ext === 'undefined') { return ''; }
    return ext;
  },
  
  toTitleCase : function(str) {
    return str.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
  },
  
};


OneSpacePopup.Controller.MessageHandler = function(controller) {
  this.controller = controller;
};

OneSpacePopup.Controller.MessageHandler.prototype = {

  initialize : function() {
    var that = this;
    chrome.runtime.onMessage.addListener(function(message, sender, sendResponse) {
      //alert(JSON.stringify(message, null, 4));
      if (message.details.event == 'onespace-popup-opened') {
        that.controller.windowId = message.details.window.id;
      } else if (message.details.event == 'onespace-new-tab-selected') {
        that.controller.onNewTabSelected(message.details.tab);
      } else if (message.details.event == 'onespace-tab-added') {
        that.controller.onTabAdded(message.details.tab);
      } else if (message.details.event == 'onespace-tab-removed') {
        that.controller.onTabRemoved(message.details.tabId);
      } else if (message.details.event == 'onespace-tab-replaced') {
        that.controller.onTabReplaced(message.details.tabDetails);
      } else if (message.details.event == 'onespace-new-page-loaded') {
        that.controller.onNewPageLoaded(message.details.tab);
      } 
    
    });
  },
  
};


 
oneSpacePopupController = new OneSpacePopup.Controller();


$(window).bind('load', function(){
  oneSpacePopupController.initialize();
});

