import {TdDialogService} from "@covalent/core/dialogs";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/map';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit} from "@angular/core";
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
import {DatasetPreviewStepperService, DataSourceChangedEvent, PreviewDataSetResultEvent} from "./dataset-preview-stepper.service";
import {ISubscription} from "rxjs/Subscription";
import {TdLoadingService} from "@covalent/core/loading";



@Component({
    selector: "preview-dataset-step",
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/preview-dataset-step.component.html",
    changeDetection:ChangeDetectionStrategy.OnPush
})
export class PreviewDatasetStepComponent implements OnInit, OnDestroy {

    static LOADER = "PreviewDataSetStepComponent.LOADING";


    @Input()
    formGroup:FormGroup;

    @Input()
    datasource:DataSource;

    previews: PreviewDataSet[] = [];

    singleSelection: boolean;


    /**
     * The index used for the accordion previews
     * @type {number}
     */
    datasetIndex = 0;

    showNoDatasetsExistScreen: boolean = false;

    previewsReady:boolean;

    loading:boolean;

    dataSourceChangedSubscription:ISubscription;
    stepChangedSubscription:ISubscription;

    constructor(private selectionService: SelectionService,
                private _dialogService: TdDialogService,
                private dataSourceService:DatasetPreviewStepperService,
                private _tdLoadingService:TdLoadingService,
                private cd:ChangeDetectorRef
    ) {
        this.singleSelection = this.selectionService.isSingleSelection();
    }


    ngOnInit() {
        if(this.formGroup == undefined){
            this.formGroup = new FormGroup({});
        }
        this.formGroup.addControl("hiddenValidFormCheck",new FormControl())

        this.dataSourceChangedSubscription =  this.dataSourceService.subscribeToDataSourceChanges(this.onDataSourceChanged.bind(this));

        this.stepChangedSubscription = this.dataSourceService.subscribeToStepChanges(this.onStepChanged.bind(this))

        //preview
        this._previewSelection();
    }

    ngOnDestroy() {
        if(this.dataSourceChangedSubscription){
            this.dataSourceChangedSubscription.unsubscribe();
        }
        if(this.stepChangedSubscription){
            this.stepChangedSubscription.unsubscribe();
        }
    }

    private  startLoading(){
        this.loading = true;
        this._tdLoadingService.register(PreviewDatasetStepComponent.LOADER);
        this.cd.markForCheck();
    }

    private   finishedLoading(){
        this.loading = false;
        this._tdLoadingService.resolve(PreviewDatasetStepComponent.LOADER);
        this.cd.markForCheck();
    }


    private onDataSourceChanged($event:DataSourceChangedEvent){
        this.datasource = $event.dataSource;
    }

    private onStepChanged(idx:number){
        if(idx == 2) {
            //we are on this step... try to preview
            this._previewSelection();
        }
    }


    private _previewSelection() {
        if (this.datasource) {

            this.previews = [];
            this.previewsReady = false;
            this.formGroup.get("hiddenValidFormCheck").setValue("")
            this.startLoading();
            let node: Node = this.selectionService.get(this.datasource.id);
            this.dataSourceService.prepareAndPopulatePreview(node, this.datasource).subscribe((ev:PreviewDataSetResultEvent) => {

                if(ev.isEmpty()){
                    //Show "Selection is needed" card
                    this.showNoDatasetsExistScreen = true;
                    this._dialogService.openAlert({
                        message: 'You need to select a source',
                        disableClose: true,
                        title: 'A selection is needed'
                    });
                    this.finishedLoading();
                }
                else {
                    this.previews = ev.dataSets;
                    if(ev.hasError()){
                        let dataSetNames = ev.errors.map((ds:PreviewDataSet) => ds.key).join(",");
                        let message = 'Kylo is unable to determine the schema for the following items:' + dataSetNames;
                        if (this.singleSelection) {
                            message += " You will need to alter the preview settings or manually create the schema"
                        }
                        //WARN different datasets
                        this._dialogService.openAlert({
                            message: message,
                            disableClose: true,
                            title: 'Error parsing source selection',
                        });
                    }
                    else {
                        this.formGroup.get("hiddenValidFormCheck").setValue("valid")

                    }
                    this.previewsReady = true;
                    this.finishedLoading();
                }


            }, err => {

                console.error(err)
                this._dialogService.openAlert({
                    message: "ERROR " + err,
                    disableClose: true,
                    title: 'Error parsing source selection',
                });
                this.previewsReady = true;
                this.finishedLoading();
            });


        }
    }




}