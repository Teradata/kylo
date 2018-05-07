import {ProfileOutputRow} from "./profile-output-row";
import {QueryResultColumn} from "./query-result-column";
import {TransformValidationResult} from "./transform-validation-result";
import {ColumnSort} from "./column-sort";

/**
 * Maintains the state of a Spark script for a single transformation.
 */
export interface ScriptState<T> {

    /**
     * Columns as returned by the server.
     */
    columns: QueryResultColumn[];

    /**
     * UI context for this script state.
     */
    context: any;

    /**
     * Policies to apply during transformation.
     */
    fieldPolicies: any[];

    /**
     * Data profile
     */
    profile: ProfileOutputRow[];

    /**
     * Rows as returned by the server.
     */
    rows: any[][];

    /**
     * Parsed transformation query.
     */
    script: T;

    /**
     * Table containing the results.
     */
    table: string;

    /**
     * Results of applying field policies.
     */
    validationResults: TransformValidationResult[][];

    /**
     * Actual number of rows analyzed by server
     */
    actualRows: number;

    /**
     * Actual number of columns analyzed by server
     */
    actualCols: number;

    /**
     * Identifier for this state
     */
    tableState: number;

    sort:ColumnSort;
}
