import * as moment from "moment";
import * as _ from "underscore";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import {OperationsRestUrlConstants} from "../../services/operations-rest-url-constants";
import {OpsManagerFeedUtil} from "./ops-manager-feed-util";
import {ReplaySubject} from "rxjs/ReplaySubject";
import {MatSnackBar} from "@angular/material/snack-bar";
import {FeedSummary} from "../../feed-mgr/model/feed/feed-summary.model";
import {Observable} from "rxjs/Observable";
import {RestUrlConstants} from "../../feed-mgr/services/RestUrlConstants";
import {RestResponseStatus} from "../../../lib/common/common.model";
import {FeedOperationsSummary} from "../../feed-mgr/model/feed/feed-operations-summary.model";

@Injectable()
export class OpsManagerFeedService {
    FEED_HEALTH_URL: string = OperationsRestUrlConstants.FEED_HEALTH_URL;
    FEED_HEALTH_COUNT_URL: string =  OperationsRestUrlConstants.FEED_HEALTH_COUNT_URL;

    DAILY_STATUS_COUNT_URL:string = OperationsRestUrlConstants.DAILY_STATUS_COUNT_URL

    FEED_DAILY_STATUS_COUNT_URL = OperationsRestUrlConstants.FEED_DAILY_STATUS_COUNT_URL

    FETCH_FEED_HEALTH_INTERVAL: number = 5000;
    /***
     * the refresh interval to fetch for feed health data
     * @type {null}
     */
    fetchFeedHealthInterval: any = null;
    /**
     * The map of health data
     * @type {{}}
     */
    feedHealth: any = {};
    /**
     * the map of feed summary data
     * @type {{}}
     */
    feedSummaryData: any = {};
    /**
     * total unhealthy feeds
     * @type {number}
     */
    feedUnhealthyCount: number = 0;
    /**
     * total healthly feeds
     * @type {number}
     */
    feedHealthyCount: number = 0;

    constructor(
        private http : HttpClient,private snackBar: MatSnackBar){

    }
    emptyFeed () {
        var feed: any = {};
        feed.displayStatus = 'LOADING';
        feed.lastStatus = 'LOADING',
            feed.timeSinceEndTime = 0;
        feed.isEmpty = true;
        return feed;
    }
/**
    decorateFeedSummary (feed: any) {
        if (feed.isEmpty == undefined) {
            feed.isEmpty = false;
        }
        var health: string = "---";
        if (!feed.isEmpty) {
            health = feed.healthy ? 'HEALTHY' : 'UNHEALTHY';
            var iconData: any = IconU.iconForFeedHealth(health);
            feed.icon = iconData.icon
            feed.iconstyle = iconData.style
        }
        feed.healthText = health;
        if (feed.running) {
            feed.displayStatus = 'RUNNING';
        }
        else if ("FAILED" == feed.lastStatus || ( "FAILED" == feed.lastExitCode && "ABANDONED" != feed.lastStatus)) {
            feed.displayStatus = 'FAILED';
        }
        else if ("COMPLETED" == feed.lastExitCode) {
            feed.displayStatus = 'COMPLETED';
        }
        else if ("STOPPED" == feed.lastStatus) {
            feed.displayStatus = 'STOPPED';
        }
        else if ("UNKNOWN" == feed.lastStatus) {
            feed.displayStatus = 'INITIAL';
            feed.sinceTimeString = '--';
            feed.runTimeString = "--"
        }
        else {
            feed.displayStatus = feed.lastStatus;
        }

        feed.statusStyle = this.iconService.iconStyleForJobStatus(feed.displayStatus);
    }
**/
    fetchFeedSummaryData () {
        var successFn =  (response: any)=> {
            this.feedSummaryData = response;
            if (response) {
                this.feedUnhealthyCount = response.failedCount;
                this.feedHealthyCount = response.healthyCount;
            }
        }
        var errorFn =  (err: any)=>{

        }
        var finallyFn =  ()=>{

        }

        var promise = this.http.get(this.FEED_HEALTH_URL).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    };

    fetchFeedHealth () {
        var successFn =  (response: any)=> {

            var unhealthyFeedNames: any[] = [];
            if (response) {
                response.forEach((feedHealth: any)=> {
                    if (this.feedHealth[feedHealth.feed]) {
                        this.feedHealth[feedHealth.feed] = _.extend(this.feedHealth[feedHealth.feed], feedHealth);
                    }
                    else {
                        this.feedHealth[feedHealth.feed] = feedHealth;
                    }
                    if (feedHealth.lastUnhealthyTime) {
                        feedHealth.sinceTimeString =  moment(feedHealth.lastUnhealthyTime).fromNow();
                    }
                    if (feedHealth.healthy) {
                    }
                    else {
                        unhealthyFeedNames.push(feedHealth.feed);
                    }
                });
            }
            this.fetchFeedHealthTimeout();
        }
        var errorFn =  (err: any)=> {
            this.fetchFeedHealthTimeout();
        }
        var finallyFn =  ()=> {

        }

        var promise = this.http.get(this.FEED_HEALTH_COUNT_URL).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    };

    startFetchFeedHealth (){
        if (this.fetchFeedHealthInterval == null) {
            this.fetchFeedHealth();

            this.fetchFeedHealthInterval = setInterval( () =>{
                this.fetchFeedHealth();
            }, this.FETCH_FEED_HEALTH_INTERVAL)
        }
    };

    fetchFeedHealthTimeout () {
        this.stopFetchFeedHealthTimeout();

        this.fetchFeedHealthInterval = setTimeout( () =>{
            this.fetchFeedHealth();
        }, this.FETCH_FEED_HEALTH_INTERVAL);
    }

    stopFetchFeedHealthTimeout() {
        if (this.fetchFeedHealthInterval != null) {
            clearTimeout(this.fetchFeedHealthInterval);
        }
    }

    getFeedHealth(feedName:string) :Observable<FeedOperationsSummary>{
        let subject = new ReplaySubject<FeedOperationsSummary>(1);

        var successFn = (response: any)=> {
            let feedHealth = new FeedOperationsSummary({});
            if (response) {
                //transform the data for UI
                if (response.feedSummary && response.feedSummary.length && response.feedSummary.length >0) {
                     feedHealth = response.feedSummary[0];
                    OpsManagerFeedUtil.decorateFeedSummary(feedHealth);
                    feedHealth = feedHealth
                }
            }
            subject.next(feedHealth)
        }
        var errorFn =  (err: any)=> {
            subject.error(err)
        }
        this.http.get(OperationsRestUrlConstants.SPECIFIC_FEED_HEALTH_URL(feedName)).subscribe( successFn, errorFn);
        return subject.asObservable();
    }

    public openSnackBar(message:string, duration?:number){
        if(duration == undefined){
            duration = 3000;
        }
        this.snackBar.open(message, null, {
            duration: duration,
        });
    }


    enableFeed(feedId:string) :Observable<FeedSummary>{
    return <Observable<FeedSummary>>  this.http.post(RestUrlConstants.ENABLE_FEED_URL(feedId),null);

    }

    disableFeed(feedId:string) :Observable<FeedSummary>{
        return <Observable<FeedSummary>>  this.http.post(RestUrlConstants.DISABLE_FEED_URL(feedId),null);

    }

     startFeed(feedId:string) {
         return <Observable<RestResponseStatus>>  this.http.post(RestUrlConstants.START_FEED_URL(feedId), null);
     }

}
