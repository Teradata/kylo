import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import * as moment from "moment";
import {Subscription} from 'rxjs';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/timeout';
import * as _ from 'underscore';

import {ObjectUtils} from '../../../lib/common/utils/object-utils';
import {BroadcastService} from '../../services/broadcast-service';
import {OpsManagerFeedUtil} from "./ops-manager-feed-util";
import {OpsManagerFeedService} from "./ops-manager-feed.service";
import {OpsManagerRestUrlService} from './OpsManagerRestUrlService';

@Injectable()
export class OpsManagerDashboardService {

    DASHBOARD_UPDATED: string = 'DASHBOARD_UPDATED';
    FEED_SUMMARY_UPDATED: string = 'FEED_SUMMARY_UPDATED';
    TAB_SELECTED: string = 'TAB_SELECTED';
    feedSummaryData: any = {};
    feedsArray: any[] = [];
    feedUnhealthyCount: number = 0;
    feedHealthyCount: number = 0;
    dashboard: any = {};
    feedsSearchResult: any = {};
    totalFeeds: number = 0;
    activeFeedRequest: Subscription = null;
    activeDashboardRequest: Subscription = null;
    skipDashboardFeedHealth: boolean = false;
    feedHealthQueryParams: any = { fixedFilter: 'All', filter: '', start: 0, limit: 10, sort: '' };

    constructor(private http: HttpClient,
        private opsManagerRestUrlService: OpsManagerRestUrlService,
        private broadcastService: BroadcastService,
        private OpsManagerFeedService: OpsManagerFeedService) {}

    selectFeedHealthTab(tab: any) {
        this.broadcastService.notify(this.TAB_SELECTED, tab);
    }
    isFetchingFeedHealth() {
        return this.activeFeedRequest != null && ObjectUtils.isDefined(this.activeFeedRequest);
    }

    isFetchingDashboard() {
        return this.activeDashboardRequest != null && ObjectUtils.isDefined(this.activeDashboardRequest);
    }

    setSkipDashboardFeedHealth(skip: any) {
        this.skipDashboardFeedHealth = skip;
    }

    fetchFeeds(tab: any, filter: any, start: any, limit: any, sort: any) {
        if (this.activeFeedRequest != null && ObjectUtils.isDefined(this.activeFeedRequest)) {
            this.activeFeedRequest.unsubscribe();
        }
        //Cancel any active dashboard queries as this will supercede them
        if (this.activeDashboardRequest != null && ObjectUtils.isDefined(this.activeDashboardRequest)) {
            this.skipDashboardFeedHealth = true;
        }
        var initDashboard = (responseData: any) => {
            this.feedsSearchResult = responseData;
            if (responseData) {
                this.setupFeedHealth(responseData);
                //reset this.dashboard.feeds.data ?
                this.totalFeeds = responseData.recordsFiltered;
            }
            this.activeFeedRequest = null;
            this.skipDashboardFeedHealth = false;
            this.broadcastService.notify(this.DASHBOARD_UPDATED, this.dashboard);
        }
        return new Promise((resolve: any, reject: any) => {
            var params = { start: start, limit: limit, sort: sort, filter: filter, fixedFilter: tab };
            var observable = this.http.get(this.opsManagerRestUrlService.DASHBOARD_PAGEABLE_FEEDS_URL, { params: params })/*.timeout(this.activeFeedRequest)*/;
            this.activeFeedRequest = observable.subscribe((response: any) => {
                resolve(response);
            }, (err: any) => {
                reject();
                this.activeFeedRequest = null;
                this.skipDashboardFeedHealth = false;
            });
        })
            .then((response: any) => { this.fetchPageableFeedNames(response) })
            .then((response: any) => { initDashboard(response) });
    }
    updateFeedHealthQueryParams(tab: any, filter: any, start: any, limit: any, sort: any) {
        var params = { start: start, limit: limit, sort: sort, filter: filter, fixedFilter: tab };
        this.feedHealthQueryParams = { ...this.feedHealthQueryParams, ...params };
    }

