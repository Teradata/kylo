import * as angular from "angular";
import {moduleName} from "../module-name";

export default class ProvenanceEventStatsService{
    module: ng.IModule;
    constructor(
                private $http: any,
                private $q: any,
                private OpsManagerRestUrlService: any
    ){
    this.module = angular.module(moduleName,[]);
    this.module.factory('ProvenanceEventStatsService',['$http','$q','OpsManagerRestUrlService',this.factoryFn.bind(this)]);
    }

      factoryFn() {
       var data: any = {

            getTimeFrameOptions: function () {

                var promise = this.$http.get(this.OpsManagerRestUrlService.PROVENANCE_EVENT_TIME_FRAME_OPTIONS);
                return promise;
            },

            getFeedProcessorDuration: function (feedName: any, from: any, to: any) {
             var successFn = function (response: any) {
                }
                var errorFn = function (err: any) {
                    this.loading = false;

                }
                var promise = this.$http.get(this.OpsManagerRestUrlService.PROCESSOR_DURATION_FOR_FEED(feedName, from, to));
                promise.then(successFn, errorFn);
                return promise;
            },
            getFeedStatisticsOverTime: function (feedName: any, from: any, to: any) {            
                var successFn = function (response: any) {

                };
                var errorFn = function (err: any) {
                    this.loading = false;
                };
                var promise = this.$http.get(this.OpsManagerRestUrlService.FEED_STATISTICS_OVER_TIME(feedName, from, to));
                promise.then(successFn, errorFn);
                return promise;
            },

            getFeedProcessorErrors: function (feedName: any, from: any, to: any, after: any) {
                var successFn = function (response: any) {

                }
                var errorFn = function (err: any) {
                    this.loading = false;

                }
                var promise = this.$http.get(this.OpsManagerRestUrlService.FEED_PROCESSOR_ERRORS(feedName, from, to),{params:{after:after}});
                promise.then(successFn, errorFn);
                return promise;
            }

        }
        return data;
      }
}