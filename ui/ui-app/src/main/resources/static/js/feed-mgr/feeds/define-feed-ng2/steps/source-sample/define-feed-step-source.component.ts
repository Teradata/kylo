import {Component, ViewChild} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {TdDialogService} from "@covalent/core/dialogs";
import {StateService} from "@uirouter/angular";
import {Observable} from "rxjs/Observable";

import {DatasetPreviewStepperSavedEvent} from "../../../../catalog-dataset-preview/preview-stepper/dataset-preview-stepper.component";
import {CatalogService} from "../../../../catalog/api/services/catalog.service";
import {SelectionService} from "../../../../catalog/api/services/selection.service";
import {PreviewDataSet} from "../../../../catalog/datasource/preview-schema/model/preview-data-set";
import {PreviewSchemaService} from "../../../../catalog/datasource/preview-schema/service/preview-schema.service";
import {FEED_DEFINITION_SECTION_STATE_NAME} from "../../../../model/feed/feed-constants";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {DefineFeedService, FeedEditStateChangeEvent} from "../../services/define-feed.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {FeedNifiPropertiesComponent} from "../feed-details/feed-nifi-properties.component";
import {DefineFeedSourceSampleService} from "./define-feed-source-sample.service";
import {ShowCatalogCanceledEvent} from "./define-feed-step-source-sample.component";
import {SKIP_SOURCE_CATALOG_KEY} from "../../../../model/feed/feed.model";



@Component({
    selector: "define-feed-step-source",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/source-sample/define-feed-step-source.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/source-sample/define-feed-step-source.component.html"
})
export class DefineFeedStepSourceComponent extends AbstractFeedStepComponent {

    static LOADER: string = "DefineFeedStepSourceComponent.LOADER"
    /**
     * the parent form group for everything
     */
    sourceForm: FormGroup;

    /**
     * Form Group for the source properties
     */
    sourcePropertiesForm: FormGroup;

    @ViewChild("feedNifiPropertiesComponent")
    feedPropertyNiFiComponent: FeedNifiPropertiesComponent

    feedDefintionDatasourceState: string = FEED_DEFINITION_SECTION_STATE_NAME + ".datasource"

    showCatalog: boolean

    showSkipSourceButton:boolean;


    constructor(defineFeedService: DefineFeedService, stateService: StateService, private selectionService: SelectionService,
                dialogService: TdDialogService,
                feedLoadingService: FeedLoadingService,
                feedSideNavService: FeedSideNavService,
                private previewSchemaService: PreviewSchemaService,
                private defineFeedSourceSampleService: DefineFeedSourceSampleService,
                private catalogService: CatalogService) {
        super(defineFeedService, stateService, feedLoadingService, dialogService, feedSideNavService);
        this.sourceForm = new FormGroup({});
        this.sourcePropertiesForm = new FormGroup({})
        this.defineFeedService.ensureSparkShell();

    }

    getStepName() {
        return FeedStepConstants.STEP_FEED_SOURCE;
    }

    onEdit() {
        this.feed.readonly = false;
        this.defineFeedService.markFeedAsEditable();
    }

    public feedStateChange(event: FeedEditStateChangeEvent) {
        this.feed.readonly = event.readonly;
        this.feed.accessControl = event.accessControl;
    }


    init() {
        let paths = this.feed.getSourcePaths();
        //if this was a feed prior to 0.9.2 it will not have any source paths defined.
        //check the sourceTableSchema and see if that exists
        let sourceSchemaDefined = this.feed.table.sourceTableSchema && this.feed.table.sourceTableSchema.isDefined();
        let userAcknowledgedContinueWithoutSource = this.step.getPropertyAsBoolean(SKIP_SOURCE_CATALOG_KEY);
        //always show the catalog if no paths are available to preview
        if (!userAcknowledgedContinueWithoutSource && !sourceSchemaDefined && (paths == undefined || paths.length == 0)) {
            this.showSkipSourceButton = true;
            this.showCatalog = true;

        }

    }

    destroy() {

    }

    public applyUpdatesToFeed(): (Observable<any> | null) {
        if (this.feedPropertyNiFiComponent) {
            this.feedPropertyNiFiComponent.applyUpdatesToFeed();
        }
        return null;
    }


    onFormInitialized() {
        this.subscribeToFormChanges(this.sourceForm);
    }

    onCatalogCanceled($event:ShowCatalogCanceledEvent){
        if($event.skip){
            //mark it in the metadata
            this.step.addProperty(SKIP_SOURCE_CATALOG_KEY,true);
        }
    }

    onSampleSourceSaved(previewEvent: DatasetPreviewStepperSavedEvent) {
        console.log("SAVE SAMPLE ",previewEvent)
        let previews: PreviewDataSet[] = previewEvent.previews;
        if (previews && previews.length) {
            let feedDataSets = this.feed.sourceDataSets;
            //check to see if schema differs
            if (feedDataSets && feedDataSets.length > 0) {
                let feedDatasetKeys = feedDataSets.map(ds => ds.id).sort().toString();
                let newDatasetKeys = previews.map(ds => ds.key).sort().toString();
                if (feedDatasetKeys != "" && feedDatasetKeys != newDatasetKeys) {
                    //WARN different datasets
                    this.dialogService.openConfirm({
                        message: 'The dataset you have selected differs from the one existing on this feed. Switching the source will result in a new target schema.  Are you sure you want to do this?',
                        disableClose: true,
                        title: 'Confirm source dataset change',
                    }).afterClosed().subscribe((accept: boolean) => {
                        if (accept) {
                            this._setSourceAndTargetAndSaveFeed(previewEvent);
                        } else {
                            // no op
                        }
                    });
                }
            }
            else {
                this._setSourceAndTargetAndSaveFeed(previewEvent);
            }
        }
        else {
            this._setSourceAndTargetAndSaveFeed(previewEvent)
        }


    }

    private _setSourceAndTargetAndSaveFeed(event: DatasetPreviewStepperSavedEvent) {


        /**
         * Save the feed
         */
        let saveFeed = () => {
            //TODO validate and mark completion as needed

            this.step.setComplete(true)
            this.defineFeedService.saveFeed(this.feed).subscribe(result => {
                this.feedLoadingService.resolveLoading()
                this.showCatalog = false;
            }, error1 => {
                this.step.setComplete(false)
                this.feedLoadingService.resolveLoading()
                this.dialogService.openAlert({
                    message: "There was an error saving the source selection " + error1,
                    title: "Error saving source selection"
                });
            });
        }


        let previews = event.previews;
        let singleSelection = event.singleSelection;
        if (previews && previews.length) {
            if (singleSelection) {
                const sourceDataSet = previews.map((ds: PreviewDataSet) => ds.toSparkDataSet())[0];
                if (sourceDataSet.dataSource && sourceDataSet.dataSource.connector && sourceDataSet.dataSource.connector.pluginId) {
                    this.catalogService.getConnectorPlugin(sourceDataSet.dataSource.connector.pluginId)
                        .subscribe(plugin => {
                            this.feed.setSourceDataSetAndUpdateTarget(sourceDataSet, undefined, plugin)
                            saveFeed();
                        });
                } else {
                    this.feed.setSourceDataSetAndUpdateTarget(sourceDataSet);
                    saveFeed();
                }
            }
        }
        else {
            //set the source and target to empty
            this.feed.setSourceDataSetAndUpdateTarget(null);
            saveFeed();
        }

    }

}
