/**
 * Field of a schema object such as a table or file.
 */
export class SchemaField {

    /**
     * Field or column name
     */
    name: string;

    /**
     * Business description of the field
     */
    description: string;

    /**
     * Data type in reference of the source (e.g. an RDMS)
     */
    nativeDataType?: string;

    /**
     * Data type in reference of the target (e.g. Hive)
     */
    derivedDataType?: string;

    /**
     * Is the field the primary key
     */
    primaryKey: boolean;

    /**
     * Can this field be set to null
     */
    nullable: boolean;

    /**
     * Sample values for field
     */
    sampleValues: string[];

    /**
     * Whether any derived properties of field can be modified
     */
    modifiable?: boolean;

    /**
     * Additional descriptor about the derived data type
     */
    dataTypeDescriptor?: any;

    /**
     * Data type with the precision and scale
     */
    dataTypeWithPrecisionAndScale?: string;

    /**
     * Precision and scale portion of the data type
     */
    precisionScale?: string;

    /**
     * Whether field represents a record creation timestamp
     */
    createdTracker?: boolean;

    /**
     * Whether field represents a record update timestamp
     */
    updatedTracker?: boolean;

    /**
     * Tags assigned to this column.
     */
    tags?: { name: string };
}
