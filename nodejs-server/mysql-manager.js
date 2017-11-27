var step = require('step');
var uuid = require('node-uuid');
var mysql = require('mysql');
 
const MAX_CORNER_COUNT_PER_USER = 3;


MysqlManager = function(server, host, user, password, database) {
  this.dbConfig = {
    host     : host,
    user     : user,
    password : password,
    database : database
  }

  this.server = server;
  this.connection = null;

  
  this.users = new MysqlManager.Users(this);
  this.places = new MysqlManager.Places(this);
  this.netlocs = new MysqlManager.Netlocs(this);
  this.corners = new MysqlManager.Corners(this);
  this.notes = new MysqlManager.Notes(this);
  this.linker = new MysqlManager.Linker(this);
  this.tweets = new MysqlManager.Tweets(this);
  this.youtube = new MysqlManager.Youtube(this);
  this.flickr = new MysqlManager.Flickr(this);
  this.instagram = new MysqlManager.Instagram(this);
  this.nea = new MysqlManager.NEA(this);
  this.lta = new MysqlManager.LTA(this);
  this.golocal = new MysqlManager.GoLocal(this);
  this.mediaUploader = new MysqlManager.MediaUploader(this);
  this.ejabberd = new MysqlManager.Ejabberd(this);

  this.handleDisconnect();
};


MysqlManager.prototype =  {

  handleDisconnect : function() {
    var that = this;
    
    console.log("MysqlManager.handleDisconnect: " + this.dbConfig.host + " / " + this.dbConfig.database);
    this.connection = mysql.createConnection(this.dbConfig);  // Recreate the connection, since the old one cannot be reused.

    this.connection.connect(function(err) {                   // The server is either down
      if(err) {                                               // or restarting (takes a while sometimes).
        console.log('error when connecting to db:', err);
        setTimeout(that.handleDisconnect, 2000);              // We introduce a delay before attempting to reconnect,
      }                                                       // to avoid a hot loop, and to allow our node script to
    });                                                       // process asynchronous requests in the meantime.
                                                              // If you're also serving http, display a 503 error.
    this.connection.on('error', function(err) {
      console.log('db error', err);
      if(err.code === 'PROTOCOL_CONNECTION_LOST') {           // Connection to the MySQL server is usually
        that.handleDisconnect();                              // lost due to either server restart, or a
      } else {                                                // connnection idle timeout (the wait_timeout
        throw err;                                            // server variable configures this)
      }
    });
  },

};





MysqlManager.Users = function(main) {
  this.main  = main; 
};

