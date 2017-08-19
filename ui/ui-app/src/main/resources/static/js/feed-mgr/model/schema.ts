import {SchemaField} from "./schema-field";

/**
 * Schema represents the structure and encoding of a dataset. Given a embedded schema such as cobol copybook or avro, the schema only derives information required to extract field structure and
 * properties of that schema that might be considered useful metadata.
 */
export interface Schema {

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
    properties: { [k: string]: string };

    /**
     * Field structure
     */
    fields: SchemaField[];
}
