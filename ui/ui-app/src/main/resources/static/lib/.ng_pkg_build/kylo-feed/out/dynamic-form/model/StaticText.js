/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FieldConfig } from "./FieldConfig";
export class StaticText extends FieldConfig {
    /**
     * @param {?=} options
     */
    constructor(options = {}) {
        super(options);
        this.controlType = StaticText.CONTROL_TYPE;
        this.showDivider = options['showDivider'] || true;
        this.textStyleClass = options['textStyleClass'] || '';
        this.staticText = options['staticText'] || '';
    }
}
StaticText.CONTROL_TYPE = "static-text";
function StaticText_tsickle_Closure_declarations() {
    /** @type {?} */
    StaticText.CONTROL_TYPE;
    /** @type {?} */
    StaticText.prototype.controlType;
    /** @type {?} */
    StaticText.prototype.showDivider;
    /** @type {?} */
    StaticText.prototype.staticText;
    /** @type {?} */
    StaticText.prototype.textStyleClass;
}
//# sourceMappingURL=StaticText.js.map