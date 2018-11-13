import * as _ from "underscore";

import {HttpClient} from "@angular/common/http";
import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {Component, Input, OnInit, ChangeDetectionStrategy, Injector, Inject, EventEmitter, Output, ChangeDetectorRef} from "@angular/core";
import {DomSanitizer} from "@angular/platform-browser";
import {SelectionService, SingleSelectionPolicy} from "../../api/services/selection.service";
import {DataSource} from "../../api/models/datasource";
import {Node} from '../../api/models/node';
import {MAT_DIALOG_DATA, MatDialog} from "@angular/material/dialog";
import {SatusDialogComponent} from "../../dialog/status-dialog.component";
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import {MatDialogRef} from "@angular/material/dialog/typings/dialog-ref";
import {TransformResponse} from "../../../visual-query/wrangler/model/transform-response";
import {QueryResultColumn} from "../../../visual-query/wrangler/model/query-result-column";
import {QueryEngine} from "../../../visual-query/wrangler/query-engine";
import {QueryEngineFactory} from "../../../visual-query/wrangler/query-engine-factory.service";
import {ITdDataTableColumn, ITdDataTableSortChangeEvent, TdDataTableService, TdDataTableSortingOrder} from '@covalent/core/data-table';
import {SchemaParseSettingsDialog} from "./schema-parse-settings-dialog.component";
import {SimpleChanges} from "@angular/core/src/metadata/lifecycle_hooks";

import {TableViewModel, TableColumn} from "./model/table-view-model"
import {Common} from "../../../../common/CommonTypes"

import {FileMetadataTransformService} from "./service/file-metadata-transform.service";
import {FileMetadata} from "./model/file-metadata";
import {FileMetadataTransformResponse} from "./model/file-metadata-transform-response";

import {PreviewSchemaService} from "./service/preview-schema.service";
import {PreviewRawService} from "./service/preview-raw.service";
import {PreviewDataSetRequest} from "./model/preview-data-set-request";
import {DatasetCollectionStatus, PreviewDataSet} from "./model/preview-data-set";
import {PreviewJdbcDataSet} from "./model/preview-jdbc-data-set";
import {PreviewFileDataSet} from "./model/preview-file-data-set";
//import {QueryEngineFactory} from "../../../visual-query/wrangler/query-engine-factory.service";
import {PreviewDatasetCollectionService} from "../../api/services/preview-dataset-collection.service";
import { TranslateService } from "@ngx-translate/core";




@Component({
    selector: "preview-schema",
    styleUrls: ["js/feed-mgr/catalog/datasource/preview-schema/preview-schema.component.css"],
    templateUrl: "js/feed-mgr/catalog/datasource/preview-schema/preview-schema.component.html"
})
export class PreviewSchemaComponent implements OnInit {

    /**
     * The datasource to use to connect and preview
     */
    @Input()
    public datasource: DataSource;

    /**
     * Optional set of incoming paths to be used to create datasets to preview.
     * This will be if a user is viewing a feed or dataset that has already been saved.
     * If not specified the component will attempt to get the paths from the selection-service.
     * This will be in the case of coming directly from the catalog
     */
    @Input()
    public paths?:string[];

    /**
     * Flag to allow for edit actions
     */
    @Input()
    public editable:boolean;

    /**
     * if true it will collect the first dataset (if not already collected) and add it to the preivew-dataset-collection service
     */
    @Input()
    public autoCollect:boolean;

    @Input()
    public addToCollectionButtonName:string = "Add"

    @Input()
    public removeFromCollectionButtonName:string = "Remove";

    /**
     * a custom event allowing users to override what happens when a user removes a dataset from the collection service.
     * NOTE the user of this needs to include the logic to do the removal of the dataset from the collection service
     * @type {EventEmitter<PreviewDataSet>}
     */
    @Output()
    public customDatasetRemoval:EventEmitter<PreviewDataSet> = new EventEmitter<PreviewDataSet>();

    /**
     * a custom event allowing users to override what happens when a user adds a dataset to the collection service.
     * NOTE: the user of this needs to include the logic to add the dataset to the collectoin service.
     * @type {EventEmitter<PreviewDataSet>}
     */
    @Output()
    public customDatasetAddition:EventEmitter<PreviewDataSet> = new EventEmitter<PreviewDataSet>();

    statusDialogRef: MatDialogRef<SatusDialogComponent>;

    /**
     * A Object<string,PreviewDataSet>  where the key is the dataset.key
     */
    datasetMap:Common.Map<PreviewDataSet>;

    /**
     * the dataset.key array
     */
    datasetKeys :string[]

    /**
     * The array of datasets to be previewed
     */
    datasets:PreviewDataSet[];

    /**
     * The selected dataset
     */
    selectedDataSet:PreviewDataSet | PreviewFileDataSet

