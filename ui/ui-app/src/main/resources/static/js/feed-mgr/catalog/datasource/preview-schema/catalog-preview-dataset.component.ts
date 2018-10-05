import {TdDialogService} from "@covalent/core/dialogs";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/map';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {SelectionService} from "../../api/services/selection.service";
import {DatasetCollectionStatus, PreviewDataSet} from "../preview-schema/model/preview-data-set";
import {PreviewDataSetRequest} from "../preview-schema/model/preview-data-set-request";
import {FileMetadataTransformResponse} from "../preview-schema/model/file-metadata-transform-response";
import {Node} from "../../api/models/node";
import {DataSource} from "../../api/models/datasource";
import {PreviewJdbcDataSet} from "../preview-schema/model/preview-jdbc-data-set";
import {Subject} from "rxjs/Subject";
import {PreviewHiveDataSet} from "../preview-schema/model/preview-hive-data-set";
import {DatabaseObject, DatabaseObjectType} from "../tables/database-object";
import {ISubscription} from "rxjs/Subscription";
import {TdLoadingService} from "@covalent/core/loading";
import {DatasetPreviewService, DataSourceChangedEvent, PreviewDataSetResultEvent} from "../preview-schema/service/dataset-preview.service";
import {DatasetPreviewContainerComponent} from "../preview-schema/preview/dataset-preview-container.component";
import {PreviewDatasetStepComponent} from "../../../catalog-dataset-preview/preview-stepper/preview-dataset-step.component";
import {StateService} from "@uirouter/angular";


@Component({
    selector: "catalog-preview-dataset",
    templateUrl: "js/feed-mgr/catalog/datasource/preview-schema/catalog-preview-dataset.component.html"
    //styleUrls:["js/feed-mgr/catalog/datasource/preview-schema/catalog-preview-dataset.component.scss"],
    //changeDetection:ChangeDetectionStrategy.OnPush
})
export class CatalogPreviewDatasetComponent implements OnInit, OnDestroy {

    static LOADER = "CatalogPreviewDatasetComponent.LOADING";


    @Input()
    formGroup:FormGroup;

    @Input()
    datasource:DataSource;

    @Input()
    displayInCard:boolean = false;

    @Input()
    autoSelectSingleDataSet = false;

    @ViewChild("datasetPreviewContainer")
    protected datasetPreviewContainer: DatasetPreviewContainerComponent;

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

    constructor(protected state:StateService,
                protected selectionService: SelectionService,
                protected _dialogService: TdDialogService,
                protected _datasetPreviewService:DatasetPreviewService,
                protected _tdLoadingService:TdLoadingService
    ) {
        this.singleSelection = this.selectionService.isSingleSelection();

    }

    backToDatasource(){
        this.state.go("catalog.datasource.browse",{datasourceId:this.datasource.id})
    }


    onPreviewValid(ds:PreviewDataSet){
        //this.__datasetPreviewService.markFormAsValid(this.formGroup)
    }

    onPreviewInvalid(ds:PreviewDataSet){
        //this.__datasetPreviewService.markFormAsInvalid(this.formGroup)
    }

    onInitialPreviewValid(){

    }

    onInitialPreviewInvalid() {

    }

    protected  initProperties(){
        if(this.formGroup == undefined){
            this.formGroup = new FormGroup({});
        }
        this.formGroup.addControl("hiddenValidFormCheck",new FormControl())

        this.dataSourceChangedSubscription =  this._datasetPreviewService.subscribeToDataSourceChanges(this.onDataSourceChanged.bind(this));
    }

    ngOnInit() {
       this.initProperties();
        //preview
        this.previewSelection();
    }

    ngOnDestroy() {
        if(this.dataSourceChangedSubscription){
            this.dataSourceChangedSubscription.unsubscribe();
        }

    }

    protected  startLoading(){
        this.loading = true;
        this._tdLoadingService.register(CatalogPreviewDatasetComponent.LOADER);

    }

    protected   finishedLoading(){
        this.loading = false;
        this._tdLoadingService.resolve(CatalogPreviewDatasetComponent.LOADER);
    }


    protected onDataSourceChanged($event:DataSourceChangedEvent){
        this.datasource = $event.dataSource;
    }



    protected previewSelection() {
        if (this.datasource) {

            this.previews = [];
            this.previewsReady = false;
            //set the preview to invalid if needed before re-previewing
            this.onInitialPreviewInvalid();
            this.startLoading();
            let node: Node = this.selectionService.get(this.datasource.id);
            this._datasetPreviewService.prepareAndPopulatePreview(node, this.datasource).subscribe((ev:PreviewDataSetResultEvent) => {

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
                      this.onInitialPreviewValid()
                    }
                    this.previewsReady = true;
                    this.finishedLoading();
                }


            }, (err:any) => {

                console.error(err)
                this._dialogService.openAlert({
                    message: "ERROR " + err,
                    disableClose: true,
                    title: 'Error parsing source selection',
                });
                this.onInitialPreviewInvalid()
                this.previewsReady = true;
                this.finishedLoading();
            });


        }
    }




}