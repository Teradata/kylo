import {Component, Injector, Input, TemplateRef, ViewChild} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {TranslateService} from '@ngx-translate/core';
import {StateService} from "@uirouter/angular";
import {Observable} from "rxjs/Observable";
import {tap} from "rxjs/operators/tap";

import {AccessControlService} from "../../../../../services/AccessControlService";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {VisualQueryStepperComponent} from "../../../../visual-query/visual-query-stepper.component";
import {DefineFeedService} from "../../services/define-feed.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";

@Component({
    selector: "define-feed-step-wrangler",
    templateUrl: "./define-feed-step-wrangler.component.html"
})
export class DefineFeedStepWranglerComponent extends AbstractFeedStepComponent {

    @Input() stateParams: any;

    @ViewChild("toolbarActionTemplate")
    private toolbarActionTemplate: TemplateRef<any>;

    @ViewChild("visualQuery")
    visualQuery: VisualQueryStepperComponent;

    /**
     * Indicates if user has access to the data sources required to edit this transformation
     */
    allowEdit = false;

    constructor(defineFeedService: DefineFeedService,
                stateService: StateService,
                private _translateService: TranslateService,
                private $$angularInjector: Injector,
                feedLoadingService: FeedLoadingService,
                dialogService: TdDialogService,
                feedSideNavService: FeedSideNavService,
                private accessControlService: AccessControlService) {
        super(defineFeedService, stateService, feedLoadingService, dialogService, feedSideNavService);
    }

    init() {
        super.init();
        this.feed.dataTransformation.datasets = this.feed.sourceDataSets;

        if (this.accessControlService.isEntityAccessControlled()) {
            this.allowEdit = (this.feed.sourceDataSets || [])
                .map(dataSet => dataSet.dataSource != null
                    && this.accessControlService.hasEntityAccess(AccessControlService.ENTITY_ACCESS.DATASOURCE.ACCESS_DATASOURCE, dataSet.dataSource))
                .reduce((previous, current) => previous && current, true);
        } else {
            this.allowEdit = true;
        }
    }

    getStepName() {
        return FeedStepConstants.STEP_WRANGLER;
    }

    getToolbarTemplateRef(): TemplateRef<any> {
        return this.toolbarActionTemplate;
    }

    protected cancelFeedEdit() {
        super.cancelFeedEdit();
        // this.defineFeedService.sideNavStateChanged({opened:true})
        // this.goToSetupGuideSummary();
    }

    protected applyUpdatesToFeed(): (Observable<any> | boolean | null) {
        super.applyUpdatesToFeed();
        this.feed.sourceDataSets = this.feed.dataTransformation.datasets;
        this.feed.dataTransformation.catalogDataSourceIds = this.feed.dataTransformation.$catalogDataSources.map(ds => ds.id);

        let datasetIds = this.feed.dataTransformation.datasets ? this.feed.dataTransformation.datasets.map(ds => ds.id) : [];
        //put all the legacy datsourceids, datasetids, and catalogdatasourceids in the dataTransformation.datasourceIds property.
        // this is for backwards compatibility for 0.9.1 and earlier templates
        let allIds: string[] = [];
        allIds = allIds.concat(this.feed.dataTransformation.datasourceIds || [], this.feed.dataTransformation.catalogDataSourceIds, datasetIds);
        this.feed.dataTransformation.datasourceIds = allIds;
        if (this.feed.table.schemaChanged) {
            return this.dialogService.openConfirm({
                title: 'Error saving feed.  Table Schema Changed.',
                message: 'The table schema no longer matches the schema previously defined. This is invalid.  If you wish to modify the underlying schema (i.e. change some column names and/or types) please clone the feed as a new feed instead.',
                acceptButton: "Ignore",
                cancelButton: "Cancel"
            }).afterClosed().pipe(
                tap(value => {
                    if (value) {
                        // Ignore changes; restore table fields and policies
                        const originalFeed = this.defineFeedService.getFeed();
                        this.feed.table.setTableFields(originalFeed.table.feedDefinitionTableSchema.fields, originalFeed.table.feedDefinitionFieldPolicies);
                        this.feed.table.syncTableFieldPolicyNames();
                    }
                })
            );
        }
        else {
            return true;
        }
    }

    isFeedSaveInProgress(): boolean {
        return this.feedLoadingService.loadingFeed;
    }
}
