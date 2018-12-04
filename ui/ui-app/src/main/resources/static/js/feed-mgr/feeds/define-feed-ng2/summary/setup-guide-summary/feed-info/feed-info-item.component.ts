import {Component, ContentChild, EventEmitter, Input, OnDestroy, Output} from "@angular/core";
import {Feed} from "../../../../../model/feed/feed.model";
import {FeedItemInfoService} from "./feed-item-info.service";
import {SaveFeedResponse} from "../../../model/save-feed-response.model";
import {ISubscription} from "rxjs/Subscription";


@Component({
    selector:"feed-info-item",
    styleUrls:["./feed-info-item.component.scss"],
    templateUrl:"./feed-info-item.component.html"
})
export class FeedInfoItemComponent implements OnDestroy{

    @Input()
    editing:boolean;

    @Input()
    editable:boolean;

    @Input()
    feed:Feed;

    @Input()
    icon:string;
    @Input()
    iconColor:string;

    @Input()
    name:string;

    @Input()
    description:string;

    @Input()
    readonlyList:string[];

    @Input()
    customReadonlyLayout:boolean = false;

    @Input()
    isSaveDisabled:boolean = false;

    @Output()
    edit:EventEmitter<any> = new EventEmitter<any>();

    @Output()
    save:EventEmitter<any> = new EventEmitter<any>();

    @Output()
    cancel:EventEmitter<any> = new EventEmitter<any>();

    feedItemSubscription:ISubscription;

    constructor( private feedItemInfoService:FeedItemInfoService){


    }
    ngOnDestroy(){
        if(this.feedItemInfoService.isActiveEdit(this)) {
            this.feedItemInfoService.clearActiveEdit();
        }
    }

    onItemSaved(response:SaveFeedResponse){
        if(response.success){
            this.editing = false;
            this.unsubscribe();
            this.feedItemInfoService.clearActiveEdit(false)
        }
    }

    onEdit(){
        this.feedItemInfoService.setActiveEdit(this);
        this.feedItemSubscription =  this.feedItemInfoService.subscribe(this.onItemSaved.bind(this))
        this.editing = true
        this.edit.emit();
    }

    onSave(){
        this.save.emit();
    }

    onCancel(){
        this.unsubscribe();
        this.editing = false
        this.cancel.emit();
    }

    private unsubscribe(){
        if(this.feedItemSubscription && this.feedItemSubscription != null){
            this.feedItemSubscription.unsubscribe();
        }
        this.feedItemSubscription = null;
    }
}