import {QueryResultColumn} from "../../model/query-result-column";
import {ProfileOutputRow} from "../../model/profile-output-row";

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
     * Data profile
     */
    profile: ProfileOutputRow[];

    /**
     * Rows as returned by the server.
     */
    rows: { [k: string]: any }[];

    /**
     * Parsed transformation query.
     */
    script: T;

    /**
     * Table containing the results.
     */
    table: string;
}
