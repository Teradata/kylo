import {Schema} from "./schema";

/**
 * Schema of a table object
 */
export interface TableSchema extends Schema {

    /**
     * Table schema name
     */
    schemaName: string;

    /**
     * Database name
     */
    databaseName: string;
}
