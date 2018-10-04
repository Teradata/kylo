import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from 'underscore';
import * as moment from "moment";
import {OpsManagerFeedUtil} from "./ops-manager-feed-util";

export default class OpsManagerDashboardService{

    
    static readonly $inject = ['$q', '$http', 'OpsManagerRestUrlService','BroadcastService']
    constructor(private $q: any,private $http: any, private OpsManagerRestUrlService: any, private BroadcastService: any){ }
     
             DASHBOARD_UPDATED:string = 'DASHBOARD_UPDATED';
             FEED_SUMMARY_UPDATED:string ='FEED_SUMMARY_UPDATED';
             TAB_SELECTED:string = 'TAB_SELECTED';
             feedSummaryData:any = {};
             feedsArray:any[]=[];
             feedUnhealthyCount:number =0;
             feedHealthyCount:number = 0;
             dashboard:any = {};
             feedsSearchResult:any = {};
             totalFeeds:number = 0;
             
             activeFeedRequest:any = null; 
                 activeDashboardRequest:any = null;
             skipDashboardFeedHealth:boolean = false
        
             selectFeedHealthTab(tab: any) {
                 this.BroadcastService.notify(this.TAB_SELECTED,tab);
             }
             
            feedHealthQueryParams:any = {fixedFilter:'All',filter:'',start:0,limit:10, sort:''}
            

