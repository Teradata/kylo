import {Component, Inject, Input, OnDestroy, OnInit, TemplateRef, ViewChild} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../services/feed-loading-service";
import {FeedSideNavService} from "../services/feed-side-nav.service";
import {FeedLineageComponment} from "./feed-lineage/feed-lineage.componment";
import {FeedActivitySummaryComponent} from "./feed-activity-summary/feed-activity-summary.component";
import {ProfileComponent} from "./profile/profile.component";
import {SetupGuideSummaryComponent} from "./setup-guide-summary/setup-guide-summary.component";
import {OverviewComponent} from "./overview/overview.component";
import {FeedLink} from "../model/feed-link.model";
import {SideNavService} from "../../../../services/SideNavService";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_SUMMARY_STATE_NAME, FEED_OVERVIEW_STATE_NAME} from "../../../model/feed/feed-constants";
import {Feed} from "../../../model/feed/feed.model";


@Component({
    styleUrls: ["./feed-summary-container.component.scss"],
    templateUrl: "./feed-summary-container.component.html"
})
export class FeedSummaryContainerComponent extends AbstractLoadFeedComponent  {

    static LOADER = "FeedSummaryContainerComponent.LOADER";

    setupGuideLink:FeedLink;

    summaryLinks:FeedLink[] = []

    staticFeedLinks:FeedLink[] = [];

    /**
     * should we show the profile, lineage, sla, etc links
     */
    additionalLinksDisabled:boolean;


    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService:FeedSideNavService, @Inject("SideNavService") private sideNavService: SideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
        this.staticFeedLinks = this.feedSideNavService.staticFeedLinks;
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
            redirectState = FEED_DEFINITION_SUMMARY_STATE_NAME+".feed-activity";
        }
        else {
            redirectState = FEED_DEFINITION_SUMMARY_STATE_NAME+".setup-guide";
       }
       this.stateService.go(redirectState,{feedId:this.feed.id, refresh:false}, {location:'replace'})
    }

    private initializeLinks(){
        if(this.feed) {
            this.summaryLinks.length =0;
            this.setupGuideLink = this.feedSideNavService.summaryLinkLatestSetupGuideLink;
            if(!this.feed.hasBeenDeployed()) {
                this.additionalLinksDisabled = true;
                this.summaryLinks.push(this.setupGuideLink)
            }else {
                if(this.feed.isDraft()) {
                    this.setupGuideLink = this.feedSideNavService.summaryLinkLatestSetupGuideLink;
                }
                else {
                    this.setupGuideLink = this.feedSideNavService.summaryLinkDeployedSetupGuideLink;
                }
                this.additionalLinksDisabled = false;
                this.summaryLinks.push(this.feedSideNavService.feedActivityLink);
                this.summaryLinks.push(this.setupGuideLink)
            }
        }
    }

    onFeedLoaded(feed:Feed){
        super.onFeedLoaded(feed);
        this.initializeLinks();
    }

    destroy() {

    }


    selectLink(link:FeedLink){
        if(!this.additionalLinksDisabled) {
            this.feedSideNavService.setSelected(link);
            this.stateService.go(link.sref, {"feedId": this.feed.id}, {location: 'replace'});
        }
    }

}