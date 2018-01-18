export interface JdbcDatasource {
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
