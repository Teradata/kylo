/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FieldConfig } from "../model/FieldConfig";
import { FieldConfigBuilder } from "./field-config-builder";
export class FormFieldBuilder {
    constructor() {
        this.fields = [];
    }
    /**
     * @param {?} fieldBuilder
     * @return {?}
     */
    field(fieldBuilder) {
        this.fields.push(fieldBuilder);
        return this;
    }
    /**
     * Builds the FieldConfig objects needed for a form
     * @return {?}
     */
    build() {
        let /** @type {?} */ fieldConfiguration = [];
        this.fields.forEach((builder, i) => {
            let /** @type {?} */ fieldConfig = builder.build();
            if (fieldConfig.key == undefined || fieldConfig.key == "") {
                //auto add the key
                fieldConfig.key = "field-" + i;
            }
            fieldConfiguration.push(fieldConfig);
        });
        return fieldConfiguration;
    }
}
function FormFieldBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    FormFieldBuilder.prototype.fields;
}
//# sourceMappingURL=form-field-builder.js.map