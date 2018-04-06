import * as angular from "angular";
import {moduleName} from "../module-name";
import * as moment from "moment";

export default class OpsManagerFeedService{
    data: any;
    //static $inject = ['OpsManagerRestUrlService'];
     constructor(private $q: any,
                private $http: any,
                private $interval: any,
                private $timeout: any,
                private HttpService: any,
                private IconService: any,
                private OpsManagerRestUrlService: any){
         this.data= {};
         this.data.FEED_HEALTH_URL = this.OpsManagerRestUrlService.FEED_HEALTH_URL;
         this.data.FEED_NAMES_URL =  this.OpsManagerRestUrlService.FEED_NAMES_URL;
         this.data.FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL = OpsManagerRestUrlService.FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL;
         this.data.FEED_HEALTH_COUNT_URL =  this.OpsManagerRestUrlService.FEED_HEALTH_COUNT_URL;
         this.data.FETCH_FEED_HEALTH_INTERVAL = 5000;
         this.data.fetchFeedHealthInterval = null;
         this.data.feedHealth = {};

        // this.data.SPECIFIC_FEED_HEALTH_COUNT_URL =  this.OpsManagerRestUrlService.SPECIFIC_FEED_HEALTH_COUNT_URL;

         this.data.SPECIFIC_FEED_HEALTH_URL =  this.OpsManagerRestUrlService.SPECIFIC_FEED_HEALTH_URL;

         this.data.DAILY_STATUS_COUNT_URL =  this.OpsManagerRestUrlService.FEED_DAILY_STATUS_COUNT_URL;

         this.data.feedSummaryData = {};
         this.data.feedUnhealthyCount = 0;
         this.data.feedHealthyCount = 0;

         this.data.feedHealth = 0;

         this.data.emptyFeed =  ()=> {
             var feed: any = {};
             feed.displayStatus = 'LOADING';
             feed.lastStatus = 'LOADING',
                 feed.timeSinceEndTime = 0;
             feed.isEmpty = true;
             return feed;
         }

         this.data.decorateFeedSummary =  (feed: any) =>{
             //GROUP FOR FAILED

             if (feed.isEmpty == undefined) {
                 feed.isEmpty = false;
             }

             var health: string = "---";
             if (!feed.isEmpty) {
                 health = feed.healthy ? 'HEALTHY' : 'UNHEALTHY';
                 var iconData: any = this.IconService.iconForFeedHealth(health);
                 feed.icon = iconData.icon
                 feed.iconstyle = iconData.style
             }
             feed.healthText = health;
             if (feed.running) {
                 feed.displayStatus = 'RUNNING';
             }
             else if ("FAILED" == feed.lastStatus || ( "FAILED" == feed.lastExitCode && "ABANDONED" != feed.lastStatus)) {
                 feed.displayStatus = 'FAILED';
             }
             else if ("COMPLETED" == feed.lastExitCode) {
                 feed.displayStatus = 'COMPLETED';
             }
             else if ("STOPPED" == feed.lastStatus) {
                 feed.displayStatus = 'STOPPED';
             }
             else if ("UNKNOWN" == feed.lastStatus) {
                 feed.displayStatus = 'INITIAL';
                 feed.sinceTimeString = '--';
                 feed.runTimeString = "--"
             }
             else {
                 feed.displayStatus = feed.lastStatus;
             }

             feed.statusStyle = this.IconService.iconStyleForJobStatus(feed.displayStatus);
         }

         this.data.fetchFeedSummaryData =  ()=> {
             var successFn =  (response: any)=> {
                 this.data.feedSummaryData = response.data;
                 if (response.data) {
                     this.data.feedUnhealthyCount = response.data.failedCount;
                     this.data.feedHealthyCount = response.data.healthyCount;
                 }
             }
             var errorFn =  (err: any)=>{

             }
             var finallyFn =  ()=>{

             }

             var promise = this.$http.get(this.data.FEED_HEALTH_URL);
             promise.then(successFn, errorFn);
             return promise;
         };

         this.data.fetchFeedHealth =  ()=> {
             var successFn =  (response: any)=> {

                 var unhealthyFeedNames: any[] = [];
                 if (response.data) {
                     angular.forEach(response.data,  (feedHealth: any)=> {
                         if (this.data.feedHealth[feedHealth.feed]) {
                             angular.extend(this.data.feedHealth[feedHealth.feed], feedHealth);
                         }
                         else {
                             this.data.feedHealth[feedHealth.feed] = feedHealth;
                         }
                         if (feedHealth.lastUnhealthyTime) {
                             feedHealth.sinceTimeString =  moment(feedHealth.lastUnhealthyTime).fromNow();
                         }
                         if (feedHealth.healthy) {
                        //     AlertsService.removeFeedFailureAlertByName(feedHealth.feed);
                         }
                         else {
                             unhealthyFeedNames.push(feedHealth.feed);
                      //       AlertsService.addFeedHealthFailureAlert(feedHealth);
                         }
                     });
                     //only unhealthy will come back
                     //if feedName is not in the response list, but currently failed.. remove it
               //      var failedFeeds = AlertsService.feedFailureAlerts;
            //         angular.forEach(failedFeeds, function (alert, feedName) {
            //             if (_.indexOf(unhealthyFeedNames, feedName) == -1) {
            //                 AlertsService.removeFeedFailureAlertByName(feedName);
             //            }
              //       });

                 }
                 this.data.fetchFeedHealthTimeout();
             }
             var errorFn =  (err: any)=> {
                 this.data.fetchFeedHealthTimeout();
             }
             var finallyFn =  ()=> {

             }

             var promise = this.$http.get(this.data.FEED_HEALTH_COUNT_URL);
             promise.then(successFn, errorFn);
             return promise;
         };

         this.data.startFetchFeedHealth =  ()=> {
             if (this.data.fetchFeedHealthInterval == null) {
                 this.data.fetchFeedHealth();

                 this.data.fetchFeedHealthInterval = this.$interval( () =>{
                     this.data.fetchFeedHealth();
                 }, this.data.FETCH_FEED_HEALTH_INTERVAL)
             }
         }

         this.data.fetchFeedHealthTimeout =  ()=>{
             this.data.stopFetchFeedHealthTimeout();

             this.data.fetchFeedHealthInterval = this.$timeout( () =>{
                 this.data.fetchFeedHealth();
             }, this.data.FETCH_FEED_HEALTH_INTERVAL);
         }

         this.data.stopFetchFeedHealthTimeout =  () =>{
             if (this.data.fetchFeedHealthInterval != null) {
                 this.$timeout.cancel(this.data.fetchFeedHealthInterval);
             }
         }

         return this.data;
    }
}

angular.module(moduleName)
        .factory('OpsManagerFeedService',['$q', '$http', '$interval', '$timeout', 'HttpService', 
                'IconService', 'OpsManagerRestUrlService',OpsManagerFeedService]);
