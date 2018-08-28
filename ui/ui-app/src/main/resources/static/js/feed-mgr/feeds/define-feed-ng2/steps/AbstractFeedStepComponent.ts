import * as angular from "angular";
import {Feed} from "../../../model/feed/feed.model";
import {FeedStepValidator} from "../../../model/feed/feed-step-validator";
import {DefineFeedService} from "../services/define-feed.service";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Input, OnDestroy, OnInit} from "@angular/core";
import {ISubscription} from "rxjs/Subscription";
import {SaveFeedResponse} from "../model/save-feed-response.model";
import {FormGroup} from "@angular/forms";
import {Step} from "../../../model/feed/feed-step.model";
import {FEED_DEFINITION_STATE_NAME} from "../../../model/feed/feed-constants";
import {FeedLoadingService} from "../services/feed-loading-service";
import {TdDialogService} from "@covalent/core/dialogs";

export abstract class AbstractFeedStepComponent implements OnInit, OnDestroy {


    public feed: Feed;

    public step : Step;

    public abstract getStepName() :string;

    public formValid:boolean;

    private beforeSaveSubscription : ISubscription;

    private feedStateChangeSubscription:ISubscription;

    private cancelFeedEditSubscription :ISubscription;

    private feedEditSubscription :ISubscription;

   // private feedSavedSubscription : ISubscription;

    constructor(protected  defineFeedService:DefineFeedService, protected stateService:StateService,
                protected feedLoadingService:FeedLoadingService, protected dialogService: TdDialogService) {
        //subscribe to the beforeSave call so the step can update the service with the latest feed information
        this.beforeSaveSubscription = this.defineFeedService.beforeSave$.subscribe(this.updateFeedService.bind(this))
        this.feedStateChangeSubscription = this.defineFeedService.feedStateChange$.subscribe(this.feedStateChanged.bind(this))
        this.cancelFeedEditSubscription = this.defineFeedService.cancelFeedEdit$.subscribe(this.cancelFeedEdit.bind(this))
        this.feedEditSubscription = this.defineFeedService.feedEdit$.subscribe(this.feedEdit.bind(this))
      //  this.feedSavedSubscription = this.defineFeedService.savedFeed$.subscribe(this.onFeedFinishedSaving.bind(this))
    }
    ngOnInit() {
        this.initData();
        this.init();
    }

    ngOnDestroy(){
        try {
            this.destroy();
        }catch(err){
            console.error("error in destroy",err);
        }
      if(!this.feed.readonly) {
          //update the feed service with any changes
          this.updateFeedService();
      }
      //unsubscribe from the beforeSave call
      this.beforeSaveSubscription.unsubscribe();
      this.feedStateChangeSubscription.unsubscribe();
      this.cancelFeedEditSubscription.unsubscribe();
      this.feedEditSubscription.unsubscribe();
  //    this.feedSavedSubscription.unsubscribe();
    }

    subscribeToFormChanges(formGroup:FormGroup, debounceTime:number = 500) {
        // initialize stream
        const formValueChanges$ = formGroup.statusChanges;

        // subscribe to the stream
        formValueChanges$.debounceTime(debounceTime).subscribe(changes => {
            this.formValid = changes == "VALID" //&&  this.tableForm.validate(undefined);
      //      console.log("changes",changes,"formStatus",formGroup.status, this.formValid,'changes',changes)
            this.step.setComplete(this.formValid);
            this.step.valid = this.formValid;
            this.step.validator.hasFormErrors = !this.formValid;
        });

    }


    /**
     * Initialize the component
     */
    public init(){

    }

    public destroy(){

    }

    registerLoading(): void {
        this.feedLoadingService.registerLoading();
    }

    resolveLoading(): void {
        this.feedLoadingService.resolveLoading();
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
        this.defineFeedService.onFeedEdit();
    }
    onCancelEdit() {

        this.dialogService.openConfirm({
            message: 'Are you sure you want to canel editing  '+this.feed.feedName+'?  All pending edits will be lost.',
            disableClose: true,
            title: 'Confirm Cancel Edit', //OPTIONAL, hides if not provided
            cancelButton: 'No', //OPTIONAL, defaults to 'CANCEL'
            acceptButton: 'Yes', //OPTIONAL, defaults to 'ACCEPT'
            width: '500px', //OPTIONAL, defaults to 400px
        }).afterClosed().subscribe((accept: boolean) => {
            if (accept) {
                if(this.feed.isNew()){
                    this.stateService.go('feeds')
                }
                else {
                    let oldFeed = this.defineFeedService.restoreLastSavedFeed();
                    this.feed.update(oldFeed);
                    //this.openSnackBar("Restored this feed")
                    this.feed.readonly = true;
                }
            } else {
                // DO SOMETHING ELSE
            }
        });
    }



    updateFeedService(){
        //update the feed service with this data
        this.defineFeedService.setFeed(this.feed);
    }

    private feedStateChanged(feed:Feed){
        this.feed.readonly = feed.readonly;
    }

    /**
     * When a feed edit is cancelled, reset the forms
     * @param {Feed} feed
     */
    protected cancelFeedEdit(feed:Feed){

    }

    /**
     * When a feed is in edit mode
     * @param {Feed} feed
     */
    protected feedEdit(feed:Feed){

    }



    public initData(){
        if(this.feed == undefined) {
            this.feed = this.defineFeedService.getFeed();
            if (this.feed == undefined) {
                this.stateService.go(FEED_DEFINITION_STATE_NAME+ ".select-template")
            }
        }
            this.step = this.feed.steps.find(step => step.systemName == this.getStepName());
            if (this.step) {
                this.defineFeedService.setCurrentStep(this.step)
                this.step.visited = true;
            }
            else {
                //ERROR OUT
            }

    }



}