            setupFeedHealth(feedsArray: any){
             var processedFeeds: any[] = [];
             if(feedsArray) {
                     var processed: any[] = [];
                     var arr: any[] = [];
                     _.each(feedsArray,  (feedHealth: any) =>{
                         //pointer to the feed that is used/bound to the ui/service
                         var feedData = null;
                         if (this.feedSummaryData[feedHealth.feed]) {
                             feedData = this.feedSummaryData[feedHealth.feed]
                             angular.extend(feedData, feedHealth);
                             feedHealth = feedData;
                         }
                         else {
                             this.feedSummaryData[feedHealth.feed] = feedHealth;
                             feedData = feedHealth;
                         }
                         arr.push(feedData);

                         processedFeeds.push(feedData);
                         if (feedData.lastUnhealthyTime) {
                             feedData.sinceTimeString = moment(feedData.lastUnhealthyTime).fromNow();
                         }

                         OpsManagerFeedUtil.decorateFeedSummary(feedData);
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
                     var keysToRemove=_.difference(Object.keys(this.feedSummaryData),processed);
                     if(keysToRemove != null && keysToRemove.length >0){
                         _.each(keysToRemove,(key: any)=>{
                             delete  this.feedSummaryData[key];
                         })
                     }
                     this.feedsArray = arr;
                 }
                 return processedFeeds;

         }

         isFetchingFeedHealth(){
             return this.activeFeedRequest != null && angular.isDefined(this.activeFeedRequest);
         }

         isFetchingDashboard() {
             return this.activeDashboardRequest != null && angular.isDefined(this.activeDashboardRequest);
         }

         setSkipDashboardFeedHealth (skip: boolean){
             this.skipDashboardFeedHealth = skip;
         }

         fetchFeeds(tab: string,filter: string,start: number,limit: number, sort: string){
             if(this.activeFeedRequest != null && angular.isDefined(this.activeFeedRequest)){
                 this.activeFeedRequest.reject();
             }
             //Cancel any active dashboard queries as this will supercede them
             if(this.activeDashboardRequest != null && angular.isDefined(this.activeDashboardRequest)){
                 this.skipDashboardFeedHealth = true;
             }

             var canceler = this.$q.defer();

             this.activeFeedRequest = canceler;

             var params = {start: start, limit: limit, sort: sort, filter:filter, fixedFilter:tab};

             var initDashboard = (responseData:any) => {
                 this.feedsSearchResult = responseData;
                 if(responseData && responseData.data) {
                     this.setupFeedHealth(responseData.data);
                     //reset this.dashboard.feeds.data ?
                     this.totalFeeds = responseData.recordsFiltered;
                 }
                 this.activeFeedRequest = null;
                 this.skipDashboardFeedHealth = false;
                 this.BroadcastService.notify(this.DASHBOARD_UPDATED, this.dashboard);
             }
             var successFn = (response: any) =>{
                 return response.data;
             }
             var errorFn =  (err: any)=> {
                 canceler.reject();
                 canceler = null;
                 this.activeFeedRequest = null;
                 this.skipDashboardFeedHealth = false;
             }
             var promise = this.$http.get(this.OpsManagerRestUrlService.DASHBOARD_PAGEABLE_FEEDS_URL,{timeout: canceler.promise,params:params});
             promise.then(successFn, errorFn).then(this.fetchPageableFeedNames.bind(this)).then(initDashboard);
             return promise;
         }

         fetchPageableFeedNames(resolveObj:any){
            var feeds = resolveObj.data;
            return this.fetchFeedNames(resolveObj, feeds);
         }

         fetchFeedNames(resolveObj:any, feeds:any){
             var deferred = this.$q.defer();

             if (feeds.length > 0) {
                 var feedNames = _.map(feeds, (feed:any) => {
                     return feed.feed;
                 });
                 var namesPromise = this.$http.post(this.OpsManagerRestUrlService.FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL, feedNames);
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
         }

         updateFeedHealthQueryParams(tab: string,filter: string,start: number,limit: number, sort: string){
             var params = {start: start, limit: limit, sort: sort, filter:filter, fixedFilter:tab};
             angular.extend(this.feedHealthQueryParams,params);
         }

         fetchDashboard() {
             if(this.activeDashboardRequest != null && angular.isDefined(this.activeDashboardRequest)){
                 this.activeDashboardRequest.reject();
             }
             var canceler = this.$q.defer();
             this.activeDashboardRequest = canceler;

             var initDashboard = (dashboard:any) => {
                 this.dashboard = dashboard;
                 //if the pagable feeds query came after this one it will flip the skip flag.
                 // that should supercede this request
                 if(!this.skipDashboardFeedHealth) {
                     this.feedsSearchResult = dashboard.feeds;
                     if (this.dashboard && this.dashboard.feeds && this.dashboard.feeds.data) {
                         var processedFeeds = this.setupFeedHealth(this.dashboard.feeds.data);
                         this.dashboard.feeds.data = processedFeeds;
                         this.totalFeeds = this.dashboard.feeds.recordsFiltered;
                     }
                 }
                 else {
                    //     console.log('Skip processing dashboard results for the feed since it was superceded');
                 }
                 if(angular.isUndefined(this.dashboard.healthCounts['UNHEALTHY'])) {
                     this.dashboard.healthCounts['UNHEALTHY'] = 0;
                 }
                 if(angular.isUndefined(this.dashboard.healthCounts['HEALTHY'])) {
                     this.dashboard.healthCounts['HEALTHY'] = 0;
                 }

                 this.feedUnhealthyCount = this.dashboard.healthCounts['UNHEALTHY'] || 0;
                 this.feedHealthyCount = this.dashboard.healthCounts['HEALTHY'] || 0;
                 this.activeDashboardRequest = null;
                 this.BroadcastService.notify(this.DASHBOARD_UPDATED, dashboard);
             }
             var successFn = (response:any) => {
                 initDashboard(response.data)
                // fetchFeedNames(response.data, response.this.feeds.data).then(initDashboard);
             }
             var errorFn = (err: any) =>{
                 canceler.reject();
                 canceler = null;
                 this.activeDashboardRequest = null;
                 this.skipDashboardFeedHealth = false;
                 console.error("Dashboard error!!!")
             }
             var params = this.feedHealthQueryParams;
             var promise = this.$http.get(this.OpsManagerRestUrlService.DASHBOARD_URL,{timeout: canceler.promise,params:params});

             promise.then(successFn, errorFn);

             return promise;
         }

}

angular.module(moduleName).service('OpsManagerDashboardService',OpsManagerDashboardService);
