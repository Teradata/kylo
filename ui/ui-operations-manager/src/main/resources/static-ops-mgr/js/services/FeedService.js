/*-
 * #%L
 * thinkbig-ui-operations-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/**
 * Service to call out to Feed REST.
 *
 */
var FeedService = angular.module(MODULE_OPERATIONS).factory('FeedData', ['$q', '$http', '$interval', '$timeout','HttpService','IconService','AlertsService','RestUrlService', function ($q, $http, $interval,$timeout,HttpService, IconService, AlertsService, RestUrlService) {
    var FeedData = {};
    FeedData.FEED_HEALTH_URL = RestUrlService.FEED_HEALTH_URL;
    FeedData.FEED_NAMES_URL = RestUrlService.FEED_NAMES_URL;
    FeedData.FEED_HEALTH_COUNT_URL = RestUrlService.FEED_HEALTH_COUNT_URL;
    FeedData.FETCH_FEED_HEALTH_INTERVAL = 5000;
    FeedData.fetchFeedHealthInterval = null;
    FeedData.feedHealth = {};


    FeedData.SPECIFIC_FEED_HEALTH_COUNT_URL = RestUrlService.SPECIFIC_FEED_HEALTH_COUNT_URL;

    FeedData.SPECIFIC_FEED_HEALTH_URL = RestUrlService.SPECIFIC_FEED_HEALTH_URL;

    FeedData.DAILY_STATUS_COUNT_URL = RestUrlService.FEED_DAILY_STATUS_COUNT_URL;


    FeedData.feedSummaryData ={};
    FeedData.feedUnhealthyCount = 0;
    FeedData.feedHealthyCount = 0;

    FeedData.feedHealth = 0;

    FeedData.emptyFeed = function() {
        var feed = {};
        feed.displayStatus = 'LOADING';
        feed.lastStatus = 'LOADING',
        feed.timeSinceEndTime = 0;
        feed.isEmpty = true;
        return feed;
    }

    FeedData.decorateFeedSummary = function(feed) {
        //GROUP FOR FAILED

        if (feed.isEmpty == undefined) {
            feed.isEmpty = false;
        }

        var health = "---";
        if (!feed.isEmpty) {
            health = feed.healthy ? 'HEALTHY' : 'UNHEALTHY';
            var iconData = IconService.iconForFeedHealth(health);
            feed.icon = iconData.icon
            feed.iconstyle = iconData.style
        }
        feed.healthText = health;
        if(feed.running){
            feed.displayStatus = 'RUNNING';
        }
        else if ("FAILED" == feed.lastStatus || ( "FAILED" == feed.lastExitCode && "ABANDONED" != feed.lastStatus)) {
            feed.displayStatus = 'FAILED';
        }
        else if ("COMPLETED" == feed.lastExitCode) {
            feed.displayStatus = 'COMPLETED';
        }
        else {
            feed.displayStatus = feed.lastStatus;
        }

        feed.statusStyle = IconService.iconStyleForJobStatus(feed.displayStatus);
    }


    FeedData.fetchFeedSummaryData = function() {
        var successFn = function(response) {
            FeedData.feedSummaryData = response.data;
            if(response.data){
                FeedData.feedUnhealthyCount = response.data.failedCount;
                FeedData.feedHealthyCount = response.data.healthyCount;
            }
        }
        var errorFn = function(err){

        }
        var finallyFn = function() {

        }

        var promise =  $http.get(FeedData.FEED_HEALTH_URL);
        promise.then(successFn,errorFn);//.success(successFn).error(errorFn).finally(finallyFn);
        return promise;
    };

    FeedData.fetchFeedHealth = function() {
        var successFn = function(response) {


            var unhealthyFeedNames = [];
            if(response.data){
                angular.forEach(response.data,function(feedHealth) {
                    if(FeedData.feedHealth[feedHealth.feed]){
                        angular.extend(FeedData.feedHealth[feedHealth.feed],feedHealth);
                    }
                    else {
                        FeedData.feedHealth[feedHealth.feed] = feedHealth;
                    }
                    if(feedHealth.lastUnhealthyTime){
                        feedHealth.sinceTimeString = new moment(feedHealth.lastUnhealthyTime).fromNow();
                    }
                    if(feedHealth.healthy){
                        AlertsService.removeFeedFailureAlertByName(feedHealth.feed);
                    }
                    else {
                        unhealthyFeedNames.push(feedHealth.feed);
                        AlertsService.addFeedHealthFailureAlert(feedHealth);
                    }
                });
                //only unhealthy will come back
                //if feedName is not in the response list, but currently failed.. remove it
                var failedFeeds = AlertsService.feedFailureAlerts;
                angular.forEach(failedFeeds,function(alert,feedName) {
                    if(_.indexOf(unhealthyFeedNames,feedName) == -1){
                        AlertsService.removeFeedFailureAlertByName(feedName);
                    }
                });


            }
            FeedData.fetchFeedHealthTimeout();
        }
        var errorFn = function(err){
            FeedData.fetchFeedHealthTimeout();
        }
        var finallyFn = function() {

        }

        var promise =  $http.get(FeedData.FEED_HEALTH_COUNT_URL);
        promise.then(successFn,errorFn);//.success(successFn).error(errorFn).finally(finallyFn);
        return promise;
    };



    FeedData.startFetchFeedHealth = function(){
        if(FeedData.fetchFeedHealthInterval == null){
            FeedData.fetchFeedHealth();

            FeedData.fetchFeedHealthInterval =  $interval(function(){
                FeedData.fetchFeedHealth();
            },FeedData.FETCH_FEED_HEALTH_INTERVAL)
        }
    }

    FeedData.fetchFeedHealthTimeout = function(){
        if(FeedData.fetchFeedHealthInterval != null) {
            $timeout.cancel(FeedData.fetchFeedHealthInterval);
        }

            FeedData.fetchFeedHealthInterval =  $timeout(function(){
                FeedData.fetchFeedHealth();
            },FeedData.FETCH_FEED_HEALTH_INTERVAL);
        }

    return FeedData;
}]);
