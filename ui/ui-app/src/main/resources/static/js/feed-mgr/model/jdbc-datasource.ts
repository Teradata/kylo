import {UserDatasource} from "./user-datasource";

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
