/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FieldConfig } from "./FieldConfig";
/** @enum {string} */
const Layout = {
    COLUMN: "column",
    ROW: "row",
};
export { Layout };
export class FieldGroup {
    /**
     * @param {?=} layout
     */
    constructor(layout = Layout.COLUMN) {
        this.layout = layout;
        this.DEFAULT_LAYOUT = "start stretch";
        this.fields = [];
        this.fields = [];
    }
    /**
     * @param {?} layoutAlign
     * @return {?}
     */
    setLayoutAlign(layoutAlign) {
        this.layoutAlign = layoutAlign ? layoutAlign : this.DEFAULT_LAYOUT;
    }
}
function FieldGroup_tsickle_Closure_declarations() {
    /** @type {?} */
    FieldGroup.prototype.DEFAULT_LAYOUT;
    /** @type {?} */
    FieldGroup.prototype.fields;
    /** @type {?} */
    FieldGroup.prototype.layoutAlign;
    /** @type {?} */
    FieldGroup.prototype.layout;
}
//# sourceMappingURL=FieldGroup.js.map