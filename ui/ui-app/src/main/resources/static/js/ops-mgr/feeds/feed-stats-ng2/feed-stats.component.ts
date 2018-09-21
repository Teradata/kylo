import {Component, Input} from "@angular/core";
import { StateService } from "@uirouter/core";

@Component({
    selector: 'feed-stats',
    templateUrl: 'js/ops-mgr/feeds/feed-stats-ng2/feed-stats.component.html'
})
export class FeedStatsComponent {
    @Input()
    feedName: string;
    constructor(private stateService: StateService) {
        this.feedName = this.stateService.params.feedName;
    }
}