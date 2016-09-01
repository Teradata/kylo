angular.module(MODULE_OPERATIONS).factory('ProvenanceEventStatsService', function ($http, $q, RestUrlService) {

    var data = {

        getTimeFrameOptions: function () {

            var promise = $http.get(RestUrlService.PROVENANCE_EVENT_TIME_FRAME_OPTIONS);
            return promise;
        },

        getFeedProcessorDuration: function (feedName, timeFrame) {
            var self = this;

            var successFn = function (response) {

            }
            var errorFn = function (err) {
                self.loading = false;

            }
            var promise = $http.get(RestUrlService.PROCESSOR_DURATION_FOR_FEED(feedName, timeFrame));
            promise.then(successFn, errorFn);
            return promise;
        }

    }
    return data;

});