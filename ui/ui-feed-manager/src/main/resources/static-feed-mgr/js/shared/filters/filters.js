angular.module(MODULE_FEED_MGR).filter('feedSystemName', ['FeedService',function(FeedService) {
    return function(feeedName) {
      return feedName;
    };
}]);


angular.module(MODULE_FEED_MGR).filter('userProperties', function() {
    return function(property) {
        return property.source != 'CONFIGURATION';
    };
});