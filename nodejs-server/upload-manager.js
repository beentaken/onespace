var step = require('step');
var fs = require('fs');
var path = require('path');
var mimeTypes = require('mime-types');
var mkdirp = require('mkdirp');
var lwip = require('lwip');

const HOST_PATH = 'http://172.29.32.195/onespace'
const DIR_PATH = '/home/christian/www/onespace';
const MEDIA_UPLOAD_DIR_IMAGES = '/media/uploads/images/';
 
const IMAGE_THUMBNAIL_SIZE_WIDTH = 150;
const IMAGE_THUMBNAIL_SIZE_HEIGHT = 150;

UploadManager = function(server) {
  this.server = server;
  this.imageManager = new UploadManager.ImageManager(this);
};



Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};


UploadManager.prototype = {

  handleFiles : function(fromJid, fromJidResource, toJid, toJidResource, files, callback) {
    console.log(files);
    var fileCount = 0;
    for (var key in files) {
      fileArrayLength = files[key].length;
      if (fileArrayLength === undefined || fileArrayLength === null) {
	fileCount += 1
      } else {
	fileCount += fileArrayLength
      }
    }

    var handledFiles = { 'file-count' : fileCount, 'file-results' : {} };
    var demo = {};
    for (var key in files) {
      fileArrayLength = files[key].length;
      if (fileArrayLength === undefined || fileArrayLength === null) {
	var file = files[key];
	this.handleFile(fromJid, fromJidResource, toJid, toJidResource, file, handledFiles, callback);
      } else {
	for (var i = 0; i < files[key].length; i++) {
	  var file = files[key][i];
	  this.handleFile(fromJid, fromJidResource, toJid, toJidResource, file, handledFiles, callback);
	}
      }
    }
    
    //console.log(handledFiles);
    //callback(null, {});
  },


  handleFile : function(fromJid, fromJidResource, toJid, toJidResource, file, handledFiles, callback) {
    var that = this;
    
    var fileContentType = mimeTypes.lookup(file['originalFilename']).toLowerCase();
    var fileMimeType = fileContentType.split('/')[0];
    //console.log(file);
    switch (fileMimeType) {
      case 'image':
	that.imageManager.handleImage(fromJid, fromJidResource, toJid, toJidResource, file, handledFiles, this.onFileHandled, callback);
	break;
      default:
	this.deleteFile(file, handledFiles, this.onFileHandled, callback);
	break;
    }    
    
  },
  
  
  onFileHandled : function(error, handledFiles, callback) {
    var handledFilesCount = Object.size(handledFiles['file-results']);
    if (handledFiles['file-count'] == handledFilesCount) {
      console.log(handledFiles);
      var result = handledFiles['file-results'];
      callback(null, result);
    };
  },
  
  
  deleteFile : function(file, handledFiles, callbackFileHandled, callbackResponse) {
    var fileName = file.name;
    fs.unlink(file.path, function(error) {
      if (error) {
	callbackFileHandled(error, handledFiles, callbackResponse)
      } else {
	handledFiles['file-results'][fileName] = { 'result-code' : 100 };
	callbackFileHandled(null, handledFiles, callbackResponse);
      }
    });
  },
  
  
  getFileName : function(path) {
    return path.replace(/^.*[\\\/]/, '');
  },
  
  addFileNameSuffix : function(fileName, suffix) {
    return fileName.replace(/^([^.]*)\.(.*)$/, '$1'+suffix+'.$2');
  },
  
  getCurrentDateString : function() {
    var today = new Date();
    var dd = today.getDate();
    var mm = today.getMonth()+1; //January is 0!
    var yyyy = today.getFullYear();

    if(dd<10) { dd='0'+dd; } 
    if(mm<10) { mm='0'+mm; }

    return yyyy + '-'  + mm + '-'+ dd;
  },
  
};




UploadManager.ImageManager = function(main) {
  this.main  = main; 
};


