import {SchemaField} from "../schema-field";
import {DefaultTableSchema} from "../default-table-schema.model";

export class SourceTableSchema extends DefaultTableSchema{
    tableSchema: string;
    constructor() {
        super();
    }
}
