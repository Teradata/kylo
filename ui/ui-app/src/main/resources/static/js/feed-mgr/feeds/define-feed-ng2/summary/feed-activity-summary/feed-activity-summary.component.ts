import {Component, Input, OnDestroy, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {Step} from '../../../../model/feed/feed-step.model';
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedStats} from "../../../../model/feed/feed-stats.model";
import {FEED_ACTIVITY_LINK} from "../../model/feed-link-constants";
import {FeedSideNavService} from "../../services/feed-side-nav.service";

@Component({
    selector: "feed-activity-summary",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/feed-activity-summary/feed-activity-summary.component.scss"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/feed-activity-summary/feed-activity-summary.component.html"
})
export class FeedActivitySummaryComponent extends AbstractLoadFeedComponent  {

    static LOADER = "FeedActivitySummaryComponent.LOADER";

    static LINK_NAME = FEED_ACTIVITY_LINK

    @Input() stateParams: any;

    feedStats:FeedStats = new FeedStats();

    getLinkName(){
        return FeedActivitySummaryComponent.LINK_NAME;
    }

    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService:FeedSideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
    }

    onFeedStatsChange(feedStats:FeedStats){
        this.feedStats = feedStats;
    }



}