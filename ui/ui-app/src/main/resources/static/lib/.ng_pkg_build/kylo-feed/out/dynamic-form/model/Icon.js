/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FieldConfig } from "./FieldConfig";
export class Icon extends FieldConfig {
    /**
     * @param {?=} options
     */
    constructor(options = {}) {
        super(options);
        this.controlType = Icon.CONTROL_TYPE;
        this.name = options['name'] || "";
        this.size = options['size'] || 20;
    }
}
Icon.CONTROL_TYPE = 'icon';
function Icon_tsickle_Closure_declarations() {
    /** @type {?} */
    Icon.CONTROL_TYPE;
    /** @type {?} */
    Icon.prototype.controlType;
    /** @type {?} */
    Icon.prototype.name;
    /** @type {?} */
    Icon.prototype.size;
}
//# sourceMappingURL=Icon.js.map