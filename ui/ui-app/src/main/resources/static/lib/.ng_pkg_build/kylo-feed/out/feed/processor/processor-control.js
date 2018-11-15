/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { Type } from "@angular/core";
export class ProcessorControl {
    /**
     * @param {?} component
     * @param {?=} processorTypes
     */
    constructor(component, processorTypes) {
        this.component = component;
        if (processorTypes == null) {
            this.supportedProcessorTypes = null;
        }
        else if (Array.isArray(processorTypes)) {
            this.supportedProcessorTypes = processorTypes;
        }
        else {
            this.supportedProcessorTypes = [processorTypes];
        }
    }
    /**
     * @param {?} processorType
     * @return {?}
     */
    supportsProcessorType(processorType) {
        if (this.supportedProcessorTypes !== null) {
            const /** @type {?} */ match = this.supportedProcessorTypes.find(supportedProcessorType => processorType === supportedProcessorType);
            return typeof match !== "undefined";
        }
        else {
            return true;
        }
    }
}
function ProcessorControl_tsickle_Closure_declarations() {
    /** @type {?} */
    ProcessorControl.prototype.supportedProcessorTypes;
    /** @type {?} */
    ProcessorControl.prototype.component;
}
//# sourceMappingURL=processor-control.js.map