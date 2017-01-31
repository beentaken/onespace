var FCM = require('fcm-node');
var step = require('step');
var config = require('./config'); 
var errors = require('./errors'); 

var fcm = new FCM(config['fcm-server-key']);

FcmManager = function(server) {
  this.server = server; 
  //this.fcm = new FCM("AIzaSyDGR5nZ48F-utaSAvUsK7Rcx3UTg_xQN7s");
};



FcmManager.prototype = {

  initialize : function() {
    console.log('FcmManager.initialize')
  },
  
  //
  // curl  -d "" "http://172.29.32.195:11090/notification/push/?fromjid=homer@172.29.33.45&tojid=chris@172.29.33.45&body=Hello!"
  //  
  handlePushNotification : function(fromJid, toJid, body, callback) {
    fromJid = fromJid.split("/")[0];
    toJid = toJid.split("/")[0];
    console.log(">>> " + toJid);
    var that = this;

    step (
      function getUser() {
        that.server.mysqlManagerOnespace.users.getUserByJid(toJid, this);
      },
      function onResultReceived(error, recipient) {
        if (error) {
          callback(null, { "errors" : [ errors.DATABASE_ERROR ] });
          console.log("[ERROR] FcmController: " + error);
        } else {
          if (recipient) {
            //console.log("[FcmController] sendMessage - recipient fcm token: " + recipient.fcmtoken);
            //that.server.emailController.send(user.email, 'Activation Code', user.activation_code, this);
            
            var message = { //this may vary according to the message type (single recipient, multicast, topic, et cetera)
              to: recipient.fcmtoken, 
                  
              notification: {
                  title: fromJid + ' sent:', 
                  body: body 
              },
    
              data: {  //you can send only notification or only data(or include both)
                  my_key: 'my value',
                  my_another_key: 'my another value'
              }
            };
            fcm.send(message, this);
            //res.json({ success: true  });
          } else {
            callback(null, { "errors" : [ errors.USER_ID_NOT_FOUND ] });
          }
        }
      },
      function onMessageSent(error, response) {
        if (error) {
          callback(null, { "errors" : [ errors.FCN_SEND_FAILED_ERROR ] });
          console.log("[ERROR] FcmController: " + error);
        } else {
          callback(null, { success: true });
        }
      }
    );
    
  },
  

	
};



exports.FcmManager = FcmManager;