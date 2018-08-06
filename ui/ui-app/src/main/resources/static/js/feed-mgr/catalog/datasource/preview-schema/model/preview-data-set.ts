
import {FileMetadata} from  "./file-metadata"
import {DataSource} from "../../../api/models/datasource";
import {SchemaParser} from "../../../../model/field-policy";
import {PreviewDataSetRequest} from "./preview-data-set-request"
import {TableViewModel, TableColumn} from "./table-view-model";
import {Common} from "../../../../../common/CommonTypes";
import {SparkDataSet} from "../../../../model/spark-data-set.model";


export enum DatasetCollectionStatus {
    NEW =1, COLLECTED =2, REMOVED =3
}

/**
 * Core Dataset used for previewing
 * There are other concrete types of data sets used for specialize options
 */
export class PreviewDataSet {

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
     * is the data loading
     */
    public loading:boolean;
    /**
     * The schema
     */
    public  schema:TableColumn[];

    /**
     * Descriptor of this type of dataset
     */
    public type:string = 'PreviewDataSet';


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
     * Create a SparkDataSet object from the Preview
     * This is used with the Data Wrangler/VisualQuery
     * @return {SparkDataSet}
     */
    public toSparkDataSet(): SparkDataSet {
        let sparkDataSet = new SparkDataSet();
        sparkDataSet.id = this.displayKey;
        sparkDataSet.dataSource = this.dataSource;
        sparkDataSet.options = {};
        sparkDataSet.schema = this.schema;
        return sparkDataSet;
    }



    public constructor(init?:Partial<PreviewDataSet>) {
        this.collectionStatus = DatasetCollectionStatus.NEW;
        Object.assign(this, init);
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
        return this.hasPreview() && this.preview.error != undefined && this.preview.error == true;
    }
    public hasRawError():boolean {
        return this.hasRaw()&& this.raw.error != undefined && this.raw.error == true;;
    }
    public clearRawError(){
        if(this.raw){
            this.raw.clearError();
        }
    }
    public finishedLoading(){
        this.loading = false;
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

}



