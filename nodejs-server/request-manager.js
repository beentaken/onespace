var step = require('step');
var request = require('ajax-request');
//var http = require('http'); 
//var najax = $ = require('najax');

RequestManager = function(server) {
  this.server = server;
  this.lta = new RequestManager.LTA(this);
};



RequestManager.LTA = function(main) {
  this.main  = main; 
  this.headers = { 'AccountKey' :'tly+LROL+7yRcCA3PQcZdw==', 'UniqueUserID' : 'adfde255-ee40-4b49-8242-17050a1b94a3', 'accept' : 'application/json' };
};

RequestManager.LTA.prototype = {

  
  // curl "http://172.29.32.195:11090/data/?type=lta-bus-arrival-times&busstopcode=1311"
  // curl "http://172.29.33.45:11090/data/?type=lta-bus-arrival-times&busstopcode=1311"
  getBusArrivalTimes : function(busStationCode, callback) {
    request({ url: 'http://datamall2.mytransport.sg/ltaodataservice/BusArrival', method: 'GET', headers: this.headers, data: { 'BusStopID': busStationCode } }, function (error, response, body) {
      //Check for error
      if(error){
        console.log('Error:', error);
        callback(error);
      }

      //Check for right status code
      if(response.statusCode !== 200){
        console.log('Invalid Status Code Returned:', response.statusCode);
        callback(error);
      }

      //All is good. Print the body
      //console.log(body); // Show the HTML for the Modulus homepage.
      callback(null, JSON.parse(body));
    });
    
  },


  getCarparkAvailability : function(tabId, type, vloc, vlocSha1, callback) {
    var that = this;
    var poi = {}
    //console.log(that.main.server.mysqlManagerOnespace.places.getPlocForVloc);
    step (
      function executeQuery(){
        that.main.server.mysqlManagerOnespace.places.getPlocForVloc(vloc, 1, this);
      }, 
      function onPlocReceived(error, result) {
        if (error) {
          callback(error);
        } else {
          poi.lat = result[0].lat;
          poi.lng = result[0].lng;
          request({ url: 'http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailability', method: 'GET', headers: that.headers, data: { } }, this);
        }
      },
      function onCarparkAvailabilityReceived(error, response, body) {
        if(error){
          console.log('Error:', error);
          callback(error);
        }

        //Check for right status code
        if(response.statusCode !== 200){
          console.log('Invalid Status Code Returned:', response.statusCode);
          callback(error);
        }
        //console.log(body);
        body = JSON.parse(body);
        
        for (var i = 0; i < body['value'].length; i++) {
          var item = body['value'][i];
          distance = Server.Util.distance(item.Latitude, item.Longitude, poi.lat, poi.lng);
          item['distance'] = distance;
        }
        
        body['value'].sort(function(a, b){
           return parseFloat(a.distance) - parseFloat(b.distance);;
        });
        
        //All is good. Print the body
        //console.log(body); // Show the HTML for the Modulus homepage.
        var result = { 'tabid' : tabId, 'type' : type, 'vloc' : vloc, 'vloc-sha1' : vlocSha1, 'data' : body['value'] };
        callback(null, result);
      }
    );
    
  },


  
};


exports.RequestManager = RequestManager;