    /**
     * Flag to indicate we are allowed to view raw or preview
     */
    selectedDataSetViewRaw:boolean;


    /**
     * optional error message populated after a dataset is previewed
     */
    message:string

    /**
     * is it set to only allow 1 node selection
     * @type {boolean}
     */
    singleNodeSelection:boolean = false;

    /**
     * Shared service with the Visual Query to store the datasets
     */
    previewDatasetCollectionService : PreviewDatasetCollectionService

    /**
     * Query engine for the data model
     */
  //  engine: QueryEngine<any>  ;

    constructor(private http: HttpClient,
        private sanitizer: DomSanitizer, 
        private selectionService: SelectionService, 
        private dialog: MatDialog, 
        private fileMetadataTransformService: FileMetadataTransformService, 
        private previewRawService :PreviewRawService,
        private previewSchemaService :PreviewSchemaService,
        private $$angularInjector: Injector, 
        private stateService: StateService,
        private translate : TranslateService) {

        this.previewDatasetCollectionService = $$angularInjector.get("PreviewDatasetCollectionService");
        this.singleNodeSelection = this.selectionService.hasPolicy(SingleSelectionPolicy);
    }

    public ngOnInit(): void {
      //  this.engine = this.sparkQueryEngine;
        //this.engine = this.queryEngineFactory.getEngine('spark')
        //this.previewDatasetCollectionService.reset();
        this.createDataSets();
    }

    public showAddToCollectionButton(dataSet:PreviewDataSet){
        let collectedSize = this.previewDatasetCollectionService.datasetCount();
        return this.editable && !dataSet.isCollected() && dataSet.loading == false;
    }

    public showRemoveFromCollectionButton(dataSet:PreviewDataSet){
        let collectedSize = this.previewDatasetCollectionService.datasetCount();
        return this.editable && dataSet.isCollected() && (!this.singleNodeSelection || (this.singleNodeSelection && this.datasets && this.datasets.length >1));
    }

    private addCollectedDatasets(){
        this.previewDatasetCollectionService.datasets.forEach(dataset => {
            let key = dataset.key;
            if(this.datasetKeys.indexOf(key) <0){
                this.datasetKeys.push(key)
                this.datasetMap[key] = dataset;
                this.datasets.push(dataset)
            }
            else {
                console.log("skipping collected dataset ",dataset,this)
            }

        })

    }

    openSchemaParseSettingsDialog(): void {
        let dialogRef = this.dialog.open(SchemaParseSettingsDialog, {
            width: '500px',
            data: { schemaParser: (<PreviewFileDataSet>this.selectedDataSet).schemaParser,
                sparkScript: (<PreviewFileDataSet>this.selectedDataSet).sparkScript
            }
        });

        dialogRef.afterClosed().subscribe(result => {

            ///update it
        });
    }

    private openStatusDialog(title: string, message: string, showProgress:boolean,renderActionButtons?: boolean): void {
        this.closeStatusDialog();
        if (renderActionButtons == undefined) {
            renderActionButtons = false;
        }
        this.statusDialogRef = this.dialog.open(SatusDialogComponent, {
            data: {
                renderActionButtons: renderActionButtons,
                title: title,
                message: message,
                showProgress:showProgress
            }
        });
    }

    private closeStatusDialog(): void {
        if (this.statusDialogRef) {
            this.statusDialogRef.close();
        }
    }

    /**
     * Switch between raw view and data preview
     */
    onToggleRaw(){
        this.selectedDataSetViewRaw = !this.selectedDataSetViewRaw
        if(this.selectedDataSetViewRaw){
            //view raw
        this.loadRawData();
        }
    }

    /**
     * make the request to load in the raw view.     *
     */
    loadRawData(){
        this.selectedDataSetViewRaw = true;
        if(this.selectedDataSet.raw == undefined) {
            this.previewRawService.preview((<PreviewFileDataSet>this.selectedDataSet)).subscribe((data: PreviewDataSet) => {
                this.selectedDataSetViewRaw = true;
            }, (error1:any) => {
                console.log("Error loading Raw data",error1)
            })
        }
    }

    /**
     * When a user selects a dataset it will attempt to load in the preview data.
     * if that has an error
     * @param {string} datasetKey
     */
    onDatasetSelected(datasetKey: string){
        this.selectedDataSet = this.datasetMap[datasetKey];
        //toggle the raw flag back to preview
        this.selectedDataSetViewRaw =false;
        this.preview();

    }

