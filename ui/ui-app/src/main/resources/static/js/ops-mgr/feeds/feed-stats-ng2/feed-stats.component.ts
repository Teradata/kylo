import {Component, Input} from "@angular/core";
import { StateService } from "@uirouter/core";

@Component({
    selector: 'feed-stats',
    templateUrl: './feed-stats.component.html'
})
export class FeedStatsComponent {
    @Input()
    feedName: string;
    constructor(private stateService: StateService) {
        this.feedName = this.stateService.params.feedName;
    }
}