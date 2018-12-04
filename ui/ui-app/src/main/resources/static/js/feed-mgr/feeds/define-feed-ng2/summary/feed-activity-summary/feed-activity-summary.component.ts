import {Component, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {Step} from '../../../../model/feed/feed-step.model';
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedStats} from "../../../../model/feed/feed-stats.model";
import {FEED_ACTIVITY_LINK} from "../../model/feed-link-constants";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {FeedSummary} from "../../../../model/feed/feed-summary.model";
import {FeedJobActivityComponent} from "./feed-job-activity/feed-job-activity.component";
import {JobsListComponent} from "../../../../../ops-mgr/jobs/jobs-list/jobs-list.component";
import {Feed} from "../../../../model/feed/feed.model";
import {FeedOperationsSummary} from "../../../../model/feed/feed-operations-summary.model";

@Component({
    selector: "feed-activity-summary",
    styleUrls: ["./feed-activity-summary.component.scss"],
    templateUrl: "./feed-activity-summary.component.html"
})
export class FeedActivitySummaryComponent extends AbstractLoadFeedComponent  {

    static LOADER = "FeedActivitySummaryComponent.LOADER";

    static LINK_NAME = FEED_ACTIVITY_LINK

    @Input() stateParams: any;

    feedStats:FeedStats = new FeedStats();

    @ViewChild("feedJobActivity")
    private feedJobActivity ?:FeedJobActivityComponent;


    @ViewChild("jobsList")
    private jobsList ?:JobsListComponent;

    getLinkName(){
        return FeedActivitySummaryComponent.LINK_NAME;
    }

    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService:FeedSideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
    }

    onFeedStatsChange(feedStats:FeedStats){
        this.feedStats = feedStats;
    }

    onFeedChange(feed:Feed){
        this.feed = feed;
    }



    onFeedHealthRefreshed(feedSummary:FeedOperationsSummary){
        if(this.feedJobActivity){
            this.feedJobActivity.updateCharts();
        }

        if(this.jobsList){
            this.jobsList.loadJobs(true)
        }
    }


}