    preview(){
        if (!this.selectedDataSet.hasPreview()) {
            let previewRequest = new PreviewDataSetRequest()
            previewRequest.dataSource = this.datasource;
            this.selectedDataSet.applyPreviewRequestProperties(previewRequest);

            let isNew = !this.selectedDataSet.hasPreview();
            this.selectedDataSet.dataSource = this.datasource;
            //add in other properties
            this.previewSchemaService.preview(this.selectedDataSet, previewRequest).subscribe((data: PreviewDataSet) => {
                this.selectedDataSetViewRaw = false;
                //auto collect the first one if there is only 1 dataset and its editable
                if(this.datasetKeys.length == 1 && this.autoCollect && this.editable){
                    this.addToCollection(this.selectedDataSet);
                }
            }, (error1:any) => {
                console.error("unable to preview dataset ",error1);
                this.selectedDataSet.previewError("unable to preview dataset ");
                if(this.selectedDataSet.allowsRawView) {
                    this.loadRawData();
                }
            })
        }
    }

    /**
     * add the dataset
     * @param {PreviewDataSet} dataset
     */
    addToCollection(dataset: PreviewDataSet){
        if(this.customDatasetAddition.observers.length >0) {
            this.customDatasetAddition.emit(dataset);
        }
        else {
            this.previewDatasetCollectionService.addDataSet(dataset);
        }
    }

    /**
     * remove the dataset
     * @param {PreviewDataSet} dataset
     */
    removeFromCollection(dataset:PreviewDataSet){

        if(this.customDatasetRemoval.observers.length >0) {
            this.customDatasetRemoval.emit(dataset);
        }
        else {
            this.previewDatasetCollectionService.remove(dataset);
        }
    }

    /**
     * Go to the visual query populating with the selected datasets
     */
    visualQuery(){
        this.stateService.go("visual-query");

    }

    /**
     * Create the datasets for the selected nodes
     */
    createDataSets(){
            let paths = this.paths;

            if(paths == undefined){
                //attempt to get the paths from the selectionService and selected node
                //this is if the paths are not explicitly passed in.  it will pull them from the catalog selection
                let node: Node = <Node> this.selectionService.get(this.datasource.id);
                if(node) {
                    paths = this.fileMetadataTransformService.getSelectedItems(node, this.datasource);
                }
            }
        if(paths) {
            //TODO Move to Factory
            this.openStatusDialog(this.translate.instant('FEEDMGR.PREVIEW_SCHEMA.DIALOG.EXAMINING_TITLE'), this.translate.instant('FEEDMGR.PREVIEW_SCHEMA.DIALOG.EXAMINING_MESSAGE'),true,false)

            if (!this.datasource.connector.template.format) {
                this.createFileBasedDataSets(paths);
            }
            else if (this.datasource.connector.template.format == "jdbc") {
                let datasets = {}
                paths.forEach(path => {
                    let dataSet = new PreviewJdbcDataSet();
                    dataSet.items = [path];
                    dataSet.displayKey = path;
                    dataSet.key = path;
                    dataSet.allowsRawView = false;
                    dataSet.updateDisplayKey();
                    datasets[dataSet.key] = dataSet;
                    //add in any cached preview responses
                    this.previewSchemaService.updateDataSetsWithCachedPreview([dataSet])
                    //update the CollectionStatus
                    if(this.previewDatasetCollectionService.exists(dataSet) && !dataSet.isCollected()){
                        dataSet.collectionStatus = DatasetCollectionStatus.COLLECTED;
                    }
                });
                this.setAndSelectFirstDataSet(datasets);

            }
        }
        else {
            this.openStatusDialog(this.translate.instant("FEEDMGR.PREVIEW_SCHEMA.DIALOG.NO_PATH_SUPPLIED"),this.translate.instant('FEEDMGR.PREVIEW_SCHEMA.DIALOG.PLEASE_SELECT_ITEM'),false,true);
        }
    }

    /**
     * set the datasets array and select the first one
     * @param {Common.Map<PreviewDataSet>} datasetMap
     */
    setAndSelectFirstDataSet(datasetMap:Common.Map<PreviewDataSet>){
        this.datasetMap = datasetMap;
        this.datasetKeys = Object.keys(this.datasetMap);
        this.datasets =this.datasetKeys.map(key=>this.datasetMap[key])
        let firstKey = this.datasetKeys[0];
        this.onDatasetSelected(firstKey);

        //add in any existing datasets that are already in the collection
        this.addCollectedDatasets()


    }

