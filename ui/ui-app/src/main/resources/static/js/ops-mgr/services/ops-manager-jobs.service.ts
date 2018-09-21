import {HttpClient} from "@angular/common/http";
import {OperationsRestUrlConstants} from "../../services/operations-rest-url-constants";
import {Injectable} from "@angular/core";
import * as _ from "underscore"

@Injectable()
export  class OpsManagerJobService {
    constructor(private http:HttpClient){ }
        //ALL_JOBS_URL = OperationsRestUrlConstants.ALL_JOBS_URL;
        JOBS_QUERY_URL = OperationsRestUrlConstants.JOBS_QUERY_URL;
        JOBS_CHARTS_QUERY_URL = OperationsRestUrlConstants.JOBS_CHARTS_QUERY_URL;
        JOB_NAMES_URL = OperationsRestUrlConstants.JOB_NAMES_URL;
        DAILY_STATUS_COUNT_URL = OperationsRestUrlConstants.DAILY_STATUS_COUNT_URL;
        RUNNING_JOB_COUNTS_URL = OperationsRestUrlConstants.RUNNING_JOB_COUNTS_URL;
        RESTART_JOB_URL = OperationsRestUrlConstants.RESTART_JOB_URL;
        STOP_JOB_URL = OperationsRestUrlConstants.STOP_JOB_URL;
        ABANDON_JOB_URL = OperationsRestUrlConstants.ABANDON_JOB_URL;
        ABANDON_ALL_JOBS_URL = OperationsRestUrlConstants.ABANDON_ALL_JOBS_URL;
        FAIL_JOB_URL = OperationsRestUrlConstants.FAIL_JOB_URL;
        LOAD_JOB_URL = OperationsRestUrlConstants.LOAD_JOB_URL;
        RELATED_JOBS_URL = OperationsRestUrlConstants.RELATED_JOBS_URL;

      lastSelectedTab = 'ALL';


    restartJob(executionId: any, params: any, callback: any, errorCallback: any){

            return this.http.post(this.RESTART_JOB_URL(executionId), params).subscribe( (data: any) =>{
                callback(data);
            }, (msg: any)=>{
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
            });
        }

        failJob(executionId: any, params: any, callback: any){
            return this.http.post(this.FAIL_JOB_URL(executionId), params).subscribe( (data: any) =>{
                callback(data);
            }, (msg: any) =>{
                var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                errorMessasge += msg.message;
                //    NotificationService.error( errorMessasge);
            })
        }
        abandonJob(executionId: any, params: any, callback: any){
            this.http.post(this.ABANDON_JOB_URL(executionId), params).subscribe( (data: any) =>{
                callback(data);
            }, (msg: any)=>{
                var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                errorMessasge += msg.message;
                //    NotificationService.error( errorMessasge);
            })
        };

        abandonAllJobs(feed: any, callback: any,errorCallback: any){
            this.http.post(this.ABANDON_ALL_JOBS_URL(feed),null).subscribe( (data: any)=>{
                callback(data);
            }, (msg: any) =>{
                if(errorCallback && _.isFunction(errorCallback)) {
                    errorCallback(msg);
                }
            })
        };

        stopJob(executionId: any, params: any, callback: any){
            this.http.post(this.STOP_JOB_URL(executionId), params).subscribe( (data: any) =>{
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
     /*   getJobCountByStatus(){
            return this.http.get(this.JOB_COUNT_BY_STATUS_URL).toPromise();

        }

        findAllJobs(successFn: any, errorFn: any, finallyFn: any){
            return this.http.get(this.ALL_JOBS_URL).subscribe(successFn,errorFn).toPromise();//.finally(finallyFn).build();
        };

        */
        loadJob(instanceId: any){
            return this.http.get(this.LOAD_JOB_URL(instanceId)).toPromise();
        };

}
/*
angular.module(moduleName)
    .factory('OpsManagerJobService',['$q', 'this.http', '$log', 'HttpService', 'NotificationService', 'OpsManagerRestUrlService',OpsManagerJobService]);
*/