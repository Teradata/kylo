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
             feedUnhealthyCount:0,
             feedHealthyCount:0,
             dashboard:{},
             feedsSearchResult:{},
             totalFeeds:0,
             activeFeedRequest:null,
             selectFeedHealthTab:function(tab) {
                 BroadcastService.notify(data.TAB_SELECTED,tab);
             },
             feedHealthQueryParams:{fixedFilter:'All',filter:'',start:0,limit:10, sort:''}
         };

         var setupFeedHealth = function(feedData){
             if(feedData) {
                     var processed = [];
                     _.each(feedData, function (feedHealth) {
                         if (data.feedSummaryData[feedHealth.feed]) {
                             angular.extend(data.feedSummaryData[feedHealth.feed], feedHealth);
                         }
                         else {
                             data.feedSummaryData[feedHealth.feed] = feedHealth;
                         }
                         if (feedHealth.lastUnhealthyTime) {
                             feedHealth.sinceTimeString = new moment(feedHealth.lastUnhealthyTime).fromNow();
                         }

                         OpsManagerFeedService.decorateFeedSummary(feedHealth);
                         if(feedHealth.stream == true && feedHealth.feedHealth){
                             feedHealth.runningCount = feedHealth.feedHealth.runningCount;
                             if(feedHealth.runningCount == null){
                                 feedHealth.runningCount =0;
                             }
                         }

                         if(feedHealth.running){
                             feedHealth.timeSinceEndTime = feedHealth.runTime;
                             feedHealth.runTimeString = '--';
                         }
                          processed.push(feedHealth.feed);
                     });
                     var keysToRemove=_.difference(Object.keys(data.feedSummaryData),processed);
                     if(keysToRemove != null && keysToRemove.length >0){
                         _.each(keysToRemove,function(key){
                             delete  data.feedSummaryData[key];
                         })
                     }
                 }

         };

         data.isFetchingFeedHealth = function(){
             return data.activeFeedRequest != null && angular.isDefined(data.activeFeedRequest);
         }


         data.fetchFeeds = function(tab,filter,start,limit, sort){
             if(data.activeFeedRequest != null && angular.isDefined(data.activeFeedRequest)){
                 data.activeFeedRequest.resolve();
             }
             var canceler = $q.defer();
             data.activeFeedRequest = canceler;

             var params = {start: start, limit: limit, sort: sort, filter:filter, fixedFilter:tab};

             var successFn = function (response) {
                 data.feedsSearchResult = response.data;
                 if(response.data && response.data.data) {
                     setupFeedHealth(response.data.data);
                     data.totalFeeds = response.data.recordsFiltered;
                 }
                 data.activeFeedRequest = null;
             }
             var errorFn = function (err) {
                 canceler.resolve();
                 canceler = null;
                 data.activeFeedRequest = null;
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
             var successFn = function (response) {
                 data.dashboard = response.data;
                 data.feedsSearchResult = response.data.feeds;
                 if(data.dashboard && data.dashboard.feeds && data.dashboard.feeds.data) {
                     setupFeedHealth(data.dashboard.feeds.data);
                     data.totalFeeds = data.dashboard.feeds.recordsFiltered;
                 }

                 if(angular.isUndefined(data.dashboard.healthCounts['UNHEALTHY'])) {
                     data.dashboard.healthCounts['UNHEALTHY'] = 0;
                 }
                 if(angular.isUndefined(data.dashboard.healthCounts['HEALTHY'])) {
                     data.dashboard.healthCounts['HEALTHY'] = 0;
                 }

                 data.feedUnhealthyCount = data.dashboard.healthCounts['UNHEALTHY'] || 0;
                 data.feedHealthyCount = data.dashboard.healthCounts['HEALTHY'] || 0;

                 BroadcastService.notify(data.DASHBOARD_UPDATED,data.dashboard);

             }
             var errorFn = function (err) {

             }
             var params = data.feedHealthQueryParams;
             var promise = $http.get(OpsManagerRestUrlService.DASHBOARD_URL,{params:params});
             promise.then(successFn, errorFn);
             return promise;
         };

         return data;
     }]);
});