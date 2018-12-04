import {TableSchema} from "./table-schema";
import {Schema} from "./schema";
import {SchemaField} from "./schema-field";
import {TableColumnDefinition} from "./TableColumnDefinition";
import {ObjectUtils} from "../../../lib/common/utils/object-utils";



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

    hasFields(){
        this.fields != undefined && this.fields.length >0;
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

    protected ensureObjectTypes() {
        let fields = this.fields.map((field: any) => {
            let tableColumnDef = ObjectUtils.getAs(field,TableColumnDefinition);
            return tableColumnDef;
        });
        this.fields = fields;
    }

}

