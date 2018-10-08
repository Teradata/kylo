import {Component, Injector, Input, TemplateRef, ViewChild} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {TranslateService} from '@ngx-translate/core';
import {StateService} from "@uirouter/angular";
import {Observable} from "rxjs/Observable";

import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {VisualQueryStepperComponent} from "../../../../visual-query/visual-query-stepper.component";
import {DefineFeedService} from "../../services/define-feed.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {FeedSideNavService} from "../../services/feed-side-nav.service";

@Component({
    selector: "define-feed-step-wrangler",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/wrangler/define-feed-step-wrangler.component.html"
})
export class DefineFeedStepWranglerComponent extends AbstractFeedStepComponent {

    @Input() stateParams: any;

    @ViewChild("toolbarActionTemplate")
    private toolbarActionTemplate:TemplateRef<any>;

    @ViewChild("visualQuery")
    visualQuery: VisualQueryStepperComponent;

    constructor(defineFeedService: DefineFeedService,
                stateService: StateService,
                private _translateService: TranslateService,
                private $$angularInjector: Injector, feedLoadingService: FeedLoadingService,
                dialogService: TdDialogService, feedSideNavService:FeedSideNavService) {
        super(defineFeedService,stateService, feedLoadingService,dialogService, feedSideNavService);
    }

    init() {
        super.init();
        this.feed.dataTransformation.datasets = this.feed.sourceDataSets;
    }

    getStepName() {
        return FeedStepConstants.STEP_WRANGLER;
    }

    getToolbarTemplateRef(): TemplateRef<any> {
        return this.toolbarActionTemplate;
    }


    protected applyUpdatesToFeed():(Observable<any> | boolean | null) {
        super.applyUpdatesToFeed();
        this.feed.sourceDataSets = this.feed.dataTransformation.datasets;
        if(this.feed.table.schemaChanged){
            this.dialogService.openAlert({
                title: 'Error saving feed.  Table Schema Changed.',
                message:'The table schema no longer matches the schema previously defined. This is invalid.  If you wish to modify the underlying schema (i.e. change some column names and/or types) please clone the feed as a new feed instead.'
            });
            return false;
        }
        else {
            return true;
        }
    }
}
