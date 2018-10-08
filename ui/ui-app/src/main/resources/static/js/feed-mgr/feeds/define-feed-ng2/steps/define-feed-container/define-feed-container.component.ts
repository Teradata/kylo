import {Component, Injector, Input, OnDestroy, OnInit, TemplateRef, ViewChild, ViewContainerRef} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {TdLoadingService} from '@covalent/core/loading';
import {TdMediaService} from '@covalent/core/media';
import {StateService} from "@uirouter/angular";
import {ISubscription} from "rxjs/Subscription";

import {SETUP_GUIDE_LINK} from "../../model/feed-link-constants";
import {FeedLink} from "../../model/feed-link.model";
import {SaveFeedResponse} from "../../model/save-feed-response.model";
import {DefineFeedService} from "../../services/define-feed.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedLinkSelectionChangedEvent, FeedSideNavService, ToolbarActionTemplateChangedEvent} from "../../services/feed-side-nav.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FEED_DEFINITION_SUMMARY_STATE_NAME} from "../../../../model/feed/feed-constants";


@Component({
    selector: "define-feed-step-container",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/define-feed-container/define-feed-container.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/define-feed-container/define-feed-container.component.html"
})
export class DefineFeedContainerComponent extends AbstractLoadFeedComponent implements OnInit, OnDestroy {

    @Input()
    stateParams: any;

    @Input()
    toolbarActionLinks: TemplateRef<any>;

    @ViewChild("defaultToolbarActionLinks")
    defaultToolbarActionLinks: TemplateRef<any>;

    @ViewChild("setupGuideToolbarActions")
    setupGuideToolbarActionLinksTemplate: TemplateRef<any>;

    /**
     * should we show the side nav or not
     */
    sideNavOpened:boolean = true;

    public savingFeed: boolean;

    /**
     * copy of the feed name.
     * the template will reference this for display so the name doesnt change until we save
     */
    public feedName: string;

    currentLink: FeedLink = FeedLink.emptyLink();

    feedLinkSelectionChangeSubscription: ISubscription;
    onFeedSaveSubscription: ISubscription;
    toolbarActionTemplateChangeSubscription: ISubscription;


    constructor(feedLoadingService: FeedLoadingService, defineFeedService: DefineFeedService, stateService: StateService, private $$angularInjector: Injector, public media: TdMediaService, private loadingService: TdLoadingService, private dialogService: TdDialogService,
                private viewContainerRef: ViewContainerRef,
                feedSideNavService: FeedSideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
        let sideNavService = $$angularInjector.get("SideNavService");
        sideNavService.hideSideNav();
        this.feedLinkSelectionChangeSubscription = this.feedSideNavService.subscribeToFeedLinkSelectionChanges(this.onFeedLinkChanged.bind(this));
        this.onFeedSaveSubscription = this.defineFeedService.subscribeToFeedSaveEvent(this.onFeedSaved.bind(this));
        this.toolbarActionTemplateChangeSubscription = this.feedSideNavService.subscribeToToolbarActionTemplateChanges(this.onTemplateActionTemplateChanged.bind(this))

    }

    init() {
        //register the setupguide action template
        this.feedSideNavService.registerStepToolbarActionTemplate(SETUP_GUIDE_LINK, this.setupGuideToolbarActionLinksTemplate)
    }

    destroy() {
        this.feedLinkSelectionChangeSubscription.unsubscribe();
        this.onFeedSaveSubscription.unsubscribe();
        this.toolbarActionTemplateChangeSubscription.unsubscribe();
    }

    onTemplateActionTemplateChanged(change: ToolbarActionTemplateChangedEvent) {
        console.log('TEMPLATE CHANGED!!!!');
        if (this.toolbarActionLinks != change.templateRef) {
            console.log('RESET template!!!');
            this.toolbarActionLinks = change.templateRef;
        }
    }

    onFeedLinkChanged(link: FeedLinkSelectionChangedEvent) {
        this.currentLink = link.newLink;
        if(this.currentLink.isStepLink()){
            const fullScreen = this.currentLink.step.fullscreen;
            if(fullScreen){
                this.sideNavOpened = false;
            }
            else {
                this.sideNavOpened = true;
            }
        }
        //see if we have an action template
        let templateRef = this.feedSideNavService.getLinkTemplateRef(this.currentLink.label);
        this.toolbarActionLinks = templateRef;
    }

    onEdit() {
        this.feed.readonly = false;
        this.defineFeedService.markFeedAsEditable();

    }

    onCancelEdit() {
        this.feed.readonly = true;
        this.defineFeedService.markFeedAsReadonly();

    }

    onDelete() {
        //confirm then delete
        this.defineFeedService.deleteFeed(this.viewContainerRef);
    }

    onFeedSaved(response: SaveFeedResponse) {
        if (response.success) {
            //update this feed
            this.feed = response.feed;
        }
    }

    gotoFeedSummary(){
        if(!this.sideNavOpened) {
            this.stateService.go(FEED_DEFINITION_SUMMARY_STATE_NAME, {feedId: this.feed.id});
        }
    }


}
