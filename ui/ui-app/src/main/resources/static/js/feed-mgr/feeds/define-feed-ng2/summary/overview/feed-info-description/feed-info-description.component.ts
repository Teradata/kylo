import {FormControl, FormGroup} from "@angular/forms";
import {Component, EventEmitter, Input, OnInit, Output, ViewChild, ViewContainerRef} from "@angular/core";
import {Feed} from "../../../../../model/feed/feed.model";
import {DefineFeedService} from "../../../services/define-feed.service";
import {AbstractFeedInfoItemComponent} from "../abstract-feed-info-item.component";
import {FeedItemInfoService} from "../feed-item-info.service";


@Component({
    selector:"feed-info-description",
    templateUrl:"js/feed-mgr/feeds/define-feed-ng2/summary/overview/feed-info-description/feed-info-description.component.html"
})
export class FeedInfoDescriptionComponent  extends AbstractFeedInfoItemComponent implements OnInit{

    constructor( defineFeedService:DefineFeedService,  feedItemInfoService:FeedItemInfoService){
        super(defineFeedService,feedItemInfoService)
    }

    ngOnInit(){
        this.formGroup.addControl("description", new FormControl(this.feed.description));
    }

    save(){
        let values = this.formGroup.value;
        this.feed.description = values.description;
        this.defineFeedService.saveFeed(this.feed);
        this.saveFeed(this.feed);
    }

    cancel(){
        this.formGroup.reset({"description": this.feed.description});
    }

}