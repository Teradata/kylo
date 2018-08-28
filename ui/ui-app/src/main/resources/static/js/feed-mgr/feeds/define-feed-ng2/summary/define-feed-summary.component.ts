import {Component, Injector, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {AbstractLoadFeedComponent} from '../shared/AbstractLoadFeedComponent';
import {FeedLoadingService} from '../services/feed-loading-service';
import {DefineFeedService} from '../services/define-feed.service';

@Component({
    selector: "define-feed-summary",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/define-feed-summary.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/define-feed-summary.component.html"
})
export class DefineFeedSummaryComponent  extends AbstractLoadFeedComponent implements OnInit {

    @Input() stateParams:any;

    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, private $$angularInjector: Injector) {
        super(feedLoadingService, stateService, defineFeedService);
        let sideNavService = $$angularInjector.get("SideNavService");
        sideNavService.hideSideNav();
    }

    ngOnInit() {
        let feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.initializeFeed(feedId);
    }
}