import {FormControl} from "@angular/forms";
import {Component, OnInit} from "@angular/core";
import {DefineFeedService} from "../../../../services/define-feed.service";
import {AbstractFeedInfoItemComponent} from "../abstract-feed-info-item.component";
import {FeedLoadingService} from "../../../../services/feed-loading-service";
import {InfoItemService} from '../../../../../../shared/info-item/item-info.service';


@Component({
    selector:"feed-info-description",
    templateUrl:"./feed-info-description.component.html"
})
export class FeedInfoDescriptionComponent  extends AbstractFeedInfoItemComponent implements OnInit{

    constructor(defineFeedService:DefineFeedService, feedItemInfoService:InfoItemService, feedLoadingService:FeedLoadingService){
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