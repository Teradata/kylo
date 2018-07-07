import {OnInit, Input} from "@angular/core";
import {FeedLoadingService} from "../services/feed-loading-service";
import {Observable} from "rxjs/Observable";
import {FeedModel, Step} from "../model/feed.model";
import {FEED_DEFINITION_STATE_NAME} from "../define-feed-states";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../services/define-feed.service";


export abstract class AbstractLoadFeedComponent  {

    public loadingFeed:boolean;

    public feed: FeedModel;

    public selectedStep : Step;

    protected  constructor(protected feedLoadingService:FeedLoadingService, protected stateService: StateService, protected defineFeedService : DefineFeedService){

    }

    private loadFeed(feedId:string) :Observable<FeedModel>{
        this.registerLoading();
      let observable = this.feedLoadingService.loadFeed(feedId);
      observable.subscribe((feedModel:FeedModel) => {
            this.feed = this.defineFeedService.getFeed();
            this._setFeedState();
            this.loadingFeed = false;
            this.resolveLoading();
        },(error:any) =>{
          this.resolveLoading();
      });
      return observable;
    }


    registerLoading(): void {
        this.feedLoadingService.registerLoading();
    }

    resolveLoading(): void {
        this.feedLoadingService.resolveLoading();
    }

    private _setFeedState(){
        this.selectedStep = this.defineFeedService.getCurrentStep();
        if(this.feed.isNew()){
            //make editable if this is a new feed
            this.feed.readonly = false;
        }
    }

    initializeFeed(feedId:string){
        if(this.feed == undefined) {
            let feed = this.defineFeedService.getFeed();
            if ((feed && feedId && feed.id != feedId) || (feed == undefined && feedId != undefined)) {
                this.loadFeed(feedId);
            }
            else {
                this.feed = feed;
                this._setFeedState();
            }
        }
        else {
            this._setFeedState();
        }
    }
}