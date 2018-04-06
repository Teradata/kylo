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
            templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-valid-results.html',
            controller: "FeedProfileValidResultsController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };
    var FeedProfileValidResultsController = /** @class */ (function () {
        function FeedProfileValidResultsController($scope, $http, FeedService, RestUrlService, HiveService, Utils, BroadcastService, FattableService) {
            this.$scope = $scope;
            this.$http = $http;
            this.FeedService = FeedService;
            this.RestUrlService = RestUrlService;
            this.HiveService = HiveService;
            this.Utils = Utils;
            this.BroadcastService = BroadcastService;
            this.FattableService = FattableService;
            // define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {
            this.model = this.FeedService.editFeedModel;
            this.loading = false;
            this.limitOptions = [10, 50, 100, 500, 1000];
            this.limit = this.limitOptions[2];
            this.queryResults = null;
            var self = this;
            //noinspection JSUnusedGlobalSymbols
            function onLimitChange() {
                getProfileValidation().then(setupTable);
            }
            function getProfileValidation() {
                self.loading = true;
                var successFn = function (response) {
                    var result = self.queryResults = HiveService.transformResultsToUiGridModel(response);
                    self.headers = result.columns;
                    self.rows = result.rows;
                    self.loading = false;
                    BroadcastService.notify('PROFILE_TAB_DATA_LOADED', 'valid');
                };
                var errorFn = function (err) {
                    self.loading = false;
                };
                var promise = $http.get(RestUrlService.FEED_PROFILE_VALID_RESULTS_URL(self.model.id), {
                    params: {
                        'processingdttm': self.processingdttm,
                        'limit': self.limit
                    }
                });
                promise.then(successFn, errorFn);
                return promise;
            }
            function setupTable() {
                FattableService.setupTable({
                    tableContainerId: "validProfile",
                    headers: self.headers,
                    rows: self.rows
                });
            }
            getProfileValidation().then(setupTable);
        }
        return FeedProfileValidResultsController;
    }());
    exports.FeedProfileValidResultsController = FeedProfileValidResultsController;
    angular.module(moduleName).controller('FeedProfileValidResultsController', ["$scope", "$http", "FeedService", "RestUrlService", "HiveService", "Utils", "BroadcastService", "FattableService", FeedProfileValidResultsController]);
    angular.module(moduleName).directive('thinkbigFeedProfileValid', directive);
});
//# sourceMappingURL=profile-valid.js.map