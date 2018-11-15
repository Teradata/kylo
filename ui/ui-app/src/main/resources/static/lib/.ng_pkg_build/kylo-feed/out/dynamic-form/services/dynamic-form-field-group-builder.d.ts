import { FormFieldBuilder } from "./form-field-builder";
import { CheckboxFieldBuilder, ChipsFieldBuilder, FieldConfigBuilder, IconFieldBuilder, InputTextFieldBuilder, RadioButtonFieldBuilder, SectionHeaderBuilder, SelectFieldBuilder, StaticTextBuilder, TextareaFieldBuilder } from "./field-config-builder";
import { FieldGroup, Layout } from "../model/FieldGroup";
import { DynamicFormBuilder } from "./dynamic-form-builder";
export declare class DynamicFormFieldGroupBuilder {
    dynamicFormBuilder: DynamicFormBuilder;
    formFieldBuilder: FormFieldBuilder;
    layout: Layout;
    layoutAlign: string;
    constructor(dynamicFormBuilder: DynamicFormBuilder, layout?: Layout);
    setLayoutAlign(layoutAlign: string): DynamicFormFieldGroupBuilder;
    field(fieldBuilder: FieldConfigBuilder<any>): this;
    select(): SelectFieldBuilder;
    text(): InputTextFieldBuilder;
    textarea(): TextareaFieldBuilder;
    radio(): RadioButtonFieldBuilder;
    checkbox(): CheckboxFieldBuilder;
    sectionHeader(): SectionHeaderBuilder;
    chips(): ChipsFieldBuilder;
    staticText(): StaticTextBuilder;
    icon(): IconFieldBuilder;
    build(): FieldGroup;
    rowComplete(): DynamicFormBuilder;
    columnComplete(): DynamicFormBuilder;
    completeGroup(): DynamicFormBuilder;
    castAs<T extends DynamicFormBuilder>(type: {
        new (): T;
    }): T;
}
