define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var OpsManagerJobService = /** @class */ (function () {
        function OpsManagerJobService($q, $http, $log, HttpService, NotificationService, OpsManagerRestUrlService) {
            var _this = this;
            this.$q = $q;
            this.$http = $http;
            this.$log = $log;
            this.HttpService = HttpService;
            this.NotificationService = NotificationService;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.data = {};
            this.data.JOBS_QUERY_URL = OpsManagerRestUrlService.JOBS_QUERY_URL;
            this.data.JOBS_CHARTS_QUERY_URL = OpsManagerRestUrlService.JOBS_CHARTS_QUERY_URL;
            this.data.JOB_NAMES_URL = OpsManagerRestUrlService.JOB_NAMES_URL;
            this.data.DAILY_STATUS_COUNT_URL = OpsManagerRestUrlService.DAILY_STATUS_COUNT_URL;
            //data.RUNNING_OR_FAILED_COUNTS_URL = OpsManagerRestUrlService.RUNNING_OR_FAILED_COUNTS_URL;
            this.data.RUNNING_JOB_COUNTS_URL = OpsManagerRestUrlService.RUNNING_JOB_COUNTS_URL;
            //  data.DATA_CONFIDENCE_URL = OpsManagerRestUrlService.DATA_CONFIDENCE_URL;
            this.data.RESTART_JOB_URL = OpsManagerRestUrlService.RESTART_JOB_URL;
            this.data.STOP_JOB_URL = OpsManagerRestUrlService.STOP_JOB_URL;
            this.data.ABANDON_JOB_URL = OpsManagerRestUrlService.ABANDON_JOB_URL;
            this.data.ABANDON_ALL_JOBS_URL = OpsManagerRestUrlService.ABANDON_ALL_JOBS_URL;
            this.data.FAIL_JOB_URL = OpsManagerRestUrlService.FAIL_JOB_URL;
            this.data.LOAD_JOB_URL = OpsManagerRestUrlService.LOAD_JOB_URL;
            this.data.RELATED_JOBS_URL = OpsManagerRestUrlService.RELATED_JOBS_URL;
            this.data.restartJob = function (executionId, params, callback, errorCallback) {
                return $http.post(_this.data.RESTART_JOB_URL(executionId), params).then(function (data) {
                    callback(data);
                }, function (msg) {
                    var errorMessage;
                    if (msg && msg.message) {
                        errorMessage = msg.message;
                    }
                    if (errorMessage && errorMessage.startsWith("A job instance already exists and is complete")) {
                        errorMessage = "Unable to restart.  This job is already complete.<br/> If you want to run this job again, change the parameters.";
                    }
                    //   NotificationService.error( errorMessage);
                    if (errorCallback) {
                        errorCallback(errorMessage);
                    }
                });
            };
            this.data.failJob = function (executionId, params, callback) {
                return $http.post(_this.data.FAIL_JOB_URL(executionId), params).then(function (data) {
                    callback(data);
                }, function (msg) {
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //    NotificationService.error( errorMessasge);
                });
            };
            this.data.abandonJob = function (executionId, params, callback) {
                $http.post(_this.data.ABANDON_JOB_URL(executionId), params).then(function (data) {
                    callback(data);
                }, function (msg) {
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //    NotificationService.error( errorMessasge);
                });
            };
            this.data.abandonAllJobs = function (feed, callback, errorCallback) {
                $http.post(_this.data.ABANDON_ALL_JOBS_URL(feed)).then(function (data) {
                    callback(data);
                }, function (msg) {
                    if (errorCallback && angular.isFunction(errorCallback)) {
                        errorCallback(msg);
                    }
                });
            };
            this.data.stopJob = function (executionId, params, callback) {
                $http.post(_this.data.STOP_JOB_URL(executionId), params).then(function (data) {
                    callback(data);
                }, function (msg) {
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //  NotificationService.error( errorMessasge);
                });
            };
            /**
             *
             * @returns {*|{promise, cancel, abort}|{requests, promise, abort}}
             */
            this.data.getJobCountByStatus = function () {
                return new HttpService.get(_this.data.JOB_COUNT_BY_STATUS_URL);
            };
            this.data.findAllJobs = function (successFn, errorFn, finallyFn) {
                return new HttpService.newRequestBuilder(_this.data.ALL_JOBS_URL).then(successFn, errorFn).finally(finallyFn).build();
            };
            this.data.loadJob = function (instanceId) {
                return $http.get(_this.data.LOAD_JOB_URL(instanceId));
            };
            this.data.lastSelectedTab = 'ALL';
            return this.data;
        }
        return OpsManagerJobService;
    }());
    exports.default = OpsManagerJobService;
    angular.module(module_name_1.moduleName)
        .factory('OpsManagerJobService', ['$q', '$http', '$log', 'HttpService', 'NotificationService', 'OpsManagerRestUrlService', OpsManagerJobService]);
});
//# sourceMappingURL=OpsManagerJobService.js.map