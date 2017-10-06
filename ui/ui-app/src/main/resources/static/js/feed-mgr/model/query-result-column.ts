/**
 * A column in a QueryResult.
 */
export interface QueryResultColumn {

    /**
     * Human-readable description of this column.
     */
    comment: string;

    /**
     * Name of the database containing the table.
     */
    databaseName: string;

    /**
     * Name of the data type for the column.
     */
    dataType: string;

    /**
     * Human-readable name for the column.
     */
    displayName: string;

    /**
     * Name of the column in the table.
     */
    field: string;

    /**
     * Suggested title for the column, usually specified by the AS clause.
     */
    hiveColumnLabel: string;

    /**
     * Position of the column in the table.
     */
    index: number;

    /**
     * Database-specific type name.
     */
    nativeDataType: string;

    /**
     * Name of the source table.
     */
    tableName: string;
}
