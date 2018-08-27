import {Component, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";

@Component({
    selector: "feed-lineage",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/feed-lineage/feed-lineage.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/feed-lineage/feed-lineage.component.html"
})
export class FeedLineageComponment extends AbstractLoadFeedComponent implements OnInit {

    static LOADER = "FeedLineage.LOADER";

    @Input() stateParams:any;


    constructor(feedLoadingService: FeedLoadingService, stateService: StateService,  defineFeedService:DefineFeedService) {
        super(feedLoadingService, stateService,defineFeedService);
    }

    ngOnInit() {
        console.log("on init");
        let feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.initializeFeed(feedId);
    }
}

