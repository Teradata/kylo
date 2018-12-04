import {Property} from "./property";

export interface Processor {

    /**
     * Unique identifier for this processor
     */
    readonly id: string;

    /**
     * The name of this processor
     */
    readonly name: string;

    /**
     * The type of this processor
     */
    readonly type: string;

    /**
     * Descriptors for all the processor's properties
     */
    readonly allProperties: Property[];

    /**
     * Descriptors for the processor's user-editable properties
     */
    readonly properties: Property[];

    /**
     * Indicates if this is an input processor.
     *
     * Input processors are typically the first processor in a flow.
     */
    inputProcessor: boolean;

    /**
     * Indicates if this processor has at least one user-editable property
     */
    userEditable: boolean;
}
