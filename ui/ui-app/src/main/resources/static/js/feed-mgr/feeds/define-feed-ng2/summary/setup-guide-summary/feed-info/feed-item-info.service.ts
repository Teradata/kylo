import {Subject} from "rxjs/Subject";
import {PartialObserver} from "rxjs/Observer";
import {SaveFeedResponse} from "../../../model/save-feed-response.model";
import {ISubscription} from "rxjs/Subscription";
import {Injectable} from "@angular/core";
import {FeedInfoItemComponent} from "./feed-info-item.component";

@Injectable()
export class FeedItemInfoService{

    itemSavedSubject :Subject<SaveFeedResponse> = new Subject<SaveFeedResponse>();

    activeEdit:FeedInfoItemComponent

    constructor(){

    }

    isActiveEdit(component:FeedInfoItemComponent){
        return this.activeEdit && this.activeEdit == component;
    }

    clearActiveEdit(cancel:boolean = true){
        if(this.activeEdit && cancel){
            this.activeEdit.onCancel();
        }
        this.activeEdit = undefined;
    }
    setActiveEdit(activeEdit:FeedInfoItemComponent){
        this.clearActiveEdit();
        this.activeEdit = activeEdit;
    }

    subscribe(o:PartialObserver<SaveFeedResponse>):ISubscription{
        return this.itemSavedSubject.subscribe(o)
    }


    savedFeed(response:SaveFeedResponse){
        this.itemSavedSubject.next(response);
    }
}