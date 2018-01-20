export * from "./model/profile-output-row";
export * from "./model/query-result-column";
export * from "./model/script-state";
export * from "./model/transform-query-result";
export * from "./model/transform-request";
export * from "./model/transform-response";
export * from "./model/transform-validation-result";
export * from "./column-delegate";
export * from "./parse-exception";
export * from "./query-engine";
export * from "./query-engine-constants";
export * from "./query-engine-factory.service";
export * from "./query-parser";
export * from "./script-builder";
export * from "./script-expression";
export * from "./script-expression-type";

export interface StringUtilsStatic {
    quote(str: string): string;

    quoteSql(str: string): string;

    quoteSql(str: string, quoteChar: string, escapeChar: string): string;
}

export const StringUtils: StringUtilsStatic = (window as any).StringUtils;

/**
 * Supported SQL dialects
 */
export enum SqlDialect {

    /** Hive Query Language dialect */
    HIVE,

    /** Teradata SQL dialect */
    TERADATA
}

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

export declare namespace DatasourcesServiceStatic {

    interface DatasourcesService {
        deleteById(id: string): angular.IPromise<any>;

        filterArrayByIds(ids: string | string[], array: JdbcDatasource[]): JdbcDatasource[];

        findAll(): angular.IPromise<any>;

        findById(id: string): angular.IPromise<any>;

        getTableSchema(id: string, table: string, schema?: string): angular.IPromise<TableSchema>;

        listTables(id: string, query?: string): angular.IPromise<TableReference[]>;

        newJdbcDatasource(): JdbcDatasource;

        save(datasource: JdbcDatasource): angular.IPromise<JdbcDatasource>;
    }

    interface JdbcDatasource {
        id?: string;
        name: string;
        description: string;
        sourceForFeeds: any;
        type: string;
        databaseConnectionUrl: string;
        databaseDriverClassName: string;
        databaseDriverLocation: string;
        databaseUser: string;
        password: string;
    }

    interface TableReference {
        schema: string;
        tableName: string;
        fullName: string;
        fullNameLower: string;
    }
}

/**
 * A data source created and managed by a Kylo user.
 */
export interface UserDatasource {

    /**
     * Unique id for this data source.
     */
    id: string;

    /**
     * Human-readable name.
     */
    name: string;

    /**
     * Description of this data source.
     */
    description: string;

    /**
     * Type name of this data source.
     */
    type: string;
}

/**
 * Defines a connection to a JDBC data source.
 */
export interface JdbcDatasource extends UserDatasource {

    /**
     * Id of the NiFi DBCPConnectionPool controller service
     */
    controllerServiceId: string;

    /**
     * A database URL of the form jdbc:<i>subprotocol:subname</i>
     */
    databaseConnectionUrl: string;

    /**
     * Database driver class name
     */
    databaseDriverClassName: string;

    /**
     * Comma-separated list of files/folders and/or URLs containing the driver JAR and its dependencies (if any)
     */
    databaseDriverLocation: string;

    /**
     * Database user name
     */
    databaseUser: string;

    /**
     * Password to use when connecting to this data source
     */
    password: string;
}
