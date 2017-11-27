OneSpacePopupView =  new Array();


OneSpacePopupView = function(controller) {
  this.controller = controller;
  this.dialogWindowManager = new OneSpacePopupView.DialogWindowManager(this.controller);
  this.notificationPanel = new OneSpacePopupView.NotificationPanel(this.controller);
  this.dashboard = new OneSpacePopupView.Dashboard(this.controller);
  this.map = new OneSpacePopupView.Map(this.controller);
  this.radar = new OneSpacePopupView.Radar(this.controller);
  this.chat = new OneSpacePopupView.Chat(this.controller);
  this.liveView = new OneSpacePopupView.LiveView(this.controller);
  this.settings = new OneSpacePopupView.Settings(this.controller);
};


OneSpacePopupView.prototype = {

  initialize : function() {
    this.dialogWindowManager.initialize();
    this.notificationPanel.initialize()
    this.dashboard.initialize()
    this.map.initialize();
    this.radar.initialize();
    this.chat.initialize();
    this.liveView.initialize();
    this.settings.initialize();
    
    this.initializeNavLinks();
    this.showContentDiv('chat');
    

  },


  initializeNavLinks : function() {
    var that = this;
    // Navigation links
    $('.nav-link').click(function() {
      //split at the '1' and take the second offset
      var target = $(this).attr('id').split('-')[2]; 
      that.showContentDiv(target);
    });
  },
  

  
  showContentDiv : function(target) {
    $('.content-div').hide();
    $('.content-div#content-div-'+target).show();
    
    if (target == 'map') { // Workaround to deal with map being in a hidden DIV
      google.maps.event.trigger(this.map.map, "resize");
    } else if (target == 'liveview') {
      this.liveView.updateGuidesList();
      this.liveView.updateFollowersList();
      this.liveView.markCurrentGuide();
    } else if (target == 'radar') {
      this.controller.onRadarViewSelected();
    }
  },
  

};



OneSpacePopupView.NotificationPanel = function(controller) {
  this.controller = controller;
  this.panel = $('#content-div-notification-panel');
};



OneSpacePopupView.NotificationPanel.prototype = {
  
  initialize : function() {
    this.panel = $('#content-div-notification-panel'); // No idea why I have to do this here again
    
    var that = this;
    // Add listener to "close" icon
    $( "#button-notification-panel-close" ).click(function(event) {
      event.preventDefault();
      that.panel.hide();
    });
    
    
  },
  
  close : function() {
    this.panel.hide();
  },
  
  reset : function() {
    // Remove info text
    $('#content-div-notification-panel-text').empty();
    // Remove all table rows except first;
    //$('#content-div-notification-panel-action-button-table').find('tr:gt(0)').remove();
    $('#content-div-notification-panel-action-button-table tbody').empty();
    $('#content-div-notification-panel-action-button-table tbody').append('<tr></tr>');
  },
  
  
  setPanelText : function(text) {
    $('#content-div-notification-panel-text').append(text);
  },
  
  addActionButton : function(buttonId, buttonText, callback, param) {
    // add row to table
    var tr = $('#content-div-notification-panel-action-button-table > tbody:first');
    tr.append('<td id="' + buttonId + '"></td>');
    var button = $('<button class="button-popup-panel" />').text(buttonText).click( function() { callback.call(this, param); }.bind(this) );
    $('#' +buttonId).html(button);
  },
  
  
  handleLiveViewShareRequestSent : function(source) {
    this.panel.hide(); 
    this.reset();
    this.setPanelText(source.role.capitalizeFirstLetter() + ' ' + user + ' wants to share his live view');
    this.addActionButton('action-button-liveview-share-accept', 'Accept', this.handleAcceptLiveViewShareRequest, source);
    this.addActionButton('action-button-liveview-share-reject', 'Decline', this.handleRejectLiveViewShareRequest, source);
    this.addActionButton('action-button-liveview-share-ignore', 'Ignore', this.handleIgnoreLiveViewShareRequest, source);
    this.panel.show();
  },


  handleLiveViewShareRequestAccepted : function(source) {
    this.panel.hide(); 
    this.reset();
    this.setPanelText(source.role.capitalizeFirstLetter() + ' ' + user + ' has accepted your request to share your live view');
    this.addActionButton('action-button-liveview-ok', 'OK', this.close, '');
    this.panel.show();
  },
  
  handleLiveViewShareRequestRejected : function(source) {
    this.panel.hide(); 
    this.reset();
    this.setPanelText(source.role.capitalizeFirstLetter() + ' ' +  user + ' has rejected your request to share your live view');
    this.addActionButton('action-button-liveview-ok', 'OK', this.close, '');
    this.panel.show();
  },
  
   handleLiveViewFollowingStopped : function(source) {
    this.panel.hide(); 
    this.reset();
    this.setPanelText(source.role.capitalizeFirstLetter() + ' ' + user + ' has stopped following you.');
    this.addActionButton('action-button-liveview-ok', 'OK', this.close, '');
    this.panel.show();
  },
  
  handleLiveViewShareGuidingStopped : function(source) {
    this.panel.hide(); 
    this.reset();
    this.setPanelText(source.role.capitalizeFirstLetter() + ' ' + user + ' has stopped sharing.');
    this.addActionButton('action-button-liveview-ok', 'OK', this.close, '');
    this.panel.show();
  },
   
  
  handleAcceptLiveViewShareRequest : function(source) {
    this.controller.handleAcceptLiveViewShareRequest(source);
    this.close();
  },

  handleRejectLiveViewShareRequest : function(source) {
    this.controller.handleRejectLiveViewShareRequest(source);
    this.close();
  },

  handleIgnoreLiveViewShareRequest : function(source) {
    this.close();
  },
  
};











OneSpacePopupView.Settings = function(controller) {
  this.controller = controller;
};



