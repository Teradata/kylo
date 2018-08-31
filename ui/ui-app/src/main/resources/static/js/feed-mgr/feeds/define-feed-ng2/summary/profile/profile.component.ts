import {Component, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedSideNavService} from "../../shared/feed-side-nav.service";

@Component({
    selector: "define-feed-profile",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/profile/profile.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/profile/profile.component.html"
})
export class ProfileComponent extends AbstractLoadFeedComponent implements OnInit {

    static LOADER = "ProfileComponent.LOADER";

    static LINK_NAME = "Profile"

    @Input() stateParams: any;

    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService:FeedSideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
    }

    getLinkName(){
        return ProfileComponent.LINK_NAME;
    }

    ngOnInit() {
        console.log("on init");
        let feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.initializeFeed(feedId);
    }
}

