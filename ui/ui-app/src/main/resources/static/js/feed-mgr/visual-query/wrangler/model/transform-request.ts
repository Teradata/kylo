/**
 * A request to perform a transformation on a table.
 */
export interface TransformRequest {

    /**
     * List of data sources to make available
     */
    datasources?: any[];

    /**
     * Previous transformation result
     */
    parent?: TransformRequestParent;

    /**
     * Field validation policies
     */
    policies?: any[];

    /**
     * Scala script with transformation
     */
    script: string;
}

/**
 * Results of a previous transformation.
 */
export interface TransformRequestParent {

    /**
     * Scala script with the transformation
     */
    script: string;

    /**
     * Table containing the results
     */
    table: string;
}
