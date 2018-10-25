import {Component, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {PROFILE_LINK} from "../../model/feed-link-constants";

@Component({
    selector: "define-feed-profile",
    styleUrls: ["./profile.component.scss"],
    templateUrl: "./profile.component.html"
})
export class ProfileComponent extends AbstractLoadFeedComponent implements OnInit {

    static LOADER = "ProfileComponent.LOADER";

    static LINK_NAME = PROFILE_LINK;

    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService:FeedSideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
    }

    getLinkName(){
        return ProfileComponent.LINK_NAME;
    }

    init() {

    }
}

