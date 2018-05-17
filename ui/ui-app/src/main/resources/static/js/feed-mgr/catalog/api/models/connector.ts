import {ConnectorTab} from "./connector-tab";
import {DataSetTemplate} from './dataset-template';

/**
 * Connector describes a how one can connect to a data source, e.g.
 * what input is required to connect to S3, JDBC, Kafka, Hive, etc
 */
export interface Connector {

    id: string;

    title: string;

    icon?: string;

    color?: string;

    tabs?: ConnectorTab[],

    template?: DataSetTemplate
}
