import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import OpsManagerRestUrlService from "./OpsManagerRestUrlService";
import { ObjectUtils } from "../../common/utils/object-utils";

@Injectable()
export default class OpsManagerJobService {

    constructor(private http: HttpClient,
                private opsManagerRestUrlService: OpsManagerRestUrlService){}
       
        JOBS_QUERY_URL = this.opsManagerRestUrlService.JOBS_QUERY_URL;
        JOBS_CHARTS_QUERY_URL = this.opsManagerRestUrlService.JOBS_CHARTS_QUERY_URL;
        JOB_NAMES_URL = this.opsManagerRestUrlService.JOB_NAMES_URL;
        DAILY_STATUS_COUNT_URL = this.opsManagerRestUrlService.DAILY_STATUS_COUNT_URL;
        RUNNING_JOB_COUNTS_URL = this.opsManagerRestUrlService.RUNNING_JOB_COUNTS_URL;
        RESTART_JOB_URL = this.opsManagerRestUrlService.RESTART_JOB_URL;
        STOP_JOB_URL = this.opsManagerRestUrlService.STOP_JOB_URL;
        ABANDON_JOB_URL = this.opsManagerRestUrlService.ABANDON_JOB_URL;
        ABANDON_ALL_JOBS_URL = this.opsManagerRestUrlService.ABANDON_ALL_JOBS_URL;
        FAIL_JOB_URL = this.opsManagerRestUrlService.FAIL_JOB_URL;
        LOAD_JOB_URL = this.opsManagerRestUrlService.LOAD_JOB_URL;
        RELATED_JOBS_URL = this.opsManagerRestUrlService.RELATED_JOBS_URL;

        restartJob(executionId: any, params: any, callback: any, errorCallback: any){
                return this.http.post(this.RESTART_JOB_URL(executionId), params).toPromise().then( (data: any) =>{
                    callback(data);
                }, (msg: any)=> {
                    var errorMessage;
                    if (msg && msg.message) {
                        errorMessage = msg.message;
                    }
                    if (errorMessage && errorMessage.startsWith("A job instance already exists and is complete")) {
                        errorMessage = "Unable to restart.  This job is already complete.<br/> If you want to run this job again, change the parameters."
                    }
                    if (errorCallback) {
                        errorCallback(errorMessage);
                    }
                })
            }

        failJob(executionId: any, params: any, callback: any){
                return this.http.post(this.FAIL_JOB_URL(executionId), params).toPromise().then( (data: any)=> {
                    callback(data);
                }, (msg: any) =>{
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                })
            }
        abandonJob(executionId: any, params: any, callback: any){
                this.http.post(this.ABANDON_JOB_URL(executionId), params).toPromise().then( (data: any) =>{
                    callback(data);
                }, (msg: any)=>{
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                })
            };

        abandonAllJobs (feed: any, callback: any,errorCallback: any) {
                this.http.post(this.ABANDON_ALL_JOBS_URL(feed), "").toPromise().then( (data: any)=>{
                    callback(data);
                }, (msg: any) =>{
                    if(errorCallback && ObjectUtils.isFunction(errorCallback)) {
                        errorCallback(msg);
                    }
                })
            };

        stopJob (executionId: any, params: any, callback: any) {
                this.http.post(this.STOP_JOB_URL(executionId), params).toPromise().then( (data: any) =>{
                    callback(data);
                }, (msg: any)=>{
                    var errorMessasge = msg.error != undefined ? msg.error + ': ' : '';
                    errorMessasge += msg.message;
                })
            };

            /**
             *
             * @returns {*|{promise, cancel, abort}|{requests, promise, abort}}
             */
        // getJobCountByStatus() {
        //         return new this.HttpService.get(this.JOB_COUNT_BY_STATUS_URL);

        //     }

        // findAllJobs =  (successFn: any, errorFn: any, finallyFn: any) =>{
        //         return new this.HttpService.newRequestBuilder(this.ALL_JOBS_URL).then(successFn,errorFn).finally(finallyFn).build();
        //     };
        loadJob (instanceId: any) {
            return this.http.get(this.LOAD_JOB_URL(instanceId));
        };

        lastSelectedTab = 'ALL';

}