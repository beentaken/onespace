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
      xmppHost: '',
      xmppHttpBind : 'http://',
      xmppResource : 'conference',
      xmppUserName : '',
      xmppPassword : ''
    }, function(items) {
      document.getElementById('options-xmpp-host').value = items.xmppHost;
      document.getElementById('options-xmpp-http-bind').value = items.xmppHttpBind;
      document.getElementById('options-xmpp-resource').value = items.xmppResource;
      document.getElementById('options-xmpp-user-name').value = items.xmppUserName;
      document.getElementById('options-xmpp-password').value = items.xmppPassword;
    });
  },


  saveOptions : function() {
    var xmppHost = document.getElementById('options-xmpp-host').value;
    var xmppHttpBind = document.getElementById('options-xmpp-http-bind').value;
    var xmppResource = document.getElementById('options-xmpp-resource').value;
    var xmppUserName = document.getElementById('options-xmpp-user-name').value;
    var xmppPassword = document.getElementById('options-xmpp-password').value;

    chrome.storage.sync.set({
      xmppHost: xmppHost,
      xmppHttpBind : xmppHttpBind,
      xmppResource : xmppResource,
      xmppUserName : xmppUserName,
      xmppPassword : xmppPassword
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
