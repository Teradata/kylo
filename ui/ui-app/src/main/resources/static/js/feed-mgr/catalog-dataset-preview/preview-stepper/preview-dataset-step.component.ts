import {TdDialogService} from "@covalent/core/dialogs";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/map';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {SelectionService} from "../../catalog/api/services/selection.service";
import {DatasetCollectionStatus, PreviewDataSet} from "../../catalog/datasource/preview-schema/model/preview-data-set";
import {PreviewDataSetRequest} from "../../catalog/datasource/preview-schema/model/preview-data-set-request";
import {FileMetadataTransformResponse} from "../../catalog/datasource/preview-schema/model/file-metadata-transform-response";
import {Node} from "../../catalog/api/models/node";
import {DataSource} from "../../catalog/api/models/datasource";
import {PreviewJdbcDataSet} from "../../catalog/datasource/preview-schema/model/preview-jdbc-data-set";
import {Subject} from "rxjs/Subject";
import {PreviewHiveDataSet} from "../../catalog/datasource/preview-schema/model/preview-hive-data-set";
import {DatabaseObject, DatabaseObjectType} from "../../catalog/datasource/tables/database-object";
import {DatasetPreviewStepperService} from "./dataset-preview-stepper.service";
import {ISubscription} from "rxjs/Subscription";
import {TdLoadingService} from "@covalent/core/loading";
import {DatasetPreviewService, DataSourceChangedEvent, PreviewDataSetResultEvent} from "../../catalog/datasource/preview-schema/service/dataset-preview.service";
import {DatasetPreviewContainerComponent} from "../../catalog/datasource/preview-schema/preview/dataset-preview-container.component";
import {CatalogPreviewDatasetComponent} from "../../catalog/datasource/preview-schema/catalog-preview-dataset.component";
import {StateService} from "@uirouter/angular";
import {KyloRouterService} from "../../../services/kylo-router.service";



@Component({
    selector: "preview-dataset-step",
    templateUrl: "./preview-dataset-step.component.html",
    styleUrls:["./preview-dataset-step.component.scss"],
    changeDetection:ChangeDetectionStrategy.OnPush
})
export class PreviewDatasetStepComponent extends CatalogPreviewDatasetComponent {

    static LOADER = "PreviewDataSetStepComponent.LOADING";


    stepChangedSubscription:ISubscription;
    updateViewSubscription:ISubscription;

    constructor(state:StateService,
                selectionService: SelectionService,
                _dialogService: TdDialogService,
                _datasetPreviewService:DatasetPreviewService,
                _tdLoadingService:TdLoadingService,
                kyloRouterService:KyloRouterService,
                private _datasetPreviewStepperService:DatasetPreviewStepperService,
                private cd:ChangeDetectorRef
    ) {
        super(state,selectionService,_dialogService,_datasetPreviewService,_tdLoadingService, kyloRouterService);
        this.singleSelection = this.selectionService.isSingleSelection();
        this.updateViewSubscription = this._datasetPreviewStepperService.subscribeToUpdateView(this.onUpdateView.bind(this))


    }
    
    onUpdateView(){
        this.cd.markForCheck();
    }


    onPreviewValid(ds:PreviewDataSet){
        this._datasetPreviewStepperService.markFormAsValid(this.formGroup)
    }

    onPreviewInvalid(ds:PreviewDataSet){
        this._datasetPreviewStepperService.markFormAsInvalid(this.formGroup)
    }

    onInitialPreviewInvalid(){
        this._datasetPreviewStepperService.markFormAsInvalid(this.formGroup)
    }
    onInitialPreviewValid(){
        this._datasetPreviewStepperService.markFormAsValid(this.formGroup)
    }


    initProperties(){
        super.initProperties();
        this.stepChangedSubscription = this._datasetPreviewStepperService.subscribeToStepChanges(this.onStepChanged.bind(this))
    }
    ngOnInit() {
        super.ngOnInit();
    }

    ngOnDestroy() {
        super.ngOnDestroy();

        if(this.stepChangedSubscription){
            this.stepChangedSubscription.unsubscribe();
        }
        if(this.updateViewSubscription) {
            this.updateViewSubscription.unsubscribe();
        }
    }

    protected  startLoading(){
        super.startLoading();
        this.cd.markForCheck();
        this._datasetPreviewStepperService.savePreviews([]);
    }

    protected  finishedLoading(){
      super.finishedLoading();
        this.cd.markForCheck();
      this._datasetPreviewStepperService.savePreviews(this.previews);
    }



    private onStepChanged(idx:number){
            if (idx == 2) {
                if(this.datasetPreviewContainer != undefined) {
                    this.datasetPreviewContainer.selectedDataSet = undefined;
                }
                //we are on this step... try to preview
                this.previewSelection();
            }

    }







}