import {Component, Input, OnDestroy, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {Step} from '../../../../model/feed/feed-step.model';
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {FeedLineageComponment} from "../feed-lineage/feed-lineage.componment";
import {SaveFeedResponse} from "../../model/save-feed-response.model";
import {ISubscription} from "rxjs/Subscription";
import {SUMMARY_LINK} from "../../model/feed-link-constants";
import {FeedLink} from "../../model/feed-link.model";

@Component({
    selector: "define-feed-overview",
    styleUrls: ["./overview.component.scss"],
    templateUrl: "./overview.component.html"
})
export class OverviewComponent extends AbstractLoadFeedComponent  {

    static LOADER = "OverviewComponent.LOADER";

    static LINK_NAME = SUMMARY_LINK;

    @Input() stateParams: any;

    feedSavedSubscription:ISubscription;

    getLinkName(){
        return OverviewComponent.LINK_NAME;
    }

    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService:FeedSideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
       this.feedSavedSubscription = this.defineFeedService.subscribeToFeedSaveEvent(this.onFeedSaved.bind(this))
     }



    destroy(){
        this.feedSavedSubscription.unsubscribe();
    }


    onFeedSaved(response:SaveFeedResponse){
        if(response.success){
            //update this feed
            this.feed = response.feed;

        }
    }




}