import {ConnectorTab} from "./connector-tab";
import {DataSourceTemplate} from './datasource-template';
import {UiOption} from './ui-option';

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

    options?: UiOption[],

    template?: DataSourceTemplate
}
