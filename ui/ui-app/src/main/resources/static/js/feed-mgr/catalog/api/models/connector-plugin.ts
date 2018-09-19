import {ConnectorPluginNifiProperties} from "./connector-plugin-nifi-properties";
import {ConnectorTab} from "./connector-tab";
import {UiOption} from './ui-option';

/**
 * ConnectorPluginDescriptor describes a how to display a connector plugin when constructing
 * a new connector.
 */
export interface ConnectorPlugin {

    pluginId: string;

    tabs?: ConnectorTab[];

    options?: UiOption[];

    optionsMapperId?: string;

    /**
     * List of NiFi properties that can be overridden by a data set
     */
    nifiProperties?: ConnectorPluginNifiProperties[];

//    dataSourcePlugin?: UiPlugin;
}
