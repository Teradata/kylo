import {Component, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {Step} from '../../../../model/feed/feed-step.model';

@Component({
    selector: "define-feed-overview",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/overview/overview.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/overview/overview.component.html"
})
export class OverviewComponent extends AbstractLoadFeedComponent implements OnInit {

    static LOADER = "OverviewComponent.LOADER";

    @Input() stateParams: any;


    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService) {
        super(feedLoadingService, stateService, defineFeedService);
     }

    ngOnInit() {
        let feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.initializeFeed(feedId);
    }

    onStepSelected(step: Step) {
        if (!step.isDisabled()) {
            this.selectedStep = step;
            let params = {"feedId": this.feed.id};
            this.stateService.go(step.sref, params, {location: "replace"})
        }
    }
}