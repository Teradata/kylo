define(["require", "exports", "angular", "./module-name", "underscore", "../services/OpsManagerFeedService", "../services/OpsManagerJobService", "../services/OpsManagerRestUrlService"], function (require, exports, angular, module_name_1, _, OpsManagerFeedService_1, OpsManagerJobService_1, OpsManagerRestUrlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $timeout, $q, $interval, $transition$, $http, OpsManagerFeedService, OpsManagerRestUrlService, StateService, OpsManagerJobService, BroadcastService) {
            var _this = this;
            this.$scope = $scope;
            this.$timeout = $timeout;
            this.$q = $q;
            this.$interval = $interval;
            this.$transition$ = $transition$;
            this.$http = $http;
            this.OpsManagerFeedService = OpsManagerFeedService;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.StateService = StateService;
            this.OpsManagerJobService = OpsManagerJobService;
            this.BroadcastService = BroadcastService;
            this.isGuid = function (str) {
                if (str[0] === "{") {
                    str = str.substring(1, str.length - 1);
                }
                //var regexGuid = /^(\{){0,1}[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}(\}){0,1}$/gi;
                var regexGuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
                return regexGuid.test(str);
            };
            this.getFeedHealth = function () {
                var canceler = _this.$q.defer();
                _this.activeRequests.push(canceler);
                var successFn = function (response) {
                    if (response.data) {
                        //transform the data for UI
                        _this.feedData = response.data;
                        if (_this.feedData.feedSummary) {
                            angular.extend(_this.feed, _this.feedData.feedSummary[0]);
                            _this.feed.isEmpty = false;
                            if (_this.feed.feedHealth && _this.feed.feedHealth.feedId) {
                                _this.feed.feedId = _this.feed.feedHealth.feedId;
                            }
                            _this.OpsManagerFeedService.decorateFeedSummary(_this.feed);
                        }
                        if (_this.loading) {
                            _this.loading = false;
                        }
                        _this.finishedRequest(canceler);
                    }
                };
                var errorFn = function (err) {
                };
                var finallyFn = function () {
                };
                _this.$http.get(_this.OpsManagerFeedService.SPECIFIC_FEED_HEALTH_URL(_this.feedName), { timeout: canceler.promise }).then(successFn, errorFn);
            };
            this.abortActiveRequests = function () {
                angular.forEach(_this.activeRequests, function (canceler, i) {
                    canceler.resolve();
                });
                _this.activeRequests = [];
            };
            this.finishedRequest = function (canceler) {
                var index = _.indexOf(_this.activeRequests, canceler);
                if (index >= 0) {
                    _this.activeRequests.splice(index, 1);
                }
                canceler.resolve();
                canceler = null;
            };
            this.getFeedNames = function () {
                var successFn = function (response) {
                    if (response.data) {
                        _this.feedNames = response.data;
                    }
                };
                var errorFn = function (err) {
                };
                var finallyFn = function () {
                };
                _this.$http.get(_this.OpsManagerFeedService.FEED_NAMES_URL).then(successFn, errorFn);
            };
            this.clearRefreshInterval = function () {
                if (_this.refreshInterval != null) {
                    _this.$interval.cancel(_this.refreshInterval);
                    _this.refreshInterval = null;
                }
            };
            this.setRefreshInterval = function () {
                _this.clearRefreshInterval();
                if (_this.refreshIntervalTime) {
                    _this.refreshInterval = _this.$interval(_this.getFeedHealth, _this.refreshIntervalTime);
                }
                _this.OpsManagerFeedService.fetchFeedHealth();
            };
            this.gotoFeedDetails = function (ev) {
                if (_this.feed.feedId != undefined) {
                    _this.StateService.FeedManager().Feed().navigateToFeedDetails(_this.feed.feedId);
                }
            };
            this.onJobAction = function (eventName, job) {
                var forceUpdate = false;
                //update status info if feed job matches
                if (_this.feedData && _this.feedData.feeds && _this.feedData.feeds.length > 0 && _this.feedData.feeds[0].lastOpFeed) {
                    var thisExecutionId = _this.feedData.feeds[0].lastOpFeed.feedExecutionId;
                    var thisInstanceId = _this.feedData.feeds[0].lastOpFeed.feedInstanceId;
                    if (thisExecutionId <= job.executionId && _this.feed) {
                        _this.abortActiveRequests();
                        _this.clearRefreshInterval();
                        _this.feed.displayStatus = job.displayStatus == 'STARTED' || job.displayStatus == 'STARTING' ? 'RUNNING' : job.displayStatus;
                        _this.feed.timeSinceEndTime = job.timeSinceEndTime;
                        if (_this.feed.displayStatus == 'RUNNING') {
                            _this.feed.timeSinceEndTime = job.runTime;
                        }
                        if (eventName == 'restartJob') {
                            _this.feed.timeSinceEndTime = 0;
                        }
                        _this.feedData.feeds[0].lastOpFeed.feedExecutionId = job.executionId;
                        _this.feedData.feeds[0].lastOpFeed.feedInstanceId = job.instanceId;
                        if (eventName == 'updateEnd') {
                            _this.setRefreshInterval();
                        }
                    }
                }
            };
            this.changedFeed = function (feedName) {
                _this.StateService.OpsManager().Feed().navigateToFeedDetails(feedName);
            };
            this.abandonedAllJobs = function () {
                _this.getFeedHealth();
            };
            this.loading = true;
            this.deferred = $q.defer();
            this.feedName = null;
            this.feedData = {};
            this.feed = OpsManagerFeedService.emptyFeed();
            this.refreshIntervalTime = 5000;
            //Track active requests and be able to cancel them if needed
            this.activeRequests = [];
            BroadcastService.subscribe($scope, 'ABANDONED_ALL_JOBS', this.abandonedAllJobs);
            this.feedName = $transition$.params().feedName;
            if (this.feedName != undefined && this.isGuid(this.feedName)) {
                //fetch the feed name from the server using the guid
                $http.get(OpsManagerRestUrlService.FEED_NAME_FOR_ID(this.feedName)).then(function (response) {
                    _this.deferred.resolve(response.data);
                }, function (err) {
                    _this.deferred.reject(err);
                });
            }
            else {
                this.deferred.resolve(this.feedName);
            }
            $q.when(this.deferred.promise).then(function (feedNameResponse) {
                _this.feedName = feedNameResponse;
                _this.loading = false;
                _this.getFeedHealth();
                // getFeedNames();
                _this.setRefreshInterval();
            });
            $scope.$on('$destroy', function () {
                _this.clearRefreshInterval();
                _this.abortActiveRequests();
                _this.OpsManagerFeedService.stopFetchFeedHealthTimeout();
            });
        } // end of constructor
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName)
        .service('OpsManagerFeedService', ['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService', OpsManagerFeedService_1.default])
        .service('OpsManagerJobService', ['$q', '$http', '$log', 'HttpService', 'NotificationService', 'OpsManagerRestUrlService', OpsManagerJobService_1.default])
        .service('OpsManagerRestUrlService', [OpsManagerRestUrlService_1.default])
        .controller('OpsManagerFeedDetailsController', ['$scope', '$timeout', '$q', '$interval', '$transition$', '$http', 'OpsManagerFeedService',
        'OpsManagerRestUrlService', 'StateService', 'OpsManagerJobService', 'BroadcastService', controller]);
});
//# sourceMappingURL=FeedDetailsController.js.map