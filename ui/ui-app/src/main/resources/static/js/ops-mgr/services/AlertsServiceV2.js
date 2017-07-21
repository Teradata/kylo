define(['angular','ops-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory('AlertsService', ["$q","$http","$interval","OpsManagerRestUrlService", function ($q,$http,$interval, OpsManagerRestUrlService) {

        /**
         * Flag to indicate the alerts have been loaded at least once
         * @type {boolean}
         */
        var loadedGlobalAlertsSummary = false;

        var alertSummaryRefreshTimeMillis = 5000;

        /**
         * ref to the refresh interval so we can cancel it
         * @type {null}
         */
        var alertsSummaryIntervalObject = null;

        var transformAlertSummaryResponse = function(alertSummaries){
            _.each(alertSummaries,function(summary){
                summary.since =  new moment(summary.lastAlertTimestamp).fromNow()
            });
        }

        var data =
        {
            alertsSummary:{
                lastRefreshTime:'',
                data:[]
            },
            isLoadingAlertsSummary:function(){
                return loadedGlobalAlertsSummary;
            },
            fetchGlobalAlertsSummary:function () {
                var deferred = $q.defer();
                $q.when(data.fetchAlertsSummary()).then(function(responseData) {
                    data.alertsSummary.data = responseData;
                    data.alertsSummary.lastRefreshTime =new Date().getTime();
                    loadedGlobalAlertsSummary = true;
                    deferred.resolve(self.alertsSummary);
                }, function (err) {
                    data.alertsSummary.lastRefreshTime = new Date().getTime();
                    deferred.reject(err)
                });
                return deferred.promise;
            },
            fetchFeedAlerts:function(feedName, feedId) {
                var deferred = $q.defer();
                $http.get(OpsManagerRestUrlService.FEED_ALERTS_URL(feedName),{params:{"feedId":feedId}}).then(function (response) {
                    transformAlertSummaryResponse(response.data)
                    deferred.resolve(response.data);
                }, function(err){
                    deferred.reject(err)
                });
                return deferred.promise;
            },
            /**
             * Fetch alerts for a type and subtype
             * @param type
             * @param subtype
             */
            fetchAlertsSummary:function (type,subtype) {
                var deferred = $q.defer();
                $http.get(OpsManagerRestUrlService.ALERTS_SUMMARY_UNHANDLED,{params:{"type":type,"subtype":subtype}}).then(function (response) {
                    transformAlertSummaryResponse(response.data);
                    deferred.resolve(response.data);
               }, function(err){
                   deferred.reject(err)
               });
                return deferred.promise;
            },
            stopRefreshingAlerts:function(){
                if (alertsSummaryIntervalObject != null) {
                    $interval.cancel(alertsSummaryIntervalObject);
                    alertsSummaryIntervalObject = null;
                }
            },
            startRefreshingAlerts:function(interval){
                if(alertsSummaryIntervalObject == null) {
                    if (interval == null) {
                        interval = alertSummaryRefreshTimeMillis;
                    }
                    data.stopRefreshingAlerts();
                    data.fetchGlobalAlertsSummary();
                    alertsSummaryIntervalObject = $interval(data.fetchGlobalAlertsSummary, interval);
                }
            }

        };
        return data;




    }]);
});



