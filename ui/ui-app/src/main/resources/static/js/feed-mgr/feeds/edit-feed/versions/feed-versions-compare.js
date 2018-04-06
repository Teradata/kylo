define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/module-name');
    var controller = /** @class */ (function () {
        function controller($scope, $http, $q, $filter, RestUrlService, FeedService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$q = $q;
            this.$filter = $filter;
            this.RestUrlService = RestUrlService;
            this.FeedService = FeedService;
            this.getCurrentVersion = function () {
                return _this.versions[0];
            };
            this.model = FeedService.editFeedModel;
            FeedService.versionFeedModel = {};
            FeedService.versionFeedModelDiff = [];
            this.current = $filter('translate')('views.feed-versions-compare.Current');
            this.leftVersion = this.current;
            this.rightVersion = {};
            this.versions = [];
            this.loading = false;
            this.loadVersions = function () {
                FeedService.getFeedVersions(_this.model.feedId).then(function (result) {
                    _this.versions = result.versions;
                    _this.leftVersion = _this.current + " (" + _this.getCurrentVersion().name + ")";
                }, function (err) {
                });
            };
            this.changeRightVersion = function () {
                var version = _.find(_this.versions, function (v) {
                    return v.id === _this.rightVersion;
                });
                _this.loading = true;
                var diff = FeedService.diffFeedVersions(_this.model.feedId, _this.rightVersion, _this.getCurrentVersion().id).then(function (result) {
                    // console.log('diff', result.difference);
                    FeedService.versionFeedModelDiff = [];
                    _.each(result.difference.patch, function (patch) {
                        FeedService.versionFeedModelDiff[patch.path] = patch;
                    });
                }, function (err) {
                });
                var versionedFeed = FeedService.getFeedVersion(_this.model.feedId, _this.rightVersion).then(function (result) {
                    _this.rightFeed = result.entity;
                    FeedService.versionFeedModel = _this.rightFeed;
                    FeedService.versionFeedModel.version = version;
                }, function (err) {
                });
                Promise.all([diff, versionedFeed]).then(function (result) {
                    _this.loading = false;
                }).catch(function (err) {
                    _this.loading = false;
                });
            };
            this.loadVersions();
        }
        return controller;
    }());
    exports.default = controller;
    angular.module(moduleName).controller('FeedVersionsCompareController', ["$scope", "$http", "$q", "$filter", "RestUrlService", "FeedService", controller]);
    angular.module(moduleName).directive('thinkbigFeedVersionsCompare', [
        function () {
            return {
                restrict: "EA",
                bindToController: {},
                controllerAs: 'vm',
                scope: {},
                templateUrl: 'js/feed-mgr/feeds/edit-feed/versions/feed-versions-compare.html',
                controller: "FeedVersionsCompareController",
                link: function ($scope, element, attrs, controller) {
                }
            };
        }
    ]);
});
//# sourceMappingURL=feed-versions-compare.js.map