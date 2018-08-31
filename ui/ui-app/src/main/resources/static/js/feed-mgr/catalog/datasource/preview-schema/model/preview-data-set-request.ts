import {DataSource} from "../../../api/models/datasource";
import {Common} from "../../../../../common/CommonTypes";
import {PageSpec} from "../../../../visual-query/wrangler/query-engine";
import {SchemaParser} from "../../../../model/field-policy";

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

    constructor(){
        this.pageSpec = new PageSpec();
        this.pageSpec.firstRow =0;
        this.pageSpec.numRows= 20;
    }

    hasPreviewPath(){
        return this.previewPath != undefined;
    }

}