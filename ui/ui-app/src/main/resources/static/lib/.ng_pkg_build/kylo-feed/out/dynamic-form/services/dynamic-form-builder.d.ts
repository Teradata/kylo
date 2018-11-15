import { FormGroup } from "@angular/forms";
import { FieldConfigBuilder } from "./field-config-builder";
import { DynamicFormFieldGroupBuilder } from "./dynamic-form-field-group-builder";
import { FieldGroup } from "../model/FieldGroup";
export declare class FormConfig {
    title: string;
    message: string;
    fieldGroups: FieldGroup[];
    onApplyFn: Function;
    onCancelFn: Function;
    form: FormGroup;
    constructor();
}
export interface StyleClassStrategy {
    applyStyleClass(builder: FieldConfigBuilder<any>): void;
}
export declare class HintLengthPaddingStrategy implements StyleClassStrategy {
    private hintLength1;
    private hintLength2;
    private hintLength3;
    constructor(hintLength1?: number, hintLength2?: number, hintLength3?: number);
    applyStyleClass(builder: FieldConfigBuilder<any>): void;
}
export declare class DynamicFormBuilder {
    private title;
    private message;
    private formFieldBuilders;
    private form;
    private onApplyFn;
    private onCancelFn;
    styleClassStrategy: StyleClassStrategy;
    constructor();
    setTitle(title: string): this;
    setMessage(value: string): this;
    setStyleClassStrategy(styleClassStrategy: StyleClassStrategy): this;
    onApply(callback: Function, bindTo?: any): this;
    onCancel(callback: Function, bindTo?: any): this;
    /**
     * Append a new row, optionally insert a new row at given index
     * param {number} index - optional index at which to create the row
     * returns {DynamicFormFieldGroupBuilder}
     */
    row(index?: number): DynamicFormFieldGroupBuilder;
    /**
     * Append a new column, optionally insert a new column at given index
     * param {number} index - optional index at which to insert a new column
     * returns {DynamicFormFieldGroupBuilder}
     */
    column(index?: number): DynamicFormFieldGroupBuilder;
    done(): this;
    resetForm(): void;
    setForm(value: FormGroup): this;
    buildFieldConfiguration(): FieldGroup[];
    build(): FormConfig;
}