OneSpacePopupView.Settings.prototype = {
  
  initialize : function() {
    this.initializeSettingsCheckBoxes();
  },
  
  initializeSettingsCheckBoxes : function() {
    var that = this;
    // Add click handler
    $('.settings-checkbox').click(function() {
      //split at the '1' and take the second offset
      var elements = $(this).attr('id').split('-');
      var target = elements[1];
      var isChecked = $(this).is(':checked');
      that.controller.handleSettingsCheckboxClick(target, isChecked);
    });
    // Set value to stored settings
    $('.settings-checkbox').each(function() {
      var elements = $(this).attr('id').split('-');
      var target = elements[1];
      that.controller.getOption(target, that.handleCheckboxReturnOption);
    });
  },
  
  handleCheckboxReturnOption : function(items) {
    for(item in items) {
      if (items[item] == true) {
        $('#settings-'+item).prop('checked', items[item]);
      }
    }
  },
  
  
};





OneSpacePopupView.Dashboard = function(controller) {
  this.controller = controller;
  
};



OneSpacePopupView.Dashboard.prototype = {
  
  initialize : function() {
    this.initializeSidebar();
    this.initializeMainContent();
    this.initializeSidebarNavLinks();
  },    
    
  
  initializeSidebar : function() {
    // Not working: sidebar loads not "fast enough" so that initializeSidebarNavLinks can find the links
    //$("#dashboard-table-main-sidebar-column").load("popup-dashboard-sidebar.html"); 
  },
  
  initializeMainContent : function(){
    //var main = $('#content-div-dashboard-main');
    //main.html('<h3>Dashboard</h3><br />Use sidebar to select relevant information.');
  },
  
  initializeSidebarNavLinks : function() {
    var that = this;
    // Navigation links
    $('.dashboard-sidebar-nav-link').click(function() {
      //split at the '1' and take the second offset
      var target = $(this).attr('id').split('-')[4]; 
      that.handleDashboardSidebarNavClick(target);
    });
  },
  
  handleDashboardSidebarNavClick : function(target) {
    this.controller.requestData(target);
  },

  clearDashBoard : function() {
    $("#data-table-tbody").empty();    
  },
  
  onContentChange : function(tab) {
    vlocSha1 = tab.vlocSha1;
  },
  
  
  setHeading : function(s) {
    $('#content-div-dashboard-main-heading').html(s);
  },
  
  displayLatestTweets : function(data) {
    this.clearDashBoard();
    var tableEnd = $('#data-table tbody');
    for (var i = 0; i < data.length; i++) {
      var tweet = data[i];
      var tweetText = tweet['tweet_text'];
      var tweetTimestamp = tweet['tweet_timestamp'];
      var tweetScreenName = tweet['tweet_screen_name'];
      tweetText = OneSpacePopup.Controller.Utility.cleanRawString(tweetText);
      tweetText = OneSpacePopup.Controller.Utility.convertToLinks(tweetText);
      var newDate = new Date();
      newDate.setTime(tweetTimestamp*1000);
      var dateString = newDate.toUTCString();
      var entry = "<div class='" + CSS_CLASS_DASHBOARD_DATA_TWEET + "'><div><b>" + tweetScreenName + "</b> tweeted on " + dateString + ":</div> <div>" + tweetText + "</div></div>";
      var row = '<tr><td>'+entry+'</td></tr>';
      tableEnd.append(row);
    }
  },
  
  
  displayYoutubeVideos : function(data) {
    this.clearDashBoard();
    var tableEnd = $('#data-table tbody');
    for (var i = 0; i < data.length; i++) {
      var video = data[i];
      var url = video.url.replace("watch?v=", "v/");
      var entry = '<div class="' + CSS_CLASS_DASHBOARD_DATA_YOUTUBE_VIDEO + '"> <iframe width="480" height="390" src="http://' + url + '" frameborder="0"></iframe></div>';
      var row = '<tr><td>'+entry+'</td></tr>';
      tableEnd.append(row);
    }
  },
  
  
  displayFlickrImages : function(data) {
    this.clearDashBoard();
    var tableEnd = $('#data-table tbody');
    for (var i = 0; i < data.length; i++) {
      var image = data[i];
      var entry = '<div class="' + CSS_CLASS_DASHBOARD_DATA_FLICKR_IMAGES + '"><img src="http://' + image.url + '"></div>';
      var row = '<tr><td>'+entry+'</td></tr>';
      tableEnd.append(row);
    }
  },

  displayInstagramImages : function(data) {
    this.clearDashBoard();
    var tableEnd = $('#data-table tbody');
    for (var i = 0; i < data.length; i++) {
      var image = data[i];
      var entry = '<div class="' + CSS_CLASS_DASHBOARD_DATA_INSTAGRAM_IMAGES + '"><img src="https://' + image.url + '"></div>';
      var row = '<tr><td>'+entry+'</td></tr>';
      tableEnd.append(row);
    }
  },
  
  displayNeaNowcast : function(data) {
    this.clearDashBoard();
    var tableEnd = $('#data-table tbody');
    for (var i = 0; i < data.length; i++) {
      var item = data[i];
      var icon = item['icon'].toUpperCase();
      icon = icon.replace("CD", "CL");
      icon = icon.replace("TS", "TL"); 
      var entry = '<div class="' + CSS_CLASS_DASHBOARD_DATA_NEA + '"><table><tr><td><img src="http://www.nea.gov.sg/Html/Nea/images/common/weather/50px/' + icon + '.png" alt="' + icon + '"></td><td style="font-size: 115%;">' +  OneSpacePopup.Controller.Utility.toTitleCase(item['location']) + ' (Distance: ' + item['distance_in_km'] + ')</td></tr></table></div>'
	  
      var row = '<tr><td>'+entry+'</td></tr>';
      tableEnd.append(row);
    }
  },
  
  displaycarparkAvailability : function(data) {
    this.clearDashBoard();
    var tableEnd = $('#data-table tbody');
    for (var i = 0; i < data.length; i++) {
      var item = data[i];
      var row = '<tr><td style="margin: 15px; font-size: 115%;"><b>'+item['Lots']+'</b></td><td style="font-size: 115%;">' + item['Development'] + '</td><td style="font-size: 115%;">(Distance: '+item['distance'].toFixed(2)+'km)</td></tr>';
      tableEnd.append(row);
    }
  },
    

  displayBusStopInformation : function(data) {
    this.clearDashBoard();
    var tableEnd = $('#data-table tbody');
    for (var i = 0; i < data.length; i++) {
      var item = data[i];
      var entry = '<div class="' + CSS_CLASS_DASHBOARD_DATA_LTA + '"><table><tr><td style="font-size: 115%;"><b>' + item['description'] + '</b> (Distance: ' + item['distance_in_km'] + ')</td></tr></table></div>'
      entry += '<div><table>';
      for (var pos in item['arrival_times']) {
        var info = item['arrival_times'][pos]
        //alert(JSON.stringify(info, null, 4));
        var time = info['arrival_time'];
        var timeConverted = new Date(time);	
        entry += '<tr><td><b>' + info['service_nr'] + '</b></td><td style="padding-left: 20px;">' + timeConverted.getHours() + ':' + timeConverted.getMinutes() + ':' + timeConverted.getSeconds() + '</td></tr>';
      }
      entry += '<table></div>'
      var row = '<tr><td style="margin: 15px;">'+entry+'</td></tr><tr></tr>';
      tableEnd.append(row);
    }
  },
  

  displayLocalProducts : function(data) {
    this.clearDashBoard();
    var tableEnd = $('#data-table tbody');
    
    for (var i = 0; i < data.length; i++) {
      var item = data[i];
      var sname = item['sname'];
      var pname = item['pname'];
      var price = item['price'];
      var productLink = item['url'];
      var lat = item['lat'];
      var lng = item['lng'];
      
      var row = '<tr><td style="padding-right: 15px; font-size: 115%;"><b>'+pname+'</b></td><td style="padding-right: 15px; font-size: 115%;">$' + price + '</td><td style="padding-right: 15px; font-size: 115%;">'+sname+'</td><td style="padding-right: 15px; font-size: 85%;"><a href="'+productLink+'" target="_blank" style="text-decoration:none;">Go to Website</a></td><td style="padding-right: 15px; font-size: 85%;"><a class="dashboard-product-link-map" id="dashboard-product-link-map-'+lat+'-'+lng+'" href="#" style="text-decoration:none;">Show on Map</a></td></tr>';
      tableEnd.append(row);
    }
    
    var that = this;
    $('.dashboard-product-link-map').click(function() {
      //split at the '1' and take the second offset
      var lat = $(this).attr('id').split('-')[4];
      var lng = $(this).attr('id').split('-')[5]; 
      that.handleDashboardProductMapLinkClick(lat, lng);
    });
  },
    
  handleDashboardProductMapLinkClick : function(lat, lng) {
    this.controller.view.map.setGenericMarkerAndFocus(lat, lng);
    this.controller.view.showContentDiv('map');
      
  },
  
  
};



