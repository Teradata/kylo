import {Component, Inject, Input, OnDestroy, OnInit, TemplateRef, ViewChild} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../services/feed-loading-service";
import {FeedSideNavService} from "../shared/feed-side-nav.service";
import {FeedLineageComponment} from "./feed-lineage/feed-lineage.componment";
import {FeedActivitySummaryComponent} from "./feed-activity-summary/feed-activity-summary.component";
import {ProfileComponent} from "./profile/profile.component";
import {SetupGuideSummaryComponent} from "./setup-guide-summary/setup-guide-summary.component";
import {OverviewComponent} from "./overview/overview.component";
import {FeedLink} from "../shared/feed-link.model";
import SideNavService from "../../../../services/SideNavService";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_SUMMARY_STATE_NAME, FEED_OVERVIEW_STATE_NAME} from "../../../model/feed/feed-constants";


@Component({
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/feed-summary-container.component.scss"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/feed-summary-container.component.html"
})
export class FeedSummaryContainerComponent extends AbstractLoadFeedComponent  {

    static LOADER = "FeedSummaryContainerComponent.LOADER";

    setupGuideLink = FeedLink.newStaticLink(SetupGuideSummaryComponent.LINK_NAME, "setup-guide", "playlist_add_check");

    feedActivityLink = FeedLink.newStaticLink(FeedActivitySummaryComponent.LINK_NAME, "feed-activity","pages");

    summaryLinks:FeedLink[] = []

    feedLinks:FeedLink[] = [];

    /**
     * should we show the profile, lineage, sla, etc links
     */
    showAdditionalLinks:boolean;


    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService:FeedSideNavService, @Inject("SideNavService") private sideNavService: SideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
        this.feedLinks = this.feedSideNavService.staticFeedLinks;
        this.sideNavService.hideSideNav();
    }



    gotoFeeds(){
        this.stateService.go("feeds");
    }

    init(){
        this.initializeLinks();
        let redirectState :string = undefined;
        // If the feed has already been deployed at least once, show the setup guide in the summary panel with feed activity, lineage, etc
        //otherwise direct them to the feed-definition/section page with just the setup guide

        if(this.feed.hasBeenDeployed()) {
            redirectState = FEED_DEFINITION_SUMMARY_STATE_NAME+".setup-guide";
        }
        else {
            redirectState = FEED_DEFINITION_SECTION_STATE_NAME+".setup-guide";
        }
        this.stateService.go(redirectState,{feedId:this.feed.id}, {location:'replace'})


    }

    private initializeLinks(){
        if(this.feed) {
            if(!this.feed.hasBeenDeployed()){
                this.summaryLinks.push(this.setupGuideLink)
                this.showAdditionalLinks = false;
            }
            else {
                this.showAdditionalLinks = true;
                this.summaryLinks.push(this.feedActivityLink);
                this.summaryLinks.push(this.setupGuideLink)

            }
        }
    }

    onFeedLoaded(){
       // this.initializeLinks();
      /*  if(this.feed.hasBeenDeployed()){
            this.stateService.go(FEED_OVERVIEW_STATE_NAME,{feedId:this.feed.id})
        }
        else {
            this.stateService.go(FEED_DEFINITION_SECTION_STATE_NAME+".setup-guide",{feedId:this.feed.id})
        }
        */
    }

    destroy() {

    }


    onLinkSelected(link:FeedLink){
        this.feedSideNavService.setSelected(link);
        this.stateService.go(link.sref,{"feedId":this.feed.id}, {location:'replace'});
    }

}