MysqlManager.Users.prototype = {

  
  
  getUserByJid : function(jid, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT HEX(id) AS id, fcm_token FROM osUsers WHERE jid = '" + jid + "'";
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          var res = null;
          for(var i = 0; i < result.length; i++) {
            res = { "id" : result[i]['id'], "jid" : jid, "fcmtoken" : result[i]['fcm_token'] };
          }
          callback(null, res );
        }
      }
    );
  },
  
  
  
  //
  // curl  -d "" "http://172.29.33.45:11090/user/login/?name=barney&password=pwd&jid=barney@172.29.32.195&jidresource=conference"
  // curl  -d "" "http://172.29.33.45:11090/user/login/?name=test&password=123456&jid=test@172.29.32.195&jidresource=conference"
  //
  handleUserLogin : function(name, password, jid, jidResource, callback) {
    console.log('>>>' + name);
    console.log('>>>' + password);
    console.log('>>>' + jid);
    console.log('>>>' + jidResource);
    var that = this;
    step(
      function getUser() {
        that.getUserByJid(jid, this);
      },
      function onGetUserResultReceived(error, result){
        if (error) {
          callback(error);
        } else {
          if (result) {
            callback(null, result);
          } else {
            var query = "INSERT INTO osUsers (name, password, jid, jid_resource) VALUES ('" + name +"', '" + password + "', '" + jid +"' , '" + jidResource +"') ";
            that.main.connection.query(query, this);
          }
        }
      },
      function onInsert(error) {
        if (error) {
          callback(error) ;
        } else {
          that.handleUserLogin(name, password, jid, jidResource, callback);
        }
      }
    );
    
  },
    
  
  //
  // curl  -X PUT "http://172.29.32.195:11090/user/fcm/?userid=f6447838e6c611e6b5233417ebb4b372&fcmtoken=123"
  //
  updateFcmToken : function(userId, fcmToken, callback) {
    var that = this;
    step(
      function executeQuery() {
        var query = "UPDATE osUsers SET fcm_token = '" + fcmToken + "' WHERE id = UNHEX('" + userId + "')";
        console.log(query);
        that.main.connection.query(query, this);
      },
      function onResult(error, result) {
        if(error) {
          callback(error);
        } else {
          callback(null, result)
        }
      }
    );
  },
    
  

  //
  // curl  -X PUT "http://172.29.32.195:11090/user/ploc/?userid=f6447838e6c611e6b5233417ebb4b372&lat=1.292454&lng=103.774118"
  //
  updatePlocOfUser : function(userId, latitude, longitude, callback) {
    var that = this;
    step(
      function executeQuery() {
        if ((Math.abs(latitude) > 90) || (Math.abs(longitude) > 180)) {
          var query = "DELETE FROM osWalkers WHERE user_id = UNHEX('" + userId + "')";
        } else {
          var query = "INSERT INTO osWalkers (user_id, ploc) VALUES (UNHEX('" + userId + "'), POINT(" + latitude + ", " + longitude + ")) ON DUPLICATE KEY UPDATE ploc = POINT(" + latitude + ", " + longitude + "), created = CURRENT_TIMESTAMP;";
        }
        q = query
        that.main.connection.query(query, this);
      },
      function onResult(error, result) {
        if(error) {
          callback(error);
        } else {
          callback(null, result)
        }
      }
    );
  },
  
  
  //              "http://172.29.32.195:11090/user/vloc/?userid=153DA8AC792A11E580E23417EBA1E68F&action=add&vloc=33818"
  // curl  -X PUT "http://172.29.32.195:11090/user/vloc/?userid=68AD856477C911E580E23417EBA1E68F&action=add&vloc=www.mytengah.com" 
  // curl  -X PUT "http://172.29.32.195:11090/user/vloc/?userid=68AD856477C911E580E23417EBA1E68F&action=add&vloc=bschool.nus.edu" 
  //
  //
  //
  updateVlocOfUser : function(userId, action, vloc, callback) {
    var that = this;
    step (
      function getPlocs() {
        that.main.places.getPlocForVloc(vloc, 1, this);
      },
      function onPlocsReceived(error, result) {
        if (error) {
          callback(error);
        } else {
          console.log(result);
          if (result.length == 0) {
            result.push({ 'lat' : 1000, 'lng' : 1000});
          }	  
          for(var i = 0; i < result.length; i++) {
            var latitude = result[i]['lat'];
            var longitude = result[i]['lng'];
            if (action == 'add') {
              var query = "INSERT IGNORE osSurfers (user_id, vloc, ploc) VALUES (UNHEX('" + userId + "'), '" + vloc + "', POINT(" + latitude + ", " + longitude + "))";
            } else if (action == 'remove'){
              var query = "DELETE FROM osSurfers WHERE user_id = UNHEX('" + userId + "') AND vloc = '" + vloc + "'";
            } else if (action == 'remove-all'){
              var query = "DELETE FROM osSurfers WHERE user_id = UNHEX('" + userId + "')";
            }
            console.log(query);
            that.main.connection.query(query, this);
          }
        }
      },
      function onResult(error, result) {
        if(error) {
          callback(error);
        } else {
          callback(null, result)
        }
      }
    );
  },
  
  
  //
  // curl "http://172.29.32.195:11090/surfers/box/?lat1=1.29648&lng1=103.769976&lat2=1.292243&lng2=103.774965&limit=1"
  // curl "http://172.29.33.45:11090/surfers/box/?lat1=1.29648&lng1=103.769976&lat2=1.292243&lng2=103.774965&limit=1"
  //
  getSurfersWithinBox : function(latA, lngA, latB, lngB, limit, callback) {
    var that = this;
    // Build bounding box polygon for MySQL spatial query
    var polygon = latA + " " + lngA + ", " + latA + " " + lngB + ", " + latB + " " + lngB + ", " + latB + " " + lngA + ", " + latA + " " + lngA;
    
    step(
      function executeQuery(){
        var query = "SELECT HEX(u.id) AS user_id, u.name AS user_name, u.jid AS jid, u.jid_resource AS resource, p.name AS place_name, X(s.ploc) AS lat, Y(s.ploc) AS lng, s.vloc AS vloc, p.website AS website FROM osUsers u, osSurfers s, osPlaces p WHERE u.id = s.user_id AND s.vloc = p.vloc AND s.ploc = p.ploc AND WITHIN(s.ploc, GeomFromText('POLYGON((" + polygon + " ))') ) LIMIT " + limit;
        that.main.connection.query(query, this);
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
  
  
  //
  // curl  "http://172.29.32.195:11090/walkers/box/?lat1=1.29648&lng1=103.769976&lat2=1.292243&lng2=103.774965&limit=1"
  //
  getWalkersWithinBox : function(latA, lngA, latB, lngB, limit, callback) {
    var that = this;
    // Build bounding box polygon for MySQL spatial query
    var polygon = latA + " " + lngA + ", " + latA + " " + lngB + ", " + latB + " " + lngB + ", " + latB + " " + lngA + ", " + latA + " " + lngA;
    
    step(
      function executeQuery(){
        var query = "SELECT HEX(u.id) AS user_id, u.name AS user_name, u.jid AS jid, u.jid_resource AS resource, X(w.ploc) AS lat, Y(w.ploc) AS lng FROM osUsers u, osWalkers w WHERE u.id = w.user_id AND WITHIN(w.ploc, GeomFromText('POLYGON((" + polygon + " ))') ) LIMIT " + limit;
        that.main.connection.query(query, this);
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
  
  
  //
  // curl  "http://172.29.32.195:11090/walkers/vloc/?vloc=www.marinabaysands.com%2F%3F&maxdistance=10000"
  //
  getWalkersAroundVloc : function(vloc, maxDistance, callback) {
    var that = this;
    
    step(
      function executeQuery(){
        var query = "SELECT HEX(u.id) AS user_id, u.name, u.jid, u.jid_resource, w2.distance_in_meter FROM osUsers u, (SELECT w.user_id, MIN(ROUND(glength(LineStringFromWKB(LineString(GeomFromText(astext(PointFromWKB(w.ploc))),GeomFromText(astext(PointFromWKB(p.ploc))))))*100*1000)) AS distance_in_meter FROM osWalkers w, (SELECT id, ploc FROM osPlaces WHERE vloc = '" + vloc + "') p GROUP BY w.user_id) w2 WHERE w2.user_id = u.id AND w2.distance_in_meter <= " + maxDistance + " ORDER BY w2.distance_in_meter";
        //console.log(query);
        that.main.connection.query(query, this);
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
};




MysqlManager.Places = function(main) {
  this.main  = main; 
};

MysqlManager.Places.prototype = {

  
  //
  // curl  "http://172.29.32.195:11090/places/box/?lat1=1.292064&lng1=103.775114&lat2=1.293019&lng2=103.776233&limit=1"
  // curl  "http://172.29.33.45:11090/places/box/?lat1=1.292064&lng1=103.775114&lat2=1.293019&lng2=103.776233&limit=1"
  //
  getPlacesWithinBox : function(latA, lngA, latB, lngB, limit, callback) {
    var that = this;
    // Build bounding box polygon for MySQL spatial query
    var polygon = latA + " " + lngA + ", " + latA + " " + lngB + ", " + latB + " " + lngB + ", " + latB + " " + lngA + ", " + latA + " " + lngA;
    
    step(
      function executeQuery(){
        var query = "SELECT id, name, formatted_address, formatted_phone_nr, primary_category, X(ploc) AS lat, Y(ploc) AS lng, vloc, website FROM osPlaces WHERE WITHIN(ploc, GeomFromText('POLYGON((" + polygon + " ))') ) LIMIT " + limit;
              console.log(query);
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          for(var i = 0; i < result.length; i++) {
            var name = result[i]['name'];
            var primaryCategory = result[i]['primary_category']
            var placeCategory = that.main.server.fileManager.placeTypeMapper.map[primaryCategory];
            result[i]['category_class'] = placeCategory;
          }
          callback(null, result)
        }
      }
    );
  },

  
  //
  // curl "http://172.29.32.195:11090/places/ploc/?vloc=www.comp.nus.edu.sg/?&limit=1"
  //
  getPlocForVloc : function(vloc, limit, callback) {
    var that = this;
    step (
      function executeQuery(){
        var query = "SELECT id, X(ploc) AS lat, Y(ploc) AS lng FROM osPlaces WHERE vloc = '" + vloc + "' LIMIT " + limit;
        console.log(query);
        that.main.connection.query(query, this);
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
  
  
};





MysqlManager.Netlocs = function(main){
  this.main = main;
};

MysqlManager.Netlocs.prototype = {
  
  
  //
  // curl "http://172.29.32.195:11090/places/ploc/?vloc=www.comp.nus.edu.sg/?&limit=1"
  //
  getNetloc : function(vloc, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT path_depth, parameter FROM osNetlocIndex WHERE vloc = '" + vloc + "'";
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          console.log(error);
          callback(error);
        } else {
          callback(null, result);
        }
      }
    );
  },
    
};





MysqlManager.Notes = function(main){
  this.main = main;
};

MysqlManager.Notes.prototype = {
  
  //
  // curl  "http://172.29.32.195:11090/notes/532227f98d9271be7f8fc875be5ff48dc8864a02/?skip=0&limit=10"
  //
  getNotes : function(placeId, skip, limit, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT HEX(vplace_id) AS vplace_id, HEX(user_id) AS user_id, user_name, created, text FROM osNotes WHERE vplace_id = UNHEX('" + placeId + "')";
        console.log(query);
        that.main.connection.query(query, this);
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
  
  //
  // curl  -d "" "http://172.29.32.195:11090/notes/add/?place_id=532227f98d9271be7f8fc875be5ff48dc8864a02&user_id=68AD856477C911E580E23417EBA1E68F&user_name=barney&text=Test"
  //
  inserNote : function(placeId, userId, userName, text, callback) {
    var that = this;
    step(
      function executeQuery() {
        var query = "INSERT IGNORE osNotes (vplace_id, user_id, user_name, text) VALUES (UNHEX('" + placeId + "'), UNHEX('" + userId + "'), '" + userName + "', '" + text + "')";
        that.main.connection.query(query, this);
      },
      function onInsert(error, result) {
        if (error) {
          callback(error) ;
        } else {
          callback(null, result);
        }
      }
    );
    
  },    
  
  
};









MysqlManager.Linker = function(main) {
  this.main  = main; 
};

MysqlManager.Linker.prototype = {

  //
  // curl  "http://172.29.32.195:11090/notes/532227f98d9271be7f8fc875be5ff48dc8864a02/?skip=0&limit=10"
  //  
  getVirtualPlaceIds : function(tabId, type, vloc, vlocSha1, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT p.vplace_id, IFNULL(p.physical_name, '[na]') AS name FROM osvplaces p, osvplaces_vlocs_map m, osvlocs l WHERE p.vplace_id = m.vplace_id AND m.vloc_id = l.vloc_id AND l.url = '" + vloc + "'"
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          var result = { 'tabid' : tabId, 'type' : type, 'vloc' : vloc, 'vloc-sha1' : vlocSha1, 'data' : result };
          callback(null, result);
        }
      }
      
      
    );
  },
  

};




MysqlManager.Corners = function(main){
  this.main = main;
};

MysqlManager.Corners.prototype = {
  
  //
  // curl  "http://172.29.32.195:11090/corners/box/?lat1=1.272064&lng1=102.775114&lat2=1.303019&lng2=104.776233&limit=1"
  //
  getUserCornersWithinBox : function(latA, lngA, latB, lngB, limit, callback) {
    var that = this;
    // Build bounding box polygon for MySQL spatial query
    var polygon = latA + " " + lngA + ", " + latA + " " + lngB + ", " + latB + " " + lngB + ", " + latB + " " + lngA + ", " + latA + " " + lngA;
    
    step(
      function executeQuery(){
        var query = "SELECT id, HEX(creator_id) AS creator_id, creator_jid, creator_jid_resource, room_jid, room_jid_resource, name, description, X(ploc) AS lat, Y(ploc) AS lng, created FROM osUserCorners WHERE WITHIN(ploc, GeomFromText('POLYGON((" + polygon + " ))') ) LIMIT " + limit;
        console.log(query);
        that.main.connection.query(query, this);
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


  //
  // curl  "http://172.29.32.195:11090/corners/user/68AD856477C911E580E23417EBA1E68F"
  //
  getUserCornersForUser : function(creatorId, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT id, HEX(creator_id) AS creator_id, creator_jid, creator_jid_resource, room_jid, room_jid_resource, name, description, X(ploc) AS lat, Y(ploc) AS lng, created FROM osUserCorners WHERE creator_id = UNHEX('" + creatorId + "')";
        console.log(query);
        that.main.connection.query(query, this);
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
  

  

  //
  // curl -d ""  "http://172.29.32.195:11090/corners/add/?creatorid=68AD856477C911E580E23417EBA1E68F&creatorname=barney&creatorjid=barney@172.29.32.195&creatorjidresource=conference&name=TestCorner&description=BlaBlubb&lat=1.292494&lng=103.774829"
  //
  createUserCorner : function(creatorId, creatorName, creatorJid, creatorJidResource, name, description, latitude, longitude, callback) {
    var that = this;
    // roomJid = room + "@" + this.resource + "." + this.server;
    var server = (creatorJid.split("@")[1]).split("/")[0]
    var roomJidResource = creatorJidResource;
    var roomJidName = uuid.v1().replace(/-/g, "");
    var roomJid = roomJidName + "@" + roomJidResource + "." + server;
    console.log('>>> ' + roomJid);

    
    step(
      function executeQuery(){
        var query = "SELECT COUNT(*) AS 'cnt' FROM osUserCorners WHERE creator_id = UNHEX('" + creatorId + "')";
        console.log(query);
        that.main.connection.query(query, this);
      },
      function onCornerCountReceived(error, result){
        if (error) {
          callback(error, { errorcode: 31, errormsg: "Something went wrong."});
        } else {
          console.log(result[0]);
          var cornerCount = result[0]['cnt'];
          if (cornerCount >= MAX_CORNER_COUNT_PER_USER) {
            callback(error, { errorcode: 30, errormsg: "You have already created " + MAX_CORNER_COUNT_PER_USER + " corners"});
          } else {
            var query = "INSERT INTO osUserCorners (creator_id, creator_name, creator_jid, creator_jid_resource, room_jid, room_jid_resource, name, description, ploc) VALUE (UNHEX('"+ creatorId + "'), '" + creatorName + "', '" + creatorJid + "' , '" + creatorJidResource + "', '" + roomJid + "', '" + roomJidResource + "', '" + name + "', '" + description + "', POINT(" + latitude + ", " + longitude + "))";
            console.log(query);
            that.main.connection.query(query, this);
          }
        }
      },
      function onResult(error, result){
        if (error) {
          console.log(error);
          callback(error, { errorcode: 34, errormsg: "Corner with the same name already exists."});
        } else {
          res = { name : name, roomjid : roomJid }
          callback(null, res);
        }
      }
    );
  },
  
  
  //
  // curl -d "" "http://172.29.32.195:11090/corners/delete/?creatorid=68AD856477C911E580E23417EBA1E68F"
  // curl -d "" "http://172.29.32.195:11090/corners/delete/?creatorid=68AD856477C911E580E23417EBA1E68F&roomjid=..."
  //
  deleteUserCorners : function(creatorId, roomJid, callback) {
    var that = this;
    step(
      function executeQuery(){
        if (typeof roomJid === 'undefined') {
          var query = "DELETE FROM osUserCorners WHERE creator_id = UNHEX('" + creatorId + "')";
        } else {
          var query = "DELETE FROM osUserCorners WHERE creator_id = UNHEX('" + creatorId + "') AND room_jid = '" + roomJid + "'";
        }
        console.log(query);
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          console.log(result);
          callback(null, result);
        }
      }
    );
  },
   
 
};




MysqlManager.Tweets = function(main) {
  this.main  = main; 
};

MysqlManager.Tweets.prototype = {

  getLatestTweets : function(tabId, type, vloc, vlocSha1, limit, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = 'SELECT tweet_timestamp, tweet_screen_name, tweet_text FROM osGeotweetsVlocs WHERE vloc = \'' + vloc + '\' ORDER BY tweet_timestamp DESC LIMIT ' + limit;
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          tweets = result;
          var result = { 'tabid' : tabId, 'type' : type, 'vloc' : vloc, 'vloc-sha1' : vlocSha1, 'data' : tweets };
          callback(null, result);
        }
      }
    );
  },
  

};



MysqlManager.Youtube = function(main) {
  this.main  = main; 
};

MysqlManager.Youtube.prototype = {

  getVideos : function(tabId, type, vloc, vlocSha1, limit, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT v1.dom, v2.url, v2.title FROM osvlocs v1, osvplaces_vlocs_map m1, osvplaces_vlocs_map m2, osvlocs v2 WHERE v1.vloc_id = m1.vloc_id AND m2.vplace_id = m1.vplace_id AND m2.vloc_id = v2.vloc_id AND m1.vloc_id != m2.vloc_id AND v2.dom = 'www.youtube.com' AND v1.url = '" + vloc + "' LIMIT " + limit;
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          tweets = result;
          var result = { 'tabid' : tabId, 'type' : type, 'vloc' : vloc, 'vloc-sha1' : vlocSha1, 'data' : tweets };
          callback(null, result);
        }
      }
    );
  },
  

};



MysqlManager.Flickr = function(main) {
  this.main  = main; 
};

MysqlManager.Flickr.prototype = {

  getImages : function(tabId, type, vloc, vlocSha1, limit, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT v1.dom, v2.url, v2.title FROM osvlocs v1, osvplaces_vlocs_map m1, osvplaces_vlocs_map m2, osvlocs v2 WHERE v1.vloc_id = m1.vloc_id AND m2.vplace_id = m1.vplace_id AND m2.vloc_id = v2.vloc_id AND m1.vloc_id != m2.vloc_id AND v2.dom LIKE 'farm%.staticflickr.com' AND v1.url = '" + vloc + "' LIMIT " + limit;
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          tweets = result;
          var result = { 'tabid' : tabId, 'type' : type, 'vloc' : vloc, 'vloc-sha1' : vlocSha1, 'data' : tweets };
          callback(null, result);
        }
      }
    );
  },
  

};



