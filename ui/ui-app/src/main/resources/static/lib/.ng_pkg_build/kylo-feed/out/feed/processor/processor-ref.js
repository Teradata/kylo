/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { AbstractControl, FormArray, FormGroup } from "@angular/forms";
import { Processor } from "../model/processor";
export class ProcessorRef {
    /**
     * @param {?} processor
     * @param {?=} feed
     */
    constructor(processor, feed) {
        this.processor = processor;
        this.feed = feed;
        this.form = new FormArray([]);
    }
    /**
     * @return {?}
     */
    get control() {
        return this.form.at(0);
    }
    /**
     * @param {?} control
     * @return {?}
     */
    set control(control) {
        if (control != null) {
            this.form.setControl(0, control);
        }
        else {
            this.form.removeAt(0);
        }
    }
    /**
     * @return {?}
     */
    get id() {
        return this.processor.id;
    }
    /**
     * @return {?}
     */
    get name() {
        return this.processor.name;
    }
    /**
     * @return {?}
     */
    get type() {
        return this.processor.type;
    }
}
function ProcessorRef_tsickle_Closure_declarations() {
    /** @type {?} */
    ProcessorRef.prototype.form;
    /** @type {?} */
    ProcessorRef.prototype.formGroup;
    /** @type {?} */
    ProcessorRef.prototype.processor;
    /** @type {?} */
    ProcessorRef.prototype.feed;
}
//# sourceMappingURL=processor-ref.js.map