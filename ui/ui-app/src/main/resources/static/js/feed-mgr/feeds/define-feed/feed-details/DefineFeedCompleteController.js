define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    var DefineFeedCompleteController = /** @class */ (function () {
        function DefineFeedCompleteController($scope, $q, $http, $mdToast, $transition$, RestUrlService, FeedService, StateService) {
            this.$scope = $scope;
            this.$q = $q;
            this.$http = $http;
            this.$mdToast = $mdToast;
            this.$transition$ = $transition$;
            this.RestUrlService = RestUrlService;
            this.FeedService = FeedService;
            this.StateService = StateService;
            this.model = $transition$.params().feedModel;
            this.error = $transition$.params().error;
            this.isValid = this.error == null;
        }
        ;
        /**
             * Gets the feed id from the FeedService
             * @returns {*}
        */
        DefineFeedCompleteController.prototype.getFeedId = function () {
            var feedId = this.model != null ? this.model.id : null;
            if (feedId == null && this.FeedService.createFeedModel != null) {
                feedId = this.FeedService.createFeedModel.id;
            }
            if (feedId == null && this.FeedService.editFeedModel != null) {
                feedId = this.FeedService.editFeedModel.id;
            }
            return feedId;
        };
        /**
            * Navigate to the Feed Details first tab
        */
        DefineFeedCompleteController.prototype.onViewDetails = function () {
            var feedId = this.getFeedId();
            this.StateService.FeedManager().Feed().navigateToFeedDetails(feedId, 0);
        };
        DefineFeedCompleteController.prototype.gotIt = function () {
            this.onViewFeedsList();
        };
        /**
             * Navigate to the Feed List page
             */
        DefineFeedCompleteController.prototype.onViewFeedsList = function () {
            this.FeedService.resetFeed();
            this.StateService.FeedManager().Feed().navigateToFeeds();
        };
        /**
             * Navigate to the Feed Details SLA tab
        */
        DefineFeedCompleteController.prototype.onAddServiceLevelAgreement = function () {
            //navigate to Feed Details and move to the 3 tab (SLA)
            var feedId = this.getFeedId();
            this.StateService.FeedManager().Feed().navigateToFeedDetails(feedId, 3);
        };
        DefineFeedCompleteController.$inject = ["$scope", "$q", "$http", "$mdToast", "$transition$", "RestUrlService", "FeedService", "StateService"];
        return DefineFeedCompleteController;
    }());
    exports.default = DefineFeedCompleteController;
    angular.module(moduleName)
        .component('thinkbigDefineFeedCompleteController', {
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-complete.html',
        controllerAs: 'vm',
        controller: DefineFeedCompleteController,
    });
});
//# sourceMappingURL=DefineFeedCompleteController.js.map