HTTP_ACTION_GET_NEAR_SITES = "get-places-within-circle";

HTTP_ACTION_WS_SEND_TO_ENDPOINT = 'push';
HTTP_ACTION_WS_BROADCAST = 'broadcast';

FILE_PLACE_TYPE_MAPPING = 'data/place-type-mapping.txt'


var http = require('http');
var express = require('express');
var url = require('url');
var step=require('step');
var bodyParser = require('body-parser');
var multipart = require('connect-multiparty');
var multipartMiddleware = multipart();

//var WebSocketServer = require('ws').Server;
var MongoDb = require('./mongodb-manager')
var mysqlManager = require('./mysql-manager');;
var urlManager = require('./url-manager');
var fileManager = require('./file-manager');
var uploadManager = require('./upload-manager');
var requestManager = require('./request-manager');



Server = function() {
  this.mongodbManager = new MongoDbManager(this, 'localhost', 27017);
  this.mysqlManagerOnespace = new MysqlManager(this, '172.29.33.45', 'onespace', '!!5656tT', 'onespace')
  //this.mysqlManagerOnespace = new MysqlManager(this, 'localhost', 'root', '', 'onespace')
  this.mysqlManagerOpenfire = new MysqlManager(this, '172.29.33.45', 'onespace', '!!5656tT', 'openfire')
  this.urlManager = new UrlManager(this);
  this.fileManager = new FileManager();
  this.requestManager = new RequestManager();
  this.uploadManager = new UploadManager(this);
  this.serverHttp = new Server.Http(this);
  // this.serverWebSocket = new Server.WebSocket(this); // Not used at the moment
};


Server.prototype = {

  initialize : function() {
    this.serverHttp.initialize();
    this.fileManager.placeTypeMapper.initialize(FILE_PLACE_TYPE_MAPPING, null);
    // this.serverWebSocket.initialize();
    
    
    // All not used at the moment
    this.wsClientId = 0;
    this.wsClients = new Object();
    this.wsEndpointToClientsMap = new Object();
    this.wsClientToEndpointsMap = new Object();
  },

  

  
};


Server.Util = {
  
  bind : function(context, method, arguments) {
    if (!arguments) { arguments = new Array(); }
    return function() { return method.apply(context, arguments); };
  },
  
  getQueryStringParams : function(url) {
    var vars = [], hash;
    var hashes = url.slice(url.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        vars[hash[0]] = hash[1];
    }
    return vars;
  },
  
  size : function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
  },
  
};




Server.Http = function(main) {
  this.main = main;
  this.app = express();
};


