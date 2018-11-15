/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FormGroup } from "@angular/forms";
import { FieldConfigBuilder } from "./field-config-builder";
import { DynamicFormFieldGroupBuilder } from "./dynamic-form-field-group-builder";
import { FieldGroup, Layout } from "../model/FieldGroup";
export class FormConfig {
    constructor() {
    }
}
function FormConfig_tsickle_Closure_declarations() {
    /** @type {?} */
    FormConfig.prototype.title;
    /** @type {?} */
    FormConfig.prototype.message;
    /** @type {?} */
    FormConfig.prototype.fieldGroups;
    /** @type {?} */
    FormConfig.prototype.onApplyFn;
    /** @type {?} */
    FormConfig.prototype.onCancelFn;
    /** @type {?} */
    FormConfig.prototype.form;
}
/**
 * @record
 */
export function StyleClassStrategy() { }
function StyleClassStrategy_tsickle_Closure_declarations() {
    /** @type {?} */
    StyleClassStrategy.prototype.applyStyleClass;
}
export class HintLengthPaddingStrategy {
    /**
     * @param {?=} hintLength1
     * @param {?=} hintLength2
     * @param {?=} hintLength3
     */
    constructor(hintLength1 = 120, hintLength2 = 160, hintLength3 = 200) {
        this.hintLength1 = hintLength1;
        this.hintLength2 = hintLength2;
        this.hintLength3 = hintLength3;
    }
    /**
     * @param {?} builder
     * @return {?}
     */
    applyStyleClass(builder) {
        builder.appendStyleClass("pad-bottom");
        /*
                if(builder.getHint() ){
                    if(builder.getHint().length > this.hintLength3){
                        builder.appendStyleClass("pad-bottom-lg");
                    } else if(builder.getHint().length > this.hintLength2){
                        builder.appendStyleClass("pad-bottom-md");
                    } else if(builder.getHint().length > this.hintLength1){
                        builder.appendStyleClass("pad-bottom");
                    }
                }
                */
    }
}
function HintLengthPaddingStrategy_tsickle_Closure_declarations() {
    /** @type {?} */
    HintLengthPaddingStrategy.prototype.hintLength1;
    /** @type {?} */
    HintLengthPaddingStrategy.prototype.hintLength2;
    /** @type {?} */
    HintLengthPaddingStrategy.prototype.hintLength3;
}
export class DynamicFormBuilder {
    constructor() {
        this.styleClassStrategy = new HintLengthPaddingStrategy();
        this.formFieldBuilders = [];
    }
    /**
     * @param {?} title
     * @return {?}
     */
    setTitle(title) {
        this.title = title;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setMessage(value) {
        this.message = value;
        return this;
    }
    /**
     * @param {?} styleClassStrategy
     * @return {?}
     */
    setStyleClassStrategy(styleClassStrategy) {
        this.styleClassStrategy = styleClassStrategy;
        return this;
    }
    /**
     * @param {?} callback
     * @param {?=} bindTo
     * @return {?}
     */
    onApply(callback, bindTo) {
        if (bindTo) {
            this.onApplyFn = callback.bind(bindTo);
        }
        else {
            this.onApplyFn = callback;
        }
        return this;
    }
    /**
     * @param {?} callback
     * @param {?=} bindTo
     * @return {?}
     */
    onCancel(callback, bindTo) {
        if (bindTo) {
            this.onCancelFn = callback.bind(bindTo);
        }
        else {
            this.onCancelFn = callback;
        }
        return this;
    }
    /**
     * Append a new row, optionally insert a new row at given index
     * param {number} index - optional index at which to create the row
     * returns {DynamicFormFieldGroupBuilder}
     * @param {?=} index
     * @return {?}
     */
    row(index) {
        let /** @type {?} */ rowBuilder = new DynamicFormFieldGroupBuilder(this, Layout.ROW);
        if (index) {
            this.formFieldBuilders.splice(index, 0, rowBuilder);
        }
        else {
            this.formFieldBuilders.push(rowBuilder);
        }
        return rowBuilder;
    }
    /**
     * Append a new column, optionally insert a new column at given index
     * param {number} index - optional index at which to insert a new column
     * returns {DynamicFormFieldGroupBuilder}
     * @param {?=} index
     * @return {?}
     */
    column(index) {
        let /** @type {?} */ columnBuilder = new DynamicFormFieldGroupBuilder(this, Layout.COLUMN);
        if (index) {
            this.formFieldBuilders.splice(index, 0, columnBuilder);
        }
        else {
            this.formFieldBuilders.push(columnBuilder);
        }
        return columnBuilder;
    }
    /**
     * @return {?}
     */
    done() {
        return this;
    }
    /**
     * @return {?}
     */
    resetForm() {
        if (this.form) {
            console.log('RESET FORM!!!! ', Object.keys(this.form.controls).length);
            Object.keys(this.form.controls).forEach(controlName => {
                if (this.form.contains(controlName)) {
                    this.form.removeControl(controlName);
                }
            });
        }
        this.formFieldBuilders = [];
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setForm(value) {
        this.form = value;
        return this;
    }
    /**
     * @return {?}
     */
    buildFieldConfiguration() {
        //set the fields
        return this.formFieldBuilders.map(builder => builder.build());
    }
    /**
     * @return {?}
     */
    build() {
        let /** @type {?} */ formConfig = new FormConfig();
        formConfig.title = this.title;
        formConfig.message = this.message;
        //set the form
        if (this.form == undefined) {
            this.form = new FormGroup({});
        }
        formConfig.form = this.form;
        //set the fields
        formConfig.fieldGroups = this.buildFieldConfiguration();
        //set the callbacks
        formConfig.onApplyFn = this.onApplyFn;
        formConfig.onCancelFn = this.onCancelFn;
        return formConfig;
    }
}
function DynamicFormBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    DynamicFormBuilder.prototype.title;
    /** @type {?} */
    DynamicFormBuilder.prototype.message;
    /** @type {?} */
    DynamicFormBuilder.prototype.formFieldBuilders;
    /** @type {?} */
    DynamicFormBuilder.prototype.form;
    /** @type {?} */
    DynamicFormBuilder.prototype.onApplyFn;
    /** @type {?} */
    DynamicFormBuilder.prototype.onCancelFn;
    /** @type {?} */
    DynamicFormBuilder.prototype.styleClassStrategy;
}
//# sourceMappingURL=dynamic-form-builder.js.map