MysqlManager.Instagram = function(main) {
  this.main  = main; 
};

MysqlManager.Instagram.prototype = {

  getImages : function(tabId, type, vloc, vlocSha1, limit, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT i.dom, i.title, i.url FROM ostweetsinstavlocs i, osGeotweetsVlocs t WHERE t.vloc='" + vloc  + "' and i.tweet_id_str = t.tweet_id_str and i.ct_id=2 LIMIT " + limit; 
        console.log(query);
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          tweets = result;
          var result = { 'tabid' : tabId, 'type' : type, 'vloc' : vloc, 'vloc-sha1' : vlocSha1, 'data' : tweets };
          callback(null, result);
        }
      }
    );
  },
  

};


MysqlManager.NEA = function(main) {
  this.main  = main; 
};

MysqlManager.NEA.prototype = {

  getNowcast : function(tabId, type, vloc, vlocSha1, limit, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT n.location, n.icon, n.update_timestamp, TRUNCATE(glength(LineStringFromWKB(LineString(GeomFromText(astext(PointFromWKB(n.ploc))),GeomFromText(astext(PointFromWKB(p.ploc))))))*100, 2) AS distance_in_km FROM (SELECT n.* FROM osNeaNowcast n JOIN (SELECT location, MAX(update_timestamp) maxTimestamp FROM osNeaNowcast GROUP BY location) n2 ON n.update_timestamp = n2.maxTimestamp AND n.location = n2.location) n, (SELECT ploc FROM osPlaces WHERE vloc = '" + vloc +"' LIMIT 1) p ORDER BY distance_in_km, update_timestamp DESC LIMIT " + limit; 
        console.log(query);
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          var data = result;
          var result = { 'tabid' : tabId, 'type' : type, 'vloc' : vloc, 'vloc-sha1' : vlocSha1, 'data' : data };
          callback(null, result);
        }
      }
    );
  },
  

};



