define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/edit-feed/module-name');
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                processingdttm: '=',
                rowsPerPage: '='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-stats.html',
            controller: "FeedProfileStatsController"
        };
    };
    var FeedProfileStatsController = /** @class */ (function () {
        function FeedProfileStatsController($scope, $http, $sce, PaginationDataService, FeedService, RestUrlService, HiveService, Utils, BroadcastService) {
            this.$scope = $scope;
            this.$http = $http;
            this.$sce = $sce;
            this.PaginationDataService = PaginationDataService;
            this.FeedService = FeedService;
            this.RestUrlService = RestUrlService;
            this.HiveService = HiveService;
            this.Utils = Utils;
            this.BroadcastService = BroadcastService;
            this.data = [];
            this.loading = true;
            this.processingDate = null;
            this.model = this.FeedService.editFeedModel;
            this.hideColumns = ["processing_dttm"];
            var self = this;
            this.processingDate = new Date(this.HiveService.getUTCTime(this.processingdttm));
            this.getProfileStats();
        }
        FeedProfileStatsController.prototype.getProfileStats = function () {
            var _this = this;
            this.loading = true;
            var successFn = function (response) {
                _this.data = response.data;
                _this.loading = false;
                _this.BroadcastService.notify('PROFILE_TAB_DATA_LOADED', 'profile-stats');
            };
            var errorFn = function (err) {
                _this.loading = false;
            };
            var promise = this.$http.get(this.RestUrlService.FEED_PROFILE_STATS_URL(this.model.id), { params: { 'processingdttm': this.processingdttm } });
            promise.then(successFn, errorFn);
            return promise;
        };
        ;
        return FeedProfileStatsController;
    }());
    exports.FeedProfileStatsController = FeedProfileStatsController;
    angular.module(moduleName).controller('FeedProfileStatsController', ["$scope", "$http", "$sce", "PaginationDataService", "FeedService", "RestUrlService", "HiveService", "Utils",
        "BroadcastService", FeedProfileStatsController]);
    angular.module(moduleName).directive('thinkbigFeedProfileStats', directive);
});
//# sourceMappingURL=profile-stats.js.map