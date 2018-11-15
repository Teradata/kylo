import { Type } from "@angular/core";
export declare class ProcessorControl {
    readonly component: Type<any>;
    private readonly supportedProcessorTypes;
    constructor(component: Type<any>, processorTypes?: string | string[]);
    supportsProcessorType(processorType: string): boolean;
}
