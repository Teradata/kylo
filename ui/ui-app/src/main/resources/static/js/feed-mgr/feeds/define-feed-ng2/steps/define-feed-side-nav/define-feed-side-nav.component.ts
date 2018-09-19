import {Step} from "../../../../model/feed/feed-step.model";
import {
    FEED_DEFINITION_STATE_NAME,
    FEED_DEFINITION_SECTION_STATE_NAME,
    FEED_OVERVIEW_STATE_NAME,
    FEED_SETUP_GUIDE_STATE_NAME,
    FEED_DEFINITION_SUMMARY_STATE_NAME
} from "../../../../model/feed/feed-constants";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Input, Component, OnInit, OnDestroy} from "@angular/core";
import {Feed, LoadMode} from "../../../../model/feed/feed.model";
import {DefineFeedService} from "../../services/define-feed.service";
import {FeedLink} from "../../shared/feed-link.model";
import {FeedLinkSelectionChangedEvent, FeedSideNavService} from "../../shared/feed-side-nav.service";
import {ISubscription} from "rxjs/Subscription";
import {StateObject} from "@uirouter/core";
import * as _ from "underscore"
import {SetupGuideSummaryComponent} from "../../summary/setup-guide-summary/setup-guide-summary.component";
import {SETUP_REVIEW_LINK} from "../../shared/feed-link-constants";

@Component({
    selector: "feed-definition-side-nav",
    styleUrls:["js/feed-mgr/feeds/define-feed-ng2/steps/define-feed-side-nav/define-feed-side-nav.component.scss"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/define-feed-side-nav/define-feed-side-nav.component.html"
})
export class DefineFeedSideNavComponent  implements OnInit, OnDestroy{
    @Input()
    feed:Feed

    summarySelected:boolean;

    public selectedStep : Step;

    stepLinks:FeedLink[] = [];

    feedLinkSelectionChangeSubscription:ISubscription;

    feedLoadedSubscription:ISubscription;

    latestSetupGuideLink = FeedLink.newSectionLink(SetupGuideSummaryComponent.LINK_NAME, "setup-guide","playlist_add_check",{"loadMode":LoadMode.LATEST});

    deployedSetupGuideLink = FeedLink.newSectionLink(SETUP_REVIEW_LINK, "deployed-setup-guide","playlist_add_check", {"loadMode":LoadMode.DEPLOYED});

    setupGuideLink:FeedLink;

    currentLink:FeedLink;

    constructor(private  stateService:StateService, private defineFeedService:DefineFeedService, private feedSideNavService:FeedSideNavService) {
        this.summarySelected = true;
        this.defineFeedService.subscribeToStepChanges(this.onStepChanged.bind(this))
        this.feedLinkSelectionChangeSubscription =  this.feedSideNavService.subscribeToFeedLinkSelectionChanges(this.onFeedLinkChanged.bind(this))
        this.feedLoadedSubscription = this.defineFeedService.subscribeToFeedLoadedEvent(this.onFeedLoaded.bind(this))
    }

    ngOnInit(){
        this._initLinks();
    }

    ngOnDestroy(){
        this.feedLinkSelectionChangeSubscription.unsubscribe();
        this.feedLoadedSubscription.unsubscribe();
    }

    gotoFeedSummary(){
        this.stateService.go(FEED_DEFINITION_SUMMARY_STATE_NAME,{feedId:this.feed.id});
    }

    gotoFeeds(){
        this.stateService.go("feeds");
    }

    onFeedLoaded(feed:Feed){
        this.feed = feed;
        this._initLinks();
    }

    onFeedLinkChanged(link:FeedLinkSelectionChangedEvent){
        this.currentLink = link.newLink;

    }

    private _initLinks(){
        if(LoadMode.DEPLOYED == this.feed.loadMode) {
            this.setupGuideLink = this.deployedSetupGuideLink;
        }
        else {
            this.setupGuideLink = this.latestSetupGuideLink;
        }
        this.stepLinks = this.feedSideNavService.buildStepLinks(this.feed);
        this.feedSideNavService.registerFeedLinks(this.stepLinks);
    }


    onLinkSelected(link:FeedLink){
        this.feedSideNavService.setSelected(link);
        let params = {"feedId":this.feed.id}
        if(link.srefParams){
            Object.keys(link.srefParams).forEach((key:string) => {
                params[key] = link.srefParams[key];
            });
        }
        this.stateService.go(link.sref,params);
     }

    /**
     * Listen when the step changes
     * @param {Step} step
     */
    onStepChanged(step:Step){
        this.feedSideNavService.setStepSelected(step);
    }


}