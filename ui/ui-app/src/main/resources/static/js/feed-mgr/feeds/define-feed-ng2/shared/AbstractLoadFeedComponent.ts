import {OnInit, Input, OnDestroy, Output, EventEmitter} from "@angular/core";
import {FeedLoadingService} from "../services/feed-loading-service";
import {Observable} from "rxjs/Observable";
import {Feed, LoadMode} from "../../../model/feed/feed.model";
import {StateService} from "@uirouter/angular";
import {DefineFeedService, FeedEditStateChangeEvent} from "../services/define-feed.service";
import {Step} from "../../../model/feed/feed-step.model";
import {FEED_DEFINITION_STATE_NAME} from "../../../model/feed/feed-constants";
import {FeedSideNavService} from "./feed-side-nav.service";
import {ISubscription} from "rxjs/Subscription";
import "rxjs/add/observable/of";


export abstract class AbstractLoadFeedComponent implements OnInit,OnDestroy {

    @Input() stateParams: any;

    public loadingFeed:boolean;

    @Input()
    public feed?:Feed;

    /**
     * Should we load the
     */
    @Input()
    loadMode?:LoadMode;

    @Output()
    public feedChange = new EventEmitter<Feed>()

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
        this.loadMode = this.stateParams ? this.stateParams.loadMode : LoadMode.LATEST;
        this.initializeFeed(this.feedId).subscribe((feed:any) => {
            this.init();
        });

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
        if(this.feed) {
            this.feed.readonly = event.readonly;
            this.feed.allowEdit = event.allowEdit;
            this.feedChange.emit(this.feed)
        }
    }



    private loadFeed(feedId:string) :Observable<Feed>{
        this.registerLoading();
      let observable = this.feedLoadingService.loadFeed(feedId, this.loadMode,false);
      observable.subscribe((feedModel:Feed) => {
            this.feed = feedModel;
            this._setFeedState();
            this.onFeedLoaded(this.feed);
            this.loadingFeed = false;
            this.resolveLoading();
            this.feedChange.emit(this.feed)
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
            this.defineFeedService.markFeedAsEditable();
        }
    }

    initializeFeed(feedId:string):Observable<any>{

        let linkName =this.getLinkName();
        if(linkName && linkName != ''){
            //attempt to select it
            this.feedSideNavService.selectLinkByName(linkName);
        }
        if(this.feed == undefined) {
            let feed = this.defineFeedService.getFeed();

            if((feed && feedId && (feed.id != feedId || feed.loadMode !=this.loadMode))|| (feed == undefined && feedId != undefined)) {
               return this.loadFeed(feedId);
            }
            else if( feed != undefined){
                this.feed = feed;
                this._setFeedState();
                return Observable.of(this.feed)
            }
            else {
                this.stateService.go(FEED_DEFINITION_STATE_NAME+ ".select-template")
                return Observable.of({})
            }
        }
        else {
            this._setFeedState();
            return Observable.of(this.feed)
        }
    }

    /**
     * Called once feed is loaded
     * @param {Feed} feed
     */
    onFeedLoaded(feed: Feed) {

    }
}