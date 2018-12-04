import {FormGroup} from "@angular/forms";
import {Component, EventEmitter, Input, OnInit, Output, ViewChild, ViewContainerRef} from "@angular/core";
import {Feed} from "../../../../../../model/feed/feed.model";
import {DefineFeedService} from "../../../../services/define-feed.service";
import {FeedScheduleComponent} from "../../feed-schedule/feed-schedule.component";
import {FeedConstants} from "../../../../../../services/FeedConstants";
import {SaveFeedResponse} from "../../../../model/save-feed-response.model";
import {FeedItemInfoService} from "../feed-item-info.service";
import {AbstractFeedInfoItemComponent} from "../abstract-feed-info-item.component";
import {FeedLoadingService} from "../../../../services/feed-loading-service";


@Component({
    selector:"feed-info-schedule",
    templateUrl:"./feed-info-schedule.component.html"
})
export class FeedInfoScheduleComponent extends AbstractFeedInfoItemComponent implements OnInit{

    @ViewChild("feedSchedule")
    feedSchedule: FeedScheduleComponent;

    readonlySchedule:string = '';
    readonlyScheduleList:string[];



    loading:boolean = false;

    constructor( defineFeedService:DefineFeedService,  feedItemInfoService:FeedItemInfoService, feedLoadingService:FeedLoadingService){
        super(defineFeedService,feedItemInfoService, feedLoadingService)
    }

    ngOnInit(){
        this.loading = true;
       this.updateReadOnlySchedule();
       this.validate();
       this.loading = false;

       this.formGroup.statusChanges.subscribe((valid:string) =>{
           this.valid = "VALID" == valid && (this.feedSchedule && this.feedSchedule.validate());
        });
       this.feedSchedule.scheduleForm.statusChanges.subscribe((valid:string) => {
           this.valid = "VALID" == valid && (this.feedSchedule && this.feedSchedule.validate());
       })
    }

    onEdit() {
        this.feedSchedule.reset(this.feed)
    }

    /**
     * When the readonlyScheduleList is undefined the readonlySchedule will be displayed
     */
    private updateReadOnlySchedule(){
        if(FeedConstants.SCHEDULE_STRATEGIES.TRIGGER_DRIVEN.value == this.feed.schedule.schedulingStrategy){
            this.readonlyScheduleList = this.feed.schedule.preconditions && this.feed.schedule.preconditions.length >0 ? this.feed.schedule.preconditions.map(p => p.propertyValuesDisplayString): undefined;
            if(this.readonlyScheduleList == undefined){
                this.readonlySchedule = "Not defined";
            }
            else {
                this.readonlySchedule = FeedConstants.scheduleStrategyLabel(this.feed.schedule.schedulingStrategy)
            }
        }
        else {
            this.readonlyScheduleList = undefined;
            let scheduleDetails =  this.feed.schedule.schedulingPeriod;
            let cluster = this.feed.schedule.executionNode ? this.feed.schedule.executionNode : "";
            this.readonlySchedule = FeedConstants.scheduleStrategyLabel(this.feed.schedule.schedulingStrategy)+", "+scheduleDetails+" "+cluster;
        }


    }

    private validate(){
        if(this.feed) {
            this.valid = (this.feedSchedule && this.feedSchedule.validate());
            return this.valid;
        }
    }

    save(){
     this.showProgress();
     let valid = this.validate();
     if(valid) {
         this.saveFeed(this.feedSchedule.updateModel())
     }
    }

    onSaveSuccess(response:SaveFeedResponse){
        this.hideProgress()
        this.feedSchedule.reset(response.feed);
        this.updateReadOnlySchedule()
        this.validate();
    }

    cancel(){
        this.feedSchedule.reset(this.feed);
        this.validate();
    }

}