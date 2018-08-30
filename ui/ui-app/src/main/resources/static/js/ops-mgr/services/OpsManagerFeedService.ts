import * as moment from "moment";
import * as _ from "underscore";
import IconService from "./IconStatusService";
import OpsManagerRestUrlService from "./OpsManagerRestUrlService";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";

@Injectable()
export class OpsManagerFeedService {
    FEED_HEALTH_URL: string = this.opsManagerRestUrlService.FEED_HEALTH_URL;
    FEED_HEALTH_COUNT_URL: string =  this.opsManagerRestUrlService.FEED_HEALTH_COUNT_URL;
    FETCH_FEED_HEALTH_INTERVAL: number = 5000;
    fetchFeedHealthInterval: any = null;
    feedHealth: any = {};
    feedSummaryData: any = {};
    feedUnhealthyCount: number = 0;
    feedHealthyCount: number = 0;
     constructor(
                private iconService: IconService,
                private opsManagerRestUrlService: OpsManagerRestUrlService,
                private http : HttpClient){
         
    }
    emptyFeed () {
        var feed: any = {};
        feed.displayStatus = 'LOADING';
        feed.lastStatus = 'LOADING',
            feed.timeSinceEndTime = 0;
        feed.isEmpty = true;
        return feed;
    }

    decorateFeedSummary (feed: any) {
        if (feed.isEmpty == undefined) {
            feed.isEmpty = false;
        }
        var health: string = "---";
        if (!feed.isEmpty) {
            health = feed.healthy ? 'HEALTHY' : 'UNHEALTHY';
            var iconData: any = this.iconService.iconForFeedHealth(health);
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
}