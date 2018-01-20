import {QueryResultColumn} from "./query-result-column";
import {TransformValidationResult} from "./transform-validation-result";

/**
 * Model used to pass the query results.
 */
export interface TransformQueryResult {

    /**
     * Columns in query result.
     */
    columns: QueryResultColumn[];

    /**
     * Rows in query result.
     */
    rows: any[][];

    /**
     * List of validation results for each row.
     */
    validationResults: TransformValidationResult[][];
}
