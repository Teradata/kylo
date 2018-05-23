


export interface SampleFile{
    /**
     * the file
     */
    fileLocation?:string;

    /**
     * the name of the local file being uploaded
     */
    localFileName?:string;
    /**
     * the local file upload object
     */
    localFileObject?:any //file object
    /**
     * did the file change
     */
    sampleFileChanged?:boolean;
    /**
     * Parsing options passed in to create the script
     */
    schemaParser?:any;

    /**
     * Generated script from the server
     */
    script?:string;
}

/**
 * Model for transforming data.
 */
export interface FeedDataTransformation {

    /**
     * List of user data sources
     */
    $datasources: any;

    /**
     * List of selected columns and tables
     */
    $selectedColumnsAndTables: any;

    /**
     * Identifier of the selected data source
     */
    $selectedDatasourceId: any;

    /**
     * Model for the flowchart
     */
    chartViewModel: object;

    /**
     * List of required datasource ids
     */
    datasourceIds: string[];

    /**
     * Script with data transformations
     */
    dataTransformScript: string;

    /**
     * SQL query
     */
    sql: string;

    /**
     * Internal representation of script in query engine.
     */
    states: object;

    /**
     * the local file if selected
     */
    sampleFile?:SampleFile;

    /**
     * Flag to indicate the sampleFile changed and a new query is needed
     * @type {boolean}
     */
    sampleFileChanged?: boolean;
}
