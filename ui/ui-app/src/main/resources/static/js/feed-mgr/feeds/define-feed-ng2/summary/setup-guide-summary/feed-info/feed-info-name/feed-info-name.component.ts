import {FormGroup} from "@angular/forms";
import {Component, EventEmitter, Input, Output, ViewChild, ViewContainerRef} from "@angular/core";
import {Feed} from "../../../../../../model/feed/feed.model";
import {DefineFeedService} from "../../../../services/define-feed.service";
import {SystemFeedNameComponent} from "../../../../shared/system-feed-name.component";
import {AbstractFeedInfoItemComponent} from "../abstract-feed-info-item.component";
import {FeedItemInfoService} from "../feed-item-info.service";

@Component({
    selector:"feed-info-name",
    templateUrl:"js/feed-mgr/feeds/define-feed-ng2/summary/setup-guide-summary/feed-info/feed-info-name/feed-info-name.component.html"
})
export class FeedInfoNameComponent  extends AbstractFeedInfoItemComponent {

    @ViewChild("systemFeedName")
    private systemFeedName:SystemFeedNameComponent;


    constructor( defineFeedService:DefineFeedService,  feedItemInfoService:FeedItemInfoService){
        super(defineFeedService,feedItemInfoService)
    }

    save() {
        let values = this.formGroup.value;
        this.feed.feedName = values.feedName;
        this.feed.systemFeedName = values.systemFeedName;
        this.saveFeed(this.feed);
    }


    cancel(){
        this.systemFeedName.resetForm();
    }

}