Server.Http.prototype = {

  initialize : function() {
    
    // Demo queries

    // curl  "http://localhost:8888/places/box/?lat1=1.292064&lng1=103.775114&lat2=1.293019&lng2=103.776233&limit=1"
    // curl  "http://localhost:8888/places/near/?lat=1.292520&lng=103.775731&radius=1000&limit=1"
    // curl  "http://localhost:8888/places/ploc/comp.nus.edu.sg/?limit=1"
    // curl  "http://localhost:8888/surfers/box/?lat1=1.29648&lng1=103.769976&lat2=1.292243&lng2=103.774965&limit=1"
    // curl  "http://localhost:8888/walkers/box/?lat1=1.29648&lng1=103.769976&lat2=1.292243&lng2=103.774965&limit=1"
    // curl  "http://localhost:8888/notes/5371d2ca1c0de924f0a2f2c9/?skip=0&limit10"
    
    // curl  -d "" "http://localhost:8888/user/login/?name=homer&password=pwd&xmpphost=vdw&xmppresource=conference"
    
    // curl  -d "" "http://localhost:8888/user/login/?name=homer&password=pwd&xmpphost=localhost&xmppresource=conference"
    // curl  -X PUT "http://localhost:8888/user/ploc/?userid=53707a5b1c0de94d70b18900&lat=1.2924345&lng=103.775706"
    // curl  -X PUT "http://localhost:8888/user/vloc/?userid=53707a5b1c0de94d70b18900&vloc=sesame.com"
    
    // curl  -d "" "http://localhost:8888/notes/add/?place_id=5371d2ca1c0de924f0a2f2c9&user_id=1&user_name=homer&text=Hello World!"
    
    // curl  "http://localhost:8888/url/unshort/http%3A%2F%2Ft.co%2F5skmePb7gp/"
    // curl  "http://localhost:8888/url/map/http%3A%2F%2Ft.co%2F5skmePb7gp/"
    
    // curl  "http://172.29.33.45:11090/url/map/http%3A%2F%2Ft.co%2F5skmePb7gp/"
    
    // curl http://localhost:11090/media/upload/ -F 'test=@test_file'
    
    // curl -F "test=@feedme-sg-hospitals.png" "http://localhost:11090/media/upload/?fromjid=homer@172.29.33.45&fromjidresource=conference&tojid=carl@172.29.33.45&tojidresource=conference"
    
    // curl "http://localhost:11090/messages/history/?fromjid=chong@172.29.33.45&fromjidresource=conference&tojid=homer@172.29.33.45&tojidresource=conference&lastsentdate=1442476004805&limit=20"
    
    
    this.app.set('port', process.env.PORT || 11090); 
 
    this.app.all('/*', function(req, res, next) {
      res.header('Access-Control-Allow-Origin', '*');
      res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE');
      res.header('Access-Control-Allow-Headers', 'Content-Type');
      next();
    });
    
    this.app.use(bodyParser());
    
    this.app.get('/url/unshort/:url/', Server.Util.bind(this, this.onUnshortenUrlRequest))
    this.app.get('/url/map/', Server.Util.bind(this, this.onMapUrlRequest))

    this.app.get('/data/', Server.Util.bind(this, this.onGetDataRequest))

    this.app.get('/corners/user/:creatorid', Server.Util.bind(this, this.onGetCornersForUserRequest));
    this.app.get('/corners/box/', Server.Util.bind(this, this.onGetCornersWithinBoxRequest));
    this.app.post('/corners/add/', Server.Util.bind(this, this.onCreateUserCornerRequest));
    this.app.post('/corners/delete/', Server.Util.bind(this, this.onDeleteUserCornerRequest));
    
    //this.app.get('/places/near/', Server.Util.bind(this, this.onGetPlacesWithinCircleRequest));
    this.app.get('/places/box/', Server.Util.bind(this, this.onGetPlacesWithinBoxRequest));
    this.app.get('/places/ploc/', Server.Util.bind(this, this.onGetPlocForVlocRequest));
    this.app.get('/surfers/box/', Server.Util.bind(this, this.onGetSurfersWithinBoxRequest));
    this.app.get('/walkers/box/', Server.Util.bind(this, this.onGetWalkersWithinBoxRequest));
    this.app.get('/notes/:placeid/', Server.Util.bind(this, this.onGetNotesRequest));
    
    this.app.post('/user/login/', Server.Util.bind(this, this.onHandleUserLoginRequest));
    
    this.app.put('/user/ploc/', Server.Util.bind(this, this.onUpdatePlocOfUserRequest));
    this.app.put('/user/vloc/', Server.Util.bind(this, this.onUpdateVlocOfUserRequest));
    
    this.app.post('/notes/add/', Server.Util.bind(this, this.onInsertNoteRequest));
    
    //this.app.post('/media/upload/', Server.Util.bind(this, this.onMediaUploadRequest));
    
    this.app.post('/media/upload', multipartMiddleware, Server.Util.bind(this, this.onMediaUploadRequest));
    
    this.app.get('/messages/history/', Server.Util.bind(this, this.onGetMessageHistoryRequest));
    
    var that = this;
   
    this.app.listen(this.app.get('port'), function(){
      console.log('Express server listening on port ' + that.app.get('port'));
    });
  },


  onUnshortenUrlRequest : function(request, response) {
    var that = this;
    console.log(request.params.url);
    this.main.urlManager.urlUnshortener.unshortenUrl(
      request.params.url,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },


  onMapUrlRequest : function(request, response) {
    var that = this;
    this.main.urlManager.urlMapper.mapUrl(
      request.query.tabid,
      request.query.url,
      request.query.unshorten,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  

  onGetDataRequest : function(request, response) {
    var that = this;
    var type = request.query.type;
    console.log(type);
    switch (type) {
      case "vplaces":
	this.onGetVirtualPlaces(request, response);
	break;
      case "twitter":
	this.onGetLatestTweetsRequest(request, response);
	break;
      case "youtube":
	this.onGetYoutubeVideosRequest(request, response);
	break
      case "flickr":
	this.onGetFlickrImagesRequest(request, response);
	break;
      case "instagram":
	this.onGetInstagramImagesRequest(request, response);
	break;
      case "nea":
	this.onGetNeaNowcastRequest(request, response);
	break;
      case "lta":
	this.onGetCarparkAvailabilityRequest(request, response);
	break;
      case "buses":
	this.onGetBusStopInformationRequest(request, response);
	break;
      case "lta-bus-arrival-times":
	this.onGetBusArrivalTimesRequest(request, response);
	break;
      case "products":
	this.onGetLocalProdcutsRequest(request, response);
	break;
      default: 
	that.sendResponse(response, 'text/json', JSON.stringify({}));
	break;
    }
  },
  
  
  
  onGetVirtualPlaces : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.linker.getVirtualPlaceIds(
      request.query.tabid,
      request.query.type,
      request.query.vloc,
      request.query.vlocsha1,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
  
  onGetLatestTweetsRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.tweets.getLatestTweets(
      request.query.tabid,
      request.query.type,
      request.query.vloc,
      request.query.vlocsha1,
      request.query.limit,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  
  onGetYoutubeVideosRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.youtube.getVideos(
      request.query.tabid,
      request.query.type,
      request.query.vloc,
      request.query.vlocsha1,
      request.query.limit,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  onGetFlickrImagesRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.flickr.getImages(
      request.query.tabid,
      request.query.type,
      request.query.vloc,
      request.query.vlocsha1,
      request.query.limit,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  onGetInstagramImagesRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.instagram.getImages(
      request.query.tabid,
      request.query.type,
      request.query.vloc,
      request.query.vlocsha1,
      request.query.limit,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  
  onGetNeaNowcastRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.nea.getNowcast(
      request.query.tabid,
      request.query.type,
      request.query.vloc,
      request.query.vlocsha1,
      request.query.limit,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  
  onGetCarparkAvailabilityRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.lta.getCarparkAvailability(
      request.query.tabid,
      request.query.type,
      request.query.vloc,
      request.query.vlocsha1,
      request.query.limit,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
  
  onGetBusStopInformationRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.lta.getBusStopInformation(
      request.query.tabid,
      request.query.type,
      request.query.vloc,
      request.query.vlocsha1,
      request.query.limit,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
  
  
  onGetBusArrivalTimesRequest : function(request, response) {
    var that = this;
    this.main.requestManager.lta.getBusArrivalTimes(
      request.query.busstopcode,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
    
  onGetLocalProdcutsRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.golocal.getLocalProducts(
      request.query.tabid,
      request.query.type,
      request.query.vloc,
      request.query.vlocsha1,
      request.query.limit,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
  
  onInsertNoteRequest :  function(request, response) {
    var that = this;
    //this.main.mongodbManager.notes.inserNote(
    this.main.mysqlManagerOnespace.notes.inserNote(
      request.query.place_id,
      request.query.user_id,
      request.query.user_name,
      request.query.text,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
  
//   onHandleUserLoginRequest :  function(request, response) {
//     var that = this;
//     this.main.mongodbManager.users.handleUserLogin(
//       request.query.name,
//       request.query.password,
//       request.query.xmpphost,
//       request.query.xmppresource,
//       function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
//     );
//   },

  onHandleUserLoginRequest :  function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.users.handleUserLogin(
      request.query.name,
      request.query.password,
      request.query.jid,
      request.query.jidresource,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  
  onUpdatePlocOfUserRequest : function(request, response) {
    var that = this;
    //this.main.mongodbManager.users.updatePlocOfUser(
    this.main.mysqlManagerOnespace.users.updatePlocOfUser(
      request.query.userid,
      parseFloat(request.query.lat), 
      parseFloat(request.query.lng),
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  onUpdateVlocOfUserRequest : function(request, response) {
    var that = this;
    //this.main.mongodbManager.users.updateVlocOfUser(
    this.main.mysqlManagerOnespace.users.updateVlocOfUser(
      request.query.userid,
      request.query.action,
      request.query.vloc,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  onGetPlacesWithinBoxRequest : function(request, response) {
    var that = this;
    //this.main.mongodbManager.places.getPlacesWithinBox(
    this.main.mysqlManagerOnespace.places.getPlacesWithinBox(
      parseFloat(request.query.lat1), 
      parseFloat(request.query.lng1),
      parseFloat(request.query.lat2), 
      parseFloat(request.query.lng2), 
      parseInt(request.query.limit, 10),
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
  

  onGetCornersForUserRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.corners.getUserCornersForUser(
      request.params.creatorid,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
  
  
  onGetCornersWithinBoxRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.corners.getUserCornersWithinBox(
      parseFloat(request.query.lat1), 
      parseFloat(request.query.lng1),
      parseFloat(request.query.lat2), 
      parseFloat(request.query.lng2), 
      parseInt(request.query.limit, 10),
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
  

  onCreateUserCornerRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.corners.createUserCorner(
      request.query.creatorid, 
      request.query.creatorname,
      request.query.creatorjid, 
      request.query.creatorjidresource,
      request.query.name, 
      request.query.description,
      parseFloat(request.query.lat), 
      parseFloat(request.query.lng), 
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  onDeleteUserCornerRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOnespace.corners.deleteUserCorners(
      request.query.creatorid, 
      request.query.roomjid,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  
  
//   onGetPlacesWithinCircleRequest : function(request, response) {
//     var that = this;
//     this.main.mongodbManager.places.getPlacesWithinCircle(
//       parseFloat(request.query.lat), 
//       parseFloat(request.query.lng), 
//       parseInt(request.query.radius), 
//       parseInt(request.query.limit),
//       function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
//     );
//   },
  
  
  onGetSurfersWithinBoxRequest : function(request, response) {
    var that = this;
    //this.main.mongodbManager.users.getSurfersWithinBox(
    this.main.mysqlManagerOnespace.users.getSurfersWithinBox(
      parseFloat(request.query.lat1), 
      parseFloat(request.query.lng1),
      parseFloat(request.query.lat2), 
      parseFloat(request.query.lng2), 
      parseInt(request.query.limit, 10),
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
  
  onGetWalkersWithinBoxRequest : function(request, response) {
    var that = this;
    //this.main.mongodbManager.users.getWalkersWithinBox(
    this.main.mysqlManagerOnespace.users.getWalkersWithinBox(
      parseFloat(request.query.lat1), 
      parseFloat(request.query.lng1),
      parseFloat(request.query.lat2), 
      parseFloat(request.query.lng2), 
      parseInt(request.query.limit, 10),
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
  
  
  onGetPlocForVlocRequest : function(request, response) {
    var that = this;
    //this.main.mongodbManager.places.getPlocForVloc(
    this.main.mysqlManagerOnespace.places.getPlocForVloc(
      request.query.vloc,
      parseInt(request.query.limit, 10),
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },
  
  
  
  onGetNotesRequest : function(request, response) {
    var that = this;
    //this.main.mongodbManager.notes.getNotes(
    this.main.mysqlManagerOnespace.notes.getNotes(
      request.params.placeid,
      parseInt(request.query.skip, 0),
      parseInt(request.query.limit, 10),
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  
  onMediaUploadRequest : function(request, response) {
    var that = this;
    //console.log(request);
    this.main.uploadManager.handleFiles(
      request.query.fromjid,
      request.query.fromjidresource,
      request.query.tojid,
      request.query.tojidresource,
      request.files,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },


  onGetMessageHistoryRequest : function(request, response) {
    var that = this;
    this.main.mysqlManagerOpenfire.openfire.getMessageHistory(
      request.query.fromjid,
      request.query.fromjidresource,
      request.query.tojid,
      request.query.tojidresource,
      request.query.lastsentdate,
      request.query.limit,
      function(error, result) { that.sendResponse(response, 'text/json', JSON.stringify(result)); } 
    );
  },

  
//   onRequest : function(request, response) {
//     var path = url.parse(request.url).pathname;
//     var dirs = path.split('/');
//     var action = dirs[1].toString();
//     
//     switch(action) {
//       case HTTP_ACTION_WS_BROADCAST:
// 	this.wsBroadcast(request, response);
// 	break;
//       case HTTP_ACTION_WS_SEND_TO_ENDPOINT:
// 	this.wsSendToEndpoint(request, response);
// 	break;
//       case HTTP_ACTION_GET_NEAR_SITES:
// 	this.main.mongodb.getNeatSites(request, response);
// 	break;
//       default:
// 	this.sendResponse(response, "text/plain", "Handled action: " + action);
//     } 
//     
//   },
  
  
  
//   wsSendToEndpoint : function(request, response) {
//     var path = url.parse(request.url).pathname;
//     var dirs = path.split('/');
//     var endpoint = dirs[2].toString();
//     
//     var wsClients = this.main.wsEndpointToClientsMap[endpoint];
//     for (var wsClientId in wsClients) {
//       this.main.wsClients[wsClientId].send('New broadcaset message - endpoint: ' + endpoint);
//     }
//     
//     this.sendResponse(response, "text/plain", "Sent to endpoint: " + endpoint);
//   },
  
  
//   wsBroadcast : function(request, response) {
//     for (var wsClientId in this.main.wsClientToEndpointsMap) {
//       this.main.wsClients[wsClientId].send('New broadcaset message: ...');
//     }
//     this.sendResponse(response, "text/plain", "Broadcast initiated");
//   },
  
  sendResponse : function(response, contentType, body) {
    console.log(body);
    response.writeHead(200, {"Content-Type": contentType});
    response.write(body);
    response.end();
  },
  
};





// Server.WebSocket = function(main) {
//   this.main = main;
//   this.wss = null;
//   
// };
// 
// 
// Server.WebSocket.prototype = {
// 
//   initialize : function() {    
//     this.wss = new WebSocketServer({port: 9999});
//     
//     var that = this;
//     this.wss.on('connection', function(ws) {
// 
//       that.handleNewConnection(ws);
//       
//       ws.on('message', function(message) {
//         ws.send('Message from ' + ws.id + ': ' + message);
//       });
// 
//       ws.on('close', function() {
// 	that.handleClosedConnection(ws);
//       });
//       
//     });
//   },
// 
// 
//   handleNewConnection : function(client) {
//     var path = client.upgradeReq.url.split('?')[0].toString();
//     var endpoint = path.split('/')[1].toString();
// 
//      if ((typeof endpoint === 'undefined') && (endpoint == '')) {
//        return;
//      }
// 
//     this.main.wsClientId++;
//     client.id = this.main.wsClientId;
// 
//     this.main.wsClients[client.id] = client;
//     this.main.wsClientToEndpointsMap[client.id] = endpoint;
// 
//     try {
//       var clients = this.main.wsEndpointToClientsMap[endpoint]
//       clients[client.id] = 1;
//       this.main.wsEndpointToClientsMap[endpoint] = clients;
//     } catch (e){
//       console.log(endpoint);
//       var clients = new Object();
//       clients[client.id] = 1;
//       this.main.wsEndpointToClientsMap[endpoint] = clients;
//     } 
//     
//   },
//   
//   handleClosedConnection : function(client) {
//     if(typeof this.main.wsClients[client.id] != 'undefined') {
//       var endpoint = this.main.wsClientToEndpointsMap[client.id];
//       
//       delete this.main.wsEndpointToClientsMap[endpoint][client.id];
//       if (Server.Util.size(this.main.wsEndpointToClientsMap[endpoint]) == 0) {
// 	delete this.main.wsEndpointToClientsMap[endpoint];
//       }
// 
//       delete this.main.wsClientToEndpointsMap[client.id];
//       delete this.main.wsClients[client.id];
//     }
//   },
//   
//   
// };





server = new Server();
server.initialize();
