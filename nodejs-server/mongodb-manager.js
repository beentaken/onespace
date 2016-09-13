var step=require('step');
var Db = require('mongodb').Db;
var Connection = require('mongodb').Connection;
var Server = require('mongodb').Server;
var BSON = require('mongodb').BSON;
var ObjectID = require('mongodb').ObjectID;

MongoDbManager = function(server, host, port) {
  this.server = server;
  this.db= new Db('onespace', new Server(host, port, {auto_reconnect: true}, {}));
  this.db.open(function(){});
  this.users = new MongoDbManager.Users(this);
  this.places = new MongoDbManager.Places(this);
  this.notes = new MongoDbManager.Notes(this);
  this.netlocs = new MongoDbManager.Netlocs(this);
};





MongoDbManager.Users = function(main) {
  this.main  = main; 
 
  var that = this
  this.main.db.collection('users', function(error, collection) {
    if (error) {}
    else {
      that.users = collection; 
    }
  });
}

MongoDbManager.Users.prototype = {
  
  getCollection : function(callback) {
    this.main.db.collection('users', function(error, collection) {
      if(error) {
	callback(error);
      } else {
	callback(null, collection);
      }
    });
  },



//   handleUserLogin : function(name, password, xmppHost, xmppResource, callback) {
//     var that = this;
//     step(
//       function getUser() {
// 	that.getUserByName(name, this);
//       },
//       function onGetUserResultReceived(error, result){
// 	if (error) {
// 	  callback(error);
// 	} else {
// 	  if (result) {
// 	    callback(null, result._id);
// 	  } else {
// 	    var query = { 'name' : name, 'password' : password, 'xmpp' : { 'host' : xmppHost , 'resource' : xmppResource } };
// 	    that.users.insert(query, this);
// 	  }
// 	}
//       },
//       function onInsert(error) {
// 	if (error) {
// 	  callback(error) ;
// 	} else {
// 	  that.handleUserLogin(name, password, xmppHost, xmppResource, callback);
// 	}
//       }
//     );
//     
//   },
//     
//   
//   getUserByName : function(name, callback) {
//     var that = this;
//     step(
//       function executeQuery(){
// 	var query = { name : name };
//  	that.users.findOne(query, this);
//       },
//       function onResult(error, result){
//  	if (error) {
//  	  callback(error);
//  	} else {
// 	  var res = { "id" : result }
//  	  callback(null, result );
//  	}
//       }
//     );
//   },
//   

//   getWalkersWithinBox : function(latitudeA, longitudeA, latitudeB, longitudeB, limit, callback) {
//     var that = this;
//     step(
//       function executeQuery() {
// 	var query = { ploc : { $geoWithin : { $box : [ [ longitudeA, latitudeA ] , [ longitudeB, latitudeB ] ] } } };
//         that.users.find(query).limit(limit).toArray(this);
//       },
//       function onResult(error, result){
//  	if (error) {
//  	  callback(error);
//  	} else {
//  	  callback(null, result)
//  	}
//       }
//     );
//   },
//   
  
  
//   getSurfersWithinBox : function(latitudeA, longitudeA, latitudeB, longitudeB, limit, callback) {
//     var that = this;
//     var places = new Object();
//     step(
//       function getPlaces() {
// 	that.main.places.getPlacesWithinBox(latitudeA, longitudeA, latitudeB, longitudeB, 100000000, this)
//       },
//       function onPlacesReceived(error, result) {
//         if (error) {
//           callback(error);
//         } else {
//           vplaces = new Array();
//           for(var i = 0; i < result.length; i++) {
//             var place = result[i];
//             var vp = place['vplaces']
//             for (var j = 0; j < vp.length; j++) {
//               var vpId = vp[j]
//               if (vplaces.indexOf(vpId) < 0) {
//                 vplaces.push(vpId);
//                 places[vpId] = place;
//               }
//             }
//           }
//           var query = { vlocs : { $elemMatch : { $in : vplaces } } };
//           console.log(query);
//           that.users.find(query).toArray(this);
//         }
//       },
//       function onUsersReceived(error, result) {
// 	if(error) {
// 	  callback(error);
// 	} else {
// 	  // Identify all relevant places (places that have indeed surfers)
// 	  var vlocs = new Object();
// 	  for(var i = 0; i < result.length; i++) {
// 	    var surfer = result[i];
// 	    for (var j = 0; j < surfer.vlocs.length; j++) {
// 	      vlocs[surfer.vlocs[j]] = 1;
// 	    }
// 	  }
// 	  // Remove all places without any surfers
// 	  var visitedPlaces = new Object();
// 	  for(place in places) {
// 	    if (place in vlocs) {
// 	      visitedPlaces[place] = places[place];
// 	    }
// 	  }
// 	  // Build final result to be sent to client
// 	  var finalResult = new Object();
// 	  finalResult['surfers'] = result;
// 	  finalResult['locations'] = visitedPlaces;
// 	  callback(null, finalResult)
// 	}
//       }
//     );
//   },

  
  
  

//   updatePlocOfUser : function(userId, latitude, longitude, callback) {
//     var that = this;
//     step(
//       function executeQuery() {
//         var query = { _id : new ObjectID.createFromHexString(userId) };
//         if ((Math.abs(latitude) > 90) || (Math.abs(longitude) > 180)) {
//           var update = { $unset : {"ploc" : 1, "last_modified_ploc" : 1 } };
//         } else {
//           var update = { $set : { "ploc" : [ longitude, latitude ], "last_modified_ploc" : new Date() } };
//         }
//         console.log(update);
//         that.users.update(query, update, this);
//       },
//       function onResult(error, result) {
//         if(error) {
//           callback(error);
//         } else {
//           callback(null, result)
//         }
//       }
//     );
//   },



//   updateVlocOfUser : function(userId, action, vloc, callback) {
//     var that = this;
//     step (
//       function executeQuery() {
//         var query = { _id : new ObjectID.createFromHexString(userId) };
//         if (action == 'add') {
//           var update = { '$addToSet': {'vlocs': vloc }, '$set' : { "last_modified_vloc" : new Date() } };
//         } else if (action == 'remove'){
//           var update = { '$pull': { 'vlocs' : vloc }, '$set' : { "last_modified_vloc" : new Date() } };
//         } else if (action == 'remove-all'){
//           var update = { '$set': { 'vlocs' : [] }, '$unset' : { "last_modified_vloc" : 1 } };
//         }
//         that.users.update(query, update, this);
//       },
//       function onResult(error, result) {
//         if(error) {
//           callback(error);
//         } else {
//           callback(null, result)
//         }
//       }
//     );
//   },

};




