import {TdDialogService} from "@covalent/core/dialogs";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/map';
import {Component, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {SelectionService} from "../../api/services/selection.service";
import {PreviewDataSet} from "../preview-schema/model/preview-data-set";
import {Node} from "../../api/models/node";
import {DataSource} from "../../api/models/datasource";
import {ISubscription} from "rxjs/Subscription";
import {TdLoadingService} from "@covalent/core/loading";
import {DatasetPreviewService, DataSourceChangedEvent, PreviewDataSetResultEvent} from "../preview-schema/service/dataset-preview.service";
import {DatasetPreviewContainerComponent} from "../preview-schema/preview/dataset-preview-container.component";
import {StateService} from "@uirouter/angular";
import {BrowserObject} from "../../api/models/browser-object";
import {KyloRouterService} from "../../../../services/kylo-router.service";
import {SparkDataSet} from '../../../model/spark-data-set.model';
import {TableColumn} from './model/table-view-model';


@Component({
    selector: "catalog-preview-dataset",
    templateUrl: "./catalog-preview-dataset.component.html",
    styleUrls:["./catalog-preview-dataset.component.scss"],
    //changeDetection:ChangeDetectionStrategy.OnPush
})
export class CatalogPreviewDatasetComponent implements OnInit, OnDestroy {

    static LOADER = "CatalogPreviewDatasetComponent.LOADER";


    @Input()
    formGroup:FormGroup;

    @Input()
    dataset: SparkDataSet;

    @Input()
    datasource:DataSource;

    @Input()
    displayInCard:boolean = false;

    @Input()
    autoSelectSingleDataSet = false;

    @Input()
    objectsToPreview?:BrowserObject[];

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

    datasetName:string = "Preview Datasets";

    selectedDataSet:PreviewDataSet;

    constructor(protected state:StateService,
                protected selectionService: SelectionService,
                protected _dialogService: TdDialogService,
                protected _datasetPreviewService:DatasetPreviewService,
                protected _tdLoadingService:TdLoadingService,
                protected  kyloRouterService:KyloRouterService
    ) {
        this.singleSelection = this.selectionService.isSingleSelection();
    }

    backToDatasource(){
        //go back if we dont have a dataset selected, or if we only have 1 dataset and we are set to auto select it

        if(this.selectedDataSet == undefined || (this.previews && this.previews.length <=1 && this.autoSelectSingleDataSet) ) {
            this.kyloRouterService.back();
        }
        else if(this.selectedDataSet != undefined){
            this.datasetPreviewContainer.selectDataSet(undefined);
        }
    }


    onPreviewValid(ds:PreviewDataSet){
        //this.__datasetPreviewService.markFormAsValid(this.formGroup)
    }

    onPreviewInvalid(ds:PreviewDataSet){
        //this.__datasetPreviewService.markFormAsInvalid(this.formGroup)
    }

    onPreviewSelected(ds:PreviewDataSet){
        this.selectedDataSet = ds;
        if (ds) {
            this.datasetName = ds.displayKey
            if (this.dataset === undefined) {
                //dataset is undefined when it hasn't been saved or annotated yet
                this.dataset = this.selectedDataSet.toSparkDataSet();
            } else {
                this.selectedDataSet.id = this.dataset.id;
                this.mergeSchemas(this.selectedDataSet, this.dataset);
            }
        } else {
            this.datasetName = "Preview Datasets";
        }
    }

    mergeSchemas(target: PreviewDataSet, source: SparkDataSet) {
        target.schema.forEach(column => {
            source.schema.find((sourceColumn: TableColumn) => {
                if (column.name === sourceColumn.name) {
                    column.description = sourceColumn.description;
                    return true;
                } else {
                    return false;
                }

            });
        });
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
            //if the objects are passed in, preview that, otherwise get the selection from the node
            let previewRequest : Observable<PreviewDataSetResultEvent> = null;
            if(this.objectsToPreview && this.objectsToPreview.length >0){
                previewRequest = this._datasetPreviewService.prepareAndPopulatePreviewDataSets(this.objectsToPreview,this.datasource);
            }
            else {
                let node: Node = this.selectionService.get(this.datasource.id);
                if(node != undefined) {
                    previewRequest = this._datasetPreviewService.prepareAndPopulatePreview(node, this.datasource);
                }
            }
            if(previewRequest != null) {
                previewRequest.subscribe((ev: PreviewDataSetResultEvent) => {

                    if (ev.isEmpty()) {
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
                        if (ev.hasError()) {
                            let dataSetNames = ev.errors.map((ds: PreviewDataSet) => ds.key).join(",");
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


                }, (err: any) => {

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
            }else {
                //redirect back to the datasource list
                this.state.go("catalog.datasources");
            }


        }
    }
}