MysqlManager.LTA = function(main) {
  this.main  = main; 
};

MysqlManager.LTA.prototype = {

  getCarparkAvailability : function(tabId, type, vloc, vlocSha1, limit, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT n.carpark_id, n.area, n.development, n.available_lots, TRUNCATE(glength(LineStringFromWKB(LineString(GeomFromText(astext(PointFromWKB(n.ploc))),GeomFromText(astext(PointFromWKB(p.ploc))))))*100, 2) AS distance_in_km FROM (SELECT n.* FROM osLtaCarparkAvailability n JOIN (SELECT MAX(update_timestamp) AS maxTimestamp FROM osLtaCarparkAvailability) n2 ON n.update_timestamp = n2.maxTimestamp) n, (SELECT ploc FROM osPlaces WHERE vloc = '" + vloc +"' LIMIT 1) p ORDER BY distance_in_km, update_timestamp DESC LIMIT " + limit; 
        console.log(query);
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          var data = result;
          var result = { 'tabid' : tabId, 'type' : type, 'vloc' : vloc, 'vloc-sha1' : vlocSha1, 'data' : data };
          callback(null, result);
        }
      }
    );
  },
  

  getBusStopInformation : function(tabId, type, vloc, vlocSha1, limit, callback) {
    var arrivalTimes = {};
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT b.code, b.description, TRUNCATE(glength(LineStringFromWKB(LineString(GeomFromText(astext(PointFromWKB(b.ploc))),GeomFromText(astext(PointFromWKB(p.ploc))))))*100, 2) AS distance_in_km FROM (SELECT ploc, code, description FROM osBusStopLocations) b, (SELECT ploc FROM osPlaces WHERE vloc = '"+vloc+"' LIMIT 1) p ORDER BY distance_in_km ASC LIMIT " + limit; 
        console.log(query);
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          var data = result;
          var numCompletedCalls = 0
          for (var i = 0; i < data.length; i++) {
            var item = data[i];
            arrivalTimes[item.code] = new Array();
            that.main.server.requestManager.lta.getBusArrivalTimes(item.code, function(error, result) { 
              for (var i = 0; i < result.Services.length; i++) {
                service = result.Services[i];
                if (service.NextBus.EstimatedArrival != '') {
                  arrivalTimes[result.BusStopID.toString()].push({'service_nr': service.ServiceNo, 'arrival_time' : service.NextBus.EstimatedArrival});
                }
              }

              numCompletedCalls++;
              if (numCompletedCalls == data.length) {
                console.log("Done all calls!");
                for (var i = 0; i < data.length; i++) {
                  var item = data[i];
                  item['arrival_times'] = arrivalTimes[item.code.toString()];
                }
                var result = { 'tabid' : tabId, 'type' : type, 'vloc' : vloc, 'vloc-sha1' : vlocSha1, 'data' : data };
                callback(null, result);
              }
            });
          }
        }
      }
    );
  },
  
};




