import {Component, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {Step} from '../../../../model/feed/feed-step.model';
import {FeedSideNavService} from "../../shared/feed-side-nav.service";
import {FeedLineageComponment} from "../feed-lineage/feed-lineage.componment";
import {SaveFeedResponse} from "../../model/save-feed-response.model";

@Component({
    selector: "define-feed-overview",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/overview/overview.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/overview/overview.component.html"
})
export class OverviewComponent extends AbstractLoadFeedComponent implements OnInit {

    static LOADER = "OverviewComponent.LOADER";

    static LINK_NAME = "Summary"

    @Input() stateParams: any;

    requiredSteps:Step[] = [];

    optionalSteps:Step[] = [];


    getLinkName(){
        return OverviewComponent.LINK_NAME;
    }

    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService:FeedSideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
        this.defineFeedService.subscribeToFeedSaveEvent(this.onFeedSaved.bind(this))
     }

    ngOnInit() {
        let feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.initializeFeed(feedId);
        this.feed.steps.forEach(step => {
            if(step.required){
                this.requiredSteps.push(step);
            }
            else {
                this.optionalSteps.push(step);
            }
        })
    }

    onStepSelected(step: Step) {
      //  if (!step.isDisabled()) {
            let params = {"feedId": this.feed.id};
            this.stateService.go(step.sref, params, {location: "replace"})
        //}
    }

    onFeedSaved(response:SaveFeedResponse){
        if(response.success){
            //update this feed
            this.feed = response.feed;
            console.log('Feed saved overview component ',this.feed)
        }
    }




}