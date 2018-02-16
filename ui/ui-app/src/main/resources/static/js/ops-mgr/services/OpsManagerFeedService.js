define(["require", "exports", "angular", "../module-name", "moment", "./OpsManagerRestUrlService", "./AlertsService", "./IconStatusService"], function (require, exports, angular, module_name_1, moment, OpsManagerRestUrlService_1, AlertsService_1, IconStatusService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var OpsManagerFeedService = /** @class */ (function () {
        function OpsManagerFeedService($q, $http, $interval, $timeout, HttpService, IconService, AlertsService, OpsManagerRestUrlService) {
            var _this = this;
            this.$q = $q;
            this.$http = $http;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.HttpService = HttpService;
            this.IconService = IconService;
            this.AlertsService = AlertsService;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.data = {};
            this.data.FEED_HEALTH_URL = this.OpsManagerRestUrlService.FEED_HEALTH_URL;
            this.data.FEED_NAMES_URL = this.OpsManagerRestUrlService.FEED_NAMES_URL;
            this.data.FEED_HEALTH_COUNT_URL = this.OpsManagerRestUrlService.FEED_HEALTH_COUNT_URL;
            this.data.FETCH_FEED_HEALTH_INTERVAL = 5000;
            this.data.fetchFeedHealthInterval = null;
            this.data.feedHealth = {};
            // this.data.SPECIFIC_FEED_HEALTH_COUNT_URL =  this.OpsManagerRestUrlService.SPECIFIC_FEED_HEALTH_COUNT_URL;
            this.data.SPECIFIC_FEED_HEALTH_URL = this.OpsManagerRestUrlService.SPECIFIC_FEED_HEALTH_URL;
            this.data.DAILY_STATUS_COUNT_URL = this.OpsManagerRestUrlService.FEED_DAILY_STATUS_COUNT_URL;
            this.data.feedSummaryData = {};
            this.data.feedUnhealthyCount = 0;
            this.data.feedHealthyCount = 0;
            this.data.feedHealth = 0;
            this.data.emptyFeed = function () {
                var feed = {};
                feed.displayStatus = 'LOADING';
                feed.lastStatus = 'LOADING',
                    feed.timeSinceEndTime = 0;
                feed.isEmpty = true;
                return feed;
            };
            this.data.decorateFeedSummary = function (feed) {
                //GROUP FOR FAILED
                if (feed.isEmpty == undefined) {
                    feed.isEmpty = false;
                }
                var health = "---";
                if (!feed.isEmpty) {
                    health = feed.healthy ? 'HEALTHY' : 'UNHEALTHY';
                    var iconData = _this.IconService.iconForFeedHealth(health);
                    feed.icon = iconData.icon;
                    feed.iconstyle = iconData.style;
                }
                feed.healthText = health;
                if (feed.running) {
                    feed.displayStatus = 'RUNNING';
                }
                else if ("FAILED" == feed.lastStatus || ("FAILED" == feed.lastExitCode && "ABANDONED" != feed.lastStatus)) {
                    feed.displayStatus = 'FAILED';
                }
                else if ("COMPLETED" == feed.lastExitCode) {
                    feed.displayStatus = 'COMPLETED';
                }
                else if ("STOPPED" == feed.lastStatus) {
                    feed.displayStatus = 'STOPPED';
                }
                else if ("UNKNOWN" == feed.lastStatus) {
                    feed.displayStatus = 'INITIAL';
                    feed.sinceTimeString = '--';
                    feed.runTimeString = "--";
                }
                else {
                    feed.displayStatus = feed.lastStatus;
                }
                feed.statusStyle = _this.IconService.iconStyleForJobStatus(feed.displayStatus);
            };
            this.data.fetchFeedSummaryData = function () {
                var successFn = function (response) {
                    _this.data.feedSummaryData = response.data;
                    if (response.data) {
                        _this.data.feedUnhealthyCount = response.data.failedCount;
                        _this.data.feedHealthyCount = response.data.healthyCount;
                    }
                };
                var errorFn = function (err) {
                };
                var finallyFn = function () {
                };
                var promise = _this.$http.get(_this.data.FEED_HEALTH_URL);
                promise.then(successFn, errorFn);
                return promise;
            };
            this.data.fetchFeedHealth = function () {
                var successFn = function (response) {
                    var unhealthyFeedNames = [];
                    if (response.data) {
                        angular.forEach(response.data, function (feedHealth) {
                            if (_this.data.feedHealth[feedHealth.feed]) {
                                angular.extend(_this.data.feedHealth[feedHealth.feed], feedHealth);
                            }
                            else {
                                _this.data.feedHealth[feedHealth.feed] = feedHealth;
                            }
                            if (feedHealth.lastUnhealthyTime) {
                                feedHealth.sinceTimeString = moment(feedHealth.lastUnhealthyTime).fromNow();
                            }
                            if (feedHealth.healthy) {
                                //     AlertsService.removeFeedFailureAlertByName(feedHealth.feed);
                            }
                            else {
                                unhealthyFeedNames.push(feedHealth.feed);
                                //       AlertsService.addFeedHealthFailureAlert(feedHealth);
                            }
                        });
                        //only unhealthy will come back
                        //if feedName is not in the response list, but currently failed.. remove it
                        //      var failedFeeds = AlertsService.feedFailureAlerts;
                        //         angular.forEach(failedFeeds, function (alert, feedName) {
                        //             if (_.indexOf(unhealthyFeedNames, feedName) == -1) {
                        //                 AlertsService.removeFeedFailureAlertByName(feedName);
                        //            }
                        //       });
                    }
                    _this.data.fetchFeedHealthTimeout();
                };
                var errorFn = function (err) {
                    _this.data.fetchFeedHealthTimeout();
                };
                var finallyFn = function () {
                };
                var promise = _this.$http.get(_this.data.FEED_HEALTH_COUNT_URL);
                promise.then(successFn, errorFn);
                return promise;
            };
            this.data.startFetchFeedHealth = function () {
                if (_this.data.fetchFeedHealthInterval == null) {
                    _this.data.fetchFeedHealth();
                    _this.data.fetchFeedHealthInterval = _this.$interval(function () {
                        _this.data.fetchFeedHealth();
                    }, _this.data.FETCH_FEED_HEALTH_INTERVAL);
                }
            };
            this.data.fetchFeedHealthTimeout = function () {
                _this.data.stopFetchFeedHealthTimeout();
                _this.data.fetchFeedHealthInterval = _this.$timeout(function () {
                    _this.data.fetchFeedHealth();
                }, _this.data.FETCH_FEED_HEALTH_INTERVAL);
            };
            this.data.stopFetchFeedHealthTimeout = function () {
                if (_this.data.fetchFeedHealthInterval != null) {
                    _this.$timeout.cancel(_this.data.fetchFeedHealthInterval);
                }
            };
            return this.data;
        }
        OpsManagerFeedService.$inject = ['OpsManagerRestUrlService'];
        return OpsManagerFeedService;
    }());
    exports.default = OpsManagerFeedService;
    angular.module(module_name_1.moduleName)
        .service("AlertsService", [AlertsService_1.default])
        .service("IconService", [IconStatusService_1.default])
        .service("OpsManagerRestUrlService", [OpsManagerRestUrlService_1.default])
        .factory('OpsManagerFeedService', ['$q', '$http', '$interval', '$timeout', 'HttpService',
        'IconService', 'AlertsService', 'OpsManagerRestUrlService', OpsManagerFeedService]);
});
//# sourceMappingURL=OpsManagerFeedService.js.map