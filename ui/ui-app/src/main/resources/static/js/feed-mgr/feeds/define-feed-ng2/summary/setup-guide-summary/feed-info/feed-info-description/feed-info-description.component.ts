import {FormControl, FormGroup} from "@angular/forms";
import {Component, EventEmitter, Input, OnInit, Output, ViewChild, ViewContainerRef} from "@angular/core";
import {Feed} from "../../../../../../model/feed/feed.model";
import {DefineFeedService} from "../../../../services/define-feed.service";
import {AbstractFeedInfoItemComponent} from "../abstract-feed-info-item.component";
import {FeedItemInfoService} from "../feed-item-info.service";
import {FeedLoadingService} from "../../../../services/feed-loading-service";


@Component({
    selector:"feed-info-description",
    templateUrl:"./feed-info-description.component.html"
})
export class FeedInfoDescriptionComponent  extends AbstractFeedInfoItemComponent implements OnInit{

    constructor( defineFeedService:DefineFeedService,  feedItemInfoService:FeedItemInfoService, feedLoadingService:FeedLoadingService){
        super(defineFeedService,feedItemInfoService, feedLoadingService)
    }

    ngOnInit(){
        this.formGroup.addControl("description", new FormControl(this.feed.description));
    }

    save(){
        this.showProgress();
        let values = this.formGroup.value;
        this.feed.description = values.description;
        this.saveFeed(this.feed);
    }

    cancel(){
        this.formGroup.reset({"description": this.feed.description});
    }

}