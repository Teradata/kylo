import {Component, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {SLA_LINK} from "../../model/feed-link-constants";

export class Sla {
    id: string;
    name: string;
    description: string;
    feedNames: string;
    rules: Array<any> = [];
    canEdit: boolean = false;
    actionConfigurations: Array<any> = [];
    actionErrors: Array<any> = [];
    editable: boolean = false;
}


@Component({
    selector: "sla",
    styleUrls: ["./sla.component.scss"],
    templateUrl: "./sla.component.html"
})
export class SlaComponent extends AbstractLoadFeedComponent implements OnInit {

    static LINK_NAME = SLA_LINK;

    constructor(feedLoadingService: FeedLoadingService, stateService: StateService, defineFeedService: DefineFeedService, feedSideNavService: FeedSideNavService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
    }

    getLinkName() {
        return SlaComponent.LINK_NAME;
    }

    init() {

    }
}

