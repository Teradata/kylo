import {Component, Input, OnDestroy, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {Step} from '../../../../model/feed/feed-step.model';
import {FeedSideNavService} from "../../shared/feed-side-nav.service";
import {FeedLineageComponment} from "../feed-lineage/feed-lineage.componment";
import {SaveFeedResponse} from "../../model/save-feed-response.model";
import {ISubscription} from "rxjs/Subscription";
import {SETUP_GUIDE_LINK} from "../../shared/feed-link-constants";
import {Feed, LoadMode} from "../../../../model/feed/feed.model";
import {FEED_DEFINITION_SECTION_STATE_NAME} from "../../../../model/feed/feed-constants";
import {NewFeedDialogComponent, NewFeedDialogData, NewFeedDialogResponse} from "../../new-feed-dialog/new-feed-dialog.component";
import {TdDialogService} from "@covalent/core/dialogs";


@Component({
    selector: "setup-guide-summary",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/setup-guide-summary/setup-guide-summary.component.scss"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/setup-guide-summary/setup-guide-summary.component.html"
})
export class SetupGuideSummaryComponent extends AbstractLoadFeedComponent  {

    static LOADER = "SetupGuideContainerComponent.LOADER";

    static LINK_NAME = SETUP_GUIDE_LINK;

    @Input() stateParams: any;

    @Input()
    showHeader:boolean



    feedSavedSubscription:ISubscription;

    showEditLink:boolean;

    getLinkName(){
        return SetupGuideSummaryComponent.LINK_NAME;
    }

    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService:FeedSideNavService, private _dialogService:TdDialogService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
       this.feedSavedSubscription = this.defineFeedService.subscribeToFeedSaveEvent(this.onFeedSaved.bind(this))
     }

    init(){
        if(this.feed.isDraft() && this.feed.canEdit() && this.feed.readonly){
            this.showEditLink = true;
        }
        else{
            //ensure there is not another draft version
            let draft = this.defineFeedService.getDraftFeed(this.feed.id).subscribe((feed:Feed) => {
                this.showEditLink = false;
            },error1 => {
                console.log("Error checking for draft ",error1)
                this.showEditLink = this.feed.canEdit() && this.feed.readonly;
            })
        }
    }

    destroy(){
        this.feedSavedSubscription.unsubscribe();
    }


    loadDeployedVersion($event:MouseEvent){
        $event.stopPropagation();
        $event.preventDefault();
        this.defineFeedService.loadDeployedFeed(this.feed.id).subscribe((feed:Feed) => {
            this.stateService.go(FEED_DEFINITION_SECTION_STATE_NAME+".setup-guide",{feedId:this.feed.id, loadMode:LoadMode.DEPLOYED},{reload:false});
        });
    }

    onFeedSaved(response:SaveFeedResponse){
        if(response.success){
            //update this feed
            this.feed = response.feed;
            console.log('Feed saved overview component ',this.feed)
        }
    }

    onEdit(){
        this.defineFeedService.markFeedAsEditable();
        //redirect to setup guide
        this.stateService.go(FEED_DEFINITION_SECTION_STATE_NAME+".setup-guide",{feedId:this.feed.id})
    }

    cloneFeed(){
        this.defineFeedService.cloneFeed(this.feed);
    }




}