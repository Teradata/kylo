import {ProfileOutputRow} from "./profile-output-row";
import {TransformQueryResult} from "./transform-query-result";

export interface TransformResponse {

    /**
     * Error message
     */
    message: string;

    /**
     * Profiled column statistics.
     */
    profile: ProfileOutputRow[];

    /**
     * Progress of the transformation
     */
    progress: number;

    /**
     * Result of a transformation
     */
    results: TransformQueryResult;

    /**
     * Success status of a transformation
     */
    status: string;

    /**
     * Table name with the results
     */
    table: string;
}
