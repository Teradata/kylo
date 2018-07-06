import * as angular from "angular";
import {FeedModel, Step} from "../model/feed.model";
import {FeedStepValidator} from "../model/feed-step-validator";
import {DefineFeedService} from "../services/define-feed.service";
import {StateRegistry, StateService} from "@uirouter/angular";
import {OnDestroy, OnInit} from "@angular/core";
import {ISubscription} from "rxjs/Subscription";
import {FEED_DEFINITION_STATE_NAME} from "../define-feed-states"

export abstract class AbstractFeedStepComponent implements OnInit, OnDestroy {


    public feed: FeedModel;

    public step : Step;

    public abstract getStepName() :string;


    constructor(protected  defineFeedService:DefineFeedService, protected stateService:StateService) {


    }
    ngOnInit() {
        this.initData();
        this.init();
    }

    ngOnDestroy(){
      this.destroy();
      this.defineFeedService.setFeed(this.feed)
    }


    /**
     * Initialize the component
     */
    public init(){

    }

    public destroy(){

    }



    public initData(){
       this.feed =this.defineFeedService.getFeed();

        if(this.feed == undefined) {
            this.stateService.go(FEED_DEFINITION_STATE_NAME+".select-template")
        }
        else {
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
}