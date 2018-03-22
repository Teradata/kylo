import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from 'underscore';
import * as moment from "moment";

export default class OpsManagerDashboardService{

    constructor(private $q: any,
                private $http: any,
                private $interval: any,
                private $timeout: any,
                private HttpService: any,
                private IconService: any,
                private OpsManagerRestUrlService: any,
                private BroadcastService: any,
                private OpsManagerFeedService: any
    ){
       let data: any;
        data= {
             DASHBOARD_UPDATED:'DASHBOARD_UPDATED',
             FEED_SUMMARY_UPDATED:'FEED_SUMMARY_UPDATED',
             TAB_SELECTED:'TAB_SELECTED',
             feedSummaryData:{},
             feedsArray:[],
             feedUnhealthyCount:0,
             feedHealthyCount:0,
             dashboard:{},
             feedsSearchResult:{},
             totalFeeds:0,
             activeFeedRequest:null,
             activeDashboardRequest:null,
             skipDashboardFeedHealth:false,
             selectFeedHealthTab:(tab: any)=> {
                 this.BroadcastService.notify(data.TAB_SELECTED,tab);
             },
             feedHealthQueryParams:{fixedFilter:'All',filter:'',start:0,limit:10, sort:''}
            };

            data.setupFeedHealth = (feedsArray: any)=>{
             var processedFeeds: any[] = [];
             if(feedsArray) {
                     var processed: any[] = [];
                     var arr: any[] = [];
                     _.each(feedsArray,  (feedHealth: any) =>{
                         //pointer to the feed that is used/bound to the ui/service
                         var feedData = null;
                         if (data.feedSummaryData[feedHealth.feed]) {
                             feedData = data.feedSummaryData[feedHealth.feed]
                             angular.extend(feedData, feedHealth);
                             feedHealth = feedData;
                         }
                         else {
                             data.feedSummaryData[feedHealth.feed] = feedHealth;
                             feedData = feedHealth;
                         }
                         arr.push(feedData);

                         processedFeeds.push(feedData);
                         if (feedData.lastUnhealthyTime) {
                             feedData.sinceTimeString = moment(feedData.lastUnhealthyTime).fromNow();
                         }

                        this.OpsManagerFeedService.decorateFeedSummary(feedData);
                         if(feedData.stream == true && feedData.feedHealth){
                             feedData.runningCount = feedData.feedHealth.runningCount;
                             if(feedData.runningCount == null){
                                 feedData.runningCount =0;
                             }
                         }

                         if(feedData.running){
                             feedData.timeSinceEndTime = feedData.runTime;
                             feedData.runTimeString = '--';
                         }
                          processed.push(feedData.feed);
                     });
                     var keysToRemove=_.difference(Object.keys(data.feedSummaryData),processed);
                     if(keysToRemove != null && keysToRemove.length >0){
                         _.each(keysToRemove,(key: any)=>{
                             delete  data.feedSummaryData[key];
                         })
                     }
                     data.feedsArray = arr;
                 }
                 return processedFeeds;

         };

         data.isFetchingFeedHealth = ()=>{
             return data.activeFeedRequest != null && angular.isDefined(data.activeFeedRequest);
         }

         data.isFetchingDashboard = ()=>{
             return data.activeDashboardRequest != null && angular.isDefined(data.activeDashboardRequest);
         }

         data.setSkipDashboardFeedHealth = (skip: any)=>{
             data.skipDashboardFeedHealth = skip;
         }

         data.fetchFeeds = (tab: any,filter: any,start: any,limit: any, sort: any)=>{
             if(data.activeFeedRequest != null && angular.isDefined(data.activeFeedRequest)){
                 data.activeFeedRequest.reject();
             }
             //Cancel any active dashboard queries as this will supercede them
             if(data.activeDashboardRequest != null && angular.isDefined(data.activeDashboardRequest)){
                 data.skipDashboardFeedHealth = true;
             }

             var canceler = this.$q.defer();

             data.activeFeedRequest = canceler;

             var params = {start: start, limit: limit, sort: sort, filter:filter, fixedFilter:tab};

             var initDashboard = (responseData:any) => {
                 data.feedsSearchResult = responseData;
                 if(responseData && responseData.data) {
                     data.setupFeedHealth(responseData.data);
                     //reset data.dashboard.feeds.data ?
                     data.totalFeeds = responseData.recordsFiltered;
                 }
                 data.activeFeedRequest = null;
                 data.skipDashboardFeedHealth = false;
                 BroadcastService.notify(data.DASHBOARD_UPDATED, data.dashboard);
             }
             var successFn = (response: any) =>{
                 return response.data;
             }
             var errorFn =  (err: any)=> {
                 canceler.reject();
                 canceler = null;
                 data.activeFeedRequest = null;
                 data.skipDashboardFeedHealth = false;
             }
             var promise = this.$http.get(this.OpsManagerRestUrlService.DASHBOARD_PAGEABLE_FEEDS_URL,{timeout: canceler.promise,params:params});
             promise.then(successFn, errorFn).then(fetchPageableFeedNames).then(initDashboard);
             return promise;
         }

         var fetchPageableFeedNames = (resolveObj:any) => {
            var feeds = resolveObj.data;
            return fetchFeedNames(resolveObj, feeds);
         };

         var fetchFeedNames = (resolveObj:any, feeds:any) => {
             var deferred = $q.defer();

             if (feeds.length > 0) {
                 var feedNames = _.map(feeds, (feed:any) => {
                     return feed.feed;
                 });
                 var namesPromise = $http.post(OpsManagerRestUrlService.FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL, feedNames);
                 namesPromise.then((result:any) => {
                     _.each(feeds, (feed:any) => {
                         feed.displayName = _.find(result.data, (systemNameToDisplayName:any) => {
                             return systemNameToDisplayName.key === feed.feed;
                         })
                     });
                     deferred.resolve(resolveObj);
                 }, function(err:any) {
                     console.error('Failed to receive feed names', err);
                     deferred.resolve(resolveObj);
                 });
             } else {
                 deferred.resolve(resolveObj);
             }

             return deferred.promise;
         };

         data.updateFeedHealthQueryParams = (tab: any,filter: any,start: any,limit: any, sort: any)=>{
             var params = {start: start, limit: limit, sort: sort, filter:filter, fixedFilter:tab};
             angular.extend(data.feedHealthQueryParams,params);
         }

         data.fetchDashboard = ()=>{
             if(data.activeDashboardRequest != null && angular.isDefined(data.activeDashboardRequest)){
                 data.activeDashboardRequest.reject();
             }
             var canceler = $q.defer();
             data.activeDashboardRequest = canceler;

             var initDashboard = (dashboard:any) => {
                 data.dashboard = dashboard;
                 //if the pagable feeds query came after this one it will flip the skip flag.
                 // that should supercede this request
                 if(!data.skipDashboardFeedHealth) {
                     data.feedsSearchResult = dashboard.feeds;
                     if (data.dashboard && data.dashboard.feeds && data.dashboard.feeds.data) {
                         var processedFeeds = data.setupFeedHealth(data.dashboard.feeds.data);
                         data.dashboard.feeds.data = processedFeeds;
                         data.totalFeeds = data.dashboard.feeds.recordsFiltered;
                     }
                 }
                 else {
                    //     console.log('Skip processing dashboard results for the feed since it was superceded');
                 }
                 if(angular.isUndefined(data.dashboard.healthCounts['UNHEALTHY'])) {
                     data.dashboard.healthCounts['UNHEALTHY'] = 0;
                 }
                 if(angular.isUndefined(data.dashboard.healthCounts['HEALTHY'])) {
                     data.dashboard.healthCounts['HEALTHY'] = 0;
                 }

                 data.feedUnhealthyCount = data.dashboard.healthCounts['UNHEALTHY'] || 0;
                 data.feedHealthyCount = data.dashboard.healthCounts['HEALTHY'] || 0;
                 data.activeDashboardRequest = null;
                 BroadcastService.notify(data.DASHBOARD_UPDATED, dashboard);
             }
             var successFn = (response:any) => {
                 fetchFeedNames(response.data, response.data.feeds.data).then(initDashboard);
             }
             var errorFn = (err: any) =>{
                 canceler.reject();
                 canceler = null;
                 data.activeDashboardRequest = null;
                 data.skipDashboardFeedHealth = false;
                 console.error("Dashboard error!!!")
             }
             var params = data.feedHealthQueryParams;
             var promise = this.$http.get(this.OpsManagerRestUrlService.DASHBOARD_URL,{timeout: canceler.promise,params:params});

             promise.then(successFn, errorFn);

             return promise;
         };

         return data;
     }
    
}

angular.module(moduleName)
.factory('OpsManagerDashboardService',['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService',
                                         'OpsManagerRestUrlService','BroadcastService',
                                        'OpsManagerFeedService',OpsManagerDashboardService]);
