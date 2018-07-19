import {Component, Injector, Input, OnDestroy, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {DataSource} from "../../../../catalog/api/models/datasource";
import {FeedModel, Step} from "../../model/feed.model";
import {DefineFeedService} from "../../services/define-feed.service";
import {StateRegistry, StateService} from "@uirouter/angular";
import {PreviewDataSet} from "../../../../catalog/datasource/preview-schema/model/preview-data-set";
import { TdMediaService } from '@covalent/core/media';
import { TdLoadingService } from '@covalent/core/loading';
import {FEED_DEFINITION_STATE_NAME} from "../../define-feed-states";
import {SaveFeedResponse} from "../../model/SaveFeedResponse";
import {MatSnackBar} from "@angular/material/snack-bar";
import {ISubscription} from "rxjs/Subscription";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {TdDialogService} from "@covalent/core/dialogs";
import * as angular from "angular";

@Component({
    selector: "define-feed-step-container",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/define-feed-container/define-feed-container.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/define-feed-container/define-feed-container.component.html"
})
export class DefineFeedContainerComponent extends AbstractLoadFeedComponent implements OnInit, OnDestroy {

    @Input() stateParams :any;

    public savingFeed:boolean;

    onCurrentStepSubscription: ISubscription;

    onFeedSavedSubscription: ISubscription;

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
        this.onCurrentStepSubscription = this.defineFeedService.currentStep$.subscribe(this.onCurrentStepChanged.bind(this))
        this.onFeedSavedSubscription = this.defineFeedService.savedFeed$.subscribe(this.onFeedFinishedSaving.bind(this))
    }

    ngOnInit() {
        let feedId = this.stateParams? this.stateParams.feedId : undefined;
        this.initializeFeed(feedId);
    }

    ngOnDestroy() {
        this.onCurrentStepSubscription.unsubscribe();
        this.onFeedSavedSubscription.unsubscribe();
    }


    onStepSelected(step:Step){
        if(!step.isDisabled()) {
            this.selectedStep = step;
            this.stateService.go(step.sref,{"feedId":this.feed.id})
        }
    }

    onSummarySelected(){
        this.stateService.go(FEED_DEFINITION_STATE_NAME+".summary",{"feedId":this.feed.id})
    }

    onSave(){
        this.registerLoading();
        //notify any subscribers that we are about to save the service feed model.
        //this gives them a chance to update the service with their data prior to the actual save call
        this.defineFeedService.beforeSave();
        //notify the subscribers on the actual save call so they can listen when the save finishes
        this.defineFeedService.saveFeed();
    }
    onEdit(){
        this.feed.readonly = false;
    }
    onCancelEdit() {

        this.dialogService.openConfirm({
            message: 'Are you sure you want to canel editing  '+this.feed.feedName+'?  All pending edits will be lost.',
            disableClose: true,
            viewContainerRef: this.viewContainerRef, //OPTIONAL
            title: 'Confirm Cancel Edit', //OPTIONAL, hides if not provided
            cancelButton: 'Nah', //OPTIONAL, defaults to 'CANCEL'
            acceptButton: 'Aight', //OPTIONAL, defaults to 'ACCEPT'
            width: '500px', //OPTIONAL, defaults to 400px
        }).afterClosed().subscribe((accept: boolean) => {
            if (accept) {
                if(this.feed.isNew()){
                    this.stateService.go('feeds')
                }
                else {
                  let oldFeed = this.defineFeedService.restoreLastSavedFeed();
                  this.feed.update(oldFeed);
                    this.openSnackBar("Restored this feed")
                    this.feed.readonly = true;
                }
            } else {
                // DO SOMETHING ELSE
            }
        });
    }
    onDelete(){
      //confirm then delete

        this.dialogService.openConfirm({
            message: 'Are you sure want to delete feed '+this.feed.feedName+'?',
            disableClose: true,
            viewContainerRef: this.viewContainerRef, //OPTIONAL
            title: 'Confirm Delete', //OPTIONAL, hides if not provided
            cancelButton: 'Disagree', //OPTIONAL, defaults to 'CANCEL'
            acceptButton: 'Agree', //OPTIONAL, defaults to 'ACCEPT'
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


    onFeedFinishedSaving(response:SaveFeedResponse){
        this.resolveLoading();
        this.savingFeed = false;
        let message = response.message;
        this.openSnackBar(message)
        if(response.success){
            angular.extend(this.feed,response.feed);
          //  this.feed = response.feed;
            this.feedName = this.feed.feedName;
        }
        if(response.newFeed){
            this.stateService.go(FEED_DEFINITION_STATE_NAME+".feed-step.general-info",{"feedId":response.feed.id},{location:"replace"})
        }

    }

    /**
     * Listener for changes from the service
     */
    onCurrentStepChanged(step:Step){
        this.selectedStep = step;
    }

}