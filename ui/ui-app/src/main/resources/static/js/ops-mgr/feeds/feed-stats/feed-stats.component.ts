import { Component } from "@angular/core";
import { StateService } from "@uirouter/core";

@Component({
    selector: 'feed-stats',
    templateUrl: 'js/ops-mgr/feeds/feed-stats/feed-stats.html'
})
export class FeedStatsComponent {
    feedName: string;
    constructor(private stateService: StateService) {
        this.feedName = this.stateService.params.feedName;
    }
}