MysqlManager.GoLocal = function(main) {
  this.main  = main; 
};

MysqlManager.GoLocal.prototype = {

  getLocalProducts : function(tabId, type, vloc, vlocSha1, limit, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT p.sname, p.pname, p.price, p.url, g.formatted_address, (g.ploc) AS lat, Y(g.ploc) AS lng FROM osPlaces g, os_local_products p, os_global_products m WHERE m.url ='" + vloc + "' AND m.p_id = p.p_id AND p.place_id = g.external_source_id AND g.external_source = 'google_places' ORDER BY p.sname ASC"; 
        console.log(query);
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          var data = result;
          var result = { 'tabid' : tabId, 'type' : type, 'vloc' : vloc, 'vloc-sha1' : vlocSha1, 'data' : data };
          callback(null, result);
        }
      }
    );
  },
  

};






MysqlManager.MediaUploader = function(main) {
  this.main  = main; 
};

MysqlManager.MediaUploader.prototype = {

  insertImageData : function(fromJid, fromJidResource, toJid, toJidResource, uploadUnixTimestamp, imageLink, thumbnailLink, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "INSERT INTO osMediaUploadsImages (from_jid, from_jid_resource, to_jid, to_jid_resource, upload_unix_timestamp, image_link, thumbnail_link) VALUES ('" + fromJid + "', '" + fromJidResource + "', '" + toJid + "', '" + toJidResource + "', " + uploadUnixTimestamp+ ", '" + imageLink +"', '" + thumbnailLink +"')"; 
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          callback(null, result);
        }
      }
    );
  },

};




