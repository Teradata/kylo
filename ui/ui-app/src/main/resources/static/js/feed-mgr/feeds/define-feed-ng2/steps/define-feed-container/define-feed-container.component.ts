import {Component, Injector, Input, OnDestroy, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
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


export class FeedLink{
    sref:string;
    constructor(public label:string,sref:string, private icon:string,isShort:boolean = true) {
        if(isShort) {
            this.sref = FEED_DEFINITION_SECTION_STATE_NAME+"."+sref;
        }
        else {
            this.sref = sref;
        }
    }

}


@Component({
    selector: "define-feed-step-container",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/define-feed-container/define-feed-container.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/define-feed-container/define-feed-container.component.html"
})
export class DefineFeedContainerComponent extends AbstractLoadFeedComponent implements OnInit, OnDestroy {

    @Input()
    stateParams :any;

    public savingFeed:boolean;

    feedLinks:FeedLink[] = [new FeedLink("Lineage",'feed-lineage',"graphic_eq",),new FeedLink("Profile","profile","track_changes"),new FeedLink("SLA","sla","beenhere"),new FeedLink("Versions","version-history","history")];

    /**
     * copy of the feed name.
     * the template will reference this for display so the name doesnt change until we save
     */
    public feedName:string;

    constructor(feedLoadingService: FeedLoadingService, defineFeedService :DefineFeedService,  stateService:StateService, private $$angularInjector: Injector, public media: TdMediaService,private loadingService: TdLoadingService,private dialogService: TdDialogService,
                private viewContainerRef: ViewContainerRef,public snackBar: MatSnackBar) {
        super(feedLoadingService, stateService,defineFeedService);
          let sideNavService = $$angularInjector.get("SideNavService");
          sideNavService.hideSideNav();
    }

    ngOnInit() {
        let feedId = this.stateParams? this.stateParams.feedId : undefined;
        this.initializeFeed(feedId);
    }

    ngOnDestroy() {

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


}