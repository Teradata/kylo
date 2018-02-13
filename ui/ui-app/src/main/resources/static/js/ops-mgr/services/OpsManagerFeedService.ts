import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from 'underscore';
import * as moment from "moment";

export default class OpsManagerFeedService{
    module: ng.IModule;
    constructor(private $q: any,
                private $http: any,
                private $interval: any,
                private $timeout: any,
                private HttpService: any,
                private IconService: any,
                private AlertsService: any,
                private OpsManagerRestUrlService: any
    ){
    this.module = angular.module(moduleName,[]);
    this.module.factory('OpsManagerFeedService',['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService','BroadcastService','OpsManagerFeedService',this.factoryFn.bind(this)]);
    }

      factoryFn() {

   var data: any = {};
         data.FEED_HEALTH_URL = this.OpsManagerRestUrlService.FEED_HEALTH_URL;
         data.FEED_NAMES_URL = this.OpsManagerRestUrlService.FEED_NAMES_URL;
         data.FEED_HEALTH_COUNT_URL = this.OpsManagerRestUrlService.FEED_HEALTH_COUNT_URL;
         data.FETCH_FEED_HEALTH_INTERVAL = 5000;
         data.fetchFeedHealthInterval = null;
         data.feedHealth = {};

        // data.SPECIFIC_FEED_HEALTH_COUNT_URL = OpsManagerRestUrlService.SPECIFIC_FEED_HEALTH_COUNT_URL;

         data.SPECIFIC_FEED_HEALTH_URL = this.OpsManagerRestUrlService.SPECIFIC_FEED_HEALTH_URL;

         data.DAILY_STATUS_COUNT_URL = this.OpsManagerRestUrlService.FEED_DAILY_STATUS_COUNT_URL;

         data.feedSummaryData = {};
         data.feedUnhealthyCount = 0;
         data.feedHealthyCount = 0;

         data.feedHealth = 0;

         data.emptyFeed = function () {
             var feed: any = {};
             feed.displayStatus = 'LOADING';
             feed.lastStatus = 'LOADING',
                 feed.timeSinceEndTime = 0;
             feed.isEmpty = true;
             return feed;
         }

         data.decorateFeedSummary = function (feed: any) {
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

         data.fetchFeedSummaryData = function () {
             var successFn = function (response: any) {
                 data.feedSummaryData = response.data;
                 if (response.data) {
                     data.feedUnhealthyCount = response.data.failedCount;
                     data.feedHealthyCount = response.data.healthyCount;
                 }
             }
             var errorFn = function (err: any) {

             }
             var finallyFn = function () {

             }

             var promise = this.$http.get(data.FEED_HEALTH_URL);
             promise.then(successFn, errorFn);
             return promise;
         };

         data.fetchFeedHealth = function () {
             var successFn = function (response: any) {

                 var unhealthyFeedNames: any[] = [];
                 if (response.data) {
                     angular.forEach(response.data, function (feedHealth: any) {
                         if (data.feedHealth[feedHealth.feed]) {
                             angular.extend(data.feedHealth[feedHealth.feed], feedHealth);
                         }
                         else {
                             data.feedHealth[feedHealth.feed] = feedHealth;
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
                 data.fetchFeedHealthTimeout();
             }
             var errorFn = function (err: any) {
                 data.fetchFeedHealthTimeout();
             }
             var finallyFn = function () {

             }

             var promise = this.$http.get(data.FEED_HEALTH_COUNT_URL);
             promise.then(successFn, errorFn);
             return promise;
         };

         data.startFetchFeedHealth = function () {
             if (data.fetchFeedHealthInterval == null) {
                 data.fetchFeedHealth();

                 data.fetchFeedHealthInterval = this.$interval(function () {
                     data.fetchFeedHealth();
                 }, data.FETCH_FEED_HEALTH_INTERVAL)
             }
         }

         data.fetchFeedHealthTimeout = function () {
             data.stopFetchFeedHealthTimeout();

             data.fetchFeedHealthInterval = this.$timeout(function () {
                 data.fetchFeedHealth();
             }, data.FETCH_FEED_HEALTH_INTERVAL);
         }

         data.stopFetchFeedHealthTimeout = function () {
             if (data.fetchFeedHealthInterval != null) {
                 this.$timeout.cancel(data.fetchFeedHealthInterval);
             }
         }

         return data;
      }
}