/**
 * A request to perform a transformation on a table.
 */
import {PageSpec} from "../query-engine";

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

    /**
     * If present will return data for the specified page only
     */
    pageSpec: PageSpec;

    /**
     * Whether to perform the profile stage
     */
    doProfile: boolean;

    /*
      Whether to perform the validation stage
     */
    doValidate: boolean;
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
