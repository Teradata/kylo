/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FormGroup, ValidatorFn } from "@angular/forms";
const /** @type {?} */ NgIfTrue = (form) => { return true; };
const ɵ0 = NgIfTrue;
export class FieldConfig {
    /**
     * @param {?=} options
     */
    constructor(options = {}) {
        this.modelValueProperty = options.modelValueProperty || 'value';
        this.value = options.value;
        this.key = options.key || '';
        this.required = !!options.required;
        this.order = options.order === undefined ? 1 : options.order;
        this.controlType = options.controlType || '';
        this.placeholder = options.placeholder || '';
        this.model = (options.model && options.model.hasOwnProperty(this.modelValueProperty)) ? options.model : this;
        this.hint = options.hint || '';
        this.readonlyValue = options.readonlyValue || this.model.value;
        this.pattern = options.pattern;
        this.disabled = options.disabled || false;
        this.styleClass = options.styleClass || '';
        this.placeholderLocaleKey = options.placeholderLocaleKey;
        this.validators = options.validators;
        this.ngIf = options.ngIf ? options.ngIf : NgIfTrue;
        this.onModelChange = options.onModelChange;
        this.getErrorMessage = options.getErrorMessage;
    }
    /**
     * @return {?}
     */
    isStaticText() {
        return this.controlType == "static-text" || this.controlType == "section-header" || this.controlType == "icon";
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setModelValue(value) {
        this.model[this.modelValueProperty] = value;
    }
    /**
     * @return {?}
     */
    getModelValue() {
        return this.model[this.modelValueProperty];
    }
}
function FieldConfig_tsickle_Closure_declarations() {
    /** @type {?} */
    FieldConfig.prototype.value;
    /** @type {?} */
    FieldConfig.prototype.key;
    /** @type {?} */
    FieldConfig.prototype.label;
    /** @type {?} */
    FieldConfig.prototype.required;
    /** @type {?} */
    FieldConfig.prototype.order;
    /** @type {?} */
    FieldConfig.prototype.controlType;
    /** @type {?} */
    FieldConfig.prototype.placeholder;
    /** @type {?} */
    FieldConfig.prototype.model;
    /** @type {?} */
    FieldConfig.prototype.hint;
    /** @type {?} */
    FieldConfig.prototype.readonlyValue;
    /** @type {?} */
    FieldConfig.prototype.modelValueProperty;
    /** @type {?} */
    FieldConfig.prototype.pattern;
    /** @type {?} */
    FieldConfig.prototype.onModelChange;
    /** @type {?} */
    FieldConfig.prototype.validators;
    /** @type {?} */
    FieldConfig.prototype.disabled;
    /** @type {?} */
    FieldConfig.prototype.placeholderLocaleKey;
    /** @type {?} */
    FieldConfig.prototype.labelLocaleKey;
    /** @type {?} */
    FieldConfig.prototype.styleClass;
    /** @type {?} */
    FieldConfig.prototype.ngIf;
    /** @type {?} */
    FieldConfig.prototype.getErrorMessage;
}
export { ɵ0 };
//# sourceMappingURL=FieldConfig.js.map