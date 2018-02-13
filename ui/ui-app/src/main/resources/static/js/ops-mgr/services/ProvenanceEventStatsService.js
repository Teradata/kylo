define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ProvenanceEventStatsService = /** @class */ (function () {
        function ProvenanceEventStatsService($http, $q, OpsManagerRestUrlService) {
            this.$http = $http;
            this.$q = $q;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.factory('ProvenanceEventStatsService', ['$http', '$q', 'OpsManagerRestUrlService', this.factoryFn.bind(this)]);
        }
        ProvenanceEventStatsService.prototype.factoryFn = function () {
            var data = {
                getTimeFrameOptions: function () {
                    var promise = this.$http.get(this.OpsManagerRestUrlService.PROVENANCE_EVENT_TIME_FRAME_OPTIONS);
                    return promise;
                },
                getFeedProcessorDuration: function (feedName, from, to) {
                    var successFn = function (response) {
                    };
                    var errorFn = function (err) {
                        this.loading = false;
                    };
                    var promise = this.$http.get(this.OpsManagerRestUrlService.PROCESSOR_DURATION_FOR_FEED(feedName, from, to));
                    promise.then(successFn, errorFn);
                    return promise;
                },
                getFeedStatisticsOverTime: function (feedName, from, to) {
                    var successFn = function (response) {
                    };
                    var errorFn = function (err) {
                        this.loading = false;
                    };
                    var promise = this.$http.get(this.OpsManagerRestUrlService.FEED_STATISTICS_OVER_TIME(feedName, from, to));
                    promise.then(successFn, errorFn);
                    return promise;
                },
                getFeedProcessorErrors: function (feedName, from, to, after) {
                    var successFn = function (response) {
                    };
                    var errorFn = function (err) {
                        this.loading = false;
                    };
                    var promise = this.$http.get(this.OpsManagerRestUrlService.FEED_PROCESSOR_ERRORS(feedName, from, to), { params: { after: after } });
                    promise.then(successFn, errorFn);
                    return promise;
                }
            };
            return data;
        };
        return ProvenanceEventStatsService;
    }());
    exports.default = ProvenanceEventStatsService;
});
//# sourceMappingURL=ProvenanceEventStatsService.js.map