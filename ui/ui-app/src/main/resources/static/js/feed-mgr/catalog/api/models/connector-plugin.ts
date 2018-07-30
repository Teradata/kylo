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

//    dataSourcePlugin?: UiPlugin;
}
