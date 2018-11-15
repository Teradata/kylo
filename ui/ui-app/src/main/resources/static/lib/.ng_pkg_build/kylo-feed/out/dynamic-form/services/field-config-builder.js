/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
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
import { Icon } from "../model/Icon";
import { ObjectUtils } from "../../common/utils/object-utils";
import { ValidatorFn } from "@angular/forms";
/**
 * @abstract
 */
export class FieldConfigBuilder {
    /**
     * @param {?=} formGroupBuilder
     */
    constructor(formGroupBuilder) {
        this.formGroupBuilder = formGroupBuilder;
    }
    /**
     * @return {?}
     */
    done() {
        return this.formGroupBuilder;
    }
    /**
     * @param {?} callback
     * @param {?=} bind
     * @return {?}
     */
    ngIf(callback, bind) {
        if (bind) {
            this.ngIfCallback = callback.bind(bind);
        }
        else {
            this.ngIfCallback = callback;
        }
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setStyleClass(value) {
        this.styleClass = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    containsStyle(value) {
        return (this.styleClass != undefined && this.styleClass.split(" ").find((v => v == value.trim())) == undefined);
    }
    /**
     * @param {?} value
     * @return {?}
     */
    appendStyleClass(value) {
        if (value) {
            if (this.styleClass == undefined || !this.containsStyle(value)) {
                if (this.styleClass == undefined) {
                    this.styleClass = value.trim();
                }
                else {
                    this.styleClass += " " + value.trim();
                }
            }
        }
        return this;
    }
    /**
     * @param {?} formGroupBuilder
     * @return {?}
     */
    setFormGroupBuilder(formGroupBuilder) {
        this.formGroupBuilder = this.formGroupBuilder;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setKey(value) {
        this.key = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setValue(value) {
        this.value = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setPlaceholderLocaleKey(value) {
        this.placeHolderLocaleKey = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setRequired(value) {
        this.required = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setOrder(value) {
        this.order = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setErrorMessageLookup(value) {
        this.getErrorMessage = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setPlaceholder(value) {
        this.placeholder = value;
        return this;
    }
    /**
     * @return {?}
     */
    getPlaceHolder() {
        return this.placeholder;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setHint(value) {
        this.hint = value;
        return this;
    }
    /**
     * @return {?}
     */
    getHint() {
        return this.hint;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setModelValueProperty(value) {
        this.modelValueProperty = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setPattern(value) {
        this.pattern = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setDisabled(value) {
        this.disabled = value;
        return this;
    }
    /**
     * @param {?} value
     * @param {?=} bind
     * @return {?}
     */
    onChange(value, bind) {
        if (bind) {
            this.onModelChange = value.bind(bind);
        }
        else {
            this.onModelChange = value;
        }
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setValidators(value) {
        this.validators = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setReadonlyValue(value) {
        this.readonlyValue = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    addValidator(value) {
        if (this.validators == undefined) {
            this.validators = [];
        }
        this.validators.push(value);
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setModel(value) {
        this.model = value;
        return this;
    }
    /**
     * @template B
     * @param {?} builder
     * @return {?}
     */
    update(builder) {
        this.key = builder.key;
        this.required = builder.required;
        this.placeholder = builder.placeholder;
        this.value = builder.value;
        this.hint = builder.hint;
        this.validators = builder.validators;
        this.model = builder.model;
        this.modelValueProperty = builder.modelValueProperty;
        this.onModelChange = builder.onModelChange;
        this.pattern = builder.pattern;
        this.order = builder.order;
        this.readonlyValue = builder.readonlyValue;
        this.disabled = builder.disabled;
        this.placeHolderLocaleKey = builder.placeHolderLocaleKey;
        this.styleClass = builder.styleClass;
        this.ngIfCallback = builder.ngIfCallback;
        this.getErrorMessage = builder.getErrorMessage;
        return this;
    }
    /**
     * @return {?}
     */
    buildOptions() {
        let /** @type {?} */ option = {
            key: this.key,
            required: this.required,
            placeholder: this.placeholder,
            value: this.value,
            hint: this.hint,
            validators: this.validators,
            model: this.model,
            modelValueProperty: this.modelValueProperty,
            onModelChange: this.onModelChange,
            pattern: this.pattern,
            order: this.order,
            readonlyValue: this.readonlyValue,
            disabled: this.disabled,
            placeholderLocaleKey: this.placeHolderLocaleKey,
            styleClass: this.styleClass,
            ngIf: this.ngIfCallback,
            getErrorMessage: this.getErrorMessage
        };
        return option;
    }
    /**
     * @return {?}
     */
    build() {
        if (this.formGroupBuilder.dynamicFormBuilder.styleClassStrategy) {
            this.formGroupBuilder.dynamicFormBuilder.styleClassStrategy.applyStyleClass(this);
        }
        let /** @type {?} */ options = this.buildOptions();
        return ObjectUtils.newType(options, this.getObjectType());
    }
}
function FieldConfigBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    FieldConfigBuilder.prototype.value;
    /** @type {?} */
    FieldConfigBuilder.prototype.key;
    /** @type {?} */
    FieldConfigBuilder.prototype.required;
    /** @type {?} */
    FieldConfigBuilder.prototype.order;
    /** @type {?} */
    FieldConfigBuilder.prototype.placeholder;
    /** @type {?} */
    FieldConfigBuilder.prototype.model;
    /** @type {?} */
    FieldConfigBuilder.prototype.hint;
    /** @type {?} */
    FieldConfigBuilder.prototype.readonlyValue;
    /** @type {?} */
    FieldConfigBuilder.prototype.modelValueProperty;
    /** @type {?} */
    FieldConfigBuilder.prototype.pattern;
    /** @type {?} */
    FieldConfigBuilder.prototype.onModelChange;
    /** @type {?} */
    FieldConfigBuilder.prototype.validators;
    /** @type {?} */
    FieldConfigBuilder.prototype.disabled;
    /** @type {?} */
    FieldConfigBuilder.prototype.placeHolderLocaleKey;
    /** @type {?} */
    FieldConfigBuilder.prototype.styleClass;
    /** @type {?} */
    FieldConfigBuilder.prototype.formGroupBuilder;
    /** @type {?} */
    FieldConfigBuilder.prototype.ngIfCallback;
    /** @type {?} */
    FieldConfigBuilder.prototype.getErrorMessage;
    /**
     * @abstract
     * @return {?}
     */
    FieldConfigBuilder.prototype.getObjectType = function () { };
}
export class ConfigurationFieldBuilder extends FieldConfigBuilder {
    /**
     * @param {?=} formGroupBuilder
     */
    constructor(formGroupBuilder) {
        super(formGroupBuilder);
    }
    /**
     * @return {?}
     */
    getObjectType() {
        return "ConfigurationFieldBuilder";
    }
    /**
     * @return {?}
     */
    build() {
        //override.
        console.log("ConfigurationFieldBuilder cant be built ");
    }
}
export class SelectFieldBuilder extends FieldConfigBuilder {
    /**
     * @param {?=} formGroupBuilder
     */
    constructor(formGroupBuilder) {
        super(formGroupBuilder);
        this.options = [];
    }
    /**
     * @return {?}
     */
    getObjectType() {
        return Select;
    }
    /**
     * @param {?} options
     * @return {?}
     */
    setOptions(options) {
        this.options = options;
        return this;
    }
    /**
     * @param {?} options
     * @return {?}
     */
    setOptionsArray(options) {
        this.options = options.map(item => {
            return { label: item, value: item };
        });
        return this;
    }
    /**
     * @param {?} label
     * @param {?} value
     * @return {?}
     */
    addOption(label, value) {
        this.options.push({ label: label, value: value });
        return this;
    }
    /**
     * @return {?}
     */
    buildOptions() {
        let /** @type {?} */ options = super.buildOptions();
        options.options = this.options;
        return options;
    }
}
function SelectFieldBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    SelectFieldBuilder.prototype.options;
}
export class RadioButtonFieldBuilder extends FieldConfigBuilder {
    /**
     * @param {?=} formGroupBuilder
     */
    constructor(formGroupBuilder) {
        super(formGroupBuilder);
        this.options = [];
    }
    /**
     * @return {?}
     */
    getObjectType() {
        return RadioButton;
    }
    /**
     * @param {?} options
     * @return {?}
     */
    setOptions(options) {
        this.options = options;
        return this;
    }
    /**
     * @param {?} options
     * @return {?}
     */
    setOptionsArray(options) {
        this.options = options.map(item => {
            return { label: item, value: item };
        });
        return this;
    }
    /**
     * @param {?} label
     * @param {?} value
     * @return {?}
     */
    addOption(label, value) {
        this.options.push({ label: label, value: value });
        return this;
    }
    /**
     * @return {?}
     */
    buildOptions() {
        let /** @type {?} */ options = super.buildOptions();
        options.options = this.options;
        return options;
    }
}
function RadioButtonFieldBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    RadioButtonFieldBuilder.prototype.options;
}
export class ChipsFieldBuilder extends FieldConfigBuilder {
    /**
     * @param {?=} formGroupBuilder
     */
    constructor(formGroupBuilder) {
        super(formGroupBuilder);
        this.stacked = false;
    }
    /**
     * @return {?}
     */
    getObjectType() {
        return Chip;
    }
    /**
     * @param {?} values
     * @return {?}
     */
    setItems(values) {
        this.items = values;
        return this;
    }
    /**
     * @param {?} stacked
     * @return {?}
     */
    setStacked(stacked) {
        this.stacked = stacked;
        return this;
    }
    /**
     * @return {?}
     */
    buildOptions() {
        let /** @type {?} */ options = super.buildOptions();
        options.items = this.items;
        options.values = this.value;
        options.stacked = this.stacked;
        return options;
    }
}
function ChipsFieldBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    ChipsFieldBuilder.prototype.items;
    /** @type {?} */
    ChipsFieldBuilder.prototype.stacked;
}
export class CheckboxFieldBuilder extends FieldConfigBuilder {
    /**
     * @param {?=} formGroupBuilder
     */
    constructor(formGroupBuilder) {
        super(formGroupBuilder);
    }
    /**
     * @return {?}
     */
    getObjectType() {
        return Checkbox;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setTrueValue(value) {
        this.trueValue = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setFalseValue(value) {
        this.falseValue = value;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setChecked(value) {
        this.checked = value;
        return this;
    }
    /**
     * @return {?}
     */
    buildOptions() {
        let /** @type {?} */ options = super.buildOptions();
        options.trueValue = this.trueValue;
        options.falseValue = this.falseValue;
        return options;
    }
}
function CheckboxFieldBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    CheckboxFieldBuilder.prototype.trueValue;
    /** @type {?} */
    CheckboxFieldBuilder.prototype.falseValue;
    /** @type {?} */
    CheckboxFieldBuilder.prototype.checked;
}
export class InputTextFieldBuilder extends FieldConfigBuilder {
    /**
     * @param {?=} formGroupBuilder
     */
    constructor(formGroupBuilder) {
        super(formGroupBuilder);
    }
    /**
     * @return {?}
     */
    getObjectType() {
        return InputText;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setReadonly(value) {
        this.readonly = value;
        return this;
    }
    /**
     * @param {?} type
     * @return {?}
     */
    setType(type) {
        this.type = type;
        return this;
    }
    /**
     * @return {?}
     */
    buildOptions() {
        let /** @type {?} */ options = super.buildOptions();
        options.type = this.type;
        options.readonly = this.readonly;
        return options;
    }
}
function InputTextFieldBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    InputTextFieldBuilder.prototype.type;
    /** @type {?} */
    InputTextFieldBuilder.prototype.readonly;
}
export class SectionHeaderBuilder extends FieldConfigBuilder {
    /**
     * @param {?=} formGroupBuilder
     */
    constructor(formGroupBuilder) {
        super(formGroupBuilder);
        this.showDivider = true;
    }
    /**
     * @param {?} showDivider
     * @return {?}
     */
    setShowDivider(showDivider) {
        this.showDivider = showDivider;
        return this;
    }
    /**
     * @return {?}
     */
    getObjectType() {
        return SectionHeader;
    }
    /**
     * @return {?}
     */
    buildOptions() {
        let /** @type {?} */ options = super.buildOptions();
        options.showDivider = this.showDivider;
        return options;
    }
}
function SectionHeaderBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    SectionHeaderBuilder.prototype.showDivider;
}
export class StaticTextBuilder extends FieldConfigBuilder {
    /**
     * @param {?=} formGroupBuilder
     */
    constructor(formGroupBuilder) {
        super(formGroupBuilder);
        this.showDivider = true;
    }
    /**
     * @param {?} showDivider
     * @return {?}
     */
    setShowDivider(showDivider) {
        this.showDivider = showDivider;
        return this;
    }
    /**
     * @param {?} text
     * @return {?}
     */
    setText(text) {
        this.staticText = text;
        return this;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setTextStyleClass(value) {
        this.textStyleClass = value;
        return this;
    }
    /**
     * @return {?}
     */
    getObjectType() {
        return StaticText;
    }
    /**
     * @return {?}
     */
    buildOptions() {
        let /** @type {?} */ options = super.buildOptions();
        options.showDivider = this.showDivider;
        options.staticText = this.staticText;
        options.textStyleClass = this.textStyleClass;
        return options;
    }
}
function StaticTextBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    StaticTextBuilder.prototype.showDivider;
    /** @type {?} */
    StaticTextBuilder.prototype.staticText;
    /** @type {?} */
    StaticTextBuilder.prototype.textStyleClass;
}
export class TextareaFieldBuilder extends FieldConfigBuilder {
    /**
     * @param {?=} formGroupBuilder
     */
    constructor(formGroupBuilder) {
        super(formGroupBuilder);
    }
    /**
     * @return {?}
     */
    getObjectType() {
        return Textarea;
    }
    /**
     * @param {?} value
     * @return {?}
     */
    setReadonly(value) {
        this.readonly = value;
        return this;
    }
    /**
     * @return {?}
     */
    buildOptions() {
        let /** @type {?} */ options = super.buildOptions();
        options.readonly = this.readonly;
        return options;
    }
}
function TextareaFieldBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    TextareaFieldBuilder.prototype.readonly;
}
export class IconFieldBuilder extends FieldConfigBuilder {
    /**
     * @param {?=} formGroupBuilder
     */
    constructor(formGroupBuilder) {
        super(formGroupBuilder);
    }
    /**
     * @return {?}
     */
    getObjectType() {
        return Icon;
    }
    /**
     * @param {?} name
     * @return {?}
     */
    setName(name) {
        this.name = name;
        return this;
    }
    /**
     * @param {?} size
     * @return {?}
     */
    setSize(size) {
        this.size = size;
        return this;
    }
    /**
     * @return {?}
     */
    buildOptions() {
        let /** @type {?} */ options = super.buildOptions();
        options.name = this.name;
        options.size = this.size;
        return options;
    }
}
function IconFieldBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    IconFieldBuilder.prototype.name;
    /** @type {?} */
    IconFieldBuilder.prototype.size;
}
//# sourceMappingURL=field-config-builder.js.map