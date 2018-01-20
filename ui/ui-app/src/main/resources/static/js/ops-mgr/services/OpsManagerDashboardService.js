define(['angular','ops-mgr/module-name'], function (angular,moduleName) {
    /**
     * Service to call out to Feed REST.
     *
     */
    angular.module(moduleName).factory('OpsManagerDashboardService', ['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService','BroadcastService','OpsManagerFeedService',
     function ($q, $http, $interval, $timeout, HttpService, IconService, AlertsService, OpsManagerRestUrlService, BroadcastService, OpsManagerFeedService) {
         var data = {
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
             selectFeedHealthTab:function(tab) {
                 BroadcastService.notify(data.TAB_SELECTED,tab);
             },
             feedHealthQueryParams:{fixedFilter:'All',filter:'',start:0,limit:10, sort:''}
         };

         var setupFeedHealth = function(feedsArray){
             var processedFeeds = [];
             if(feedsArray) {
                     var processed = [];
                     var arr = [];
                     _.each(feedsArray, function (feedHealth) {
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
                             feedData.sinceTimeString = new moment(feedData.lastUnhealthyTime).fromNow();
                         }

                         OpsManagerFeedService.decorateFeedSummary(feedData);
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
                         _.each(keysToRemove,function(key){
                             delete  data.feedSummaryData[key];
                         })
                     }
                     data.feedsArray = arr;
                 }
                 return processedFeeds;

         };

         data.isFetchingFeedHealth = function(){
             return data.activeFeedRequest != null && angular.isDefined(data.activeFeedRequest);
         }

         data.isFetchingDashboard = function(){
             return data.activeDashboardRequest != null && angular.isDefined(data.activeDashboardRequest);
         }

         data.setSkipDashboardFeedHealth = function(skip){
             data.skipDashboardFeedHealth = skip;
         }

         data.fetchFeeds = function(tab,filter,start,limit, sort){
             if(data.activeFeedRequest != null && angular.isDefined(data.activeFeedRequest)){
                 data.activeFeedRequest.reject();
             }
             //Cancel any active dashboard queries as this will supercede them
             if(data.activeDashboardRequest != null && angular.isDefined(data.activeDashboardRequest)){
                 data.skipDashboardFeedHealth = true;
             }

             var canceler = $q.defer();

             data.activeFeedRequest = canceler;

             var params = {start: start, limit: limit, sort: sort, filter:filter, fixedFilter:tab};

             var successFn = function (response) {
                 data.feedsSearchResult = response.data;
                 if(response.data && response.data.data) {
                     setupFeedHealth(response.data.data);
                     //reset data.dashboard.feeds.data ?
                     data.totalFeeds = response.data.recordsFiltered;
                 }
                 data.activeFeedRequest = null;
                 data.skipDashboardFeedHealth = false;
             }
             var errorFn = function (err) {
                 canceler.reject();
                 canceler = null;
                 data.activeFeedRequest = null;
                 data.skipDashboardFeedHealth = false;
             }
             var promise = $http.get(OpsManagerRestUrlService.DASHBOARD_PAGEABLE_FEEDS_URL,{timeout: canceler.promise,params:params});
             promise.then(successFn, errorFn);
             return promise;
         }

         data.updateFeedHealthQueryParams = function(tab,filter,start,limit, sort){
             var params = {start: start, limit: limit, sort: sort, filter:filter, fixedFilter:tab};
             angular.extend(data.feedHealthQueryParams,params);
         }

         data.fetchDashboard = function() {
             if(data.activeDashboardRequest != null && angular.isDefined(data.activeDashboardRequest)){
                 data.activeDashboardRequest.reject();
             }
             var canceler = $q.defer();
             data.activeDashboardRequest = canceler;

             var successFn = function (response) {

                 data.dashboard = response.data;
                 //if the pagable feeds query came after this one it will flip the skip flag.
                 // that should supercede this request
                 if(!data.skipDashboardFeedHealth) {
                     data.feedsSearchResult = response.data.feeds;
                     if (data.dashboard && data.dashboard.feeds && data.dashboard.feeds.data) {
                         var processedFeeds = setupFeedHealth(data.dashboard.feeds.data);
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
                 BroadcastService.notify(data.DASHBOARD_UPDATED,data.dashboard);

             }
             var errorFn = function (err) {
                 canceler.reject();
                 canceler = null;
                 data.activeDashboardRequest = null;
                 data.skipDashboardFeedHealth = false;
                 console.error("Dashboard error!!!")
             }
             var params = data.feedHealthQueryParams;
             var promise = $http.get(OpsManagerRestUrlService.DASHBOARD_URL,{timeout: canceler.promise,params:params});
             promise.then(successFn, errorFn);
             return promise;
         };

         return data;
     }]);
});