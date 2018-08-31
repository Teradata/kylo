import {Step} from "../../../model/feed/feed-step.model";
import {FEED_DEFINITION_STATE_NAME,FEED_DEFINITION_SECTION_STATE_NAME} from "../../../model/feed/feed-constants";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Input, Component, OnInit} from "@angular/core";
import {Feed} from "../../../model/feed/feed.model";
import {DefineFeedService} from "../services/define-feed.service";
import {FeedLink} from "./feed-link.model";
import {FeedSideNavService} from "./feed-side-nav.service";
import {FeedLineageComponment} from "../summary/feed-lineage/feed-lineage.componment";
import {ProfileComponent} from "../summary/profile/profile.component";
import {OverviewComponent} from "../summary/overview/overview.component";

@Component({
    selector: "feed-definition-side-nav",
    styleUrls:["js/feed-mgr/feeds/define-feed-ng2/shared/feed-side-nav.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/shared/feed-side-nav.component.html"
})
export class FeedSideNavComponent  implements OnInit{
    @Input()
    feed:Feed

    summarySelected:boolean;

    public selectedStep : Step;

    feedLinks:FeedLink[] = [FeedLink.newStaticLink(FeedLineageComponment.LINK_NAME,'feed-lineage',"graphic_eq"),
        FeedLink.newStaticLink(ProfileComponent.LINK_NAME,"profile","track_changes"),
        FeedLink.newStaticLink("SLA","sla","beenhere"),
        FeedLink.newStaticLink("Versions","version-history","history")];

    summaryLink = FeedLink.newSummaryLink(OverviewComponent.LINK_NAME, "overview");

    stepLinks:FeedLink[] = [];

    constructor(private  stateService:StateService, private defineFeedService:DefineFeedService, private feedSideNavService:FeedSideNavService) {
        this.summarySelected = true;
        this.defineFeedService.subscribeToStepChanges(this.onStepChanged.bind(this))
    }
    ngOnInit(){
        this.stepLinks = this.feedSideNavService.buildStepLinks(this.feed);
        let feedLinks = this.feedLinks.concat(this.stepLinks)
        feedLinks.unshift(this.summaryLink);
        this.feedSideNavService.registerFeedLinks(feedLinks);
    }


    gotoFeeds(){
        this.stateService.go("feeds");
    }


    onLinkSelected(link:FeedLink){
        this.feedSideNavService.setSelected(link);
        this.stateService.go(link.sref,{"feedId":this.feed.id});
    }

    /**
     * Listen when the step changes
     * @param {Step} step
     */
    onStepChanged(step:Step){
        this.feedSideNavService.setStepSelected(step);
    }


}