import {TdDialogService} from "@covalent/core/dialogs";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/map';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {SelectionService} from "../../catalog/api/services/selection.service";
import {FileMetadataTransformService} from "../../catalog/datasource/preview-schema/service/file-metadata-transform.service";
import {PreviewSchemaService} from "../../catalog/datasource/preview-schema/service/preview-schema.service";
import {DatasetCollectionStatus, PreviewDataSet} from "../../catalog/datasource/preview-schema/model/preview-data-set";
import {PreviewDataSetRequest} from "../../catalog/datasource/preview-schema/model/preview-data-set-request";
import {FileMetadataTransformResponse} from "../../catalog/datasource/preview-schema/model/file-metadata-transform-response";
import {Node} from "../../catalog/api/models/node";
import {DataSource} from "../../catalog/api/models/datasource";
import {PreviewJdbcDataSet} from "../../catalog/datasource/preview-schema/model/preview-jdbc-data-set";
import {Subject} from "rxjs/Subject";
import {PreviewHiveDataSet} from "../../catalog/datasource/preview-schema/model/preview-hive-data-set";
import {DatabaseObject, DatabaseObjectType} from "../../catalog/datasource/tables/database-object";
import {DatasetPreviewStepperService, DataSourceChangedEvent} from "./dataset-preview-stepper.service";
import {ISubscription} from "rxjs/Subscription";

export enum DataSetType {
    FILE=1,HIVE=2,JDBC=3
}

@Component({
    selector: "preview-dataset-step",
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/preview-dataset-step.component.html",
    changeDetection:ChangeDetectionStrategy.OnPush
})
export class PreviewDatasetStepComponent implements OnInit, OnDestroy {


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
                private _fileMetadataTransformService: FileMetadataTransformService,
                private previewSchemaService: PreviewSchemaService,
                private dataSourceService:DatasetPreviewStepperService,
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
        this.cd.markForCheck();
    }

    private   finishedLoading(){
        this.loading = false;
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

    preparePreviewFiles(node: Node) :Observable<PreviewDataSet[]> {
        let subject = new Subject<PreviewDataSet[]>();
        this._fileMetadataTransformService.detectFormatForNode(node, this.datasource).subscribe((response: FileMetadataTransformResponse) => {
            let dataSetMap = response.results.datasets;
            let previews: PreviewDataSet[] = [];

            if (dataSetMap) {
                let keys = Object.keys(dataSetMap);
                keys.forEach(key => {
                    let dataSet: PreviewDataSet = dataSetMap[key];
                    previews.push(dataSet);
                })
            }
            subject.next(previews);
            //TODO HANDLE ERRORS
        }, error1 => {

                this._dialogService.openAlert({
                    message: 'Error parsing the file datasets',
                    disableClose: true,
                    title: 'Error parsing the file datasets'
                });
        });
            return subject.asObservable();
    }

    preparePreviewTables(node: Node,type:DataSetType):Observable<PreviewDataSet[]> {
        let datasets:PreviewDataSet[] = [];
        let selectedNodes  = node.getSelectedDescendants();


        if(selectedNodes) {

            selectedNodes.forEach(node => {
                let dbObject:DatabaseObject = <DatabaseObject> node.getBrowserObject();
                if(DatabaseObjectType.isTableType(dbObject.type)) {
                let dataSet = null;
                if(DataSetType.HIVE == type) {
                    dataSet = new PreviewHiveDataSet();
                }
                else { //if(DataSetType.JDBC == type) {
                    dataSet = new PreviewJdbcDataSet()
                }


                    let schema = dbObject.schema ? dbObject.schema : dbObject.catalog;
                    let table = dbObject.name
                    let key = schema+"."+table;
                    dataSet.items = [key];
                    dataSet.displayKey = key;
                    dataSet.key = key;
                    dataSet.allowsRawView = false;
                    dataSet.updateDisplayKey();
                    datasets.push(dataSet);
                    //add in any cached preview responses
                    //this.previewSchemaService.updateDataSetsWithCachedPreview([dataSet])
                }
            });
        }
        else {
            console.error("CANT FIND PATH!!!!!!")
        }
        return Observable.of(datasets);

    }


    getDataSetType():DataSetType{
        if (!this.datasource.connector.template.format) {
          return DataSetType.FILE;
        }
        else if (this.datasource.connector.template.format == "jdbc") {
            return DataSetType.JDBC;
        }
        else if (this.datasource.connector.template.format == "hive") {
            return DataSetType.HIVE;
        }
        else {
            console.log("Unsupported type, defaulting to file ",this.datasource.connector.template.format)
            return DataSetType.FILE;
        }
    }

    preparePreviewDataSets(node:Node)  :Observable<PreviewDataSet[]> {

        let type:DataSetType = this.getDataSetType();

        if(DataSetType.FILE == type){
            return this.preparePreviewFiles(node);
        }
        else if(DataSetType.HIVE == type || DataSetType.JDBC == type){
            return this.preparePreviewTables(node, type);
        }
        else {
            console.log("unsupported datasets")
            return Observable.of([]);
        }
    }

    private _previewSelection() {
        if (this.datasource) {
            this.previews = [];
            this.previewsReady = false;
            this.formGroup.get("hiddenValidFormCheck").setValue("")
            this.startLoading();
            let node: Node = this.selectionService.get(this.datasource.id);
            if (node.countSelectedDescendants() > 0) {
                this.showNoDatasetsExistScreen = false;
                this.cd.markForCheck();
                /// preview and save to feed
                this.preparePreviewDataSets(node).subscribe(dataSets => {
                    let previews: Observable<PreviewDataSet>[] = [];
                    if (dataSets) {
                        dataSets.forEach(dataSet => {
                            let previewRequest = new PreviewDataSetRequest();
                            previewRequest.dataSource = this.datasource;
                            previews.push(this.previewSchemaService.preview(dataSet, previewRequest).catch((e: any, obs: Observable<PreviewDataSet>) => Observable.of(e)));
                        })
                    }
                    Observable.forkJoin(previews).subscribe((results: PreviewDataSet[]) => {
                            let errors: PreviewDataSet[] = [];
                            this.previews = results

                            results.forEach(result => {

                                if (result.hasPreviewError()) {
                                    errors.push(result);
                                }

                            });
                            if (errors.length > 0) {
                                let dataSetNames = errors.map(ds => ds.key).join(",");
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
                        },
                        err => {

                            console.error(err)
                            this._dialogService.openAlert({
                                message: "ERROR " + err,
                                disableClose: true,
                                title: 'Error parsing source selection',
                            });
                            this.previewsReady = true;
                            this.finishedLoading();
                        });


                });


            }
            else {

                //Show "Selection is needed" card
                this.showNoDatasetsExistScreen = true;
                this._dialogService.openAlert({
                    message: 'You need to select a source',
                    disableClose: true,
                    title: 'A selection is needed'
                });
                this.finishedLoading();
            }
        }
    }




}