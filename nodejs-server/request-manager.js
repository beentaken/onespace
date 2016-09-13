var request = require('ajax-request');
//var http = require('http'); 
//var najax = $ = require('najax');

RequestManager = function() {
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


  
};


exports.RequestManager = RequestManager;
