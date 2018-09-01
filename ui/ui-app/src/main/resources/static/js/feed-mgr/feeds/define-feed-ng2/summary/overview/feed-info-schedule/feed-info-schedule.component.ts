import {FormGroup} from "@angular/forms";
import {Component, EventEmitter, Input, OnInit, Output, ViewChild, ViewContainerRef} from "@angular/core";
import {Feed} from "../../../../../model/feed/feed.model";
import {DefineFeedService} from "../../../services/define-feed.service";
import {FeedScheduleComponent} from "../../../feed-schedule/feed-schedule.component";
import {FeedConstants} from "../../../../../services/FeedConstants";
import {SaveFeedResponse} from "../../../model/save-feed-response.model";
import {FeedItemInfoService} from "../feed-item-info.service";
import {AbstractFeedInfoItemComponent} from "../abstract-feed-info-item.component";


@Component({
    selector:"feed-info-schedule",
    templateUrl:"js/feed-mgr/feeds/define-feed-ng2/summary/overview/feed-info-schedule/feed-info-schedule.component.html"
})
export class FeedInfoScheduleComponent extends AbstractFeedInfoItemComponent implements OnInit{

    @ViewChild("feedSchedule")
    feedSchedule: FeedScheduleComponent;

    readonlySchedule:string = '';

    constructor( defineFeedService:DefineFeedService,  feedItemInfoService:FeedItemInfoService){
       super(defineFeedService,feedItemInfoService)
    }

    ngOnInit(){
       this.updateReadOnlySchedule();
    }

    private updateReadOnlySchedule(){
        this.readonlySchedule = FeedConstants.scheduleStrategyLabel(this.feed.schedule.schedulingStrategy)+", "+this.feed.schedule.schedulingPeriod;
    }

    save(){
     this.saveFeed(this.feedSchedule.updateModel())
    }

    onSaveSuccess(response:SaveFeedResponse){
        this.feedSchedule.reset(response.feed);
        this.updateReadOnlySchedule()
    }

    cancel(){
        this.feedSchedule.reset(this.feed);
    }

}