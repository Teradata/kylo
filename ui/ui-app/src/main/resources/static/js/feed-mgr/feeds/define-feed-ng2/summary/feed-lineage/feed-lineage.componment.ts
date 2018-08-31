import {Component, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedSideNavService} from "../../shared/feed-side-nav.service";

@Component({
    selector: "feed-lineage",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/feed-lineage/feed-lineage.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/feed-lineage/feed-lineage.component.html"
})
export class FeedLineageComponment extends AbstractLoadFeedComponent implements OnInit {

    static LOADER = "FeedLineage.LOADER";

    static LINK_NAME = "Lineage"

    @Input() stateParams:any;


    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService:FeedSideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
    }

    getLinkName(){
        return FeedLineageComponment.LINK_NAME;
    }

    ngOnInit() {
        let feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.initializeFeed(feedId);
    }
}

