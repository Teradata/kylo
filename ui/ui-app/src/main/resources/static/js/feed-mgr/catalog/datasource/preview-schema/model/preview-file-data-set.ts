import {SchemaParser} from "../../../../model/field-policy";
import {FileMetadata} from "./file-metadata";
import {PreviewDataSet} from "./preview-data-set";
import {PreviewDataSetRequest} from "./preview-data-set-request";
import {SparkDataSet} from "../../../../model/spark-data-set.model";


export interface SparkScript{
    fileLocation:string;
    script:string
}


/**
 * File based dataset (i.e. for local file, S3, hadoop etc)
 */
export class PreviewFileDataSet extends PreviewDataSet{
    public files:FileMetadata[]
    public mimeType:string;
    public sparkScript?:SparkScript;
    public schemaParser:SchemaParser;

    /**
     * if a user updates the schema parser via the ui it will update it here
     */
    public userModifiedSchemaParser:SchemaParser;

    public constructor(init?:Partial<PreviewFileDataSet>) {
        super(init);
        Object.assign(this, init);
        this.type = "FileDataSet"
        this.items = this.files;
        this.allowsRawView = true;
        this.displayKey = this.key.substring(this.key.lastIndexOf("/"));
    }

    hasSparkScript():boolean {
        return this.sparkScript != undefined && this.sparkScript.script != undefined
    }

    public  getPreviewItemPath() :string{
        if(this.files && this.files.length >0){
            return this.files[0].filePath;
        }
    }

    public applyPreviewRequestProperties(previewRequest: PreviewDataSetRequest){
        super.applyPreviewRequestProperties(previewRequest);
        previewRequest.schemaParser = this.schemaParser
        previewRequest.properties = {};
        previewRequest.properties.path = previewRequest.previewItem;
    }

    public toSparkDataSet(): SparkDataSet {
        let sparkDataSet = super.toSparkDataSet();
        sparkDataSet.paths = Array.isArray(this.files) ? this.files.map(file => file.filePath) : null;

        //parse the schemaParser
        if(this.schemaParser){
            sparkDataSet.format = this.schemaParser.sparkFormat;
            this.schemaParser.properties.forEach((property) => {
                if(property.additionalProperties){
                    let properties = property.additionalProperties.filter((labelValue)=> "spark.option" == labelValue.label).map((labelValue) => labelValue.value);
                    if(properties && properties.length>0){
                        sparkDataSet.options[properties[0]] = property.value;
                    }
                }
            })
        }
        return sparkDataSet;
    }

    applyPreview(dataset:PreviewDataSet, rawData:boolean){
        super.applyPreview(dataset, rawData);
        if(!rawData){
            this.schemaParser = (<PreviewFileDataSet>dataset).schemaParser;
        }
    }

}
