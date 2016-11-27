const ONESPACE_CSS_CLASS__GOOGLE_RESULT_INFO = "onespace-result-info";




OneSpace = function() { };
OneSpace.prototype = { };



OneSpace.Content =  function() { };

OneSpace.Content.prototype = { };



OneSpace.Content.Controller =  function() {
  this.contentView = new OneSpace.Content.View(this);
  this.mutationObserver = null;
};

OneSpace.Content.Controller.prototype = {

  initialize : function() {
    this.contentView.initialize();
    
    var that = this;
    $(document).ready(function() { 
      that.initializeMutationObserver();
    });
    


  },
  
  initializeMutationObserver : function() {
    var that = this;
    
    // create an observer instance
    this.mutationObserver = new MutationObserver(function(mutations) {
      that.handleContentUpdate();
    });

    // configuration of the observer:
    var config = { childList: true, subtree: true };

    // pass in the target node, as well as the observer options
    this.mutationObserver.observe(document.body, config);
  },
  
  
  handleContentUpdate : function() {
    this.contentView.updateGoogleResultLinks();
  },

};


OneSpace.Content.Utilities = {

  getRandomInteger : function(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;;
  },
  
};


OneSpace.Content.View = function(controller) {
  this.controller = controller;
};

OneSpace.Content.View.prototype = {

  initialize : function() {

  },
  
  
  extractGoogleResultLinks : function() {
    var links = [];
    $('h3.r').each( function(){
      $(this).children('a').each(function () {
        links.push($(this).attr('href'));
      });
    });
    return links;
  },
  
  
  updateGoogleResultLinks : function() {
    var links = this.extractGoogleResultLinks();
    links = links.filter(function(elem, index, self) {
      return index == self.indexOf(elem);
    });
    
    this.injectLinkData(links)
  },
  
  
  injectLinkData : function(linkData) {
    // Just to be sure since we change the content by injecting
    this.controller.mutationObserver.disconnect();
    
    for (var i = 0; i < linkData.length; i++) {
      link = linkData[i];
      
      $('a[href="' + link + '"]').each(function(){
        var parentH3 = $(this).parent();
        var next = parentH3.next();
        try {
          classes = next.attr('class').split(" ");
          // Check if info has already been injected
          if ($.inArray(ONESPACE_CSS_CLASS__GOOGLE_RESULT_INFO, classes) == -1) {
            parentH3.after('<div class="s ' + ONESPACE_CSS_CLASS__GOOGLE_RESULT_INFO + '">OneSpace &ndash; #Surfers: ' + OneSpace.Content.Utilities.getRandomInteger(0,10) + ', #Walkers: ' + OneSpace.Content.Utilities.getRandomInteger(0,5) + '</div>');
          }
        } catch (err) {
          
        }
      });
    }
    
    this.controller.initializeMutationObserver();
  },
  
};  
  


oneSpaceContentController = new OneSpace.Content.Controller();
oneSpaceContentController.initialize();

