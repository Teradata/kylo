import * as _ from "underscore";
import "pascalprecht.translate";
import { OpsManagerFeedService } from "../services/OpsManagerFeedService";
import OpsManagerJobService from "../services/OpsManagerJobService";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";
import { StateService } from '@uirouter/core';
import BroadcastService from "../../services/broadcast-service";
import { HttpClient } from "@angular/common/http";
import { OnDestroy, OnInit, Component, Inject } from "@angular/core";
import { Subscription } from "rxjs";

@Component({
    selector: 'ops-mgr-feed-details',
    templateUrl: "js/ops-mgr/feeds/feed-details.html",
    styles: [`.listItemTitle { padding: 0px }`]
})
export class FeedDetailsComponent implements OnDestroy, OnInit {
    loading: boolean = true;
    feedName: string = null;
    feedData: any = {};
    feed: any = {};
    refreshIntervalTime: number = 5000;
    activeRequests: Subscription[] = [];
    refreshInterval: number;
    feedNames: string[];

    ngOnInit() {
        this.feed = this.opsManagerFeedService.emptyFeed();
        this.broadcastService.subscribe(null, 'ABANDONED_ALL_JOBS', this.abandonedAllJobs);
        this.feedName = this.stateService.params.feedName;
        new Promise((resolve, reject) => {
            if (this.feedName != undefined && this.isGuid(this.feedName)) {
                //fetch the feed name from the server using the guid
                this.http.get(this.opsManagerRestUrlService.FEED_NAME_FOR_ID(this.feedName)).toPromise().then((response: any) => {
                    resolve(response);
                }, (err: any) => {
                    reject(err);
                });
            }
            else {
                resolve(this.feedName);
            }
        }).then((feedNameResponse: any) => {
            this.feedName = feedNameResponse;
            this.loading = false;
            this.getFeedHealth();
            this.setRefreshInterval();
        })
    }
    ngOnDestroy() {
        this.clearRefreshInterval();
        this.abortActiveRequests();
        this.opsManagerFeedService.stopFetchFeedHealthTimeout();
    }
    constructor(private opsManagerFeedService: OpsManagerFeedService,
        private opsManagerRestUrlService: OpsManagerRestUrlService,
        private stateService: StateService,
        private opsManagerJobService: OpsManagerJobService,
        @Inject("BroadcastService") private broadcastService: BroadcastService,
        private http: HttpClient) {

    }// end of constructor
    isGuid(str: any) {
        if (str[0] === "{") {
            str = str.substring(1, str.length - 1);
        }
        //var regexGuid = /^(\{){0,1}[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}(\}){0,1}$/gi;
        var regexGuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
        return regexGuid.test(str);
    }
    getFeedHealth() {
        var successFn = (response: any, feedHealthSubscription : Subscription) => {
            if (response) {
                //transform the data for UI
                this.feedData = response;
                if (this.feedData.feedSummary) {
                    this.feed = _.extend(this.feed, this.feedData.feedSummary[0]);//@TODO Ahmad Hassan
                    this.feed.isEmpty = false;
                    if (this.feed.feedHealth && this.feed.feedHealth.feedId) {
                        this.feed.feedId = this.feed.feedHealth.feedId
                    }
                    this.opsManagerFeedService.decorateFeedSummary(this.feed);

                }
                if (this.loading) {
                    this.loading = false;
                }
                this.finishedRequest(feedHealthSubscription);
            }
        }
        var errorFn = (err: any) => {
        }
        var finallyFn = () => {

        }
        var feedHealthObservable = this.http.get(this.opsManagerRestUrlService.SPECIFIC_FEED_HEALTH_URL(this.feedName)
                        /*, { timeout: canceler.promise } //@TODO Ahmad Hassan*/);
        var feedHealthSubscription = feedHealthObservable.subscribe((response: any) => {
            successFn(response,feedHealthSubscription);
        }, (err: any) => {
            errorFn(err);
        });
        this.activeRequests.push(feedHealthSubscription);
    }
    abortActiveRequests() {
        Object.keys(this.activeRequests).forEach((key: any) => {
            this.activeRequests[key].unsubscribe();//@TODO Ahmad Hassan
        });
        this.activeRequests = [];
    }
    finishedRequest(canceler: Subscription) {
        var index = _.indexOf(this.activeRequests, canceler);
        if (index >= 0) {
            this.activeRequests.splice(index, 1);
        }
        canceler.unsubscribe();
        canceler = null;
    }
    getFeedNames() {
        var successFn = (response: any) => {
            if (response) {
                this.feedNames = response;
            }
        }
        var errorFn = (err: any) => {
        }
        var finallyFn = () => {

        }
        this.http.get(this.opsManagerRestUrlService.FEED_NAMES_URL).toPromise().then(successFn, errorFn);
    }
    clearRefreshInterval() {
        if (this.refreshInterval != null) {
            clearInterval(this.refreshInterval);
            this.refreshInterval = null;
        }
    }
    setRefreshInterval() {
        this.clearRefreshInterval();
        if (this.refreshIntervalTime) {
            this.refreshInterval = setInterval(() => { this.getFeedHealth() }, this.refreshIntervalTime);
        }
        this.opsManagerFeedService.fetchFeedHealth();
    }
    gotoFeedDetails(ev: any) {
        if (this.feed.feedId != undefined) {
            this.stateService.go('feed-details', { feedId: this.feed.feedId, tabIndex: 0 });
        }
    }
    onJobAction(event: any) {
        var eventName = event.action;
        var job = event.job;
        var forceUpdate = false;
        //update status info if feed job matches
        if (this.feedData && this.feedData.feeds && this.feedData.feeds.length > 0 && this.feedData.feeds[0].lastOpFeed) {
            var thisExecutionId = this.feedData.feeds[0].lastOpFeed.feedExecutionId;
            var thisInstanceId = this.feedData.feeds[0].lastOpFeed.feedInstanceId;
            if (thisExecutionId <= job.executionId && this.feed) {
                this.abortActiveRequests();
                this.clearRefreshInterval();
                this.feed.displayStatus = job.displayStatus == 'STARTED' || job.displayStatus == 'STARTING' ? 'RUNNING' : job.displayStatus;
                this.feed.timeSinceEndTime = job.timeSinceEndTime;
                if (this.feed.displayStatus == 'RUNNING') {
                    this.feed.timeSinceEndTime = job.runTime;
                }
                if (eventName == 'restartJob') {
                    this.feed.timeSinceEndTime = 0;
                }
                this.feedData.feeds[0].lastOpFeed.feedExecutionId = job.executionId;
                this.feedData.feeds[0].lastOpFeed.feedInstanceId = job.instanceId;
                if (eventName == 'updateEnd') {
                    this.setRefreshInterval();
                }
            }
        }
    }
    changedFeed(name: string) {
        // this.stateService.OpsManager().Feed().navigateToFeedDetails(feedName);
        this.stateService.go('ops-feed-details', { feedName: name });
    }
    abandonedAllJobs() {
        this.getFeedHealth();
    }
}