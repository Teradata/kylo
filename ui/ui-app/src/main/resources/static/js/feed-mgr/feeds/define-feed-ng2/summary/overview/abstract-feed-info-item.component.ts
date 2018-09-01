import {FormGroup} from "@angular/forms";
import {Component, EventEmitter, Input, OnInit, Output, ViewChild, ViewContainerRef} from "@angular/core";
import {Feed} from "../../../../model/feed/feed.model";
import {DefineFeedService} from "../../services/define-feed.service";
import {FeedScheduleComponent} from "../../feed-schedule/feed-schedule.component";
import {FeedConstants} from "../../../../services/FeedConstants";
import {SaveFeedResponse} from "../../model/save-feed-response.model";
import {Observable} from "rxjs/Observable";
import {FeedItemInfoService} from "./feed-item-info.service";


export abstract class AbstractFeedInfoItemComponent {

    @Input()
    editing:boolean;

    @Input()
    feed:Feed;


    readonlySchedule:string = '';

    formGroup:FormGroup;

    protected  constructor(protected defineFeedService:DefineFeedService, protected feedItemInfoService:FeedItemInfoService){
        this.formGroup = new FormGroup({})
    }

    /**
     * Called when save is successful
     * override and handle the callback
     * @param {SaveFeedResponse} response
     */
    onSaveSuccess(response:SaveFeedResponse){

    }

    /**
     * called when failed to save
     * Override and handle callback
     * @param {SaveFeedResponse} response
     */
    onSaveFail(response:SaveFeedResponse){

    }


    /**
     * call the service to save the feed
     * @param {} feed
     */
    saveFeed(feed:Feed) :Observable<SaveFeedResponse>{

        let observable = this.defineFeedService.saveFeed(feed);
        observable.subscribe((response:SaveFeedResponse) => {
            if(response.success) {
                this.feed = response.feed;
                this.onSaveSuccess(response);
                this.editing = false;
                this.feedItemInfoService.savedFeed(response);
            }
            else {
                this.onSaveFail(response);
            }

         });
   return observable;

}


}