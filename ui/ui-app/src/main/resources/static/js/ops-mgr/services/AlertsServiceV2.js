define(["require", "exports", "angular", "../module-name", "underscore", "moment"], function (require, exports, angular, module_name_1, _, moment) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AlertsServiceV2 = /** @class */ (function () {
        function AlertsServiceV2($q, $http, $interval, OpsManagerRestUrlService) {
            this.$q = $q;
            this.$http = $http;
            this.$interval = $interval;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.factory('AlertsService', ["$q", "$http", "$interval", "OpsManagerRestUrlService", this.factoryFn.bind(this)]);
        }
        AlertsServiceV2.prototype.factoryFn = function () {
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
            var data = {
                alertsSummary: {
                    lastRefreshTime: '',
                    data: alertSummData
                },
                transformAlerts: transformAlertSummaryResponse,
                fetchFeedAlerts: function (feedName, feedId) {
                    var deferred = this.$q.defer();
                    this.$http.get(this.OpsManagerRestUrlService.FEED_ALERTS_URL(feedName), { params: { "feedId": feedId } }).then(function (response) {
                        transformAlertSummaryResponse(response.data);
                        deferred.resolve(response.data);
                    }, function (err) {
                        deferred.reject(err);
                    });
                    return deferred.promise;
                },
            };
            return data;
        };
        return AlertsServiceV2;
    }());
    exports.default = AlertsServiceV2;
});
//# sourceMappingURL=AlertsServiceV2.js.map