UploadManager.ImageManager.prototype = {

  handleImage : function(fromJid, fromJidResource, toJid, toJidResource, file, handledFiles, callbackFileHandled, callbackResponse) {
    var that = this;
    
    var fileName = file.name;
    var tmpPath = file.path;
    var newFileName = this.main.getFileName(tmpPath);
    var thumbnailFileName = ''
    var currentDateString = '';
    var targetPath = '';
    var uploadUnixTimestamp = 0
    var imageLink = '';
    var thumbnailLink = '';
    var result = {};
    
    step(
      function createDirIfNotExists(){
	currentDateString = that.main.getCurrentDateString();
	console.log(currentDateString);
	mkdirp(path.join(DIR_PATH, MEDIA_UPLOAD_DIR_IMAGES, currentDateString), this);
      },
      function onDirCreated(error){
 	if (error) {
 	  callbackFileHandled(error, handledFiles, callbackResponse);
 	} else {
	  targetPath = path.join(DIR_PATH, MEDIA_UPLOAD_DIR_IMAGES, currentDateString, '/', newFileName)
	  console.log(targetPath);
	  fs.rename(tmpPath, targetPath, this);
 	}
      },
      function onFileMoved(error) {
	if (error) {
	  callbackFileHandled(error, handledFiles, callbackResponse);
	} else {
	  lwip.open(targetPath, this)
	}
      },
      function onImageFileOpened(error, image) {
 	if (error) {
 	  callbackFileHandled(error, handledFiles, callbackResponse);
  	} else {
	  thumbnailFileName = that.main.addFileNameSuffix(newFileName, '_t');
 	  thumbnailPath = path.join(DIR_PATH, MEDIA_UPLOAD_DIR_IMAGES, currentDateString, '/', thumbnailFileName);
	  console.log(thumbnailFileName);
	  dimensions = that.calculateResizeDimensions(image.width(), image.height());
	  
	  image.batch()
	    .resize(dimensions[0], dimensions[1])
	    .writeFile(thumbnailPath, this);
        }
      },
      function onThumbnailCreated(error) {
	if (error) {
  	  callbackFileHandled(error, handledFiles, callbackResponse);
  	} else {
	  uploadUnixTimestamp = Math.round(Date.now() / 1000);
	  imageLink = path.join(HOST_PATH, MEDIA_UPLOAD_DIR_IMAGES, currentDateString, '/', newFileName);
	  thumbnailLink = path.join(HOST_PATH, MEDIA_UPLOAD_DIR_IMAGES, currentDateString, '/', thumbnailFileName);
	  console.log(that.main.server.mysqlManagerOnespace.dbConfig);
	  that.main.server.mysqlManagerOnespace.mediaUploader.insertImageData(fromJid, fromJidResource, toJid, toJidResource, uploadUnixTimestamp, imageLink, thumbnailLink, this);
	}
      },
      function onImageDataStored(error, result) {
	if (error) {
   	  callbackFileHandled(error, handledFiles, callbackResponse);
  	} else {
	  result = { 'result-code' : 0, 'from-jid' : fromJid, 'from-jid-resource' : fromJidResource, 'to-jid' : toJid, 'to-jid-resource' : toJidResource, 'upload-unix-timestamp' : uploadUnixTimestamp, 'image-link' : imageLink , 'thumbnail-link' : thumbnailLink };
	  handledFiles['file-results'][fileName] = result;
	  console.log(result);
	  callbackFileHandled(null, handledFiles, callbackResponse);
	}
      }
    );    
    
  },
  
  
  calculateResizeDimensions : function(width, height){
    var ratio = width / height;
    var size = IMAGE_THUMBNAIL_SIZE_WIDTH;

    targetWidth = Math.min(size, Math.max(width, height));
    targetHeight = Math.min(size, Math.max(width, height));
    
    if (ratio < 1) {
      targetWidth = targetHeight * ratio;
    } else {
      targetHeight = targetWidth / ratio;
    }

    return [targetWidth, targetHeight];
  },
  
};




exports.UploadManager = UploadManager;
