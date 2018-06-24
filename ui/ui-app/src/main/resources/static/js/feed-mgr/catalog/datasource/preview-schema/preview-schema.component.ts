import * as _ from "underscore";

import {HttpClient} from "@angular/common/http";
import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {Component, Input, OnInit, ChangeDetectionStrategy, Injector} from "@angular/core";
import {DomSanitizer} from "@angular/platform-browser";
import {SelectionService} from "../../api/services/selection.service";
import {DataSource} from "../../api/models/datasource";
import {Node} from '../../api/models/node';
import {MatDialog} from "@angular/material/dialog";
import {SatusDialogComponent} from "../../dialog/status-dialog.component";
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import {MatDialogRef} from "@angular/material/dialog/typings/dialog-ref";
import {TransformResponse} from "../../../visual-query/wrangler/model/transform-response";
import {QueryResultColumn} from "../../../visual-query/wrangler/model/query-result-column";
import {QueryEngine} from "../../../visual-query/wrangler/query-engine";
import {QueryEngineFactory} from "../../../visual-query/wrangler/query-engine-factory.service";
import {ITdDataTableColumn} from '@covalent/core/data-table';
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
import {PreviewDataSet} from "./model/preview-data-set";
import {PreviewJdbcDataSet} from "./model/preview-jdbc-data-set";
import {PreviewFileDataSet} from "./model/preview-file-data-set";
//import {QueryEngineFactory} from "../../../visual-query/wrangler/query-engine-factory.service";
import {PreviewDatasetCollectionService} from "../../api/services/preview-dataset-collection.service";



@Component({
    selector: "preview-schema",
    styleUrls: ["js/feed-mgr/catalog/datasource/preview-schema/preview-schema.component.css"],
    templateUrl: "js/feed-mgr/catalog/datasource/preview-schema/preview-schema.component.html"
})
export class PreviewSchemaComponent implements OnInit {

    @Input()
    public connection: any;

    @Input()
    public datasource: DataSource;
  
    statusDialogRef: MatDialogRef<SatusDialogComponent>;

    datasetMap:Common.Map<PreviewDataSet>;

    datasets:PreviewDataSet[];

    selectedDataSet:PreviewDataSet | PreviewFileDataSet

    /**
     * view Raw or preview
     */
    selectedDataSetViewRaw:boolean;

    datasetKeys :string[]
    
    message:string

    /**
     * Shared service with the Visual Query to store the datasets
     */
    previewDatasetCollectionService : PreviewDatasetCollectionService

    /**
     * Query engine for the data model
     */
  //  engine: QueryEngine<any>  ;

    constructor(private http: HttpClient, private sanitizer: DomSanitizer, private selectionService: SelectionService, private dialog: MatDialog, private fileMetadataTransformService: FileMetadataTransformService, private previewRawService :PreviewRawService, private previewSchemaService :PreviewSchemaService, private $$angularInjector: Injector, private stateService: StateService) {
        this.previewDatasetCollectionService = $$angularInjector.get("PreviewDatasetCollectionService");
    }

    public ngOnInit(): void {
      //  this.engine = this.sparkQueryEngine;
        //this.engine = this.queryEngineFactory.getEngine('spark')
        this.previewDatasetCollectionService.reset();
        this.createDataSets();
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
        this.previewDatasetCollectionService.addDataSet(dataset);
    }

    /**
     * remove the dataset
     * @param {PreviewDataSet} dataset
     */
    removeFromCollection(dataset:PreviewDataSet){
        this.previewDatasetCollectionService.remove(dataset);
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
           //TODO Move to Factory
            if(!this.datasource.connector.template.format){
                this.createFileBasedDataSets();
            }
            else if(this.datasource.connector.template.format == "jdbc"){
                let node: Node = <Node> this.selectionService.get(this.datasource.id);
                let paths = this.fileMetadataTransformService.getSelectedItems(node,this.datasource);
                let datasets = {}
                 paths.forEach(path => {
                    let dataSet = new PreviewJdbcDataSet();
                    dataSet.items = [path];
                    dataSet.displayKey = path;
                    dataSet.key = path;
                    dataSet.allowsRawView = false;
                    dataSet.updateDisplayKey();
                    datasets[dataSet.key] = dataSet;
                });
                this.setAndSelectFirstDataSet(datasets);

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
    }

    /**
     * Attempt to detect the file formats and mimetypes if the datasource used is a file based datasource
     */
    createFileBasedDataSets(): void {
        this.openStatusDialog("Examining file metadata", "Validating file metadata",true,false)
        let node: Node = <Node> this.selectionService.get(this.datasource.id);
        if(node) {
            this.fileMetadataTransformService.detectFormatForNode(node,this.datasource).subscribe((response:FileMetadataTransformResponse)=> {
                if (response.results ) {
                    this.message = response.message;
                    //select and transform the first dataset
                    this.setAndSelectFirstDataSet(response.results.datasets);
                    this.closeStatusDialog();
                }
                else {
                    this.openStatusDialog("Error. Cant process", "No results found ", false,true);
                }
            },error1 => (response:FileMetadataTransformResponse) => {
                this.openStatusDialog("Error","Error",false,true);
            });

        }
        else {
            this.openStatusDialog("Could not find selected node. ","No Node Found",false,true);
        }
    }


}


@Component({
    selector: 'dataset-simple-table',
    template:`<table td-data-table>
            <thead>
            <tr td-data-table-column-row>
              <th td-data-table-column
                  *ngFor="let column of columns">
                {{column.label}}
              </th>
            </tr>
            </thead>
            <tbody>
            <tr td-data-table-row *ngFor="let row of rows">
              <td td-data-table-cell *ngFor="let column of columns">
                {{row[column.name]}}
              </td>
            </tr>
            </tbody>
          </table>`
})
export class SimpleTableComponent {

    @Input()
    rows:Common.Map<any>;

    @Input()
    columns:TableColumn[]

    constructor(){

    }
    ngOnChanges(changes :SimpleChanges) {
        console.log('changes',changes)
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
          <mat-select placeholder="Select"  [(value)]="column.dataType">
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


    compareFn(x: string, y: string): boolean {
        return x && y ? x == y : x ===y;
    }


}
