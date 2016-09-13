var crypto = require('crypto');
var unshort = require('unshort');
var step=require('step');
var nodejsUrl = require('url');
 
 
UrlManager = function(server) {
  this.server = server;
  this.urlUnshortener = new UrlManager.Unshortener(this);
  this.urlMapper = new UrlManager.UrlMapper(this);
};



UrlManager.Unshortener = function(main) {
  this.main  = main; 
};

UrlManager.Unshortener.prototype = {

  unshortenUrl : function(url, doUnshorten, callback) {
    if (doUnshorten > 0) {
      unshort(url, function (error, result) {
	if (error) {
	  finalResult = { 'url' : url};
	} else {
	  finalResult = { 'url' : result };	
	}
	callback(null, finalResult);
      });
    } else {
      callback(null, { 'url' : url});
    }
  },  

  
  
  
  
};



UrlManager.UrlMapper = function(main) {
  this.main = main; 
  this.sha1 = crypto.createHash('sha1');
};

UrlManager.UrlMapper.prototype = {

  calculateSha1 : function(str) {
    //this.sha1.update(str);
    //return this.sha1.digest('hex');
    return require('crypto').createHash('sha1').update(str).digest('hex');
  },
  
  
  
  
  //
  // curl "localhost:11090/url/map/?tabid=0&url=http%3A%2F%2Fwww.youtube.com%2Fwatch%3Ffeature%3Dplayer_detailpage%26v%3DkA0pkemJxMc%26hd%3D1%23t%3D4886&unshorten=0"
  // curl "localhost:11090/url/map/?tabid=0&url=http%3A%2F%2Fwww.marinabaysands.com%2Fwatch%3Ffeature%3Dplayer_detailpage%26v%3DkA0pkemJxMc%26hd%3D1%23t%3D4886&unshorten=0"
  //
  mapUrl : function(tabId, url, doUnshorten, callback) {
    var that = this;
    var finalUrl = ''
    step (
      function unshortenUrl() {
	that.main.urlUnshortener.unshortenUrl(url, doUnshorten, this);
      },
      function onUnshortenedUrlReceived(error, result) {
	if (error) {
	  callback(error);
	} else {
	  finalUrl = result.url;
	  var netloc = nodejsUrl.parse(finalUrl).hostname;
	  that.main.server.mysqlManagerOnespace.netlocs.getNetloc(netloc, this);
	}
      },
      function onNetlocDataReceived(error, result) {
	if (error) {
	  callback(null, {});
	} else {
	  var vloc = that.handleNetlocResult(finalUrl, result);
	  if(vloc.indexOf('?') === -1) { vloc += '?'; }
	  var vlocSha1 = that.calculateSha1(vloc);
	  //var finalResult = { 'tabid' : tabid, 'url' : finalUrl, 'vloc' : vloc, 'vloc-sha1' : vlocSha1 };
	  //callback(null, finalResult);
	  that.main.server.mysqlManagerOnespace.linker.getVirtualPlaceIds(tabId, 'vplaces', vloc, vlocSha1, this);
	  console.log(vloc);
	}
      },
      function onVirtualPlaceIdsReceived(error, result) {
        if (error) {
	  console.log(error);
          callback(null, {});
        } else {
          var vplaces = result['data'];
          //if (vplaces.length == 0) { vplaces = [result['vloc-sha1']]; }
          var finalResult = { 'tabid' : tabId, 'url' : url, 'vloc' : result['vloc'], 'vloc-sha1' : result['vloc-sha1'], 'vplaces' : vplaces };
          callback(null, finalResult);
        }
      }

      


    );
  },  
  
  
  handleNetlocResult : function(url, query_result) {
    if (query_result.length == 0) {
      var parsedUrl = nodejsUrl.parse(url, true);
      delete parsedUrl['hash'];
      var new_url = nodejsUrl.format(parsedUrl);
      new_url = new_url.replace(/.*?:\/\//g, "");
      return new_url;
    }

    vlocDict = {}
    for(var i = 0; i < query_result.length; i++) {
      var pathDepth = query_result[i]['path_depth'];
      var parameter = query_result[i]['parameter']
      if (pathDepth in vlocDict) {
	vlocDict[pathDepth].push(parameter);
      } else {
	vlocDict[pathDepth] = [parameter];
      }
    }
    
    if (-1 in vlocDict) {
      var parsedUrl = nodejsUrl.parse(url, true);
      delete parsedUrl['hash'];
      delete parsedUrl['search'];
      delete parsedUrl['query'];
      parsedUrl['pathname'] = '/';
      var new_url = nodejsUrl.format(parsedUrl);
      new_url = new_url.replace(/.*?:\/\//g, "");
      return new_url;
    }
    
    var parsedUrl = nodejsUrl.parse(url, true); // true is needed here to also parse the query string into a dictionary
    var pathDepth = this.getPathDepth(parsedUrl.pathname);
    if (pathDepth in vlocDict) {
      var new_query = {};
      var keys = vlocDict[pathDepth];
      for (var key in parsedUrl.query) {
	if (keys.indexOf(key) > -1) {
	  new_query[key] = parsedUrl.query[key];
	}
      }
      delete parsedUrl['search'];
      delete parsedUrl['hash'];
      parsedUrl.query = new_query;
      new_url = nodejsUrl.format(parsedUrl);
      new_url = new_url.replace(/.*?:\/\//g, "");
      return new_url;
    }
    
    return url;
    
    
    
    //if (query_result.result.preset) {
    //  return query_result.result.preset;
    //}

//     if (query_result.result.map) {
//       var parsedUrl = nodejsUrl.parse(url, true); // true is needed here to also parse the query string into a dictionary
//       var pathDepth = this.getPathDepth(parsedUrl.pathname);
//       if (pathDepth in query_result.result.map) {
//         var new_query = {};
//         var keys = query_result.result.map[pathDepth];
//         for (var key in parsedUrl.query) {
//           if (keys.indexOf(key) > -1) {
//             new_query[key] = parsedUrl.query[key];
//           }
//         }
//         delete parsedUrl['search'];
// 	delete parsedUrl['hash'];
//         parsedUrl.query = new_query;
//         new_url = nodejsUrl.format(parsedUrl);
// 	new_url = new_url.replace(/.*?:\/\//g, "");
//         return new_url;
//       } else {
//         // WHAT TO DO HERE???
//         return url;
//       }
//
//    }
    
    return url;
  },
  
  
  getPathDepth : function(pathname) {
    return(pathname.match(/\//g) || []).length;
  },
  

};





exports.UrlManager = UrlManager;

// curl "http://172.29.33.45:11090/url/map/?tabid=0&url=http%3A%2F%2Fwww.youtube.com%2Fwatch%3Ffeature%3Dplayer_detailpage%26v%3DkA0pkemJxMc%26hd%3D1%23t%3D4886&unshorten=0"


// curl "http://172.29.33.45:11090/url/map/?tabid=0&url=http%3A%2F%2Fwww.tripadvisor.com.sg%2FShowUserReviews-g294265-d3351111-r258512897-Ristorante_Amarone-Singapore.html%23REVIEWS&unshorten=0"
// curl "http://172.29.33.45:11090/url/map/?tabid=0&url=http%3A%2F%2Fwww.marinabaysands.com&unshorten=0"