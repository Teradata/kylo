import {OnInit, Input, OnDestroy} from "@angular/core";
import {FeedLoadingService} from "../services/feed-loading-service";
import {Observable} from "rxjs/Observable";
import {Feed} from "../../../model/feed/feed.model";
import {StateService} from "@uirouter/angular";
import {DefineFeedService, FeedEditStateChangeEvent} from "../services/define-feed.service";
import {Step} from "../../../model/feed/feed-step.model";
import {FEED_DEFINITION_STATE_NAME} from "../../../model/feed/feed-constants";
import {FeedSideNavService} from "./feed-side-nav.service";
import {ISubscription} from "rxjs/Subscription";


export abstract class AbstractLoadFeedComponent implements OnInit,OnDestroy {

    @Input() stateParams: any;

    public loadingFeed:boolean;

    public feed: Feed;

    public feedId: string;

    private feedEditStateChangeEvent:ISubscription;

    protected  constructor(protected feedLoadingService:FeedLoadingService, protected stateService: StateService, protected defineFeedService : DefineFeedService, protected feedSideNavService:FeedSideNavService){
        this.feedEditStateChangeEvent = this.defineFeedService.subscribeToFeedEditStateChangeEvent(this.onFeedEditStateChange.bind(this))
    }

    public init(){

    }
    public destroy(){

    }
    ngOnInit(){
        this.feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.initializeFeed(this.feedId);
        this.init();
    }

    ngOnDestroy(){
        if(this.feedEditStateChangeEvent){
            this.feedEditStateChangeEvent.unsubscribe();
        }
        this.destroy();
    }

    /**
     * return the label of the side nav link
     * @return {string}
     */
    getLinkName():string {
        return "";
    }


    onFeedEditStateChange(event:FeedEditStateChangeEvent){
        console.log("FEED STATE CHANGED!!!!",event)
        if(event.readonly){
            this.feed.readonly = true;
        }
        else {
            this.feed.readonly = false;
        }
    }



    private loadFeed(feedId:string) :Observable<Feed>{
        this.registerLoading();
      let observable = this.feedLoadingService.loadFeed(feedId);
      observable.subscribe((feedModel:Feed) => {
            this.feed = feedModel;
            this._setFeedState();
            this.onFeedLoaded(this.feed);
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
         if(this.feed.isNew()){
            //make editable if this is a new feed
            this.feed.readonly = false;
        }
    }

    initializeFeed(feedId:string){

        let linkName =this.getLinkName();
        if(linkName && linkName != ''){
            //attempt to select it
            this.feedSideNavService.selectLinkByName(linkName);
        }
        if(this.feed == undefined) {
            let feed = this.defineFeedService.getFeed();
            if ((feed && feedId && feed.id != feedId) || (feed == undefined && feedId != undefined)) {
                this.loadFeed(feedId);
            }
            else if( feed != undefined){
                this.feed = feed;
                this._setFeedState();
            }
            else {
                this.stateService.go(FEED_DEFINITION_STATE_NAME+ ".select-template")
            }
        }
        else {
            this._setFeedState();
        }
    }

    /**
     * Called once feed is loaded
     * @param {Feed} feed
     */
    onFeedLoaded(feed: Feed) {

    }
}