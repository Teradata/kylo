define(["require", "exports", "angular", "../module-name", "./OpsManagerRestUrlService"], function (require, exports, angular, module_name_1, OpsManagerRestUrlService_1) {
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
            var data = {};
            data.JOBS_QUERY_URL = this.OpsManagerRestUrlService.JOBS_QUERY_URL;
            data.JOBS_CHARTS_QUERY_URL = this.OpsManagerRestUrlService.JOBS_CHARTS_QUERY_URL;
            data.JOB_NAMES_URL = this.OpsManagerRestUrlService.JOB_NAMES_URL;
            data.DAILY_STATUS_COUNT_URL = this.OpsManagerRestUrlService.DAILY_STATUS_COUNT_URL;
            //data.RUNNING_OR_FAILED_COUNTS_URL = OpsManagerRestUrlService.RUNNING_OR_FAILED_COUNTS_URL;
            data.RUNNING_JOB_COUNTS_URL = this.OpsManagerRestUrlService.RUNNING_JOB_COUNTS_URL;
            //  data.DATA_CONFIDENCE_URL = OpsManagerRestUrlService.DATA_CONFIDENCE_URL;
            data.RESTART_JOB_URL = this.OpsManagerRestUrlService.RESTART_JOB_URL;
            data.STOP_JOB_URL = this.OpsManagerRestUrlService.STOP_JOB_URL;
            data.ABANDON_JOB_URL = this.OpsManagerRestUrlService.ABANDON_JOB_URL;
            data.ABANDON_ALL_JOBS_URL = this.OpsManagerRestUrlService.ABANDON_ALL_JOBS_URL;
            data.FAIL_JOB_URL = this.OpsManagerRestUrlService.FAIL_JOB_URL;
            data.LOAD_JOB_URL = this.OpsManagerRestUrlService.LOAD_JOB_URL;
            data.RELATED_JOBS_URL = this.OpsManagerRestUrlService.RELATED_JOBS_URL;
            data.restartJob = function (executionId, params, callback, errorCallback) {
                return _this.$http.post(data.RESTART_JOB_URL(executionId), params).then(function (data) {
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
            data.failJob = function (executionId, params, callback) {
                return _this.$http.post(data.FAIL_JOB_URL(executionId), params).then(function (data) {
                    callback(data);
                }, function (msg) {
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //    NotificationService.error( errorMessasge);
                });
            };
            data.abandonJob = function (executionId, params, callback) {
                _this.$http.post(data.ABANDON_JOB_URL(executionId), params).then(function (data) {
                    callback(data);
                }, function (msg) {
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //    NotificationService.error( errorMessasge);
                });
            };
            data.abandonAllJobs = function (feed, callback, errorCallback) {
                _this.$http.post(data.ABANDON_ALL_JOBS_URL(feed)).then(function (data) {
                    callback(data);
                }, function (msg) {
                    if (errorCallback && angular.isFunction(errorCallback)) {
                        errorCallback(msg);
                    }
                });
            };
            data.stopJob = function (executionId, params, callback) {
                _this.$http.post(data.STOP_JOB_URL(executionId), params).then(function (data) {
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
            data.getJobCountByStatus = function () {
                return new _this.HttpService.get(data.JOB_COUNT_BY_STATUS_URL);
            };
            data.findAllJobs = function (successFn, errorFn, finallyFn) {
                return new _this.HttpService.newRequestBuilder(data.ALL_JOBS_URL).then(successFn, errorFn).finally(finallyFn).build();
            };
            data.loadJob = function (instanceId) {
                return _this.$http.get(data.LOAD_JOB_URL(instanceId));
            };
            data.lastSelectedTab = 'ALL';
            return data;
        }
        return OpsManagerJobService;
    }());
    exports.default = OpsManagerJobService;
    angular.module(module_name_1.moduleName, [])
        .service("OpsManagerRestUrlService", [OpsManagerRestUrlService_1.default]).
        factory('OpsManagerJobService', ['$q', '$http', '$log', 'HttpService', 'NotificationService', 'OpsManagerRestUrlService', OpsManagerJobService]);
});
//# sourceMappingURL=OpsManagerJobService.js.map