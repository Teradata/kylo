/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FieldConfig } from "./FieldConfig";
export class Select extends FieldConfig {
    /**
     * @param {?=} options
     */
    constructor(options = {}) {
        super(options);
        this.controlType = Select.CONTROL_TYPE;
        this.options = [];
        this.options = options['options'] || [];
    }
}
Select.CONTROL_TYPE = 'select';
function Select_tsickle_Closure_declarations() {
    /** @type {?} */
    Select.CONTROL_TYPE;
    /** @type {?} */
    Select.prototype.controlType;
    /** @type {?} */
    Select.prototype.options;
}
//# sourceMappingURL=Select.js.map