angular.module(MODULE_FEED_MGR).factory('SlaService', function ($http, $q, $mdToast, $mdDialog, RestUrlService) {

    var data = {

        getPossibleSlaMetricOptions: function () {

            var successFn = function (response) {
                return response.data;
            }
            var errorFn = function (err) {
                console.log('ERROR ', err)
            }
            var promise = $http.get(RestUrlService.GET_POSSIBLE_SLA_METRIC_OPTIONS_URL);
            promise.then(successFn, errorFn);
            return promise;

        },

        getPossibleSlaActionOptions: function () {

            var successFn = function (response) {
                return response.data;
            }
            var errorFn = function (err) {
                console.log('ERROR ', err)
            }
            var promise = $http.get(RestUrlService.GET_POSSIBLE_SLA_ACTION_OPTIONS_URL);
            promise.then(successFn, errorFn);
            return promise;

        },

        saveFeedSla: function (serviceLevelAgreements) {
            var successFn = function (response) {
                return response.data;
            }
            var errorFn = function (err) {
                console.log('ERROR ', err)
            }
            var promise = $http({
                url: RestUrlService.SAVE_FEED_SLA_URL(serviceLevelAgreements.feedId),
                method: "POST",
                data: angular.toJson(serviceLevelAgreements),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
            return promise;

        },
        getFeedSlas: function (feedId) {
            var successFn = function (response) {
                return response.data;
            }
            var errorFn = function (err) {
                console.log('ERROR ', err)
            }
            var promise = $http.get(RestUrlService.GET_FEED_SLA_URL(feedId));
            promise.then(successFn, errorFn);
            return promise;

        },
        init: function () {

        }

    };
    data.init();
    return data;

});
