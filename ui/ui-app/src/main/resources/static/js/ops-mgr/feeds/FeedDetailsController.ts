import * as angular from 'angular';
import {moduleName} from "./module-name";
import * as _ from "underscore";
import OpsManagerFeedService from "../services/OpsManagerFeedService";
import OpsManagerJobService from "../services/OpsManagerJobService";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";

export class controller implements ng.IComponentController{
loading: any;
deferred: any;
feedName: any;
feedData: any;
feed: any;
refreshIntervalTime: any;
activeRequests: any[];
refreshInterval: any;
feedNames: any[];
    constructor(private $scope: any,
                private $timeout: any,
                private $q: any,
                private $interval: any,
                private $transition$: any,
                private $http: any,
                private OpsManagerFeedService: any,
                private OpsManagerRestUrlService: any,
                private StateService: any,
                private OpsManagerJobService: any,
                private BroadcastService : any){

        this.loading = true;
        this.deferred = $q.defer();
        this.feedName = null;
        this.feedData = {}
        this.feed = OpsManagerFeedService.emptyFeed();
        this.refreshIntervalTime = 5000;

        //Track active requests and be able to cancel them if needed
        this.activeRequests = []

        BroadcastService.subscribe($scope, 'ABANDONED_ALL_JOBS', this.abandonedAllJobs);

        this.feedName =$transition$.params().feedName;
        if(this.feedName != undefined && this.isGuid(this.feedName)){
            //fetch the feed name from the server using the guid
            $http.get(OpsManagerRestUrlService.FEED_NAME_FOR_ID(this.feedName)).then( (response: any)=>{
                this.deferred.resolve(response.data);
            }, (err: any)=> {
               this.deferred.reject(err);
            });
        }
        else {
            this.deferred.resolve(this.feedName);
        }

        $q.when(this.deferred.promise).then((feedNameResponse: any)=>{
            this.feedName = feedNameResponse;
            this.loading = false;
            this.getFeedHealth();
            // getFeedNames();
            this.setRefreshInterval();
        });

            $scope.$on('$destroy', ()=>{
                this.clearRefreshInterval();
                this.abortActiveRequests();
                this.OpsManagerFeedService.stopFetchFeedHealthTimeout();
        });
    }// end of constructor



    isGuid=(str: any)=> {
            if (str[0] === "{")
            {
                str = str.substring(1, str.length - 1);
            }
            //var regexGuid = /^(\{){0,1}[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}(\}){0,1}$/gi;
            var regexGuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
            return regexGuid.test(str);
        }


     getFeedHealth=()=>{
            var canceler = this.$q.defer();
            this.activeRequests.push(canceler);
            var successFn = (response: any)=> {
                if (response.data) {
                    //transform the data for UI
                    this.feedData = response.data;
                    if(this.feedData.feedSummary){
                        angular.extend(this.feed,this.feedData.feedSummary[0]);
                        this.feed.isEmpty = false;
                        if(this.feed.feedHealth && this.feed.feedHealth.feedId ){
                            this.feed.feedId = this.feed.feedHealth.feedId
                        }
                        this.OpsManagerFeedService.decorateFeedSummary(this.feed);

                    }
                    if (this.loading) {
                        this.loading = false;
                    }
                    this.finishedRequest(canceler);
                }
            }
            var errorFn =  (err: any)=> {
            }
            var finallyFn =  ()=> {

            }


            this.$http.get(this.OpsManagerFeedService.SPECIFIC_FEED_HEALTH_URL(this.feedName),{timeout: canceler.promise}).then( successFn, errorFn);
        }

        abortActiveRequests=()=>{
            angular.forEach(this.activeRequests,function(canceler,i){
                canceler.resolve();
            });
            this.activeRequests = [];
        }

        finishedRequest=(canceler: any)=> {
            var index = _.indexOf(this.activeRequests,canceler);
            if(index >=0){
                this.activeRequests.splice(index,1);
            }
            canceler.resolve();
            canceler = null;
        }

        getFeedNames=()=>{

            var successFn =  (response: any)=> {
                if (response.data) {
                   this.feedNames = response.data;
                }
            }
            var errorFn = (err: any)=> {
            }
            var finallyFn = ()=> {

            }
            this.$http.get(this.OpsManagerFeedService.FEED_NAMES_URL).then( successFn, errorFn);
        }


        clearRefreshInterval=()=> {
            if (this.refreshInterval != null) {
                this.$interval.cancel(this.refreshInterval);
                this.refreshInterval = null;
            }
        }

        setRefreshInterval=()=> {
            this.clearRefreshInterval();
            if (this.refreshIntervalTime) {
                this.refreshInterval = this.$interval(this.getFeedHealth, this.refreshIntervalTime);

            }
            this.OpsManagerFeedService.fetchFeedHealth();
        }

       gotoFeedDetails = (ev: any)=>{
            if(this.feed.feedId != undefined) {
                this.StateService.FeedManager().Feed().navigateToFeedDetails(this.feed.feedId);
            }
        }

        onJobAction = (eventName: any,job: any)=> {

            var forceUpdate = false;
            //update status info if feed job matches
            if(this.feedData && this.feedData.feeds && this.feedData.feeds.length >0 && this.feedData.feeds[0].lastOpFeed){
                var thisExecutionId = this.feedData.feeds[0].lastOpFeed.feedExecutionId;
                var thisInstanceId = this.feedData.feeds[0].lastOpFeed.feedInstanceId;
                if(thisExecutionId <= job.executionId && this.feed){
                    this.abortActiveRequests();
                    this.clearRefreshInterval();
                    this.feed.displayStatus = job.displayStatus =='STARTED' || job.displayStatus == 'STARTING' ? 'RUNNING' : job.displayStatus;
                    this.feed.timeSinceEndTime = job.timeSinceEndTime;
                    if(this.feed.displayStatus == 'RUNNING'){
                        this.feed.timeSinceEndTime = job.runTime;
                    }
                    if(eventName == 'restartJob'){
                        this.feed.timeSinceEndTime =0;
                    }
                    this.feedData.feeds[0].lastOpFeed.feedExecutionId = job.executionId;
                    this.feedData.feeds[0].lastOpFeed.feedInstanceId = job.instanceId;
                    if(eventName == 'updateEnd'){
                        this.setRefreshInterval();
                    }

                }
            }
        }

        changedFeed = (feedName: any)=>{
            this.StateService.OpsManager().Feed().navigateToFeedDetails(feedName);
        }

        abandonedAllJobs=()=> {
            this.getFeedHealth();
        }

}

angular.module(moduleName)
.controller('OpsManagerFeedDetailsController',
['$scope', '$timeout','$q', '$interval','$transition$','$http','OpsManagerFeedService',
'OpsManagerRestUrlService','StateService', 'OpsManagerJobService', 'BroadcastService', controller]);
