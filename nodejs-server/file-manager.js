var fs = require('fs');
var path = require('path');


 
FileManager = function() {
  this.placeTypeMapper = new FileManager.PlaceTypeMapper(this);
};



FileManager.PlaceTypeMapper = function(main) {
  this.main  = main; 
  this.map = {};
};

FileManager.PlaceTypeMapper.prototype = {

  initialize : function(fileName, callback) {
    var that = this;
    var filePath = path.join(__dirname, fileName);
    
    fs.readFileSync(filePath).toString().split('\n').forEach(function (line) { 
      //console.log(line); 
      var elements = line.split('\t');
      that.map[elements[0]] = elements[1];
    });
    
    //console.log(this.map);
  },  

  
};


exports.FileManager = FileManager;
