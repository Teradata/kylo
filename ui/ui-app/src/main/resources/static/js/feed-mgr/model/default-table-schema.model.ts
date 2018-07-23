import {TableSchema} from "./table-schema";
import {Schema} from "./schema";
import {SchemaField} from "./schema-field";
import {TableColumnDefinition} from "./TableColumnDefinition";



export class DefaultSchema implements Schema {

    /**
     * Unique id of the schema object
     */
    id: string;

    /**
     * Unique name
     */
    name: string;

    /**
     * Business description of the object
     */
    description: string;

    /**
     * Canonical charset name
     */
    charset: string;

    /**
     * Format-specific properties of the data structure. For example, whether the file contains a header, footer, field or row delimiter types, escape characters, etc.
     */
    properties: { [k: string]: string } = {};

    /**
     * Field structure
     */
    fields: SchemaField[] = [];
    constructor() {

    }
}



export class DefaultTableSchema extends DefaultSchema implements TableSchema{

    /**
     * Table schema name
     */
    schemaName: string;

    /**
     * Database name
     */
    databaseName: string;
    constructor() {
        super();
    }

}

