import {Component, Injector, Input, ViewChild} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {TranslateService} from '@ngx-translate/core';
import {StateService} from "@uirouter/angular";

import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {VisualQueryStepperComponent} from "../../../../visual-query/visual-query-stepper.component";
import {DefineFeedService} from "../../services/define-feed.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";

@Component({
    selector: "define-feed-step-wrangler",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/wrangler/define-feed-step-wrangler.component.html"
})
export class DefineFeedStepWranglerComponent extends AbstractFeedStepComponent {

    @Input() stateParams: any;

    @ViewChild("visualQuery")
    visualQuery: VisualQueryStepperComponent;

    constructor(defineFeedService: DefineFeedService,
                stateService: StateService,
                private _translateService: TranslateService,
                private $$angularInjector: Injector, feedLoadingService: FeedLoadingService,
                dialogService: TdDialogService) {
        super(defineFeedService, stateService, feedLoadingService, dialogService);
    }

    init() {
        super.init();
    }

    getStepName() {
        return FeedStepConstants.STEP_WRANGLER;
    }
}
