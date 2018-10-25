import {Component, ViewChild} from "@angular/core";

import {DefineFeedService} from "../../../../services/define-feed.service";
import {FeedLoadingService} from "../../../../services/feed-loading-service";
import {SystemFeedNameComponent} from "../../../../shared/system-feed-name.component";
import {AbstractFeedInfoItemComponent} from "../abstract-feed-info-item.component";
import {FeedItemInfoService} from "../feed-item-info.service";

@Component({
    selector: "feed-info-name",
    templateUrl: "./feed-info-name.component.html"
})
export class FeedInfoNameComponent extends AbstractFeedInfoItemComponent {

    @ViewChild("systemFeedName")
    private systemFeedName: SystemFeedNameComponent;

    constructor(defineFeedService: DefineFeedService, feedItemInfoService: FeedItemInfoService, feedLoadingService: FeedLoadingService) {
        super(defineFeedService, feedItemInfoService, feedLoadingService)
    }

    save() {
        this.showProgress();
        let values = this.formGroup.value;
        this.feed.feedName = values.feedName;
        if (values.systemFeedName) {
            this.feed.systemFeedName = values.systemFeedName;
        }
        this.saveFeed(this.feed);
    }

    cancel() {
        this.systemFeedName.resetForm();
    }
}
