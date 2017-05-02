define(['angular', 'feed-mgr/feeds/edit-feed/module-name'], function (angular, moduleName) {

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

    var controller = function ($scope, $http, $sce, PaginationDataService, FeedService, RestUrlService, HiveService, Utils, BroadcastService) {
        var self = this;

        self.data = [];
        self.loading = true;
        self.processingDate = new Date(HiveService.getUTCTime(self.processingdttm));
        self.model = FeedService.editFeedModel;
        self.hideColumns = ["processing_dttm"];

        self.getProfileStats = function () {
            self.loading = true;
            var successFn = function (response) {
                self.data = response.data;
                self.loading = false;
                BroadcastService.notify('PROFILE_TAB_DATA_LOADED', 'profile-stats');
            };
            var errorFn = function (err) {
                self.loading = false;
            };
            var promise = $http.get(RestUrlService.FEED_PROFILE_STATS_URL(self.model.id), {params: {'processingdttm': self.processingdttm}});
            promise.then(successFn, errorFn);
            return promise;
        };

        self.getProfileStats();
    };

    angular.module(moduleName).controller('FeedProfileStatsController', ["$scope", "$http", "$sce", "PaginationDataService", "FeedService", "RestUrlService", "HiveService", "Utils",
                                                                         "BroadcastService", controller]);
    angular.module(moduleName).directive('thinkbigFeedProfileStats', directive);
});
