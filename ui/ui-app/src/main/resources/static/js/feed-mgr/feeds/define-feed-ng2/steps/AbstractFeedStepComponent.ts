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

    protected constructor(protected  defineFeedService:DefineFeedService, protected stateService:StateService,
                protected feedLoadingService:FeedLoadingService, protected dialogService: TdDialogService) {

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

    /**
     * Called before save to apply updates to the feed model
     */
    protected applyUpdatesToFeed():void{

    }

    registerLoading(): void {
        this.feedLoadingService.registerLoading();
    }

    resolveLoading(): void {
        this.feedLoadingService.resolveLoading();
    }



    onSave(){
        this.registerLoading();
        this.applyUpdatesToFeed();
        //notify the subscribers on the actual save call so they can listen when the save finishes
        this.defineFeedService.saveFeed(this.feed).subscribe((response:SaveFeedResponse) => {
            this.defineFeedService.openSnackBar("Saved the feed ",3000);
        })
    }
    onEdit(){
        this.feed.readonly = false;
      //  this.defineFeedService.onFeedEdit();
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

                    let oldFeed = this.defineFeedService.getFeed();
                    this.feed.update(oldFeed);
                    //this.openSnackBar("Restored this feed")
                    this.feed.readonly = true;
                    this.cancelFeedEdit();
                }
            } else {
                // DO SOMETHING ELSE
            }
        });
    }


    /**
     * When a feed edit is cancelled, reset the forms
     * @param {Feed} feed
     */
    protected cancelFeedEdit(){

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
                this.step.visited = true;
                this.defineFeedService.setCurrentStep(this.step)
            }
            else {
                //ERROR OUT
            }

    }



}