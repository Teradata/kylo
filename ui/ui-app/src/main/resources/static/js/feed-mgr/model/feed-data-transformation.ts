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
}
