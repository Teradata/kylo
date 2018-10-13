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
import {ShowCatalogCanceledEvent} from "./define-feed-step-source-sample.component";
import {SKIP_SOURCE_CATALOG_KEY} from "../../../../model/feed/feed.model";
import {PreviewFileDataSet} from "../../../../catalog/datasource/preview-schema/model/preview-file-data-set";
import {FormGroupUtil} from "../../../../../services/form-group-util";
import {DefineFeedSourceSampleService} from "./define-feed-source-sample.service";



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

    /**
     * Should the catalog browser be rendered
     */
    showCatalog: boolean

    /**
     * When browsing the catalog should the "skip" button appear allowing the user to skip supplying a source sample
     */
    showSkipSourceButton:boolean;

    /**
     * Show the source card with the catalog browser be rendered?
     * For Data Transformation feeds this will be set to false since the wrangler is responsible for defining the source sample
     * @type {boolean}
     */
    showSourceSample:boolean = true;


    constructor(defineFeedService: DefineFeedService, stateService: StateService, private selectionService: SelectionService,
                dialogService: TdDialogService,
                feedLoadingService: FeedLoadingService,
                feedSideNavService: FeedSideNavService,
                private previewSchemaService: PreviewSchemaService,
                private catalogService: CatalogService,
                private defineFeedSourceSampleService:DefineFeedSourceSampleService) {
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
        if ( !sourceSchemaDefined && (paths == undefined || paths.length == 0)) {
            this.showSkipSourceButton = true;
            this.showCatalog = !userAcknowledgedContinueWithoutSource;
        }
        if(this.feed.isDataTransformation()){
            this.showSourceSample = false;
            this.showCatalog = false;
        }
        else {
            this.showSourceSample = true;
        }

    }

    /**
     * Should the skip header row option be shown?
     * @returns {boolean}
     */
    allowSkipHeaderOption(): boolean {
        return this.feed.schemaParser && this.feed.schemaParser.allowSkipHeader;
    }

    destroy() {

    }

    public applyUpdatesToFeed(): (Observable<any> | boolean | null) {
        let inputControl = this.feedPropertyNiFiComponent.inputProcessorControl;
        if(inputControl.invalid){
            this.step.validator.hasFormErrors = true;
            //show the errors
            inputControl.markAsTouched()
            return false;
        }
        else {
            this.step.validator.hasFormErrors = false;
            if (this.feedPropertyNiFiComponent) {
                this.feedPropertyNiFiComponent.applyUpdatesToFeed();
            }
            return true;
        }


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
                else {
                    this._setSourceAndTargetAndSaveFeed(previewEvent);
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
        this.feedLoadingService.registerLoading();


        let _save = () => {
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

        /**
         * Save the feed
         */
        let saveFeed = () => {
            if(event.previews[0] instanceof PreviewFileDataSet) {

              this.defineFeedSourceSampleService.parseTableSettings((<PreviewFileDataSet>event.previews[0])).subscribe( (response:any)=> {
                    console.log("SCHEMA RESPONSE ",response)
                    this.feed.table.feedFormat = response.hiveFormat;
                    this.feed.table.structured = response.structured;
                    this.feed.table.feedTblProperties = response.serdeTableProperties;
                    _save();
                });
            }
            else {
            _save();
            }
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

    cancelFeedEdit(){
        //reassign the propertiesInitialized flag when canceling edit
        let propertiesInitialized = this.feed.propertiesInitialized;
        super.cancelFeedEdit();
        this.feed.propertiesInitialized = propertiesInitialized;

    }

}