MongoDbManager.Places = function(main){
  this.main = main;
  
  var that = this
  this.main.db.collection('googleplaces', function(error, collection) {
    if (error) {}
    else {
      that.places = collection; 
    }
  });
};

MongoDbManager.Places.prototype = {
  


//   getPlacesWithinBox : function(latitudeA, longitudeA, latitudeB, longitudeB, limit, callback) {
//     var that = this;
//     step(
//       function executeQuery(){
// 	var query = { ploc : { $geoWithin : { $box : [ [ longitudeA, latitudeA ] , [ longitudeB, latitudeB ] ] } }, website : { $exists : 1 } };
// 	console.log(query);
//  	that.places.find(query).limit(limit).toArray(this);
//       },
//       function onResult(error, result){
//  	if (error) {
//  	  callback(error);
//  	} else {
// 	  for(var i = 0; i < result.length; i++) {
// 	    var placeType = result[i]['types'][0];
// 	    var placeCategory = that.main.server.fileManager.placeTypeMapper.map[placeType];
// 	    console.log(placeCategory);
// 	    result[i]['category'] = placeCategory;
// 	  }
//  	  callback(null, result)
//  	}
//       }
//     );
//   },
  
  
  getPlacesWithinCircle : function(latitude, longitude, radius, limit, callback) {
    var that = this;
    step(
      function executeQuery(){
	var query = { ploc : { $near : [ longitude, latitude ] , $maxDistance: radius/111120 } , website : { $exists : 1 } };
	var projection = { name : 1, vloc : 1 };
 	that.places.find(query, projection).limit(limit).toArray(this);
      },
      function onResult(error, result){
 	if (error) {
 	  callback(error);
 	} else {
	  for(var i = 0; i < result.length; i++) {
	    var placeType = result[i]['types'][0];
	    var placeCategory = that.main.server.fileManager.placeTypeMapper.map[placeType];
	    console.log(placeCategory);
	    result[i]['category'] = placeCategory;
	  }
 	  callback(null, result)
 	}
      }
    );
  },
  
  
//   getPlocForVloc : function(vloc, limit, callback) {
//     var that = this;
//     step (
//       function executeQuery(){
// 	var query = { vloc : vloc };
// 	var projection = { ploc : 1 };
//  	that.places.find(query, projection).limit(limit).toArray(this);
//       },
//       function onResult(error, result){
//  	if (error) {
//  	  callback(error);
//  	} else {
//  	  callback(null, result)
//  	}
//       }
//     );
//   },
//   
};


MongoDbManager.Notes = function(main){
  this.main = main;
  
  var that = this
  this.main.db.collection('notes', function(error, collection) {
    if (error) {}
    else {
      that.notes = collection; 
    }
  });
};

MongoDbManager.Notes.prototype = {
  
  getNotes : function(placeId, skip, limit, callback) {
    var that = this;
    step(
      function executeQuery(){
	var query = { place_id : placeId };
 	that.notes.find(query).skip(skip).limit(limit).toArray(this);
      },
      function onResult(error, result){
 	if (error) {
 	  callback(error);
 	} else {
 	  callback(null, result)
 	}
      }
    );
  },
  
    
  inserNote : function(placeId, userId, userName, text, callback) {
    var that = this;
    console.log(text);
    step(
      function executeQuery() {
	var date = new Date();
	var query = { 'place_id' : placeId, 'author' : { 'id' : userId , 'name' : userName }, 'created_at' : date, 'text' : text };
	console.log(query);
	that.notes.insert(query, this);
      },
      function onInsert(error) {
	if (error) {
	  callback(error) ;
	} else {
	  var result = {}
	  callback(null, result);
	}
      }
    );
    
  },
    
};





MongoDbManager.Netlocs = function(main){
  this.main = main;
  
  var that = this
  this.main.db.collection('netlocindex', function(error, collection) {
    if (error) {}
    else {
      that.netlocIndex = collection; 
    }
  });
};

MongoDbManager.Netlocs.prototype = {
  
//   getNetloc : function(netloc, callback) {
//     var that = this;
//     step(
//       function executeQuery(){
// 	var query = { netloc : netloc };
// 	console.log(query);
//  	that.netlocIndex.findOne(query, this);
//       },
//       function onResult(error, result){
//  	if (error) {
// 	  console.log(error);
//  	  callback(error);
//  	} else {
// 	  console.log(finalResult);
// 	  var finalResult = { 'netloc' : netloc, 'result' : result };
// 	  console.log(finalResult);
//  	  callback(null, finalResult);
//  	}
//       }
//     );
//   },
  
    
};



exports.MongoDbManager = MongoDbManager;