import {Component, Input} from "@angular/core";

import {DefineFeedService} from "../../../../services/define-feed.service";
import {FeedLoadingService} from "../../../../services/feed-loading-service";
import {AbstractFeedInfoItemComponent} from "../abstract-feed-info-item.component";
import {FeedItemInfoService} from "../feed-item-info.service";

@Component({
    selector: "feed-info-category",
    templateUrl: "./feed-info-category.component.html"
})
export class FeedInfoCategoryComponent extends AbstractFeedInfoItemComponent {

    @Input()
    editable: boolean;

    constructor(defineFeedService: DefineFeedService, feedItemInfoService: FeedItemInfoService, feedLoadingService: FeedLoadingService) {
        super(defineFeedService, feedItemInfoService, feedLoadingService)
    }

    onSaveSuccess() {
        this.hideProgress();
        this.cancel();
    }

    save() {
        this.showProgress();
        let values = this.formGroup.value;
        this.feed.category = values.category;
        this.saveFeed(this.feed);
    }

    cancel() {
        this.formGroup.reset({"category": this.feed.category});
    }
}
