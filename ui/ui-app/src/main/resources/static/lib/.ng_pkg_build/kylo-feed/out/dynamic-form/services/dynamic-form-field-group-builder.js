/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FormFieldBuilder } from "./form-field-builder";
import { CheckboxFieldBuilder, ChipsFieldBuilder, FieldConfigBuilder, IconFieldBuilder, InputTextFieldBuilder, RadioButtonFieldBuilder, SectionHeaderBuilder, SelectFieldBuilder, StaticTextBuilder, TextareaFieldBuilder } from "./field-config-builder";
import { FieldGroup, Layout } from "../model/FieldGroup";
import { DynamicFormBuilder } from "./dynamic-form-builder";
export class DynamicFormFieldGroupBuilder {
    /**
     * @param {?} dynamicFormBuilder
     * @param {?=} layout
     */
    constructor(dynamicFormBuilder, layout = Layout.COLUMN) {
        this.dynamicFormBuilder = dynamicFormBuilder;
        this.formFieldBuilder = new FormFieldBuilder();
        this.layout = layout;
    }
    /**
     * @param {?} layoutAlign
     * @return {?}
     */
    setLayoutAlign(layoutAlign) {
        this.layoutAlign = layoutAlign;
        return this;
    }
    /**
     * @param {?} fieldBuilder
     * @return {?}
     */
    field(fieldBuilder) {
        fieldBuilder.setFormGroupBuilder(this);
        this.formFieldBuilder.field(fieldBuilder);
        return this;
    }
    /**
     * @return {?}
     */
    select() {
        let /** @type {?} */ builder = new SelectFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    /**
     * @return {?}
     */
    text() {
        let /** @type {?} */ builder = new InputTextFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    /**
     * @return {?}
     */
    textarea() {
        let /** @type {?} */ builder = new TextareaFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    /**
     * @return {?}
     */
    radio() {
        let /** @type {?} */ builder = new RadioButtonFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    /**
     * @return {?}
     */
    checkbox() {
        let /** @type {?} */ builder = new CheckboxFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    /**
     * @return {?}
     */
    sectionHeader() {
        let /** @type {?} */ builder = new SectionHeaderBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    /**
     * @return {?}
     */
    chips() {
        let /** @type {?} */ builder = new ChipsFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    /**
     * @return {?}
     */
    staticText() {
        let /** @type {?} */ builder = new StaticTextBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    /**
     * @return {?}
     */
    icon() {
        let /** @type {?} */ builder = new IconFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    /**
     * @return {?}
     */
    build() {
        let /** @type {?} */ group = new FieldGroup(this.layout);
        group.setLayoutAlign(this.layoutAlign);
        group.fields = this.formFieldBuilder.build();
        return group;
    }
    /**
     * @return {?}
     */
    rowComplete() {
        return this.dynamicFormBuilder;
    }
    /**
     * @return {?}
     */
    columnComplete() {
        return this.dynamicFormBuilder;
    }
    /**
     * @return {?}
     */
    completeGroup() {
        return this.dynamicFormBuilder;
    }
    /**
     * @template T
     * @param {?} type
     * @return {?}
     */
    castAs(type) {
        return /** @type {?} */ (this.dynamicFormBuilder);
    }
}
function DynamicFormFieldGroupBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    DynamicFormFieldGroupBuilder.prototype.formFieldBuilder;
    /** @type {?} */
    DynamicFormFieldGroupBuilder.prototype.layout;
    /** @type {?} */
    DynamicFormFieldGroupBuilder.prototype.layoutAlign;
    /** @type {?} */
    DynamicFormFieldGroupBuilder.prototype.dynamicFormBuilder;
}
//# sourceMappingURL=dynamic-form-field-group-builder.js.map