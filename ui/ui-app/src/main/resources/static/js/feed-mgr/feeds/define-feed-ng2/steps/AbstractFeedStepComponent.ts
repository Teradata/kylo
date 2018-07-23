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

export abstract class AbstractFeedStepComponent implements OnInit, OnDestroy {


    public feed: Feed;

    public step : Step;

    public abstract getStepName() :string;

    public formValid:boolean;

    private beforeSaveSubscription : ISubscription;

    private feedStateChangeSubscription:ISubscription;
   // private feedSavedSubscription : ISubscription;

    constructor(protected  defineFeedService:DefineFeedService, protected stateService:StateService) {
        //subscribe to the beforeSave call so the step can update the service with the latest feed information
        this.beforeSaveSubscription = this.defineFeedService.beforeSave$.subscribe(this.updateFeedService.bind(this))
        this.feedStateChangeSubscription = this.defineFeedService.feedStateChange$.subscribe(this.feedStateChanged.bind(this))
      //  this.feedSavedSubscription = this.defineFeedService.savedFeed$.subscribe(this.onFeedFinishedSaving.bind(this))
    }
    ngOnInit() {
        this.initData();
        this.init();
    }

    ngOnDestroy(){
      this.destroy();
      //update the feed service with any changes
      this.updateFeedService();
      //unsubscribe from the beforeSave call
      this.beforeSaveSubscription.unsubscribe();
      this.feedStateChangeSubscription.unsubscribe();
  //    this.feedSavedSubscription.unsubscribe();
    }

    subscribeToFormChanges(formGroup:FormGroup, debounceTime:number = 500) {
        // initialize stream
        const formValueChanges$ = formGroup.statusChanges;

        // subscribe to the stream
        formValueChanges$.debounceTime(debounceTime).subscribe(changes => {
            console.log("changes",changes,"formStatus",formGroup.status)
            this.formValid = changes == "VALID" //&&  this.tableForm.validate(undefined);
            this.step.setComplete(this.formValid);
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

    updateFeedService(){
        //update the feed service with this data
        this.defineFeedService.setFeed(this.feed);
    }

    private feedStateChanged(feed:Feed){
        this.feed.readonly = feed.readonly;
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