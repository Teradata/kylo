
/*
 * Copyright (c) 2015.
 */

(function () {

    var controller = function($scope, $timeout,$q, $interval,$stateParams, $http, FeedData, StateService, JobData, BroadcastService ){
        var self = this;
        self.feedName = $stateParams.feedName;
        self.feedData = {}
        self.feed = FeedData.emptyFeed();
        self.feedNames = [];
        self.refreshIntervalTime = 5000;

        //Track active requests and be able to cancel them if needed
        this.activeRequests = []

        BroadcastService.subscribe($scope, 'ABANDONED_ALL_JOBS', abandonedAllJobs);

        getFeedHealth();
        getFeedNames();
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
                        FeedData.decorateFeedSummary(self.feed);

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


            $http.get(FeedData.SPECIFIC_FEED_HEALTH_URL(self.feedName),{timeout: canceler.promise}).then( successFn, errorFn);
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
            $http.get(FeedData.FEED_NAMES_URL).then( successFn, errorFn);
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
            StateService.navigateToFeedDetails(feedName);
        }

        $scope.$on('$destroy', function(){
            clearRefreshInterval();
            abortActiveRequests();
        });

        function abandonedAllJobs() {
            getFeedHealth();
        }



    };

    angular.module(MODULE_OPERATIONS).controller('FeedDetailsController',['$scope', '$timeout','$q', '$interval','$stateParams','$http','FeedData','StateService', 'JobData', 'BroadcastService', controller]);



}());


