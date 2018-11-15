/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FieldConfig } from "./FieldConfig";
export class Textarea extends FieldConfig {
    /**
     * @param {?=} options
     */
    constructor(options = {}) {
        super(options);
        this.controlType = Textarea.CONTROL_TYPE;
        this.readonly = options['readonly'];
        if (this.readonly) {
            this.value = this.readonlyValue;
        }
    }
}
Textarea.CONTROL_TYPE = 'textarea';
function Textarea_tsickle_Closure_declarations() {
    /** @type {?} */
    Textarea.CONTROL_TYPE;
    /** @type {?} */
    Textarea.prototype.controlType;
    /** @type {?} */
    Textarea.prototype.readonly;
}
//# sourceMappingURL=Textarea.js.map