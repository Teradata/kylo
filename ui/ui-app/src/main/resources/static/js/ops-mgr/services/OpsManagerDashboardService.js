define(['angular','ops-mgr/module-name'], function (angular,moduleName) {
    /**
     * Service to call out to Feed REST.
     *
     */
    angular.module(moduleName).factory('OpsManagerDashboardService', ['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService','BroadcastService',
     function ($q, $http, $interval, $timeout, HttpService, IconService, AlertsService, OpsManagerRestUrlService, BroadcastService) {
         var data = {
             DASHBOARD_UPDATED:'DASHBOARD_UPDATED',
             FEED_SUMMARY_UPDATED:'FEED_SUMMARY_UPDATED',
             TAB_SELECTED:'TAB_SELECTED',
             feedSummaryData:{},
             feedUnhealthyCount:0,
             feedHealthyCount:0,
             dashboard:{},
             selectFeedHealthTab:function(tab) {
                 BroadcastService.notify(data.TAB_SELECTED,tab);
             }
         };

         var setupFeedHealth = function(){
             if(data.dashboard.feedStatus) {
                 if(data.dashboard.feedStatus.feeds) {
                     var processed = [];
                     _.each(data.dashboard.feedStatus.feedSummary, function (feedHealth) {
                         if (data.feedSummaryData[feedHealth.feed]) {
                             angular.extend(data.feedSummaryData[feedHealth.feed], feedHealth);
                         }
                         else {
                             data.feedSummaryData[feedHealth.feed] = feedHealth;
                         }
                         if (feedHealth.lastUnhealthyTime) {
                             feedHealth.sinceTimeString = new moment(feedHealth.lastUnhealthyTime).fromNow();
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
                 data.feedUnhealthyCount = data.dashboard.feedStatus.failedCount;
                 data.feedHealthyCount = data.dashboard.feedStatus.healthyCount;
             }
         };

         data.fetchDashboard = function() {
             var successFn = function (response) {
                 data.dashboard = response.data;
                 setupFeedHealth();
                 BroadcastService.notify(data.DASHBOARD_UPDATED,data.dashboard);

             }
             var errorFn = function (err) {

             }
             var promise = $http.get(OpsManagerRestUrlService.DASHBOARD_URL);
             promise.then(successFn, errorFn);
             return promise;
         };

         return data;
     }]);
});