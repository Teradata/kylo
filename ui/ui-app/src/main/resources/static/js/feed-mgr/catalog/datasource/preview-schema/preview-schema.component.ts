import {HttpClient} from "@angular/common/http";
import {Component, Input, OnInit,ChangeDetectionStrategy} from "@angular/core";
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
//import {QueryEngine} from "../../../visual-query/wrangler/query-engine";
//import {QueryEngineFactory} from "../../../visual-query/wrangler/query-engine-factory.service";
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



export class TransformResponseUtil {

    private columnIndexMap = new Map<string, number>();

    constructor(private transformResponse :TransformResponse){

        transformResponse.results.columns.forEach((column: QueryResultColumn, index: number) => {
            this.columnIndexMap[column.hiveColumnLabel] = index;
        });
    }


    public getValue(row:any[],column:string):any{
    return row[this.columnIndexMap[column]];
    }
}




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
     * Query engine for the data model
     */
    //engine: QueryEngine<any>  ;

    constructor(private http: HttpClient, private sanitizer: DomSanitizer, private selectionService: SelectionService, private dialog: MatDialog, private fileMetadataTransformService: FileMetadataTransformService, private previewRawService :PreviewRawService, private previewSchemaService :PreviewSchemaService) {
    }

    public ngOnInit(): void {
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

    onToggleRaw(){
        this.selectedDataSetViewRaw = !this.selectedDataSetViewRaw
        if(this.selectedDataSetViewRaw){
            //view raw
        this.loadRawData();
        }
        else {
            //view preview
        }
    }

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


    onDatasetSelected(datasetKey: string){
        this.selectedDataSet = this.datasetMap[datasetKey];
        //toggle the raw flag back to preview
        this.selectedDataSetViewRaw =false;
        //TODO change this logic to call server code without spark script
        if(this.selectedDataSet.allowsRawView && !this.selectedDataSet.isType("FileDataSet") && (<PreviewFileDataSet>this.selectedDataSet).hasSparkScript()) {
            if (!(<PreviewFileDataSet>this.selectedDataSet).hasSparkScript()) {
                //show RAW
                //Show error unable to preview
                let message = "Unable to preview the dataset via spark. No parser found";
                this.selectedDataSet.previewError(message);
                //Toast the message
                // script is empty
                //present user with manual options
            }
            //Load Raw Data
            this.loadRawData()
        }
        else {
            this.preview();
        }
    }

    preview(){
        if (!this.selectedDataSet.hasPreview()) {
            let previewRequest = new PreviewDataSetRequest()
            previewRequest.dataSource = this.datasource;
            this.selectedDataSet.applyPreviewRequestProperties(previewRequest);

            //add in other properties
            this.previewSchemaService.preview(this.selectedDataSet, previewRequest).subscribe((data: PreviewDataSet) => {
                this.selectedDataSetViewRaw = false;
            }, (error1:any) => {
                console.error("unable to preview dataset ",error1);
            })
        }

    }


    createDataSets(){
        //Move to Factory
            if(!this.datasource.connector.template.format){
                this.detectFormat();
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

    setAndSelectFirstDataSet(datasetMap:Common.Map<PreviewDataSet>){
        this.datasetMap = datasetMap;
        this.datasetKeys = Object.keys(this.datasetMap);
        this.datasets =this.datasetKeys.map(key=>this.datasetMap[key])
        let firstKey = this.datasetKeys[0];
        this.onDatasetSelected(firstKey);
    }

    detectFormat(): void {
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
