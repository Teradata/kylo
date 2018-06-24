import {TableSchema} from "../visual-query/wrangler/index";

export declare namespace DatasourcesServiceStatic {

    interface DatasourcesService {
        deleteById(id: string): angular.IPromise<any>;

        filterArrayByIds(ids: string | string[], array: JdbcDatasource[]): JdbcDatasource[];

        findAll(): angular.IPromise<any>;

        findById(id: string): angular.IPromise<any>;

        getTableSchema(id: string, table: string, schema?: string): angular.IPromise<TableSchema>;

        listTables(id: string, query?: string): angular.IPromise<TableReference[]>;

        newJdbcDatasource(): JdbcDatasource;

        getHiveDatasource(): JdbcDatasource;

        save(datasource: JdbcDatasource): angular.IPromise<JdbcDatasource>;

        defaultIconName(): string;

        defaultIconColor(): string;
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