OneSpacePopupView.Map = function(controller) {
  this.controller = controller;
  this.map = null;
  this.infoPanel = new OneSpacePopupView.Map.InfoPanel(this.controller);
  this.contextMenu = new OneSpacePopupView.Map.ContextMenu(this.controller);
  this.markersGeneric = new Array();
  this.markersLocations = new Array(); 
  this.markersUserCorners = new Array();
  this.markersSurfers = new Array(); 
  this.markersWalkers = new Array(); 
  this.markersOwnLocation = new Array();
  this.showLocations = false;
  this.showUserCorners = false; 
  this.showSurfers = false; 
  this.showWalkers = false; 
  this.isMapDragging = false;
  this.shownLocationCategories = {};
};



OneSpacePopupView.Map.prototype = {
  
  initialize : function() {
    this.initializeMap();
    this.initializeMapLegendCheckBoxes();
    this.infoPanel.initialize();
    this.contextMenu.initialize()
    
    //
    // Creepy work-around to fetch the event when dragging the map is finished;
    // all "simple" events fire repeatedly while dragging 
    // (found on the Web)
    //
    
    var that = this;
    
    google.maps.event.addListener(that.map, "rightclick", function(event) {
      var lat = event.latLng.lat();
      var lng = event.latLng.lng();
      var cursorX = event.pixel.x;
      var cursorY = event.pixel.y;
      that.contextMenu.open(lat, lng, cursorX, cursorY);
    });
    
    google.maps.event.addListener(that.map, "click", function(event) {
      that.contextMenu.close();
    });


    google.maps.event.addListener(that.map, "idle", function() {
        if (that.isMapDragging) {
            that.idleSkipped = true;
            return;
        }
        that.idleSkipped = false;
        that.updateMap();
    });
    
    google.maps.event.addListener(that.map, "dragstart", function() {
        that.isMapDragging = true;
    });

    google.maps.event.addListener(that.map, "dragend", function() {
        that.isMapDragging = false;
        if (that.idleSkipped == true) {
            that.updateMap();
            that.idleSkipped = false;
        }
    });

    google.maps.event.addListener(that.map, "bounds_changed", function() {
        that.idleSkipped = false;
    });
    
   
    ////////////////////////////////////////////////////////////////////////////
  },
  
  
  initializeMap : function() {
    var myOptions = {
      center: new google.maps.LatLng(1.292448, 103.775595),
      zoom: 18,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    this.map = new google.maps.Map($("#content-div-map-canvas").get(0), myOptions);
    

  },
  
  initializeMapLegendCheckBoxes : function() {
    var that = this;
    $('.map-legend-checkbox').click(function() {
      //split at the '1' and take the second offset
      var elements = $(this).attr('id').split('-');
      var action = elements[2]; 
      var target = elements[3];
      var isChecked = $(this).is(':checked');
      that.controller.handleMapLegendCheckboxClick(action, target, isChecked);
    });
    $('.map-legend-checkbox-location-filter').click(function() {
      //split at the '1' and take the second offset
      var target = $(this).attr('value');
      var isChecked = $(this).is(':checked');
      that.updateShownLocationCategories(target, isChecked);
    });

  },
  
  updateShownLocationCategories : function(target, isChecked) {
    if (target in this.shownLocationCategories) {
      if (isChecked == false) {
        delete this.shownLocationCategories[target];
      }
    } else {
      if (isChecked == true) {
        this.shownLocationCategories[target] = 1;
      }      
    }
    this.controller.onShownLocationUpdated();
  },
  
  
  clearGenericOverlay : function() { 
    for (var i = 0; i < this.markersGeneric.length; i++ ) { this.markersGeneric[i].setMap(null); } 
    this.markersGeneric = new Array(); 
  },
  
  clearLocationsOverlay : function() { 
    for (var i = 0; i < this.markersLocations.length; i++ ) { this.markersLocations[i].setMap(null); } 
    this.markersLocations = new Array(); 
  },
  
  clearUserCornersOverlay : function() { 
    for (var i = 0; i < this.markersUserCorners.length; i++ ) { this.markersUserCorners[i].setMap(null); } 
    this.markersUserCorners = new Array(); 
  },  
  
  clearSurfersOverlay : function() { 
    for (var i = 0; i < this.markersSurfers.length; i++ ) { this.markersSurfers[i].setMap(null); } 
    this.markersSurfers = new Array(); 
  },

  clearWalkersOverlay : function() { 
    for (var i = 0; i < this.markersWalkers.length; i++ ) { this.markersWalkers[i].setMap(null); } 
    this.markersWalkers = new Array(); 
  },

  clearOwnLocationOverlay : function() { 
    for (var i = 0; i < this.markersOwnLocation.length; i++ ) { this.markersOwnLocation[i].setMap(null); } 
    this.markersOwnLocation = new Array(); 
  },
  
  
  toggleLocationFilterLegend : function(isChecked) {
    if (isChecked == true) {
      $('#content-div-map-legend-location-categories').show();
    } else {
      $('#content-div-map-legend-location-categories').hide();
    }
  },
  
  updateMap : function() {
    if(this.showLocations) { this.controller.requestLocations(this.map.getBounds()); }
    if(this.showUserCorners) { this.controller.requestUserCorners(this.map.getBounds()); }
    if(this.showSurfers) { this.controller.requestSurfers(this.map.getBounds()); }
    if(this.showWalkers) { this.controller.requestWalkers(this.map.getBounds()); }
  },
  
  
  setGenericMarkerAndFocus(lat, lng) {
    this.clearGenericOverlay();  
    var point = new google.maps.LatLng(lat, lng);
    var marker = new google.maps.Marker({position: point, icon: "images/icon-corner.png", map: this.map });
    this.markersGeneric.push(marker);
    marker.setMap(this.map);
    this.map.setCenter(marker.getPosition());
    this.map.setZoom(19);
  },
  
  onLocationsReceived : function(locations) {
    this.clearLocationsOverlay();
    var that = this;
    var icon = "images/icon-location.png";
    for ( i = 0; i < locations.length; i++) {
      var location = locations[i];
      if (location.category in this.shownLocationCategories) {
        var point = new google.maps.LatLng(location.lat, location.lng);
        var marker = new google.maps.Marker({position: point, icon: "images/icon-location-"+location.category+".png", map: that.map });
        marker.location = location;
        google.maps.event.addListener(marker, "click",  function() { that.handleOnLocationClicked(this); } , false );
        this.markersLocations.push(marker);
        marker.setMap(this.map);
      }
    }
  },
  
  
  onUserCornersReceived : function(userCorners) {
    this.clearUserCornersOverlay();
    var that = this;
    var icon = "images/icon-corner.png";
    for ( i = 0; i < userCorners.length; i++) {
      var userCorner = userCorners[i];
      var point = new google.maps.LatLng(userCorner.lat, userCorner.lng);
      var marker = new google.maps.Marker({position: point, icon: icon, map: that.map });
      marker.userCorner = userCorner;
      google.maps.event.addListener(marker, "click",  function() { that.handleOnUserCornerClicked(this); } , false );
      this.markersUserCorners.push(marker);
      marker.setMap(this.map);
    }
  },
  
  
  onSurfersReceived : function(surfers, locationsMap) {  
    this.clearSurfersOverlay();
    var that = this;
    var icon = "images/icon-surfer.png";
    for (var i = 0; i < surfers.length; i++) {
      var surfer = surfers[i];
      var point = new google.maps.LatLng(surfer.lat, surfer.lng);
      var marker = new google.maps.Marker({position: point, icon: icon, map: that.map });
      marker.surfer = surfer;
      google.maps.event.addListener(marker, "click",  function() { that.handleOnSurferClicked(this); }, false );
      this.markersSurfers.push(marker);
      marker.setMap(this.map);
    }
  },
 
  onWalkersReceived : function(walkers) { 
    this.clearWalkersOverlay();
    var that = this;
    icon = "./images/icon-walker.png";
    for (i = 0; i < walkers.length; i++) {
      walker = walkers[i];
      //if (walker.id == this.view.controller.model.userId) { continue; } // don't display own location as "walker"
      point = new google.maps.LatLng(walker.lat, walker.lng);

      var marker = new google.maps.Marker({ position: point, icon: icon, map: this.map });
      marker.walker = walker;

      google.maps.event.addListener(marker, "click",  function() { that.handleOnWalkerClicked(this); }, false );
      
      this.markersWalkers.push(marker);
      marker.setMap(this.map);
    }
  },
  
  handleOnLocationClicked : function(marker) { this.infoPanel.handleOnLocationClicked(marker) },
  
  handleOnUserCornerClicked : function(marker) { this.infoPanel.handleOnUserCornerClicked(marker) },
  
  handleOnSurferClicked : function(marker) { this.infoPanel.handleOnSurferClicked(marker) },
  
  handleOnWalkerClicked : function(marker) { this.infoPanel.handleOnWalkerClicked(marker) },
  

};




OneSpacePopupView.Map.ContextMenu = function(controller) {
  this.controller = controller;
  this.lastRightClickCursorX = 0;
  this.lastRightClickCursorY = 0;
  this.lastRightClickLat = 0;
  this.lastRightClickLng = 0;
};



OneSpacePopupView.Map.ContextMenu.prototype = {
  
  initialize : function() {
    this.initializeMenuItemLinks();
  },

  initializeMenuItemLinks : function() {
    var that = this;
    $('.content-div-map-context-menu-item-link').click(function(e) {
      e.preventDefault();
      //split at the '1' and take the second offset
      var elements = $(this).attr('id').split('-');
      var action = elements[7];
      that.controller.handleMapContextMenuClick(action);
    });
  },
  
  open : function(lat, lng, cursorX, cursorY) {
    var divContextMenu = $('#content-div-map-context-menu');
    this.lastRightClickCursorX = cursorX;
    this.lastRightClickCursorY = cursorY;
    this.lastRightClickLat = lat;
    this.lastRightClickLng = lng;
    divContextMenu.css({top: (cursorY-10)+'px',left: (cursorX-10)+'px'});
    divContextMenu.show();
  },
  
  close : function(lat, lng, cursorX, cursorY) {
    var divContextMenu = $('#content-div-map-context-menu');
    divContextMenu.hide();
  },

};




OneSpacePopupView.Map.InfoPanel = function(controller) {
  this.controller = controller;
  this.panel = $('#content-div-map-info-panel');
};



OneSpacePopupView.Map.InfoPanel.prototype = {
  
  initialize : function() {
    this.panel = $('#content-div-map-info-panel'); // No idea why I have to do this here again
    
    var that = this;
    // Add listener to "close" icon
    $( "#button-info-panel-close" ).click(function(event) {
      event.preventDefault();
      that.panel.hide();
    });
    
    
  },
  
  
  handleJoinWebGroupChatClick : function(url) {
    this.controller.handleJoinWebGroupChatClick(url);
  },

  handleOpenWebsiteClick : function(url) {
    this.controller.handleOpenWebsiteClick(url);
  },
  
  handleJoinUserCornerGroupChatClick : function(userCorner) {
    this.controller.handleJoinUserCornerGroupChatClick(userCorner.roomJid, userCorner.name, userCorner.creatorJid);
  },
  
  handleDeleteUserCornerClick : function(userCorner) {
    this.controller.handleDeleteUserCornerClick(userCorner.creatorId, userCorner.roomJid);
  },
  
  handleStartPrivateChatClick : function(userJid) { 
    this.controller.handleStartPrivateChatClick(userJid, true);
  },
  
  handleStartShareSessionClick : function(userJid) {
    this.controller.handleStartShareSessionClick(userJid);
  },
  
  
  close : function() {
    this.panel.hide();
  },
  
  reset : function() {
    // Remove info text
    $('#content-div-map-info-panel-text').empty();
    // Remove all table rows except first;
    $('#content-div-map-info-panel-action-button-table').find('tr:gt(0)').remove();
  },
  
  handleOnLocationClicked : function(marker) {
    this.panel.hide(); 
    this.reset();
    this.setPanelText(marker.location.name);
    this.addActionButton('action-button-join-group-chat', 'Join Group Chat', this.handleJoinWebGroupChatClick, marker.location.url);
    this.addActionButton('action-button-join-open-website', 'Open Website', this.handleOpenWebsiteClick, marker.location.url);
    this.panel.show();
  },
  
  handleOnUserCornerClicked : function(marker) {
    this.panel.hide(); 
    this.reset();
    this.setPanelText('User Corner: ' + marker.userCorner.name + '<br/>' + marker.userCorner.description);
    this.addActionButton('action-button-join-group-chat', 'Join Group Chat', this.handleJoinUserCornerGroupChatClick, marker.userCorner);
    var userJid = this.controller.model.xmpp.user+"@"+this.controller.model.xmpp.server;
    if (userJid == marker.userCorner.creatorJid) {
      this.addActionButton('action-button-delete-user-corner', 'Delere Corner', this.handleDeleteUserCornerClick, marker.userCorner);
    }    
    this.panel.show();
  },
  
  handleOnSurferClicked : function(marker) {
    this.panel.hide(); 
    this.reset();
    this.setPanelText('Surfer ' + marker.surfer.name);
    this.addActionButton('action-button-join-group-chat', 'Join Group Chat', this.handleJoinWebGroupChatClick, marker.surfer.url);
    this.addActionButton('action-button-start-private-chat', 'Start Private Chat', this.handleStartPrivateChatClick, marker.surfer.fullJid);
    this.addActionButton('action-button-start-share-session', 'Share Session', this.handleStartShareSessionClick, marker.surfer.fullJid);
    this.panel.show();
  },
  
  handleOnWalkerClicked : function(marker) {
    this.panel.hide(); 
    this.reset();
    this.setPanelText('Walker ' + marker.walker.name);
    this.addActionButton('action-button-start-private-chat', 'Start Private Chat', this.handleStartPrivateChatClick, marker.walker.fullJid);
    this.addActionButton('action-button-start-share-session', 'Share Session', this.handleStartShareSessionClick, marker.walker.fullJid);
    this.panel.show();
  },


  
  
  setPanelText : function(text) {
    $('#content-div-map-info-panel-text').append(text);
  },
  
  addActionButton : function(buttonId, buttonText, callback, param) {
    // add row to table
    var table = $('#content-div-map-info-panel-action-button-table tbody');
    table.append('<tr><td id="' + buttonId + '"></td></tr>');
    //add button to table cell
    var button = $('<button class="button-popup-panel" />').text(buttonText).click( function() { callback.call(this, param); }.bind(this) );
    $('#' +buttonId).html(button);
  },
};



OneSpacePopupView.Chat = function(controller) {
  this.controller = controller;
  this.currentDisplayedChat = '';
};



OneSpacePopupView.Chat.prototype = {
  
  initialize : function() {
    var that = this;
    $( "#chat-select" ).change(function() {
      that.onNewLocationSelected($( this ).val());
    });
    
    $('#button-chat-send-message').click(function() {
      that.controller.handleSendMessageClick();
    });
    
    $('#button-chat-close').click(function() {
      that.controller.handleCloseChatClick();
    });
    
    $('#chat-send-message-text').keypress(function( event ) {
      if ( event.which == 13 ) { 
        event.preventDefault(); 
        that.controller.handleSendMessageClick();
      }
    });
    
    $( "#button-share-media" ).click(function(event) {
      event.preventDefault();
      $('#input-share-media').trigger('click');
      //that.controller.handleShareMediaClick();
    });
    
    
    $('#input-share-media').change(function(event) {
      files = event.target.files;
      var formData = new FormData($(this)[0]);
      // Create a formdata object and add the files
      var formData = new FormData();
      $.each(files, function(key, value) {
        formData.append(key, value);
      });
      that.controller.handleShareMediaFileSelected(formData);
    });
    
  },
  
  
  onNewLocationSelected : function(jid) {
    this.switchToChat(jid);
  },
  
  addGroupChatToList : function(roomJid, roomName, type) {
    //var roomName = roomJid.split('@')[0];
    $("#chat-select").append('<option value="' + roomJid + '">' + type + ': ' + roomName + '</option>'); 
  },

  addPrivateChatToList : function(userJid) {
    var userName = userJid.split('@')[0];
    $("#chat-select").append('<option value="' + userJid + '">PC: Private chat with ' + userName + '</option>'); 
  },

  
  removeGroupChatFromList : function(roomJid) {
    $('#chat-select option[value="' + roomJid +'"]').remove();
  },
  
  switchToChat : function(jid) {
    if (jid == this.currentDisplayedChat) { return; }
    
    this.clearAll();
    this.currentDisplayedChat = jid;
    $('#chat-select option[value="' + jid +'"]').prop('selected', true);
    this.updateParticipantsList(jid);
    this.updateReceivedMessages(jid);
    this.updateCloseChatButton(jid);

  },

  updateCloseChatButton : function(jid) {
    if (jid in this.controller.model.xmpp.groupChats) {
      var type = this.controller.model.xmpp.groupChats[jid].type;
      if (type == GROUP_CHAT_TYPE_DEFAULT) {
        $('#button-chat-close').show();
      } else {
        $('#button-chat-close').hide();
      }
    } else {
      $('#button-chat-close').show();
    }
  },
  
  displayNewGroupChatPresence : function(user, status) {
    if (user != null) {
      var entry = '';
      var info = '';
      if (status == 'available') {
        info = 'has joined the chat';
      } else { 
        info = 'has left the chat';
      }
      entry = "<div class='"+ CSS_CLASS_CHAT_PRESENCE + "'><span class='" + CSS_CLASS_CHAT_PRESENCE_USER + "'>" + user + "</span> <span class='" + CSS_CLASS_CHAT_PRESENCE_INFO + "'>" + info + "</div>";
      $('#chat-received-messages').append(entry);
      scrollHeight = $('#chat-received-messages')[0].scrollHeight;
      $('#chat-received-messages').scrollTop(scrollHeight);
    }
  },
  
  updateParticipantsList : function(jid) {
    $('#chat-participants').empty();
    str = "<div class='" + CSS_CLASS_CHAT_PARTICIPANTS + "'>";
    participants = null;
    var bareJid = jid.split("/")[0];
    if (this.controller.model.xmpp.groupChats[jid] != null) {
      participants = this.controller.model.xmpp.groupChats[jid].participants;
    } else if (this.controller.model.xmpp.privateChats[bareJid] != null) {
      participants = this.controller.model.xmpp.privateChats[bareJid].participants;
    } else {
      return;
    }
    for(jid in participants) {
      var participant = participants[jid];
      str += "<table id='table-chat-participants'><tr><td width='100%'><div class='" + CSS_CLASS_CHAT_PARTICIPANTS_USER + "'>" + participant.name + "</div></td><td><a href='#' title='Click to share your browsing session'><img class='share-session-icon' id='share-session-" + participant.jid + "' src='images/share-session-icon.png' /></a></td><td><a href='#' title='Click to start private chat'><img class='private-chat-icon' id='private-chat-" + participant.jid + "' src='images/private-chat-icon.png' /></a></td></tr></table>";
    }
    str += "</div>";
    
    $('#chat-participants').append(str);
    
    // Attache click handler to private chat icons
    var that = this;
    $('.private-chat-icon').click(function() {
      var elements = $(this).attr('id').split('-');
      var userJid = elements[2]; 
      that.controller.handleStartPrivateChatClick(userJid, true);
    });
    $('.share-session-icon').click(function() {
      var elements = $(this).attr('id').split('-');
      var userJid = elements[2]; 
      that.controller.handleStartShareSessionClick(userJid);
    });
    
  },
  
  updateReceivedMessages : function(jid) {
    var bareJid = jid.split("/")[0];
    $('#chat-received-messages').empty();
    if (this.controller.model.xmpp.groupChats[jid] != null) {
      this.controller.model.xmpp.groupChats[jid].showHistory(jid);
    } else if (this.controller.model.xmpp.privateChats[bareJid] != null) {
      this.controller.model.xmpp.privateChats[bareJid].showHistory(jid);
    } else {
      return;
    }
  },

  displayNewMessage : function(jid, user, text, convert) {
    if (text.indexOf(XMPP_CHAT_COMMAND_PREFIX) > -1) {
      return; // don't display CMD messages
    }
    
    var displayText = text;
    if (convert == true) {
      displayText = OneSpacePopup.Controller.Utility.convertToLinks(text);
    }
    
    
    if (user != null) {
      if (user == this.controller.model.xmpp.user) {
        entry = "<div class='bubble you'>" + displayText + "</div>"
      } else {
        name = jid.split('@')[0];
        if (name == user) {
          entry = "<div class='bubble me'>" + displayText + "</div>";
        } else {
          entry = "<div class='bubble me'><b>" + user + ":</b> " + displayText + "</div>";
        }
      }
      $('#chat-received-messages').append(entry);
      scrollHeight = $('#chat-received-messages')[0].scrollHeight;
      $('#chat-received-messages').scrollTop(scrollHeight);
    }
  },
  
  clearAll : function() {
    $('#chat-participants').empty();
    $('#chat-received-messages').empty();
    $('#chat-send-message-text').val('');
  },
};





OneSpacePopupView.Radar = function(controller) {
  this.controller = controller;
};


OneSpacePopupView.Radar.prototype = {
  
  initialize : function() {

  },

  updateSurfersList : function(jid) {
    $('#radar-table-surfer').empty();
    str = "<div class='" + CSS_CLASS_CHAT_PARTICIPANTS + "'><table id='table-radar-surfers'>";
    participants = null;
    if (this.controller.model.xmpp.groupChats[jid] != null) {
      participants = this.controller.model.xmpp.groupChats[jid].participants;
    } else {
      return;
    }
    for(jid in participants) {
      var participant = participants[jid];
      str += "<tr><td width='100%'><div class='radar-user'>" + participant.name + "</div></td><td><a href='#' title='Click to share your browsing session'><img class='share-session-icon' id='share-session-" + participant.jid + "' src='images/share-session-icon.png' /></a></td><td><a href='#' title='Click to start private chat'><img class='private-chat-icon' id='private-chat-" + participant.jid + "' src='images/private-chat-icon.png' /></a></td></tr>";
    }
    str += "</table></div>";
    
    $('#radar-table-surfer').append(str);
    
    // Attache click handler to private chat icons
    var that = this;
    $('.private-chat-icon').click(function() {
      var elements = $(this).attr('id').split('-');
      var userJid = elements[2]; 
      that.controller.handleStartPrivateChatClick(userJid, true);
    });
    $('.share-session-icon').click(function() {
      var elements = $(this).attr('id').split('-');
      var userJid = elements[2]; 
      that.controller.handleStartShareSessionClick(userJid);
    });
    
  },  
  
  
  updateWalkersList : function(response) {
    $('#radar-table-walker').empty();
    str = "<div class='" + CSS_CLASS_CHAT_PARTICIPANTS + "'><table id='table-radar-surfers'>";

    for(var i = 0; i < response.length; i++) {
      var walker = response[i];
      var jid = walker['jid'] + '/' + walker['jid_resource'];
      str += "<tr><td width='100%'><span class='radar-user'>" + walker['name'] + "</span> (" + walker['distance_in_meter'] + "m)</td><td><a href='#' title='Click to start private chat'><img class='private-chat-icon' id='private-chat-" + jid + "' src='images/private-chat-icon.png' /></a></td></tr>";
    }
    str += "</table></div>";
    
    $('#radar-table-walker').append(str);
    
    // Attache click handler to private chat icons
    var that = this;
    $('.private-chat-icon').click(function() {
      var elements = $(this).attr('id').split('-');
      var userJid = elements[2]; 
      that.controller.handleStartPrivateChatClick(userJid, true);
    });
  },  
};



OneSpacePopupView.LiveView = function(controller) {
  this.controller = controller;
  this.currentDisplayedGuideJid = '';
  this.hasWebcam = false;
  this.snapshotVideo = null;
};


OneSpacePopupView.LiveView.prototype = {
  
  initialize : function() {
    this.initializeListeners();
  },

  
  initializeListeners : function() {
    var that = this;
    
    $('input[id^="liveview-tab-"]').click(function() {
      var id = $(this).attr('id');
      if (id == 'liveview-tab-snapshot') {
        that.checkIfWebcamAvailable();
      } else {
        $('#liveview-snapshot-wrapper').empty();
      }
    });
    
    $('#button-liveview-share-snapshot').click(function() {
      that.onSnapshotButtonClick();
    });
  },
  
  checkIfWebcamAvailable : function() {
    navigator.getMedia = ( navigator.getUserMedia || // use the proper vendor prefix
                       navigator.webkitGetUserMedia ||
                       navigator.mozGetUserMedia ||
                       navigator.msGetUserMedia);
    
    var that = this;
    navigator.getMedia({video: true}, function() {
      that.onWebcamFound()
    }, function() {
      //alert('Webcam not found.');
    });
  },
  
  
  onWebcamFound : function() {
    this.hasWebcam = true;
    $('#liveview-snapshot-wrapper').empty();
    $('#liveview-snapshot-wrapper').append('<video id="liveview-snapshot-video" width="98%" height="98%" autoplay></video>');
    $('#liveview-snapshot-wrapper').append('<canvas id="liveview-snapshot-canvas" width="640" height="480" style="display: none;"></canvas>');
    navigator.webkitGetUserMedia({ "video": true }, this.handleVideo, this.handleVideoError);
    
  },
  
  handleVideo : function(stream) {
    var snapshotVideo = $("#liveview-snapshot-video");
    snapshotVideo.attr('src' ,window.URL.createObjectURL(stream));
  },
  
  handleVideoError : function(error) {
    alert("Video capture error: ", error.code); 
  },


  onSnapshotButtonClick : function() {
    var snapshotVideo = document.getElementById("liveview-snapshot-video");
    var canvas = document.getElementById("liveview-snapshot-canvas");
    var context = canvas.getContext("2d");
    context.drawImage(snapshotVideo, 0, 0, 640, 480);
    var imageBase64 = canvas.toDataURL("image/png");
    this.controller.onSnapshotButtonClick(imageBase64);
  },
  
  
  update : function() {

  },
  
  
  clear : function() {
    $('#liveview-content-wrapper').empty();
    $('#liveview-address-bar-text-field').attr('value', '');
  },
  
  updateLiveView : function(jid, command) {
    if (jid != this.currentDisplayedGuideJid) { return; }
    
    this.clear();
    if ('url' in command) {
      $('#liveview-address-bar-text-field').attr('value', command['url']);
      
      fileExtension = OneSpacePopup.Controller.Utility.getFileExtension(command['url']).toLowerCase();
      if (fileExtension in IMAGE_MEDIA_FILE_SUFFIXES) { // raw media content
        $('#liveview-content-wrapper').append('<table width="100%"  height="100%"><tr><td><img id="liveview-img-snapshot" src="" /></td></tr></table>');
        //$('#liveview-img-snapshot').attr('src', 'http://172.29.32.195/onespace/service/liveview-grabber.php?url='+command['url']);
        $('#liveview-img-snapshot').attr('src', command['url']);
      } else { // normal HTML content
        $('#liveview-content-wrapper').append('<iframe id="liveview-frame" src="" frameborder="0"></iframe>');
        //$('#liveview-frame').attr('src', 'http://172.29.32.195/onespace/service/liveview-grabber.php?url='+command['url']);
        $('#liveview-frame').attr('src', command['url']);
      }
    } else if ('base64' in command) {
      $('#liveview-content-wrapper').append('<table width="100%"  height="100%"><tr><td><img id="liveview-img-snapshot" src="" /></td></tr></table>');
      $('#liveview-address-bar-text-field').attr('value', 'Snapshot');
      $('#liveview-img-snapshot').attr('src', command['base64']);
    }
  },


  
  
  
  updateGuidesList : function() {
    $('#liveview-guides').empty();
    var str = "<div class='" + CSS_CLASS_LIVEVIEW_GUIDES + "'>";
    str += "<table class='table-liveview-guides'><tr><td width='100%'><b>Users you follow:</b></td></tr></table>";

    var guides = this.controller.model.liveView.surferGuides;
    for(jid in guides) {
      var userName = jid.split('@')[0];
      str += "<table class='table-liveview-guides' id='table-liveview-guide-"+jid+"-surfer'><tr><td width='100%'><div class='" + CSS_CLASS_LIVEVIEW_USERS + "'><a class='table-liveview-guide-link' id='table-liveview-guide-link-"+jid+"-surfer' href='#'><img src='images/icon-surfer-liveview.png' />" + userName + "</a></div></td><td><a href='#' title='Click stop following user'><img class='cancel-following-session-icon' id='cancel-following-" + jid + "-surfer' src='images/icon-cancel.png' /></a></td></tr></table>";
    }
   
    var guides = this.controller.model.liveView.walkerGuides;
    for(jid in guides) {
      var userName = jid.split('@')[0];
      str += "<table class='table-liveview-guides' id='table-liveview-guide-"+jid+"-walker'><tr><td width='100%'><div class='" + CSS_CLASS_LIVEVIEW_USERS + "'><a class='table-liveview-guide-link' id='table-liveview-guide-link-"+jid+"-walker' href='#'><img src='images/icon-walker-liveview.png' />" + userName + "</a></div></td><td><a href='#' title='Click stop following user'><img class='cancel-following-session-icon' id='cancel-following-" + jid + "-walker' src='images/icon-cancel.png' /></a></td></tr></table>";
    }

    
    str += "</div>";
    
    $('#liveview-guides').append(str);
    
    // Attache click handler to private chat icons
    var that = this;
    $('.table-liveview-guide-link').click(function() {
      var elements = $(this).attr('id').split('-');
      var userJid = elements[4]; 
      var role = elements[5]; 
      that.handleGuideClick(userJid, role);
    });
    
    $('.cancel-following-session-icon').click(function() {
      var elements = $(this).attr('id').split('-');
      var userJid = elements[2];
      var role = elements[3]; 
      that.controller.handleStopFollowingUserClick(userJid, role);
    });
    
  },
  
  
  
  updateFollowersList : function() {
    $('#liveview-followers').empty();
    var str = "<div class='" + CSS_CLASS_LIVEVIEW_FOLLOWERS + "'>";
    str += "<table class='table-liveview-followers'><tr><td width='100%'><b>Users following you:</b></td></tr></table>";

    var followers = this.controller.model.liveView.surferFollowers;
    for(jid in followers) {
      var liveViewSurfer = followers[jid];
      var userName = jid.split('@')[0];
      str += "<table class='table-liveview-followers' id='table-liveview-followers-"+jid+"'><tr><td width='100%'><div class='" + CSS_CLASS_LIVEVIEW_USERS + "'><a class='table-liveview-follower-link' href='#'><img src='images/icon-surfer-liveview.png' />" + userName + "</a></div></td><td><a href='#' title='Click stop sharing session with user'><img class='cancel-sharing-session-icon' id='cancel-sharing-" + jid + "-surfer' src='images/icon-cancel.png' /></a></td></tr></table>";
    }
    
    var followers = this.controller.model.liveView.walkerFollowers;
    for(jid in followers) {
      var userName = jid.split('@')[0];
      str += "<table class='table-liveview-followers' id='table-liveview-followers-"+jid+"'><tr><td width='100%'><div class='" + CSS_CLASS_LIVEVIEW_USERS + "'><a class='table-liveview-follower-link' href='#'><img src='images/icon-walker-liveview.png' />" + userName + "</a></div></td><td><a href='#' title='Click stop sharing session with user'><img class='cancel-sharing-session-icon' id='cancel-sharing-" + jid + "-walker' src='images/icon-cancel.png' /></a></td></tr></table>";
    }

    str += "</div>";
    
    $('#liveview-followers').append(str);
    
    // Attache click handler to private chat icons
    var that = this;

    $('.cancel-sharing-session-icon').click(function() {
      var elements = $(this).attr('id').split('-');
      var userJid = elements[2];
      var role = elements[3]; 
      that.controller.handleStopSharingClick(userJid, role);
    });
    
  },
  
  
  
  handleGuideClick : function(userJid, role) {
    this.currentDisplayedGuideJid = userJid;
    
    if (role == 'surfer') {
      var surferGuide = this.controller.model.liveView.surferGuides[userJid];
      var command = { "role" : role, "url" : surferGuide.url };
    } else {
      var walkerGuide = this.controller.model.liveView.walkerGuides[userJid];
      var command = { "role" : role, "base64" : walkerGuide.imageBase64 };
      
    }
    this.updateLiveView(userJid, command);
    this.markCurrentGuide();
  },
  
  
  markCurrentGuide : function() {
    var that = this;
    $('.table-liveview-guides').each(function() {
      if (($(this).attr('id') == 'table-liveview-guide-'+that.currentDisplayedGuideJid+'-surfer') || ($(this).attr('id') == 'table-liveview-guide-'+that.currentDisplayedGuideJid+'-walker') ) {
        $(this).css('background-color', '#CCDDFF');
      } else {
        $(this).css('background-color', '#FFFFFF');
      }
    });
  },
  
  
  
  
};





OneSpacePopupView.DialogWindowManager = function(controller) {
  this.controller = controller;
};



OneSpacePopupView.DialogWindowManager.prototype = {
  
  initialize : function() {
    var that = this;
    
    $('.button-dialog-window-close').click(function() {
      //split at the '1' and take the second offset
      $('.dialog-window').hide();
    });
    
    $('button[id^="button-dialog-window-"]').click(function() {
      var buttonId = $(this).attr('id');
      var action = buttonId.split('-')[3];
      that.handleButtonClick(action);
    });
  },

  
  handleButtonClick : function(action) {
    switch (action) {
      case 'close':
        $('.dialog-window').hide();
        break;
      case 'createcorner':
        var name = $('#dialog-window-create-corner-name').val().trim();
        var description = $('#dialog-window-create-corner-description').val().trim();
        if (name == '') { alert('Name field cannot be empty'); return ;}
        this.controller.handleCreateNewCornerClick(name, description);
        break;
    } 
  },


  openCreateCornerDialog : function() {
    $('#dialog-window-create-corner-name').val('');
    $('#dialog-window-create-corner-description').val('');
    $('#dialog-window-create-corner').show();
  },
  
  close : function() {
    $('.dialog-window').hide();
  },
  
};