MysqlManager.Ejabberd = function(main) {
  this.main  = main; 
};

MysqlManager.Ejabberd.prototype = {

  
  getMessageHistory : function(fromJid, fromJidResource, toJid, toJidResource, lastSentDate, limit, callback) {
    var that = this;
    step(
      function executeQuery(){
        var query = "SELECT timestamp AS sentDate, xml AS stanza, IF(username = SUBSTRING_INDEX('" + fromJid + "', '@', 1),'me','other') AS source FROM archive WHERE ((username = SUBSTRING_INDEX('" + fromJid + "', '@', 1) AND bare_peer = '" + toJid + "') OR (username = SUBSTRING_INDEX('" + toJid + "', '@', 1) AND bare_peer = '" + fromJid + "')) AND kind = 'chat' ORDER BY timestamp DESC LIMIT " + limit;
        //console.log(query);
        that.main.connection.query(query, this);
      },
      function onResult(error, result){
        if (error) {
          callback(error);
        } else {
          var messages = result;
          var result = { 'fromjid' : fromJid, 'fromjidresource' : fromJidResource, 'tojid' : toJid, 'tojidresource' : toJidResource, 'messages' : messages };
          callback(null, result);
        }
      }
    );
  },

};






exports.MysqlManager = MysqlManager;


