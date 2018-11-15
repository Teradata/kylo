/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FieldConfig } from "./FieldConfig";
export class RadioButton extends FieldConfig {
    /**
     * @param {?=} options
     */
    constructor(options = {}) {
        super(options);
        this.controlType = RadioButton.CONTROL_TYPE;
        this.options = [];
        this.options = options['options'] || [];
    }
}
RadioButton.CONTROL_TYPE = 'radio';
function RadioButton_tsickle_Closure_declarations() {
    /** @type {?} */
    RadioButton.CONTROL_TYPE;
    /** @type {?} */
    RadioButton.prototype.controlType;
    /** @type {?} */
    RadioButton.prototype.options;
}
//# sourceMappingURL=RadioButton.js.map