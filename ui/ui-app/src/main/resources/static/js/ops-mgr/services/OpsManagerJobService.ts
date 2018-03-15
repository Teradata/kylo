import * as angular from "angular";
import {moduleName} from "../module-name";

export default class OpsManagerJobService{
data: any = {};
    constructor(private $q: any,
                private $http: any,
                private $log: any,
                private HttpService: any,
                private NotificationService: any,
                private OpsManagerRestUrlService: any
    ){
        
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

          this.data.restartJob = (executionId: any, params: any, callback: any, errorCallback: any)=>{
                return $http.post(this.data.RESTART_JOB_URL(executionId), params).then( (data: any) =>{
                    callback(data);
                }, (msg: any)=> {
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

           this.data.failJob = (executionId: any, params: any, callback: any)=> {
                return $http.post(this.data.FAIL_JOB_URL(executionId), params).then( (data: any)=> {
                    callback(data);
                }, (msg: any) =>{
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //    NotificationService.error( errorMessasge);
                })
            }
            this.data.abandonJob =  (executionId: any, params: any, callback: any)=>{
                $http.post(this.data.ABANDON_JOB_URL(executionId), params).then( (data: any) =>{
                    callback(data);
                }, (msg: any)=>{
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //    NotificationService.error( errorMessasge);
                })
            };

            this.data.abandonAllJobs =  (feed: any, callback: any,errorCallback: any)=> {
                $http.post(this.data.ABANDON_ALL_JOBS_URL(feed)).then( (data: any)=>{
                    callback(data);
                }, (msg: any) =>{
                    if(errorCallback && angular.isFunction(errorCallback)) {
                        errorCallback(msg);
                    }
                })
            };

           this.data.stopJob =  (executionId: any, params: any, callback: any)=> {
                $http.post(this.data.STOP_JOB_URL(executionId), params).then( (data: any) =>{
                    callback(data);
                }, (msg: any)=>{
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                    //  NotificationService.error( errorMessasge);
                })
            };

            /**
             *
             * @returns {*|{promise, cancel, abort}|{requests, promise, abort}}
             */
            this.data.getJobCountByStatus =  ()=> {
                return new HttpService.get(this.data.JOB_COUNT_BY_STATUS_URL);

            }

            this.data.findAllJobs =  (successFn: any, errorFn: any, finallyFn: any) =>{
                return new HttpService.newRequestBuilder(this.data.ALL_JOBS_URL).then(successFn,errorFn).finally(finallyFn).build();
            };
            this.data.loadJob =  (instanceId: any) =>{
                return $http.get(this.data.LOAD_JOB_URL(instanceId));
            };

            this.data.lastSelectedTab = 'ALL';

            return this.data;
   }
       

}

angular.module(moduleName)
        .factory('OpsManagerJobService',['$q', '$http', '$log', 'HttpService', 'NotificationService', 'OpsManagerRestUrlService',OpsManagerJobService]);
