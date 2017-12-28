import {JdbcDatasource} from "./datasource";

/**
 * Request to save data wrangler results.
 */
export interface SaveRequest {

    /**
     * Name of the output data source.
     */
    format?: string;

    /**
     * Identifier of the target data source.
     */
    jdbc?: JdbcDatasource;

    /**
     * Behavior when data or table already exists.
     */
    mode?: string;

    /**
     * Target table name.
     */
    tableName?: string;
}

/**
 * Status of saving data wrangler results.
 */
export interface SaveResponse {

    /**
     * Save identifier
     */
    id: string;

    /**
     * Location for downloading file
     */
    location?: string;

    /**
     * Error message
     */
    message?: string;

    /**
     * Progress of the save
     */
    progress?: number;

    /**
     * Success status of the save
     */
    status: SaveResponseStatus;
}

/**
 * Status of a save.
 */
export enum SaveResponseStatus {

    ERROR = "ERROR",
    PENDING = "PENDING",
    SUCCESS = "SUCCESS"
}
