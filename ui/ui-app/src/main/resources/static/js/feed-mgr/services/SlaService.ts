import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');


// export class SlaService {
    function SlaService ($http:any, $q:any, mdToast:any, $mdDialog:any, RestUrlService:any) {

        var data = {

            getPossibleSlaMetricOptions: function () {

                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = $http.get(RestUrlService.GET_POSSIBLE_SLA_METRIC_OPTIONS_URL);
                promise.then(successFn, errorFn);
                return promise;

            },

            validateSlaActionClass: function (actionClass:any) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = $http.get(RestUrlService.VALIDATE_SLA_ACTION_URL, {params: {"actionConfigClass": actionClass}});
                promise.then(successFn, errorFn);
                return promise;

            },
            validateSlaActionRule: function (rule:any) {
                rule.validConfiguration = true;
                rule.validationMessage = '';
                var successFn = function (response:any) {
                    if (response.data && response.data.length) {
                        var validationMessage = "";
                        _.each(response.data, function (validation:any) {
                            if (!validation.valid) {
                                if (validationMessage != "") {
                                    validationMessage += ", ";
                                }
                                validationMessage += validation.validationMessage;
                            }
                        });
                        if (validationMessage != "") {
                            rule.validConfiguration = false;
                            rule.validationMessage = validationMessage;
                        }
                    }
                    ;

                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = $http.get(RestUrlService.VALIDATE_SLA_ACTION_URL, {params: {"actionConfigClass": rule.objectClassType}});
                promise.then(successFn, errorFn);
                return promise;

            },

            getPossibleSlaActionOptions: function () {

                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = $http.get(RestUrlService.GET_POSSIBLE_SLA_ACTION_OPTIONS_URL);
                promise.then(successFn, errorFn);
                return promise;

            },

            saveFeedSla: function (feedId:any, serviceLevelAgreement:any) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
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
            saveSla: function (serviceLevelAgreement:any) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
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
            deleteSla: function (slaId:any) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
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
            getSlaById: function (slaId:any) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = $http.get(RestUrlService.GET_SLA_BY_ID_URL(slaId));
                promise.then(successFn, errorFn);
                return promise;

            },
            getSlaForEditForm: function (slaId:any) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = $http.get(RestUrlService.GET_SLA_AS_EDIT_FORM(slaId));
                promise.then(successFn, errorFn);
                return promise;

            },
            getFeedSlas: function (feedId:any) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = $http.get(RestUrlService.GET_FEED_SLA_URL(feedId));
                promise.then(successFn, errorFn);
                return promise;

            },
            getAllSlas: function () {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
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

    }
// }

angular.module(moduleName).factory('SlaService', ["$http","$q","$mdToast","$mdDialog","RestUrlService",SlaService]);
