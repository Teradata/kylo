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
import {FeedLinkSelectionChangedEvent, FeedSideNavService, ToolbarActionTemplateChangedEvent} from "../../shared/feed-side-nav.service";
import {FeedLink} from "../../shared/feed-link.model";




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
                private viewContainerRef: ViewContainerRef,public snackBar: MatSnackBar,
                feedSideNavService:FeedSideNavService) {
        super(feedLoadingService, stateService,defineFeedService, feedSideNavService);
          let sideNavService = $$angularInjector.get("SideNavService");
          sideNavService.hideSideNav();
        this.feedLinkSelectionChangeSubscription =  this.feedSideNavService.subscribeToFeedLinkSelectionChanges(this.onFeedLinkChanged.bind(this))
        this.onFeedSaveSubscription =  this.defineFeedService.subscribeToFeedSaveEvent(this.onFeedSaved.bind(this))
        this.toolbarActionTemplateChangeSubscription =this.feedSideNavService.subscribeToToolbarActionTemplateChanges(this.onTemplateActionTemplateChanged.bind(this))
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

        this.dialogService.openConfirm({
            message: 'Are you sure want to delete feed '+this.feed.feedName+'?',
            disableClose: true,
            viewContainerRef: this.viewContainerRef, //OPTIONAL
            title: 'Confirm Delete', //OPTIONAL, hides if not provided
            cancelButton: 'No', //OPTIONAL, defaults to 'CANCEL'
            acceptButton: 'Yes', //OPTIONAL, defaults to 'ACCEPT'
            width: '500px', //OPTIONAL, defaults to 400px
        }).afterClosed().subscribe((accept: boolean) => {
            if (accept) {
                this.registerLoading();
                this.defineFeedService.deleteFeed().subscribe(() => {
                    this.openSnackBar("Deleted the feed")
                    this.resolveLoading();
                    this.stateService.go('feeds')
                },(error:any) => {
                    this.openSnackBar("Error deleting the feed ")
                    this.resolveLoading();
                })
            } else {
                // DO SOMETHING ELSE
            }
        });

    }
    private openSnackBar(message:string, duration?:number){
        if(duration == undefined){
            duration = 3000;
        }
        this.snackBar.open(message, null, {
            duration: duration,
        });
    }



    onFeedSaved(response:SaveFeedResponse){
        if(response.success){
            //update this feed
            this.feed = response.feed;
        }
    }



}