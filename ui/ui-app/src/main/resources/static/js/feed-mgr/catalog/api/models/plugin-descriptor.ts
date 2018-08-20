import {ConnectorTab} from "./connector-tab";
import {UiOption} from './ui-option';

/**
 * Connector describes a how one can connect to a data source, e.g.
 * what input is required to connect to S3, JDBC, Kafka, Hive, etc
 */
export interface PluginDescriptor {

    pluginId: string;

    tabs?: ConnectorTab[];

    options?: UiOption[];

    optionsMapperId?: string;

//    dataSourcePlugin?: UiPlugin;
}
