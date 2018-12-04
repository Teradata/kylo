import {Component, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {SLA_LINK} from "../../model/feed-link-constants";




@Component({
    selector: "sla",
    styleUrls: ["./feed-sla.component.scss"],
    templateUrl: "./feed-sla.component.html"
})
export class FeedSlaComponent extends AbstractLoadFeedComponent implements OnInit {

    static LINK_NAME = SLA_LINK;

    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService: FeedSideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
    }

    getLinkName() {
        return FeedSlaComponent.LINK_NAME;
    }

    init() {

    }
}

