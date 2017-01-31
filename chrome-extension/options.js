OneSpaceOptions =  new Array();


OneSpaceOptions = function() {

};


OneSpaceOptions.prototype = {

  initialize : function() {
    var that = this;
    document.getElementById('button-save-options').addEventListener('click', that.saveOptions);
    this.restoreOptions();
  },

  
  restoreOptions : function (){
    chrome.storage.sync.get({
      apiBaseUrl: 'http://',
      xmppHost: '',
      xmppHttpBind : 'http://',
      xmppResource : 'conference',
      xmppUserName : '',
      xmppPassword : '',
      mapMaxPlaces : 1000,
      mapMaxCorners : 1000,
      mapMaxSurfers : 100,
      mapMaxWalkers : 100,
      dashboardMaxItems : 20
    }, function(items) {
      document.getElementById('options-api-base-url').value = items.apiBaseUrl;
      document.getElementById('options-xmpp-host').value = items.xmppHost;
      document.getElementById('options-xmpp-http-bind').value = items.xmppHttpBind;
      document.getElementById('options-xmpp-resource').value = items.xmppResource;
      document.getElementById('options-xmpp-user-name').value = items.xmppUserName;
      document.getElementById('options-xmpp-password').value = items.xmppPassword;
      document.getElementById('options-map-max-places').value = items.mapMaxPlaces;
      document.getElementById('options-map-max-corners').value = items.mapMaxCorners;
      document.getElementById('options-map-max-surfers').value = items.mapMaxSurfers;
      document.getElementById('options-map-max-walkers').value = items.mapMaxWalkers;
      document.getElementById('options-dashboard-max-items').value = items.dashboardMaxItems;
    });
  },


  saveOptions : function() {
    var apiBaseUrl = document.getElementById('options-api-base-url').value;
    var xmppHost = document.getElementById('options-xmpp-host').value;
    var xmppHttpBind = document.getElementById('options-xmpp-http-bind').value;
    var xmppResource = document.getElementById('options-xmpp-resource').value;
    var xmppUserName = document.getElementById('options-xmpp-user-name').value;
    var xmppPassword = document.getElementById('options-xmpp-password').value;
    var mapMaxPlaces = document.getElementById('options-map-max-places').value;
    var mapMaxCorners = document.getElementById('options-map-max-corners').value;
    var mapMaxSurfers = document.getElementById('options-map-max-surfers').value;
    var mapMaxWalkers = document.getElementById('options-map-max-walkers').value;
    var dashboardMaxItems = document.getElementById('options-dashboard-max-items').value;

    chrome.storage.sync.set({
      apiBaseUrl : apiBaseUrl,
      xmppHost : xmppHost,
      xmppHttpBind : xmppHttpBind,
      xmppResource : xmppResource,
      xmppUserName : xmppUserName,
      xmppPassword : xmppPassword,
      mapMaxPlaces : mapMaxPlaces,
      mapMaxCorners : mapMaxCorners,
      mapMaxSurfers : mapMaxSurfers,
      mapMaxWalkers : mapMaxWalkers,
      dashboardMaxItems : dashboardMaxItems
    }, function() {
     // Update status to let user know options were saved.
      var status = document.getElementById('status');
      status.textContent = 'Options saved.';
      setTimeout(function() {
        status.textContent = '';
      }, 750);
    });
  },

  
};
 


oneSpaceOptions = new OneSpaceOptions();
document.addEventListener('DOMContentLoaded', function() { oneSpaceOptions.initialize(); });


