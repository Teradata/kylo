import {DataSource} from "../../../api/models/datasource";
import {PageSpec} from "../../../../visual-query/wrangler/query-engine";
import {SchemaParser} from "../../../../model/field-policy";
import {Common} from '../../../../../../lib/common/CommonTypes';

/**
 * Request to preview a given data set
 */
export class PreviewDataSetRequest {
    /**
     * Datasource used for preview
     */
    dataSource:DataSource;
    /**
     * The item being previewed
     */
    previewItem:string;
    /**
     * Limit the rows returned in the preview
     */
    pageSpec:PageSpec
    /**
     * Optional path of the file being previewed
     */
    previewPath?:string;
    /**
     * Additional Spark options that will be addded to the request
     */
    properties:Common.Map<string>;
    /**
     * Optional schema parser used for the request
     */
    schemaParser?:SchemaParser;

    displayKey ?:string;

    addPreviewItemToPath:boolean;

    /**
     * Should we fallback to use the text parser if it errors out
     */
    fallbackToTextOnError:boolean;

    /**
     * is this a file based preview (i.e. should it use a schema parser)
     */
    filePreview:boolean

    constructor(){
        this.pageSpec = new PageSpec();
        this.pageSpec.firstRow =0;
        this.pageSpec.numRows= 20;
    }

    hasPreviewPath(){
        return this.previewPath != undefined;
    }

    getSchemaParserSparkOptions(){
        let sparkOptions : { [key: string]: string } = {};
        if(this.schemaParser){
            if(this.schemaParser.properties){
                this.schemaParser.properties.forEach(policy => {
                    let value = policy.value;
                    if(policy.additionalProperties) {
                        let options = policy.additionalProperties.filter(p => "spark.option" == p.label).forEach(lv => {
                            sparkOptions[lv.value] = value
                        });
                    }
                })
            }
            sparkOptions["format"] = this.schemaParser.sparkFormat;
        }
        return sparkOptions;
    }

}
