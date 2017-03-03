define(['angular','ops-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory('ProvenanceEventStatsService', ["$http", "$q", "OpsManagerRestUrlService", function ($http, $q, OpsManagerRestUrlService) {

        var data = {

            getTimeFrameOptions: function () {

                var promise = $http.get(OpsManagerRestUrlService.PROVENANCE_EVENT_TIME_FRAME_OPTIONS);
                return promise;
            },

            getFeedProcessorDuration: function (feedName, timeFrame) {
                var self = this;

                var successFn = function (response) {

                }
                var errorFn = function (err) {
                    self.loading = false;

                }
                var promise = $http.get(OpsManagerRestUrlService.PROCESSOR_DURATION_FOR_FEED(feedName, timeFrame));
                promise.then(successFn, errorFn);
                return promise;
            },
            getFeedStatisticsOverTime: function (feedName, timeFrame) {
                var self = this;

                var successFn = function (response) {

                }
                var errorFn = function (err) {
                    self.loading = false;

                }
                var promise = $http.get(OpsManagerRestUrlService.FEED_STATISTICS_OVER_TIME(feedName, timeFrame));
                promise.then(successFn, errorFn);
                return promise;
            }

        }
        return data;

    }]);
});