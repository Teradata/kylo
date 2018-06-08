import {SchemaParser} from "../../../model/field-policy";
import {Common} from "../../../../common/CommonTypes";

export interface FileMetadata {

    mimeType:string;
    encoding:string;
    delimiter:string;
    headerCount:string;
    filePath:string;
}
export interface SparkScript{
    fileLocation:string;
    script:string
}

export interface TableColumn {
    name:string;
    label:string;
    dataType:string;
    uuid?:string
}

export class DataSetPreview{
    error:boolean;
    errorMessage:string;
    columns:any;
    rows:any;

    public constructor(init?:Partial<DataSetPreview>) {
        Object.assign(this, init);
    }

    hasColumns(){
        return this.columns != undefined;
    }

    updateError(msg:string){
        this.error = true;
        this.errorMessage = msg;
    }


    clearError(){
        this.error = false;
        this.errorMessage = null;
    }
}

export class FileDataSet {
    files:FileMetadata[]
    mimeType:string;
    sparkScript:SparkScript;
    message:string;
    schemaParser:SchemaParser;
    preview?:DataSetPreview;
    raw?:DataSetPreview;
    displayKey:string;
    key:string;
    loading:boolean;

    schema:TableColumn[];

    public constructor(init?:Partial<FileDataSet>) {
        Object.assign(this, init);
        this.displayKey = this.key.substring(this.key.lastIndexOf("/"));
    }


    hasSparkScript():boolean {
        return this.sparkScript != undefined && this.sparkScript.script != undefined
    }

    hasPreview():boolean {
        return this.preview != undefined && this.preview.hasColumns();
    }

    hasRaw() :boolean {
        return this.raw != undefined && this.raw.hasColumns();
    }
    hasPreviewError():boolean {
        return this.hasPreview() && this.preview.error;
    }
    hasRawError():boolean {
        return this.hasRaw() && this.raw.error;
    }
    clearRawError(){
        if(this.raw){
            this.raw.clearError();
        }
    }
    finishedLoading(){
        this.loading = false;
    }

    clearPreviewError(){
        if(this.preview){
            this.preview.clearError();
        }
    }

    rawError(message:string){
        if(!this.raw){
            this.raw = new DataSetPreview({error:true,errorMessage:message})
        }
        else {
            this.raw.updateError(this.message)
        }
    }

    previewError(message:string){
        if(!this.preview){
            this.preview = new DataSetPreview({error:true,errorMessage:message})
        }
        else {
            this.preview.updateError(message)
        }
    }
}



export class FileMetadataResponse {
    message:string;
    fileMetadata:FileMetadata[];
    datasets:Common.Map<FileDataSet>;

    datasetList:FileDataSet[] = [];

    public constructor(init?:Partial<FileMetadataResponse>) {
      this.message = init.message;
      this.fileMetadata = init.fileMetadata
        if(init.datasets){
           this.datasets = {};
           Object.keys(init.datasets).forEach(key => {
               let datasetData = init.datasets[key];
               datasetData.key = key;
               let dataset = new FileDataSet(datasetData);
              this.datasets[key] = dataset;
              this.datasetList.push(dataset)
          });
        }
    }
}

export class FileMetadataTransformResponse {

    /**
     * Error message
     */
    message: string;
    /**
     * Progress of the transformation
     */
    progress: number;

    /**
     * Result of a transformation
     */
    results: FileMetadataResponse;

    /**
     * Success status of a transformation
     */
    status: string;

    /**
     * Table name with the results
     */
    table: string;

    /**
     * Actual number of rows analyzed
     */
    actualRows: number;

    /**
     * Actual number of cols analyzed
     */
    actualCols: number;

    public constructor(init?:Partial<FileMetadataTransformResponse>) {
        Object.assign(this, init);
        if(init.results){
            this.results = new FileMetadataResponse(init.results);
        }
    }
}
