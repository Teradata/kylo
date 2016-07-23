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

        saveFeedSla: function (feedId, serviceLevelAgreement) {
            var successFn = function (response) {
                return response.data;
            }
            var errorFn = function (err) {
                console.log('ERROR ', err)
            }
            var promise = $http({
                url: RestUrlService.SAVE_FEED_SLA_URL(feedId),
                method: "POST",
                data: angular.toJson(serviceLevelAgreement),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
            return promise;

        },
        saveSla: function (serviceLevelAgreement) {
            var successFn = function (response) {
                return response.data;
            }
            var errorFn = function (err) {
                console.log('ERROR ', err)
            }
            var promise = $http({
                url: RestUrlService.SAVE_SLA_URL,
                method: "POST",
                data: angular.toJson(serviceLevelAgreement),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
            return promise;

        },
        deleteSla: function (slaId) {
            var successFn = function (response) {
                return response.data;
            }
            var errorFn = function (err) {
                console.log('ERROR ', err)
            }
            var promise = $http({
                url: RestUrlService.DELETE_SLA_URL(slaId),
                method: "DELETE",
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
            return promise;

        },
        getSlaForEditForm: function (slaId) {
            var successFn = function (response) {
                return response.data;
            }
            var errorFn = function (err) {
                console.log('ERROR ', err)
            }
            var promise = $http.get(RestUrlService.GET_SLA_AS_EDIT_FORM(slaId));
            promise.then(successFn, errorFn);
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
        getAllSlas: function () {
            var successFn = function (response) {
                return response.data;
            }
            var errorFn = function (err) {
                console.log('ERROR ', err)
            }
            var promise = $http.get(RestUrlService.GET_SLAS_URL);
            promise.then(successFn, errorFn);
            return promise;

        },
        init: function () {

        }

    };
    data.init();
    return data;

});
