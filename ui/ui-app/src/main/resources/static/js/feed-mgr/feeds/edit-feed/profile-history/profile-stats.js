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
        function FeedProfileStatsController($http, FeedService, RestUrlService, HiveService, BroadcastService) {
            this.$http = $http;
            this.FeedService = FeedService;
            this.RestUrlService = RestUrlService;
            this.HiveService = HiveService;
            this.BroadcastService = BroadcastService;
            this.data = [];
            this.loading = true;
            this.processingDate = null;
            this.model = this.FeedService.editFeedModel;
            this.hideColumns = ["processing_dttm"];
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
        FeedProfileStatsController.prototype.$onInit = function () {
            this.processingDate = new Date(this.HiveService.getUTCTime(this.processingdttm));
            this.getProfileStats();
        };
        return FeedProfileStatsController;
    }());
    exports.FeedProfileStatsController = FeedProfileStatsController;
    angular.module(moduleName).controller('FeedProfileStatsController', ["$http", "FeedService", "RestUrlService", "HiveService", "BroadcastService", FeedProfileStatsController]);
    angular.module(moduleName).directive('thinkbigFeedProfileStats', directive);
});
//# sourceMappingURL=profile-stats.js.map