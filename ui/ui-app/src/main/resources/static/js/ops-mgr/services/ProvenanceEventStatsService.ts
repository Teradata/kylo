import {OpsManagerRestUrlService} from "./OpsManagerRestUrlService";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";

@Injectable()
export class ProvenanceEventStatsService{
    loading : boolean = false;
    constructor(
                private http : HttpClient,
                private opsManagerRestUrlService: OpsManagerRestUrlService
    ){}
    getTimeFrameOptions() {

        var promise = this.http.get(this.opsManagerRestUrlService.PROVENANCE_EVENT_TIME_FRAME_OPTIONS).toPromise();
        return promise;
    };
    getFeedProcessorDuration (feedName: any, from: any, to: any) {
     var successFn =  (response: any) =>{
        }
        var errorFn =  (err: any) =>{
            this.loading = false;

        }
        var promise = this.http.get(this.opsManagerRestUrlService.PROCESSOR_DURATION_FOR_FEED(feedName, from, to)).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    };
    getFeedStatisticsOverTime (feedName: any, from: any, to: any) {
        var successFn =  (response: any)=> {

        };
        var errorFn =  (err: any)=> {
            this.loading = false;
        };
        var promise = this.http.get(this.opsManagerRestUrlService.FEED_STATISTICS_OVER_TIME(feedName, from, to)).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    };
    getFeedProcessorErrors(feedName: any, from: any, to: any, after: any) {
        var successFn = (response: any) =>{

        }
        var errorFn =  (err: any)=>{
            this.loading = false;

        }
        var promise = this.http.get(this.opsManagerRestUrlService.FEED_PROCESSOR_ERRORS(feedName, from, to,after)).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    }
}
