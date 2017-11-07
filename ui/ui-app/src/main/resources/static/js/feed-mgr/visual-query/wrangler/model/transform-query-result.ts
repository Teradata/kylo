import {QueryResultColumn} from "./query-result-column";
import {TransformValidationResult} from "./transform-validation-result";

/**
 * Model used to pass the query results.
 */
export interface TransformQueryResult {

    /**
     * Column display name map.
     */
    columnDisplayNameMap: { [displayName: string]: QueryResultColumn };

    /**
     * Column field map.
     */
    columnFieldMap: { [field: string]: QueryResultColumn };

    /**
     * Columns in query result.
     */
    columns: QueryResultColumn[];

    /**
     * Query string.
     */
    query: string;

    /**
     * Rows in query result.
     */
    rows: object[];

    /**
     * List of validation results for each row.
     */
    validationResults: TransformValidationResult[][];
}
