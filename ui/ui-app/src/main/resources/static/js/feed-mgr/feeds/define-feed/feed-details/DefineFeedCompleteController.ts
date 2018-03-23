import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/define-feed/module-name');


export default class DefineFeedCompleteController {

    model:any;
    error:any;
    isValid:any;
    onAddServiceLevelAgreement:any;
    onViewDetails:any;
    onViewFeedsList:any;
    gotIt:any;


    constructor(private $scope:any, private $q:any, private $http:any, private $mdToast:any,private $transition$:any, private RestUrlService:any, private FeedService:any, private StateService:any) {
        var self = this;
       // self.model = $transition$.params().feedModel;
       // self.error = $transition$.params().error;
        self.model = $transition$.params().feedModel;
        self.error = $transition$.params().error;

        self.isValid = self.error == null;

        /**
         * Gets the feed id from the FeedService
         * @returns {*}
         */
        function getFeedId() {
            var feedId = self.model != null ? self.model.id : null;
            if (feedId == null && FeedService.createFeedModel != null) {
                feedId = FeedService.createFeedModel.id;
            }
            if (feedId == null && FeedService.editFeedModel != null) {
                feedId = FeedService.editFeedModel.id;
            }
            return feedId;
        }

        /**
         * Navigate to the Feed Details SLA tab
         */
        this.onAddServiceLevelAgreement = function () {
            //navigate to Feed Details and move to the 3 tab (SLA)
            var feedId = getFeedId();
            StateService.FeedManager().Feed().navigateToFeedDetails(feedId, 3);
        }
        this.onViewDetails = function () {
            StateService.FeedManager().Sla().navigateToServiceLevelAgreements();
        }

        /**
         * Navigate to the Feed Details first tab
         */
        this.onViewDetails = function () {
            var feedId = getFeedId();
            StateService.FeedManager().Feed().navigateToFeedDetails(feedId, 0);
        }

        /**
         * Navigate to the Feed List page
         */
        this.onViewFeedsList = function () {
            FeedService.resetFeed();
            StateService.FeedManager().Feed().navigateToFeeds();
        }

        this.gotIt = function () {
            self.onViewFeedsList();
        }

    };    
}

angular.module(moduleName).controller('DefineFeedCompleteController', ["$scope","$q","$http","$mdToast","$transition$","RestUrlService","FeedService","StateService",DefineFeedCompleteController]);

