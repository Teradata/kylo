/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FieldConfig } from "./FieldConfig";
/** @enum {string} */
const InputType = {
    color: "color",
    date: "date",
    datetime_local: "datetime-local",
    email: "email",
    month: "month",
    number: "number",
    password: "password",
    search: "search",
    tel: "tel",
    text: "text",
    time: "time",
    url: "url",
    week: "week",
    cron: "cron",
};
export { InputType };
export class InputText extends FieldConfig {
    /**
     * @param {?=} options
     */
    constructor(options = {}) {
        super(options);
        this.controlType = InputText.CONTROL_TYPE;
        this.type = options['type'] || '';
        this.readonly = options['readonly'];
        if (this.readonly) {
            this.value = this.readonlyValue;
        }
    }
}
InputText.CONTROL_TYPE = 'textbox';
function InputText_tsickle_Closure_declarations() {
    /** @type {?} */
    InputText.CONTROL_TYPE;
    /** @type {?} */
    InputText.prototype.controlType;
    /** @type {?} */
    InputText.prototype.type;
    /** @type {?} */
    InputText.prototype.readonly;
}
//# sourceMappingURL=InputText.js.map