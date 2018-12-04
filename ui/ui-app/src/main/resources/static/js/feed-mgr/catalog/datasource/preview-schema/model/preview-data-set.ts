import {DataSource} from "../../../api/models/datasource";
import {PreviewDataSetRequest} from "./preview-data-set-request"
import {TableColumn, TableViewModel} from "./table-view-model";
import {SparkDataSet} from "../../../../model/spark-data-set.model";
import * as _ from "underscore";
import {CloneUtil} from "../../../../../common/utils/clone-util";


export enum DatasetCollectionStatus {
    NEW =1, COLLECTED =2, REMOVED =3
}

/**
 * Core Dataset used for previewing
 * There are other concrete types of data sets used for specialize options
 */
export class PreviewDataSet {

    static EMPTY = new PreviewDataSet();

    /**
     * items in the dataset
     */
    public items:any[];

    /**
     * datasource attached to this data set
     */
    public dataSource:DataSource;

    /**
     * Error message
     */
    public message:string;
    /**
     * the preview of the data
     */
    public preview?:TableViewModel;
    /**
     * the Raw dataset preview, if available
     */
    public raw?:TableViewModel;

    /**
     * flag to determine if raw view is even allowed
     */
    public allowsRawView:boolean;
    /**
     * user friendly display name of the key
     */
    public displayKey:string;
    /**
     * internal key representing this set
     */
    public key:string;

    /**
     * loading the preview
     */
    public previewLoading:boolean;

    /**
     * loading the raw
     */
    public rawLoading:boolean;

    /**
     * The schema
     */
    public  schema:TableColumn[];

    /**
     * Descriptor of this type of dataset
     */
    public type:string = 'PreviewDataSet';

    /**
     * map of the spark options needed to preview this dataset
     */
    public sparkOptions?:{ [key: string]: string } = {}


    public collectionStatus:DatasetCollectionStatus = DatasetCollectionStatus.NEW;


    /**
     * Apply options to the preview request for this dataset
     * @param {PreviewDataSetRequest} previewRequest
     */
    public applyPreviewRequestProperties(previewRequest: PreviewDataSetRequest){
        previewRequest.previewItem = this.getPreviewItemPath()
        previewRequest.properties = {}
    }

    /**
     * If no schemaparser is on this request, but it has spark options, then apply them to the request
     * @param {PreviewDataSetRequest} previewRequest
     */
    public applySparkOptions(previewRequest:PreviewDataSetRequest){
        if(!previewRequest.schemaParser){
            if(this.sparkOptions && !_.isEmpty(this.sparkOptions)){
                Object.keys(this.sparkOptions).forEach(k => {
                    if(k == "format" && (this.dataSource.template.format == undefined || this.dataSource.template.format == '')  ){
                        let format = this.sparkOptions[k]
                        previewRequest.dataSource= CloneUtil.deepCopy(previewRequest.dataSource);
                        previewRequest.dataSource.template.format = format;
                    }
                    else {
                        previewRequest.properties[k] = this.sparkOptions[k];
                    }
                });
            }
            //apply the format if found
        }

    }

    /**
     * Create a SparkDataSet object from the Preview
     * This is used with the Data Wrangler/VisualQuery
     * @return {SparkDataSet}
     */
    public toSparkDataSet(): SparkDataSet {
        let sparkDataSet = new SparkDataSet();
        //sparkDataSet.id = this.displayKey;
        sparkDataSet.title = this.displayKey;
        sparkDataSet.dataSource = this.dataSource;
        sparkDataSet.options = this.sparkOptions;
        sparkDataSet.schema = this.schema;
        sparkDataSet.preview = this;
        sparkDataSet.previewPath = this.getPreviewItemPath();
        if(this.dataSource && this.dataSource.connector && this.dataSource.connector.pluginId == "file-upload"){
            sparkDataSet.isUpload = true;
        }
        return sparkDataSet;
    }



    public constructor(init?:Partial<PreviewDataSet>) {
        this.collectionStatus = DatasetCollectionStatus.NEW;
        Object.assign(this, init);
        if(this.raw){
            this.raw = new TableViewModel(this.raw)
        }
        if(this.preview){
            this.preview = new TableViewModel(this.preview)
        }
    }

    public isCollected(){
        return this.collectionStatus == DatasetCollectionStatus.COLLECTED;
    }

    public isRemoved(){
        return this.collectionStatus == DatasetCollectionStatus.REMOVED;
    }

    public hasPreview():boolean {
        return this.preview != undefined && this.preview.hasColumns();
    }

    public hasRaw() :boolean {
        return this.raw != undefined && this.raw.hasColumns();
    }
    public hasPreviewError():boolean {
        return this.preview && this.preview.error != undefined && this.preview.error == true;
    }
    public hasRawError():boolean {
        return this.raw && this.raw.error != undefined && this.raw.error == true;;
    }
    public clearRawError(){
        if(this.raw){
            this.raw.clearError();
        }
    }

    public isLoading(){
        return this.previewLoading || this.rawLoading;
    }


    public clearPreviewError(){
        if(this.preview){
            this.preview.clearError();
        }
    }

    public rawError(message:string){
        if(!this.raw){
            this.raw = new TableViewModel({error:true,errorMessage:message})
        }
        else {
            this.raw.updateError(this.message)
        }
    }

    public previewError(message:string){
        if(!this.preview){
            this.preview = new TableViewModel({error:true,errorMessage:message})
        }
        else {
            this.preview.updateError(message)
        }
    }

    public isType(type:string){
        return this.type.toLowerCase() == type.toLowerCase();
    }

    public  getPreviewItemPath() :string{
        return this.items != undefined && this.items.length >0 ? this.items[0] : "";
    }

    hasSparkOptions(){
        return this.sparkOptions != undefined && !_.isEmpty(this.sparkOptions)
    }

    public start(rawData:boolean) {
        if (rawData) {
            this.rawLoading = true;
        }
        else {
            this.previewLoading = true;
        }
    }

    public error(rawData:boolean, message:string) {
        if (rawData) {
            this.rawLoading = false;
        }
        else {
            this.previewLoading = false;
        }
    }

    public success(preview:TableViewModel, rawData:boolean){
        if (rawData) {
            this.raw = preview;
            this.clearRawError();
            this.rawLoading = false;
        }
        else {
            this.previewLoading = false;
            this.clearPreviewError()
            this.schema = preview.columns;
            this.preview = preview;
        }
    }

    /**
     * Apply another datasets preview to this dataset
     * @param {PreviewDataSet} dataset
     * @param {boolean} rawData
     */
    applyPreview(dataset:PreviewDataSet, rawData:boolean){
        let preview = rawData ? dataset.raw : dataset.preview;
        this.success(preview,rawData)
        if(!rawData){
            this.sparkOptions = dataset.sparkOptions;
        }
    }

}



