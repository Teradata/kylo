
(function () {

    var controller =  function($scope, $q,$http,$mdToast,RestUrlService,$stateParams, FeedService,StateService) {
        var self = this;
        self.model = $stateParams.feedModel;
        self.error =$stateParams.error;

        self.isValid = self.error == null;


        this.onAddServiceLevelAgreement = function() {
            StateService.navigateToServiceLevelAgreements();
        }
        this.onViewDetails = function() {
            StateService.navigateToServiceLevelAgreements();
        }

        this.onViewFeedsList = function() {
            FeedService.resetFeed();
            StateService.navigateToFeeds();
        }

        this.gotIt = function(){
            self.onViewFeedsList();
        }

    };


    angular.module(MODULE_FEED_MGR).controller('DefineFeedCompleteController', controller);

})();


