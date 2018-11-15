import { GetErrorMessage, NgIfCallback, OnFieldChange } from "../model/FieldConfig";
import { Select } from "../model/Select";
import { Textarea } from "../model/Textarea";
import { RadioButton } from "../model/RadioButton";
import { InputText, InputType } from "../model/InputText";
import { Chip } from "../model/Chip";
import { SectionHeader } from "../model/SectionHeader";
import { Checkbox } from "../model/Checkbox";
import { DynamicFormFieldGroupBuilder } from "./dynamic-form-field-group-builder";
import { StaticText } from "../model/StaticText";
import { Icon } from '../model/Icon';
import { ValidatorFn } from '@angular/forms';
export declare abstract class FieldConfigBuilder<T> {
    protected value: any;
    private key;
    private required;
    private order;
    private placeholder;
    private model?;
    private hint?;
    private readonlyValue;
    private modelValueProperty;
    private pattern?;
    private onModelChange?;
    private validators?;
    private disabled?;
    private placeHolderLocaleKey;
    private styleClass;
    private formGroupBuilder?;
    private ngIfCallback?;
    private getErrorMessage?;
    protected constructor(formGroupBuilder?: DynamicFormFieldGroupBuilder);
    abstract getObjectType(): any;
    done(): DynamicFormFieldGroupBuilder;
    ngIf(callback: NgIfCallback, bind?: any): this;
    setStyleClass(value: string): this;
    containsStyle(value: string): boolean;
    appendStyleClass(value: string): this;
    setFormGroupBuilder(formGroupBuilder: DynamicFormFieldGroupBuilder): this;
    setKey(value: string): this;
    setValue(value: any): this;
    setPlaceholderLocaleKey(value: string): this;
    setRequired(value: boolean): this;
    setOrder(value: number): this;
    setErrorMessageLookup(value: GetErrorMessage): this;
    setPlaceholder(value: string): this;
    getPlaceHolder(): string;
    setHint(value: string): this;
    getHint(): string;
    setModelValueProperty(value: string): this;
    setPattern(value: string): this;
    setDisabled(value: boolean): this;
    onChange(value: OnFieldChange, bind?: any): this;
    setValidators(value: ValidatorFn[]): this;
    setReadonlyValue(value: string): this;
    addValidator(value: ValidatorFn): this;
    setModel(value: any): this;
    update<B extends FieldConfigBuilder<any>>(builder: B): this;
    protected buildOptions(): any;
    build(): T;
}
export declare class ConfigurationFieldBuilder extends FieldConfigBuilder<any> {
    constructor(formGroupBuilder?: DynamicFormFieldGroupBuilder);
    getObjectType(): any;
    build(): any;
}
export declare class SelectFieldBuilder extends FieldConfigBuilder<Select> {
    options: {
        label: string;
        value: string;
    }[];
    constructor(formGroupBuilder?: DynamicFormFieldGroupBuilder);
    getObjectType(): any;
    setOptions(options: {
        label: string;
        value: string;
    }[]): SelectFieldBuilder;
    setOptionsArray(options: any[]): this;
    addOption(label: string, value: string): SelectFieldBuilder;
    protected buildOptions(): any;
}
export declare class RadioButtonFieldBuilder extends FieldConfigBuilder<RadioButton> {
    options: {
        label: string;
        value: string;
    }[];
    constructor(formGroupBuilder?: DynamicFormFieldGroupBuilder);
    getObjectType(): any;
    setOptions(options: {
        label: string;
        value: string;
    }[]): RadioButtonFieldBuilder;
    setOptionsArray(options: any[]): this;
    addOption(label: string, value: string): RadioButtonFieldBuilder;
    protected buildOptions(): any;
}
export declare class ChipsFieldBuilder extends FieldConfigBuilder<Chip> {
    items: any[];
    stacked: boolean;
    constructor(formGroupBuilder?: DynamicFormFieldGroupBuilder);
    getObjectType(): any;
    setItems(values: any): ChipsFieldBuilder;
    setStacked(stacked: boolean): ChipsFieldBuilder;
    protected buildOptions(): any;
}
export declare class CheckboxFieldBuilder extends FieldConfigBuilder<Checkbox> {
    trueValue: string;
    falseValue: string;
    checked: boolean;
    constructor(formGroupBuilder?: DynamicFormFieldGroupBuilder);
    getObjectType(): any;
    setTrueValue(value: string): CheckboxFieldBuilder;
    setFalseValue(value: string): CheckboxFieldBuilder;
    setChecked(value: boolean): CheckboxFieldBuilder;
    protected buildOptions(): any;
}
export declare class InputTextFieldBuilder extends FieldConfigBuilder<InputText> {
    type: InputType;
    private readonly;
    constructor(formGroupBuilder?: DynamicFormFieldGroupBuilder);
    getObjectType(): any;
    setReadonly(value: boolean): this;
    setType(type: InputType): InputTextFieldBuilder;
    protected buildOptions(): any;
}
export declare class SectionHeaderBuilder extends FieldConfigBuilder<SectionHeader> {
    showDivider: boolean;
    constructor(formGroupBuilder?: DynamicFormFieldGroupBuilder);
    setShowDivider(showDivider: boolean): this;
    getObjectType(): any;
    protected buildOptions(): any;
}
export declare class StaticTextBuilder extends FieldConfigBuilder<StaticText> {
    showDivider: boolean;
    staticText: string;
    textStyleClass: string;
    constructor(formGroupBuilder?: DynamicFormFieldGroupBuilder);
    setShowDivider(showDivider: boolean): this;
    setText(text: string): this;
    setTextStyleClass(value: string): this;
    getObjectType(): any;
    protected buildOptions(): any;
}
export declare class TextareaFieldBuilder extends FieldConfigBuilder<Textarea> {
    private readonly;
    constructor(formGroupBuilder?: DynamicFormFieldGroupBuilder);
    getObjectType(): any;
    setReadonly(value: boolean): this;
    protected buildOptions(): any;
}
export declare class IconFieldBuilder extends FieldConfigBuilder<Icon> {
    private name;
    private size;
    constructor(formGroupBuilder?: DynamicFormFieldGroupBuilder);
    getObjectType(): any;
    setName(name: string): this;
    setSize(size: number): this;
    protected buildOptions(): any;
}
