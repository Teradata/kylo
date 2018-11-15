/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FieldConfig } from "./FieldConfig";
import { MatCheckboxChange } from "@angular/material";
export class Checkbox extends FieldConfig {
    /**
     * @param {?=} options
     */
    constructor(options = {}) {
        super(options);
        this.controlType = Checkbox.CONTROL_TYPE;
        this.trueValue = options['trueValue'] || 'true';
        this.falseValue = options['falseValue'] || 'false';
        this.initCheckedValue();
    }
    /**
     * @return {?}
     */
    initCheckedValue() {
        let /** @type {?} */ value = this.getModelValue();
        if (value && value == this.trueValue) {
            this.checked = true;
        }
        else {
            this.checked = false;
        }
    }
    /**
     * @param {?} event
     * @return {?}
     */
    onChange(event) {
        if (event.checked) {
            this.setModelValue(this.trueValue);
        }
        else {
            this.setModelValue(this.falseValue);
        }
    }
}
Checkbox.CONTROL_TYPE = "checkbox";
function Checkbox_tsickle_Closure_declarations() {
    /** @type {?} */
    Checkbox.CONTROL_TYPE;
    /** @type {?} */
    Checkbox.prototype.controlType;
    /** @type {?} */
    Checkbox.prototype.trueValue;
    /** @type {?} */
    Checkbox.prototype.falseValue;
    /** @type {?} */
    Checkbox.prototype.checked;
}
//# sourceMappingURL=Checkbox.js.map