// curl "http://172.29.33.45:11090/data/?tabid=0&type=twitter&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=twitter&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"

// curl "http://172.29.33.45:11090/data/?tabid=0&type=youtube&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=youtube&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"

// curl "http://172.29.33.45:11090/data/?tabid=0&type=flickr&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=flickr&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"

// curl "http://172.29.33.45:11090/data/?tabid=0&type=instagram&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=instagram&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"

// curl "http://172.29.33.45:11090/data/?tabid=0&type=nea&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=nea&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"

// curl "http://172.29.33.45:11090/data/?tabid=0&type=lta&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=lta&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"




// curl "http://172.29.32.195:11090/data/?tabid=0&type=lta-bus-stop-information&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=3"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=lta-bus-stop-information&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=3"

// curl "http://172.29.33.45:11090/data/?tabid=0&type=vplaces&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=vplaces&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a"






// curl "http://172.29.33.45:11090/data/?tabid=0&type=twitter&vloc=www.clarkequay.com.sg%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=1"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=twitter&vloc=www.clarkequay.com.sg%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=1"

// curl "http://172.29.33.45:11090/data/?tabid=0&type=youtube&vloc=www.clarkequay.com.sg%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=1"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=youtube&vloc=www.clarkequay.com.sg%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=1"

// curl "http://172.29.33.45:11090/data/?tabid=0&type=flickr&vloc=www.clarkequay.com.sg%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=1"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=flickr&vloc=www.clarkequay.com.sg%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=1"

// curl "http://172.29.33.45:11090/data/?tabid=0&type=instagram&vloc=www.clarkequay.com.sg%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=1"
// curl "http://172.29.32.195:11090/data/?tabid=0&type=instagram&vloc=www.clarkequay.com.sg%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=1"





// curl "http://172.29.32.195:11090/messages/history/?fromjid=chris@172.29.33.45&fromjidresource=conference&tojid=homer@172.29.33.45&tojidresource=conference&lastsentdate=1442476004805&limit=20"
