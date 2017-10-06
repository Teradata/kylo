define(['angular','ops-mgr/module-name'], function (angular,moduleName) {
    /**
     * Service to hold all JobRestController.java urls
     */
    angular.module(moduleName).factory('OpsManagerJobService',
        ['$q', '$http', '$log', 'HttpService', 'NotificationService', 'OpsManagerRestUrlService', function ($q, $http, $log, HttpService, NotificationService, OpsManagerRestUrlService) {
            var data = {};
            data.JOBS_QUERY_URL = OpsManagerRestUrlService.JOBS_QUERY_URL;
            data.JOBS_CHARTS_QUERY_URL = OpsManagerRestUrlService.JOBS_CHARTS_QUERY_URL;
            data.JOB_NAMES_URL = OpsManagerRestUrlService.JOB_NAMES_URL;
            data.DAILY_STATUS_COUNT_URL = OpsManagerRestUrlService.DAILY_STATUS_COUNT_URL;

            //data.RUNNING_OR_FAILED_COUNTS_URL = OpsManagerRestUrlService.RUNNING_OR_FAILED_COUNTS_URL;

            data.RUNNING_JOB_COUNTS_URL = OpsManagerRestUrlService.RUNNING_JOB_COUNTS_URL;

          //  data.DATA_CONFIDENCE_URL = OpsManagerRestUrlService.DATA_CONFIDENCE_URL;

            data.RESTART_JOB_URL = OpsManagerRestUrlService.RESTART_JOB_URL;

            data.STOP_JOB_URL = OpsManagerRestUrlService.STOP_JOB_URL;

            data.ABANDON_JOB_URL = OpsManagerRestUrlService.ABANDON_JOB_URL;

            data.ABANDON_ALL_JOBS_URL = OpsManagerRestUrlService.ABANDON_ALL_JOBS_URL;

            data.FAIL_JOB_URL = OpsManagerRestUrlService.FAIL_JOB_URL;

            data.LOAD_JOB_URL = OpsManagerRestUrlService.LOAD_JOB_URL;

            data.RELATED_JOBS_URL = OpsManagerRestUrlService.RELATED_JOBS_URL;

            data.restartJob = function (executionId, params, callback, errorCallback) {
                return $http.post(data.RESTART_JOB_URL(executionId), params).then(function (data) {
                    callback(data);
                },function (msg) {
                    var errorMessage;
                    if (msg && msg.message) {
                        errorMessage = msg.message;
                    }
                    if (errorMessage && errorMessage.startsWith("A job instance already exists and is complete")) {
                        errorMessage = "Unable to restart.  This job is already complete.<br/> If you want to run this job again, change the parameters."
                    }

                    //   NotificationService.error( errorMessage);
                    if (errorCallback) {
                        errorCallback(errorMessage);
                    }
                })
            }

            data.failJob = function (executionId, params, callback) {
                return $http.post(data.FAIL_JOB_URL(executionId), params).then(function (data) {
                    callback(data);
                },function (msg) {
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //    NotificationService.error( errorMessasge);
                })
            }
            data.abandonJob = function (executionId, params, callback) {
                $http.post(data.ABANDON_JOB_URL(executionId), params).then(function (data) {
                    callback(data);
                },function (msg) {
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //    NotificationService.error( errorMessasge);
                })
            };

            data.abandonAllJobs = function (feed, callback,errorCallback) {
                $http.post(data.ABANDON_ALL_JOBS_URL(feed)).then(function (data) {
                    callback(data);
                },function (msg) {
                    if(errorCallback && angular.isFunction(errorCallback)) {
                        errorCallback(msg);
                    }
                })
            };

            data.stopJob = function (executionId, params, callback) {
                $http.post(data.STOP_JOB_URL(executionId), params).then(function (data) {
                    callback(data);
                },function (msg) {
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //  NotificationService.error( errorMessasge);
                })
            };

            /**
             *
             * @returns {*|{promise, cancel, abort}|{requests, promise, abort}}
             */
            data.getJobCountByStatus = function () {
                return new HttpService.get(data.JOB_COUNT_BY_STATUS_URL);

            }

            data.findAllJobs = function (successFn, errorFn, finallyFn) {
                return new HttpService.newRequestBuilder(data.ALL_JOBS_URL).then(successFn,errorFn).finally(finallyFn).build();
            };
            data.loadJob = function (instanceId) {
                return $http.get(data.LOAD_JOB_URL(instanceId));
            };

            data.lastSelectedTab = 'ALL';

            return data;
        }]);
});
