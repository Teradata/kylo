/**
 * Maps data set properties to NiFi properties
 */
export interface ConnectorPluginNifiProperties {

    /**
     * List of NiFi processor types matching this connector
     */
    processorTypes: string[];

    /**
     * Map of data set properties to NiFi properties
     */
    properties: Map<string, string>;
}
