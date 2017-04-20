define(['angular','ops-mgr/feeds/module-name'], function (angular,moduleName) {

    var controller = function($scope, $timeout,$q, $interval,$transition$, $http, OpsManagerFeedService, StateService, OpsManagerJobService, BroadcastService ){
        var self = this;
        self.feedName = $transition$.params().feedName;
        self.feedData = {}
        self.feed = OpsManagerFeedService.emptyFeed();
        self.refreshIntervalTime = 5000;

        //Track active requests and be able to cancel them if needed
        this.activeRequests = []

        BroadcastService.subscribe($scope, 'ABANDONED_ALL_JOBS', abandonedAllJobs);

        getFeedHealth();
       // getFeedNames();
        setRefreshInterval();
        function getFeedHealth(){
            var canceler = $q.defer();
            self.activeRequests.push(canceler);
            var successFn = function (response) {
                if (response.data) {
                    //transform the data for UI
                    self.feedData = response.data;
                    if(self.feedData.feedSummary){
                        angular.extend(self.feed,self.feedData.feedSummary[0]);
                        self.feed.isEmpty = false;
                        if(self.feed.feedHealth && self.feed.feedHealth.feedId ){
                            self.feed.feedId = self.feed.feedHealth.feedId
                        }
                        OpsManagerFeedService.decorateFeedSummary(self.feed);

                    }
                    if (self.loading) {
                        self.loading = false;
                    }
                    finishedRequest(canceler);
                }
            }
            var errorFn = function (err) {
            }
            var finallyFn = function () {

            }


            $http.get(OpsManagerFeedService.SPECIFIC_FEED_HEALTH_URL(self.feedName),{timeout: canceler.promise}).then( successFn, errorFn);
        }

        function abortActiveRequests(){
            angular.forEach(self.activeRequests,function(canceler,i){
                canceler.resolve();
            });
            self.activeRequests = [];
        }

        function finishedRequest(canceler) {
            var index = _.indexOf(self.activeRequests,canceler);
            if(index >=0){
                self.activeRequests.splice(index,1);
            }
            canceler.resolve();
            canceler = null;
        }


        function getFeedNames(){

            var successFn = function (response) {
                if (response.data) {
                   self.feedNames = response.data;
                }
            }
            var errorFn = function (err) {
            }
            var finallyFn = function () {

            }
            $http.get(OpsManagerFeedService.FEED_NAMES_URL).then( successFn, errorFn);
        }




        function clearRefreshInterval() {
            if (self.refreshInterval != null) {
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
            }
        }

        function setRefreshInterval() {
            clearRefreshInterval();
            if (self.refreshIntervalTime) {
                self.refreshInterval = $interval(getFeedHealth, self.refreshIntervalTime);

            }
            OpsManagerFeedService.fetchFeedHealth();
        }

        this.gotoFeedDetails = function(ev){
            if(self.feed.feedId != undefined) {
                StateService.FeedManager().Feed().navigateToFeedDetails(self.feed.feedId);
            }
        }

        this.onJobAction = function(eventName,job) {

            var forceUpdate = false;
            //update status info if feed job matches
            if(self.feedData && self.feedData.feeds && self.feedData.feeds.length >0 && self.feedData.feeds[0].lastOpFeed){
                var thisExecutionId = self.feedData.feeds[0].lastOpFeed.feedExecutionId;
                var thisInstanceId = self.feedData.feeds[0].lastOpFeed.feedInstanceId;
                if(thisExecutionId <= job.executionId && self.feed){
                    abortActiveRequests();
                    clearRefreshInterval();
                    self.feed.displayStatus = job.displayStatus =='STARTED' || job.displayStatus == 'STARTING' ? 'RUNNING' : job.displayStatus;
                    self.feed.timeSinceEndTime = job.timeSinceEndTime;
                    if(self.feed.displayStatus == 'RUNNING'){
                        self.feed.timeSinceEndTime = job.runTime;
                    }
                    if(eventName == 'restartJob'){
                        self.feed.timeSinceEndTime =0;
                    }
                    self.feedData.feeds[0].lastOpFeed.feedExecutionId = job.executionId;
                    self.feedData.feeds[0].lastOpFeed.feedInstanceId = job.instanceId;
                    if(eventName == 'updateEnd'){
                        setRefreshInterval();
                    }

                }
            }
        }

        this.changedFeed = function(feedName){
            StateService.OpsManager().Feed().navigateToFeedDetails(feedName);
        }

        $scope.$on('$destroy', function(){
            clearRefreshInterval();
            abortActiveRequests();
            OpsManagerFeedService.stopFetchFeedHealthTimeout();
        });

        function abandonedAllJobs() {
            getFeedHealth();
        }



    };

    angular.module(moduleName).controller('OpsManagerFeedDetailsController',['$scope', '$timeout','$q', '$interval','$transition$','$http','OpsManagerFeedService','StateService', 'OpsManagerJobService', 'BroadcastService', controller]);



});


