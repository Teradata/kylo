import {Component, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";

@Component({
    selector: "define-feed-profile",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/profile/profile.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/profile/profile.component.html"
})
export class ProfileComponent extends AbstractLoadFeedComponent implements OnInit {

    static LOADER = "ProfileComponent.LOADER";

    @Input() stateParams: any;

    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService) {
        super(feedLoadingService, stateService, defineFeedService);
        console.log('constructor');
    }

    ngOnInit() {
        console.log("on init");
        let feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.initializeFeed(feedId);
    }
}

