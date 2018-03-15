define(["require", "exports", "angular", "../module-name", "underscore", "moment", "../module"], function (require, exports, angular, module_name_1, _, moment) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AlertsServiceV2 = /** @class */ (function () {
        function AlertsServiceV2($q, $http, $interval, OpsManagerRestUrlService) {
            var _this = this;
            this.$q = $q;
            this.$http = $http;
            this.$interval = $interval;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            /**
             * Flag to indicate the alerts have been loaded at least once
             * @type {boolean}
             */
            var loadedGlobalAlertsSummary = false;
            var alertSummaryRefreshTimeMillis = 5000;
            /**
             * ref to the refresh interval so we can cancel it
             * @type {null}
             */
            var alertsSummaryIntervalObject = null;
            var transformAlertSummaryResponse = function (alertSummaries) {
                _.each(alertSummaries, function (summary) {
                    summary.since = moment(summary.lastAlertTimestamp).fromNow();
                });
            };
            var alertSummData;
            this.data =
                {
                    alertsSummary: {
                        lastRefreshTime: '',
                        data: alertSummData
                    },
                    transformAlerts: transformAlertSummaryResponse,
                    fetchFeedAlerts: function (feedName, feedId) {
                        var deferred = _this.$q.defer();
                        _this.$http.get(_this.OpsManagerRestUrlService.FEED_ALERTS_URL(feedName), { params: { "feedId": feedId } }).then(function (response) {
                            transformAlertSummaryResponse(response.data);
                            deferred.resolve(response.data);
                        }, function (err) {
                            deferred.reject(err);
                        });
                        return deferred.promise;
                    }
                };
            return this.data;
        }
        return AlertsServiceV2;
    }());
    exports.default = AlertsServiceV2;
    angular.module(module_name_1.moduleName)
        .factory('AlertsServiceV2', ["$q", "$http", "$interval", "OpsManagerRestUrlService", AlertsServiceV2]);
});
//# sourceMappingURL=AlertsServiceV2.js.map