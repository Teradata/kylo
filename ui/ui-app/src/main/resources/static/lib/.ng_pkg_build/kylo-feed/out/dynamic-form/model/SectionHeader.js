/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FieldConfig } from "./FieldConfig";
export class SectionHeader extends FieldConfig {
    /**
     * @param {?=} options
     */
    constructor(options = {}) {
        super(options);
        this.controlType = SectionHeader.CONTROL_TYPE;
        this.showDivider = options['showDivider'] || true;
    }
}
SectionHeader.CONTROL_TYPE = "section-header";
function SectionHeader_tsickle_Closure_declarations() {
    /** @type {?} */
    SectionHeader.CONTROL_TYPE;
    /** @type {?} */
    SectionHeader.prototype.controlType;
    /** @type {?} */
    SectionHeader.prototype.showDivider;
}
//# sourceMappingURL=SectionHeader.js.map