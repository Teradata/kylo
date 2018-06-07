import {HttpClient} from "@angular/common/http";
import {Component, Input, OnInit} from "@angular/core";
import {DomSanitizer} from "@angular/platform-browser";
import {SelectionService} from "../../api/services/selection.service";
import {DataSource} from "../../api/models/datasource";
import {Node} from "../api/node";
import {MatDialog} from "@angular/material/dialog";
import {SatusDialogComponent} from "../../dialog/status-dialog.component";
import {MatDialogRef} from "@angular/material/dialog/typings/dialog-ref";
import {TransformResponse} from "../../../visual-query/wrangler/model/transform-response";
import {QueryResultColumn} from "../../../visual-query/wrangler";
import {SchemaParser} from "../../../model/field-policy";
import {FeedDataTransformation} from "../../../model/feed-data-transformation";
//import {QueryEngine} from "../../../visual-query/wrangler/query-engine";
import {DataSetPreview, FileDataSet, FileMetadata, FileMetadataTransformResponse, TableColumn} from "./file-metadata-transform-response";
import {Common} from "../../../../common/CommonTypes"
//import {QueryEngineFactory} from "../../../visual-query/wrangler/query-engine-factory.service";
import {FileMetadataTransformService} from "./file-metadata-transform.service";
import {FilePreviewService, UpdatedParserWithScript} from "./preview-schema.service";
import { ITdDataTableColumn } from '@covalent/core/data-table';
import {SchemaParseSettingsDialog} from "./schema-parse-settings-dialog.component";
import {SimpleChanges} from "@angular/core/src/metadata/lifecycle_hooks";


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

    columns: ITdDataTableColumn[];

    data: any[];

    raw: any;

    statusDialogRef: MatDialogRef<SatusDialogComponent>;

    fileFormatResults: FileMetadataTransformResponse;

    selectedDataSet:FileDataSet

    /**
     * view Raw or preview
     */
    selectedDataSetViewRaw:boolean;

    datasetKeys :string[]

    /**
     * Query engine for the data model
     */
    //engine: QueryEngine<any>  ;

    constructor(private http: HttpClient, private sanitizer: DomSanitizer, private selectionService: SelectionService, private dialog: MatDialog, private fileMetadataTransformService: FileMetadataTransformService, private filePreviewService :FilePreviewService) {
    }

    public ngOnInit(): void {
       /*
       if(this.engine == undefined) {
            this.queryEngineFactory.getEngine("spark").then((engine) => {
                this.engine = engine;
            });
        }
        */
      //  this.transformModel = {} as FeedDataTransformation;
        this.detectFormat();
    }

    openSchemaParseSettingsDialog(): void {
        let dialogRef = this.dialog.open(SchemaParseSettingsDialog, {
            width: '500px',
            data: { schemaParser: this.selectedDataSet.schemaParser,
                sparkScript: this.selectedDataSet.sparkScript
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
            this.selectedDataSet.loading=true;
            let firstFile = this.selectedDataSet.files[0].filePath;

            let sparkScript = "var df = sqlContext.read.format(\"text\").load(\""+firstFile+"\")";

            let sparkScriptWithLimit = this.limitSparkScript(sparkScript);
            this.filePreviewService.transform(sparkScriptWithLimit).subscribe((data: TransformResponse) => {
                let preview = this.buildTable(data);
                this.selectedDataSet.raw = preview;
                this.selectedDataSet.clearRawError();
                this.selectedDataSet.finishedLoading()
                this.selectedDataSetViewRaw = true;
            }, error1 => {
                this.selectedDataSet.finishedLoading()
                this.selectedDataSet.rawError("Error previewing the raw data " + error1)
            })
        }
    }

    limitSparkScript(sparkScript:string) {
        let sparkScriptWithLimit = "import org.apache.spark.sql._\n" + sparkScript + "\ndf=df.limit(20)\n df";
        return sparkScriptWithLimit;
    }


    onDatasetSelected(datasetKey: string){
        this.selectedDataSet = this.fileFormatResults.results.datasets[datasetKey];
        //toggle the raw flag back to preview
        this.selectedDataSetViewRaw =false;

        if(!this.selectedDataSet.hasSparkScript()) {
            if (!this.selectedDataSet.hasSparkScript()) {
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
            let sparkScript = this.selectedDataSet.sparkScript.script;
            let selectedFilePaths = this.selectedDataSet.files.map((fileMetadata: FileMetadata) => fileMetadata.filePath);
            // this.engine.setSampleFile(response.results.sparkScript);
            // this.transformModel.sampleFile = response.results.sparkScript;
            //preview it
            if (sparkScript != null) {
                if (!this.selectedDataSet.hasPreview()) {
                    //Show Progress Bar
                    this.selectedDataSet.loading = true;

                    let sparkScriptWithLimit = this.limitSparkScript(sparkScript);
                    this.filePreviewService.transform(sparkScriptWithLimit).subscribe((data: TransformResponse) => {
                        let preview = this.buildTable(data);
                        this.selectedDataSet.finishedLoading()
                        this.selectedDataSet.clearPreviewError()
                        this.selectedDataSet.schema = preview.columns;
                        this.selectedDataSet.preview =preview;
                        this.selectedDataSetViewRaw = false;
                    }, error1 => {
                        this.selectedDataSet.finishedLoading()
                        this.selectedDataSet.previewError("Error previewing the data " + error1);
                    })
                }

            }
        }
    }



    detectFormat(): void {
        this.openStatusDialog("Examining file metadata", "Validating file metadata",true,false)
        let node: Node = <Node> this.selectionService.get(this.datasource.id);
        if(node) {
            this.fileMetadataTransformService.detectFormatForNode(node,this.datasource).subscribe((response:FileMetadataTransformResponse)=> {
                if (response.results ) {
                    this.fileFormatResults = response;
                    //select and transform the first dataset
                   this.datasetKeys = Object.keys(this.fileFormatResults.results.datasets);
                   let firstKey = this.datasetKeys[0];
                   this.onDatasetSelected(firstKey);
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

    /**
     * CAlled if a user updates the schema parser

    updateSchemaParserAndPrevew(){
        if(this.selectedParser) {
            this.filePreviewService.updateParserSettings(this.selectedFilePaths, this.selectedParser).subscribe((data:UpdatedParserWithScript) =>{
                this.selectedDataSet.sparkScript = data.updatedScript;
             this.buildTable(data.results);
        });
        }
        // call /spark/files-list, {files:this.selectedFilePaths, parserDescriptor:this.selectedParser,dataFrameVariable:"df"} .then  preview()

        //1. call rest endpoint  generateSparkScriptForSchemaParser(this.selectedParser, this.selectedFilePaths).subscribe(script) =>  then execute the script
    }
    */
    private buildTable(data:TransformResponse) : DataSetPreview{
        let columns = data.results.columns.map((column: any) => {
            return {name: column.field, label: column.displayName, dataType:column.dataType}
        });
        let rows = data.results.rows.map((row: any) => {
            const data1 = {};
            for (let i = 0; i < data.results.columns.length; ++i) {
                data1[data.results.columns[i].field] = row[i];
            }
            return data1;
        });
        return new DataSetPreview({columns:columns, rows:rows})
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
