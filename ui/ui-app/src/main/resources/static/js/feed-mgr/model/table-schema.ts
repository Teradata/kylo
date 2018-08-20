import {Schema} from "./schema";
import {SchemaField} from "./schema-field";

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
