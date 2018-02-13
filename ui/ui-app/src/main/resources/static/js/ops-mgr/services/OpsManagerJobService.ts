import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from 'underscore';
import * as moment from "moment";

export default class OpsManagerJobService{
    module: ng.IModule;
    constructor(private $q: any,
                private $http: any,
                private $log: any,
                private HttpService: any,
                private NotificationService: any,
                private OpsManagerRestUrlService: any
    ){
    this.module = angular.module(moduleName,[]);
    this.module.factory('OpsManagerJobService',['$q', '$http', '$log', 'HttpService', 'NotificationService', 'OpsManagerRestUrlService',this.factoryFn.bind(this)]);
    }

      factoryFn() {

   var data: any = {};
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

            data.restartJob = function (executionId: any, params: any, callback: any, errorCallback: any) {
                return this.$http.post(data.RESTART_JOB_URL(executionId), params).then(function (data: any) {
                    callback(data);
                },function (msg: any) {
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

            data.failJob = function (executionId: any, params: any, callback: any) {
                return this.$http.post(data.FAIL_JOB_URL(executionId), params).then(function (data: any) {
                    callback(data);
                },function (msg: any) {
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //    NotificationService.error( errorMessasge);
                })
            }
            data.abandonJob = function (executionId: any, params: any, callback: any) {
                this.$http.post(data.ABANDON_JOB_URL(executionId), params).then(function (data: any) {
                    callback(data);
                },function (msg: any) {
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //    NotificationService.error( errorMessasge);
                })
            };

            data.abandonAllJobs = function (feed: any, callback: any,errorCallback: any) {
                this.$http.post(data.ABANDON_ALL_JOBS_URL(feed)).then(function (data: any) {
                    callback(data);
                },function (msg: any) {
                    if(errorCallback && angular.isFunction(errorCallback)) {
                        errorCallback(msg);
                    }
                })
            };

            data.stopJob = function (executionId: any, params: any, callback: any) {
                this.$http.post(data.STOP_JOB_URL(executionId), params).then(function (data: any) {
                    callback(data);
                },function (msg: any) {
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
                return new this.HttpService.get(data.JOB_COUNT_BY_STATUS_URL);

            }

            data.findAllJobs = function (successFn: any, errorFn: any, finallyFn: any) {
                return new this.HttpService.newRequestBuilder(data.ALL_JOBS_URL).then(successFn,errorFn).finally(finallyFn).build();
            };
            data.loadJob = function (instanceId: any) {
                return this.$http.get(data.LOAD_JOB_URL(instanceId));
            };

            data.lastSelectedTab = 'ALL';

            return data;
      }
}