    fetchDashboard() {
        if (this.activeDashboardRequest != null && ObjectUtils.isDefined(this.activeDashboardRequest)) {
            this.activeDashboardRequest.unsubscribe();
        }
        return new Promise((resolve: any, reject: any) => {
            var params = this.feedHealthQueryParams;
            this.activeDashboardRequest = this.http.get(this.opsManagerRestUrlService.DASHBOARD_URL, { params: params })
                /*.timeout(this.activeDashboardRequest)*/.subscribe((response: any) => {
                    this.fetchFeedNames(response, response.feeds.data)
                        .then((dashboard: any) => {
                            this.initDashboard(dashboard);
                            resolve(response);
                        });
                }, (err: any) => {
                    this.activeDashboardRequest = null;
                    this.skipDashboardFeedHealth = false;
                    reject();
                    console.error("Dashboard error!!!")
                });
        })
    };
    initDashboard(dashboard: any) {
        this.dashboard = dashboard;
        //if the pagable feeds query came after this one it will flip the skip flag.
        // that should supercede this request
        if (!this.skipDashboardFeedHealth) {
            this.feedsSearchResult = dashboard.feeds;
            if (this.dashboard && this.dashboard.feeds && this.dashboard.feeds.data) {
                var processedFeeds = this.setupFeedHealth(this.dashboard.feeds.data);
                this.dashboard.feeds.data = processedFeeds;
                this.totalFeeds = this.dashboard.feeds.recordsFiltered;
            }
        }
        else {
            //     console.log('Skip processing dashboard results for the feed since it was superceded');
        }
        if (ObjectUtils.isUndefined(this.dashboard.healthCounts['UNHEALTHY'])) {
            this.dashboard.healthCounts['UNHEALTHY'] = 0;
        }
        if (ObjectUtils.isUndefined(this.dashboard.healthCounts['HEALTHY'])) {
            this.dashboard.healthCounts['HEALTHY'] = 0;
        }

        this.feedUnhealthyCount = this.dashboard.healthCounts['UNHEALTHY'] || 0;
        this.feedHealthyCount = this.dashboard.healthCounts['HEALTHY'] || 0;
        this.activeDashboardRequest = null;
        this.broadcastService.notify(this.DASHBOARD_UPDATED, dashboard);
    }
    setupFeedHealth(feedsArray: any) {
        var processedFeeds: any[] = [];
        if (feedsArray) {
            var processed: any[] = [];
            var arr: any[] = [];
            _.each(feedsArray, (feedHealth: any) => {
                //pointer to the feed that is used/bound to the ui/service
                var feedData = null;
                if (this.feedSummaryData[feedHealth.feed]) {
                    feedData = this.feedSummaryData[feedHealth.feed]
                    feedData = { ...feedData, ...feedHealth };
                    feedHealth = feedData;
                }
                else {
                    this.feedSummaryData[feedHealth.feed] = feedHealth;
                    feedData = feedHealth;
                }
                arr.push(feedData);

                processedFeeds.push(feedData);
                if (feedData.lastUnhealthyTime) {
                    feedData.sinceTimeString = moment(feedData.lastUnhealthyTime).fromNow();
                }

                OpsManagerFeedUtil.decorateFeedSummary(feedData);
                if (feedData.stream == true && feedData.feedHealth) {
                    feedData.runningCount = feedData.feedHealth.runningCount;
                    if (feedData.runningCount == null) {
                        feedData.runningCount = 0;
                    }
                }

                if (feedData.running) {
                    feedData.timeSinceEndTime = feedData.runTime;
                    feedData.runTimeString = '--';
                }
                processed.push(feedData.feed);
            });
            var keysToRemove = _.difference(Object.keys(this.feedSummaryData), processed);
            if (keysToRemove != null && keysToRemove.length > 0) {
                _.each(keysToRemove, (key: any) => {
                    delete this.feedSummaryData[key];
                })
            }
            this.feedsArray = arr;
        }
        return processedFeeds;

    };
    fetchPageableFeedNames(resolveObj: any) {
        var feeds = resolveObj;
        return this.fetchFeedNames(resolveObj, feeds);
    };

    fetchFeedNames(resolveObj: any, feeds: any) {
        return new Promise((resolve, reject) => {
            if (feeds.length > 0) {
                var feedNames = _.map(feeds, (feed: any) => {
                    return feed.feed;
                });
                var headers = new HttpHeaders({'Content-Type':'application/json; charset=utf-8'});
                this.http.post(this.opsManagerRestUrlService.FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL, feedNames, {headers : headers})
                    .toPromise().then((result: any) => {
                        _.each(feeds, (feed: any) => {
                            feed.displayName = _.find(result, (systemNameToDisplayName: any) => {
                                return systemNameToDisplayName.key === feed.feed;
                            })
                        });
                        resolve(resolveObj);
                    }, (err: any) => {
                        console.error('Failed to receive feed names', err);
                        resolve(resolveObj);
                    });
            } else {
                resolve(resolveObj);
            }
        })
    };

}