    /**
     * Attempt to detect the file formats and mimetypes if the datasource used is a file based datasource
     */
    createFileBasedDataSets(paths:string[]): void {
        if(paths && paths.length >0) {
            this.fileMetadataTransformService.detectFormatForPaths(paths,this.datasource).subscribe((response:FileMetadataTransformResponse)=> {
                if (response.results ) {
                    this.message = response.message;
                    //add in any cached preview responses
                    _.each(response.results.datasets,(dataset:PreviewDataSet,key:string)=> {
                        this.previewSchemaService.updateDataSetsWithCachedPreview([dataset])
                        if(this.previewDatasetCollectionService.exists(dataset) && !dataset.isCollected()){
                            dataset.collectionStatus = DatasetCollectionStatus.COLLECTED;
                        }
                    });

                    //select and transform the first dataset
                    this.setAndSelectFirstDataSet(response.results.datasets);
                    this.closeStatusDialog();
                }
                else {
                    this.openStatusDialog(this.translate.instant('FEEDMGR.PREVIEW_SCHEMA.DIALOG.CANT_PROCESS_ERROR'),this.translate.instant('views.common.Nrf'), false,true);
                }
            },error1 => (response:FileMetadataTransformResponse) => {
                this.openStatusDialog(this.translate.instant("views.common.error"),this.translate.instant("views.common.error"),false,true);
            });

        }

    }


}


@Component({
    selector: 'dataset-simple-table',
    styleUrls:["js/feed-mgr/catalog/datasource/preview-schema/dataset-simple-table.component.css"],
    template:`
    <table td-data-table >
      <thead>
      <tr td-data-table-column-row>
        <th td-data-table-column
            *ngFor="let column of columns"
            [name]="column.name"
            [sortable]="false"
            [numeric]="column.numeric"
            (sortChange)="sort($event)"
            [sortOrder]="sortOrder">
          {{column.label}} <br/>
          ({{column.dataType}})
        </th>
      </tr>
      </thead>
      <tbody>
      <tr td-data-table-row *ngFor="let row of filteredData">
        <td td-data-table-cell *ngFor="let column of columns"
            [numeric]="column.numeric">
          {{row[column.name]}}
        </td>
      </tr>
      </tbody>
    </table>
    
    
    
    
    
    
    <div  *ngIf="!filteredData.length ===0" fxLayout="row" fxLayoutAlign="center center">
      <h3>No results to display.</h3>
    </div>`
})
export class SimpleTableComponent {

    @Input()
    rows:any[];

    @Input()
    columns:TableColumn[] = [];


    constructor(  private _dataTableService: TdDataTableService){

    }

    /**
     * All the data
     * @type {any[]}
     */
    data:any[] = [];

    /**
     * sorted/filtered data displayed in the ui
     * @type {any[]}
     */
    filteredData:any[] = [];


    sortBy: string = '';

    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Descending;



    sort(sortEvent: ITdDataTableSortChangeEvent): void {
        this.sortBy = sortEvent.name;
        this.sortOrder = sortEvent.order === TdDataTableSortingOrder.Descending ? TdDataTableSortingOrder.Ascending : TdDataTableSortingOrder.Descending;
        this.filter();
    }

    filter(){
        let newData:any[] = this.data;
        newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
        this.filteredData = newData;
    }



    ngOnInit(){
        this.initTable();
    }

    ngOnChanges(changes: SimpleChanges) {

        if(changes && (!changes.rows.firstChange || !changes.columns.firstChange)){
            this.initTable();
        }

    }

    initTable(){
        if(this.columns) {
            this.sortBy = this.columns[0].name;
        }
        else {
            this.columns = [];
        }

        // Add table data
        this.data = this.rows;
        this.filter();
    }





}


@Component({
    selector:'dataset-schema-definition',
    template:`
      <div *ngFor="let column of columns" fxLayout="row">

          <mat-form-field>
            <input matInput placeholder="Column Name" [(value)]="column.label">
          </mat-form-field>
        <span fxFlex="10"></span>
          <mat-form-field>
          <mat-select placeholder="Select"  [(value)]="column.dataType" (change)="onColumnChange(column)">
            <mat-option [value]="option" *ngFor="let option of columnDataTypes">{{option}}</mat-option>
          </mat-select>
        </mat-form-field>

      </div>
    `

})
export class SchemaDefinitionComponent  implements OnInit {

    private columnDataTypes: string[] = ['string', 'int', 'bigint', 'tinyint', 'decimal', 'double', 'float', 'date', 'timestamp', 'boolean', 'binary']

    @Input()
    columns:TableColumn[]


    @Output()
    columnsChange = new EventEmitter<TableColumn[]>();

    constructor() {

    }

    ngOnInit(){
        if(this.columns == undefined){
            this.columns = [];
        }

        this.columns.forEach(column => {
            if(this.columnDataTypes.indexOf(column.dataType) == -1) {
                this.columnDataTypes.push(column.dataType);
            }
        });
    }

    onColumnChange(column:TableColumn){
        column.numeric = TableViewModel.isNumeric(column.dataType)
        if(this.columnsChange){
           this.columnsChange.emit(this.columns)
        }
    }


    compareFn(x: string, y: string): boolean {
        return x && y ? x == y : x ===y;
    }


}
