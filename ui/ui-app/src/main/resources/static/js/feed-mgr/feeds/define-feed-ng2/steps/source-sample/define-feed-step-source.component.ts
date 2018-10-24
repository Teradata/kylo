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

    }

    /**
     * Should the skip header row option be shown?
     * @returns {boolean}
     */
    allowSkipHeaderOption(): boolean {
        return true;
    }

    destroy() {

    }

    public applyUpdatesToFeed(): (Observable<any> | boolean | null) {
        let inputControl = this.feedPropertyNiFiComponent.inputProcessorControl;
        if(inputControl.invalid || this.sourcePropertiesForm.invalid){
            this.step.validator.hasFormErrors = true;
            //show the errors
            inputControl.markAsTouched();
            if(this.sourcePropertiesForm.invalid){
                FormGroupUtil.touchFormControls(this.sourcePropertiesForm);
            }
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



    cancelFeedEdit(){
        //reassign the propertiesInitialized flag when canceling edit
        let propertiesInitialized = this.feed.propertiesInitialized;
        super.cancelFeedEdit();
        this.feed.propertiesInitialized = propertiesInitialized;

    }

}
