import * as angular from "angular";
import {moduleName} from "../module-name";

export default class ProvenanceEventStatsService{
    constructor(
                private $http: any,
                private $q: any,
                private OpsManagerRestUrlService: any
    ){
         let data: any = {
            getTimeFrameOptions: function () {

                var promise = $http.get(OpsManagerRestUrlService.PROVENANCE_EVENT_TIME_FRAME_OPTIONS);
                return promise;
            },

            getFeedProcessorDuration: function (feedName: any, from: any, to: any) {
             var successFn =  (response: any) =>{
                }
                var errorFn =  (err: any) =>{
                    this.loading = false;

                }
                var promise = $http.get(OpsManagerRestUrlService.PROCESSOR_DURATION_FOR_FEED(feedName, from, to));
                promise.then(successFn, errorFn);
                return promise;
            },
            getFeedStatisticsOverTime: function (feedName: any, from: any, to: any) {            
                var successFn =  (response: any)=> {

                };
                var errorFn =  (err: any)=> {
                    this.loading = false;
                };
                var promise = $http.get(OpsManagerRestUrlService.FEED_STATISTICS_OVER_TIME(feedName, from, to));
                promise.then(successFn, errorFn);
                return promise;
            },

            getFeedProcessorErrors: function (feedName: any, from: any, to: any, after: any) {
                var successFn = (response: any) =>{

                }
                var errorFn =  (err: any)=>{
                    this.loading = false;

                }
                var promise = $http.get(OpsManagerRestUrlService.FEED_PROCESSOR_ERRORS(feedName, from, to),{params:{after:after}});
                promise.then(successFn, errorFn);
                return promise;
            }

        }
        return data;
    }
}

   angular.module(moduleName)
   .factory('ProvenanceEventStatsService',['$http','$q','OpsManagerRestUrlService',ProvenanceEventStatsService]);
