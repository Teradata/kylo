import {Type} from "@angular/core";

export class ProcessorControl {

    private readonly supportedProcessorTypes: string[];

    constructor(readonly component: Type<any>, processorTypes?: string | string[]) {
        if (processorTypes == null) {
            this.supportedProcessorTypes = null;
        } else if (Array.isArray(processorTypes)) {
            this.supportedProcessorTypes = processorTypes;
        } else {
            this.supportedProcessorTypes = [processorTypes];
        }
    }

    supportsProcessorType(processorType: string) {
        if (this.supportedProcessorTypes !== null) {
            const match = this.supportedProcessorTypes.find(supportedProcessorType => processorType === supportedProcessorType);
            return typeof match !== "undefined";
        } else {
            return true;
        }
    }
}
