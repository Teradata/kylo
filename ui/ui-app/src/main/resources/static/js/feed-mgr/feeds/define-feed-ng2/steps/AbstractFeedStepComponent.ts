import * as angular from "angular";
import {FeedModel, Step} from "../model/feed.model";
import {FeedStepValidator} from "../model/feed-step-validator";
import {DefineFeedService} from "../services/define-feed.service";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Input, OnDestroy, OnInit} from "@angular/core";
import {ISubscription} from "rxjs/Subscription";
import {FEED_DEFINITION_STATE_NAME} from "../define-feed-states"

export abstract class AbstractFeedStepComponent implements OnInit, OnDestroy {


    public feed: FeedModel;

    public step : Step;

    public abstract getStepName() :string;

    private beforeSaveSubscription : ISubscription;

    constructor(protected  defineFeedService:DefineFeedService, protected stateService:StateService) {
        //subscribe to the beforeSave call so the step can update the service with the latest feed information
        this.beforeSaveSubscription = this.defineFeedService.beforeSave$.subscribe(this.updateFeedService.bind(this))
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
    }


    /**
     * Initialize the component
     */
    public init(){

    }

    public destroy(){

    }

    private updateFeedService(){
        //update the feed service with this data
        this.defineFeedService.setFeed(this.feed);
    }



    public initData(){
        if(this.feed == undefined) {
            this.feed = this.defineFeedService.getFeed();
            if (this.feed == undefined) {
                this.stateService.go(FEED_DEFINITION_STATE_NAME + ".select-template")
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