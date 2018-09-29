import {Component, ContentChild, Injector, Input, OnDestroy, OnInit, TemplateRef, ViewChild, ViewContainerRef} from "@angular/core";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";
import {DefineFeedService} from "../../services/define-feed.service";
import {StateRegistry, StateService} from "@uirouter/angular";
import { TdMediaService } from '@covalent/core/media';
import { TdLoadingService } from '@covalent/core/loading';
import {SaveFeedResponse} from "../../model/save-feed-response.model";
import {MatSnackBar} from "@angular/material/snack-bar";
import {ISubscription} from "rxjs/Subscription";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {TdDialogService} from "@covalent/core/dialogs";
import * as angular from "angular";
import {FEED_DEFINITION_STATE_NAME,FEED_DEFINITION_SECTION_STATE_NAME} from "../../../../model/feed/feed-constants";
import {FeedLinkSelectionChangedEvent, FeedSideNavService, ToolbarActionTemplateChangedEvent} from "../../services/feed-side-nav.service";
import {FeedLink} from "../../model/feed-link.model";
import {SETUP_GUIDE_LINK} from "../../model/feed-link-constants";




@Component({
    selector: "define-feed-step-container",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/define-feed-container/define-feed-container.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/define-feed-container/define-feed-container.component.html"
})
export class DefineFeedContainerComponent extends AbstractLoadFeedComponent implements OnInit, OnDestroy {

    @Input()
    stateParams :any;

    @Input()
    toolbarActionLinks:TemplateRef<any>;

    @ViewChild("defaultToolbarActionLinks")
    defaultToolbarActionLinks:TemplateRef<any>;

    @ViewChild("setupGuideToolbarActions")
    setupGuideToolbarActionLinksTemplate:TemplateRef<any>;


    public savingFeed:boolean;

    /**
     * copy of the feed name.
     * the template will reference this for display so the name doesnt change until we save
     */
    public feedName:string;

    currentLink:FeedLink = FeedLink.emptyLink() ;

    feedLinkSelectionChangeSubscription:ISubscription;
    onFeedSaveSubscription:ISubscription;
    toolbarActionTemplateChangeSubscription:ISubscription;

    constructor(feedLoadingService: FeedLoadingService, defineFeedService :DefineFeedService,  stateService:StateService, private $$angularInjector: Injector, public media: TdMediaService,private loadingService: TdLoadingService,private dialogService: TdDialogService,
                private viewContainerRef: ViewContainerRef,
                feedSideNavService:FeedSideNavService) {
        super(feedLoadingService, stateService,defineFeedService, feedSideNavService);
          let sideNavService = $$angularInjector.get("SideNavService");
          sideNavService.hideSideNav();
        this.feedLinkSelectionChangeSubscription =  this.feedSideNavService.subscribeToFeedLinkSelectionChanges(this.onFeedLinkChanged.bind(this))
        this.onFeedSaveSubscription =  this.defineFeedService.subscribeToFeedSaveEvent(this.onFeedSaved.bind(this))
        this.toolbarActionTemplateChangeSubscription =this.feedSideNavService.subscribeToToolbarActionTemplateChanges(this.onTemplateActionTemplateChanged.bind(this))

    }

    init(){
      //register the setupguide action template
            this.feedSideNavService.registerStepToolbarActionTemplate(SETUP_GUIDE_LINK, this.setupGuideToolbarActionLinksTemplate)
    }

    destroy() {
        this.feedLinkSelectionChangeSubscription.unsubscribe();
        this.onFeedSaveSubscription.unsubscribe();
        this.toolbarActionTemplateChangeSubscription.unsubscribe();
    }

    onTemplateActionTemplateChanged(change:ToolbarActionTemplateChangedEvent){
        console.log('TEMPLATE CHANGED!!!!')
        if(this.toolbarActionLinks != change.templateRef) {
         console.log('RESET template!!!')
            this.toolbarActionLinks = change.templateRef;
        }
    }

    onFeedLinkChanged(link:FeedLinkSelectionChangedEvent){
        this.currentLink = link.newLink;
        //see if we have an action template
        let templateRef = this.feedSideNavService.getLinkTemplateRef(this.currentLink.label);
        this.toolbarActionLinks = templateRef;
    }

    onEdit(){
        this.feed.readonly = false;
        this.defineFeedService.markFeedAsEditable();

    }

    onCancelEdit(){
        this.feed.readonly = true;
        this.defineFeedService.markFeedAsReadonly();

    }

    onDelete(){
      //confirm then delete
        this.defineFeedService.deleteFeed(this.viewContainerRef);
    }

    onFeedSaved(response:SaveFeedResponse){
        if(response.success){
            //update this feed
            this.feed = response.feed;
        }
    }



}