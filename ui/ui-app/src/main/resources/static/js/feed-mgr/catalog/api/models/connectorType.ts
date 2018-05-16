import {ConnectorTab} from "./connector-tab";

/**
 * ConnectorType describes a how one can connect to a data source, e.g.
 * what input is required to connect to S3, JDBC, Kafka, Hive, etc
 */
export interface ConnectorType {

    type: string,

    color?: string;

    hidden?: boolean;

    icon?: string;

    id?: string;

    tabs?: ConnectorTab[],

    title: string;
}
