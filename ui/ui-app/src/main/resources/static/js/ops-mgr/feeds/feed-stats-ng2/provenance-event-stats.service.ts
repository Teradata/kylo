import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import {OperationsRestUrlConstants} from "../../../services/operations-rest-url-constants";

@Injectable()
export  class ProvenanceEventStatsServiceNg2{
    loading : boolean = false;
    constructor(
        private http : HttpClient
    ){}
    getTimeFrameOptions() {

        var promise = this.http.get(OperationsRestUrlConstants.PROVENANCE_EVENT_TIME_FRAME_OPTIONS).toPromise();
        return promise;
    };
    getFeedProcessorDuration (feedName: string, from: number, to: number) {
        var successFn =  (response: any) =>{
        }
        var errorFn =  (err: any) =>{
            this.loading = false;

        }
        var promise = this.http.get(OperationsRestUrlConstants.PROCESSOR_DURATION_FOR_FEED(feedName, from, to)).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    };
    getFeedStatisticsOverTime (feedName: string, from: number, to: number) {
        var successFn =  (response: any)=> {

        };
        var errorFn =  (err: any)=> {
            this.loading = false;
        };
        var promise = this.http.get(OperationsRestUrlConstants.FEED_STATISTICS_OVER_TIME(feedName, from, to)).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    };
    getFeedProcessorErrors(feedName: string, from: number, to: number, after: number) {
        var successFn = (response: any) =>{

        }
        var errorFn =  (err: any)=>{
            this.loading = false;

        }
        var promise = this.http.get(OperationsRestUrlConstants.FEED_PROCESSOR_ERRORS(feedName, from, to,after)).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    }
}