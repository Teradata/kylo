import {Input} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {Observable} from "rxjs/Observable";

import {Feed} from "../../../../../model/feed/feed.model";
import {SaveFeedResponse} from "../../../model/save-feed-response.model";
import {DefineFeedService} from "../../../services/define-feed.service";
import {FeedLoadingService} from "../../../services/feed-loading-service";
import {FeedItemInfoService} from "./feed-item-info.service";


export abstract class AbstractFeedInfoItemComponent {

    @Input()
    editing: boolean;

    @Input()
    feed: Feed;

    readonlySchedule: string = '';

    valid:boolean = true;

    formGroup: FormGroup;

    protected constructor(protected defineFeedService: DefineFeedService, protected feedItemInfoService: FeedItemInfoService, private feedLoadingService: FeedLoadingService) {
        this.initForm();
    }

    initForm() {
        this.formGroup = new FormGroup({})
    }

    /**
     * Called when save is successful
     * override and handle the callback
     * @param {SaveFeedResponse} response
     */
    onSaveSuccess(response: SaveFeedResponse) {
        this.hideProgress();
    }

    /**
     * called when failed to save
     * Override and handle callback
     * @param {SaveFeedResponse} response
     */
    onSaveFail(response: SaveFeedResponse) {
        this.hideProgress();
    }

    showProgress() {
        this.feedLoadingService.registerLoading()
    }

    hideProgress() {
        this.feedLoadingService.resolveLoading()
    }

    /**
     * call the service to save the feed
     * @param {} feed
     */
    saveFeed(feed: Feed): Observable<SaveFeedResponse> {
        let observable = this.defineFeedService.saveFeed(feed);
        observable.subscribe(
            (response: SaveFeedResponse) => {
                if (response.success) {
                    this.feed = response.feed;
                    this.onSaveSuccess(response);
                    this.editing = false;
                    this.feedItemInfoService.savedFeed(response);
                } else {
                    this.onSaveFail(response);
                }
            },
            error => this.onSaveFail(error)
        );
        return observable;
    }
}
