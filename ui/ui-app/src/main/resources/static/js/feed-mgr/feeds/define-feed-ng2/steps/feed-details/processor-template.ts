/**
 * Define custom NiFi Processor Rendering when creating/editing feeds
 */
export interface ProcessorTemplate {

    /**
     * Match against the display name of the processor
     */
    processorDisplayName: string;

    /**
     * An array of the NiFi processor class name (i.e. com.thinkbiganalytics.nifi.GetTableData)
     */
    processorTypes: string[];

    /**
     * The Angular module containing the providers and components for rendering the processor.
     */
    module: string;
}
