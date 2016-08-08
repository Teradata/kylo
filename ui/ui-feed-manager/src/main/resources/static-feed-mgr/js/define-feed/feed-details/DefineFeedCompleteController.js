
(function () {

    var controller;
    controller = function ($scope, $q, $http, $mdToast, RestUrlService, $stateParams, FeedService, StateService) {
        var self = this;
        self.model = $stateParams.feedModel;
        self.error = $stateParams.error;

        self.isValid = self.error == null;

        this.onAddServiceLevelAgreement = function () {
            //navigate to Feed Details and move to the 3 tab (SLA)
            var feedId = self.model != null ? self.model.id : null;
            if (feedId == null && FeedService.createFeedModel != null) {
                feedId = FeedService.createFeedModel.id;
            }
            if (feedId == null && FeedService.editFeedModel != null) {
                feedId = FeedService.editFeedModel.id;
            }
            StateService.navigateToFeedDetails(feedId, 3);
        }
        this.onViewDetails = function () {
            StateService.navigateToServiceLevelAgreements();
        }

        this.onViewFeedsList = function () {
            FeedService.resetFeed();
            StateService.navigateToFeeds();
        }

        this.gotIt = function () {
            self.onViewFeedsList();
        }

    };


    angular.module(MODULE_FEED_MGR).controller('DefineFeedCompleteController', controller);

})();


