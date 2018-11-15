/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import { FormGroup } from "@angular/forms";
import * as _ from "underscore";
import { Templates } from "../../feed-mgr/services/TemplateTypes";
import { FieldConfig } from "../../dynamic-form/model/FieldConfig";
import { FieldGroup } from "../../dynamic-form/model/FieldGroup";
import { InputType } from "../../dynamic-form/model/InputText";
import { DynamicFormBuilder } from "../../dynamic-form/services/dynamic-form-builder";
import { DynamicFormFieldGroupBuilder } from "../../dynamic-form/services/dynamic-form-field-group-builder";
import { DynamicFormService } from "../../dynamic-form/services/dynamic-form.service";
import { ConfigurationFieldBuilder } from "../../dynamic-form/services/field-config-builder";
import { Property } from "../model/property";
import { ProcessorRef } from "./processor-ref";
export class FieldConfigurationState {
    constructor() {
        this.formFieldOrder = 0;
        /**
         * Map of the inputProcessorId to the controls related to that input
         */
        this.inputFieldsMap = {};
        /**
         * Map of all processors other inputs to array of configs
         */
        this.processorFieldMap = {};
    }
    /**
     * Return all fieldconfig objects for a given input processor
     * @param {?} processorId
     * @param {?=} emptyArrayIfNull
     * @return {?}
     */
    getFieldsForInput(processorId, emptyArrayIfNull = true) {
        if (!this.hasInputFields(processorId) && emptyArrayIfNull) {
            return [];
        }
        else {
            return this.inputFieldsMap[processorId];
        }
    }
    /**
     * are there input fields defined for the processor
     * param {string} processorId
     * return {boolean}
     * @param {?} processorId
     * @return {?}
     */
    hasInputFields(processorId) {
        return this.inputFieldsMap[processorId] != undefined;
    }
    /**
     * are there fields defined for the processor
     * param {string} processorId
     * return {boolean}
     * @param {?} processorId
     * @return {?}
     */
    hasNonInputFields(processorId) {
        return this.processorFieldMap[processorId] != undefined;
    }
    /**
     * Return all fieldconfig objects for a given input processor
     * @param {?} processorId
     * @param {?=} emptyArrayIfNull
     * @return {?}
     */
    getFieldsForNonInput(processorId, emptyArrayIfNull = true) {
        if (!this.hasNonInputFields(processorId) && emptyArrayIfNull) {
            return [];
        }
        else {
            return this.processorFieldMap[processorId];
        }
    }
    /**
     * @return {?}
     */
    getAndIncrementFieldOrder() {
        let /** @type {?} */ order = this.formFieldOrder;
        this.formFieldOrder++;
        return order;
    }
    /**
     * @return {?}
     */
    incrementAndGetFieldOrder() {
        this.formFieldOrder++;
        return this.formFieldOrder;
    }
    /**
     * @return {?}
     */
    reset() {
        this.formFieldOrder = 0;
        this.inputFieldsMap = {};
        this.processorFieldMap = {};
    }
}
function FieldConfigurationState_tsickle_Closure_declarations() {
    /** @type {?} */
    FieldConfigurationState.prototype.formFieldOrder;
    /**
     * Map of the inputProcessorId to the controls related to that input
     * @type {?}
     */
    FieldConfigurationState.prototype.inputFieldsMap;
    /**
     * Map of all processors other inputs to array of configs
     * @type {?}
     */
    FieldConfigurationState.prototype.processorFieldMap;
}
export class FieldConfigurationBuilder {
    /**
     * @param {?} state
     * @param {?} addSectionHeader
     */
    constructor(state, addSectionHeader) {
        this.state = state;
        this.addSectionHeader = addSectionHeader;
    }
    /**
     * @param {?} processor
     * @param {?=} properties
     * @return {?}
     */
    createFormFields(processor, properties) {
        this.formGroupBuilder = new DynamicFormBuilder().column();
        return this.toFieldConfig(/** @type {?} */ (processor), (properties != null) ? properties : processor.processor.properties);
    }
    /**
     * @param {?} property
     * @return {?}
     */
    buildField(property) {
        let /** @type {?} */ field;
        //build the generic options to be used by all fields
        let /** @type {?} */ label = property.propertyDescriptor.displayName || property.propertyDescriptor.name;
        let /** @type {?} */ configBuilder = new ConfigurationFieldBuilder().setKey(property.nameKey).setOrder(this.state.getAndIncrementFieldOrder()).setPlaceholder(label).setRequired(property.required).setValue(property.value).setModel(property).setHint(property.propertyDescriptor.description);
        if (this.isInputText(property)) {
            //get the correct input type
            let /** @type {?} */ type = property.renderType;
            if (property.sensitive) {
                type = "password";
            }
            let /** @type {?} */ inputType = /** @type {?} */ (InputType[type]) || InputType.text;
            //create the field
            field = this.formGroupBuilder.text().update(configBuilder).setType(inputType).build();
        }
        else if (property.renderType == "select") {
            field = this.formGroupBuilder.select().update(configBuilder).setOptions(this.getSelectOptions(property)).build();
        }
        else if (property.renderType == "radio") {
            field = this.formGroupBuilder.radio().update(configBuilder).setOptions(this.getSelectOptions(property)).build();
        }
        else if (property.renderType == "checkbox-true-false" || property.renderType == "checkbox-custom") {
            let /** @type {?} */ trueValue = property.renderOptions['trueValue'] || 'true';
            let /** @type {?} */ falseValue = property.renderOptions['falseValue'] || 'false';
            field = this.formGroupBuilder.checkbox().update(configBuilder).setTrueValue(trueValue).setFalseValue(falseValue).build();
        }
        else if (property.renderType == "textarea") {
            field = this.formGroupBuilder.textarea().update(configBuilder).build();
        }
        return field;
    }
    /**
     * convert the property allowable values to label,value objects
     * @param {?} property
     * @return {?}
     */
    getSelectOptions(property) {
        //get the select options
        let /** @type {?} */ options = [];
        if (property.propertyDescriptor.allowableValues && property.propertyDescriptor.allowableValues.length > 0) {
            options = (/** @type {?} */ (property.propertyDescriptor.allowableValues)).map(allowableValue => {
                return { label: allowableValue.displayName, value: allowableValue.value };
            });
        }
        else if (property.renderOptions && property.renderOptions["selectOptions"] && property.renderOptions["selectOptions"].length > 0) {
            let /** @type {?} */ selectOptions = JSON.parse(property.renderOptions["selectOptions"]);
            options = (/** @type {?} */ (selectOptions)).map(allowableValue => {
                return { label: allowableValue, value: allowableValue };
            });
        }
        //add in the not set value
        if (!property.required) {
            options.unshift({ label: "Not Set", value: "" });
        }
        return options;
    }
    /**
     * @param {?} processor
     * @param {?} properties
     * @return {?}
     */
    toFieldConfig(processor, properties) {
        let /** @type {?} */ elements = [];
        properties.filter((property) => property.userEditable).map((property) => {
            if (!property.uniqueId) {
                property.uniqueId = _.uniqueId("property-");
            }
            let /** @type {?} */ fieldConfig = this.buildField(property);
            if (property.inputProperty) {
                if (this.state.inputFieldsMap[processor.id] == undefined) {
                    this.state.inputFieldsMap[processor.id] = [];
                }
                this.state.inputFieldsMap[processor.id].push(fieldConfig);
            }
            else {
                if (this.state.processorFieldMap[processor.id] == undefined) {
                    this.state.processorFieldMap[processor.id] = [];
                    if (this.addSectionHeader !== false) {
                        //add a new SectionHeader
                        let /** @type {?} */ sectionHeader = this.formGroupBuilder.sectionHeader().setOrder(this.state.getAndIncrementFieldOrder()).setPlaceholder(processor.name).setShowDivider(true).build();
                        elements.push(sectionHeader);
                        this.state.processorFieldMap[processor.id].push(sectionHeader);
                    }
                }
                this.state.processorFieldMap[processor.id].push(fieldConfig);
            }
            elements.push(fieldConfig);
        });
        return elements;
    }
    /**
     * @param {?} property
     * @return {?}
     */
    isInputText(property) {
        return (property.renderType == null || property.renderType == "text" || property.renderType == "email" || property.renderType == "number" || property.renderType == "password");
    }
}
function FieldConfigurationBuilder_tsickle_Closure_declarations() {
    /** @type {?} */
    FieldConfigurationBuilder.prototype.formGroupBuilder;
    /** @type {?} */
    FieldConfigurationBuilder.prototype.state;
    /** @type {?} */
    FieldConfigurationBuilder.prototype.addSectionHeader;
}
export class ProcessorFormComponent {
    /**
     * @param {?} dynamicFormService
     */
    constructor(dynamicFormService) {
        this.dynamicFormService = dynamicFormService;
        this.fieldGroups = [new FieldGroup()];
        this.fieldConfigurationState = new FieldConfigurationState();
    }
    /**
     * @param {?} changes
     * @return {?}
     */
    ngOnChanges(changes) {
        if ((changes["processor"] || changes["properties"])) {
            if (this.processor != null) {
                this.fieldConfigurationState.reset();
                new FieldConfigurationBuilder(this.fieldConfigurationState, (this.addSectionHeader !== "false" && this.addSectionHeader !== false))
                    .createFormFields(this.processor, (typeof this.properties === "undefined" || Array.isArray(this.properties)) ? /** @type {?} */ (this.properties) : [this.properties])
                    .sort((n1, n2) => {
                    return n1.order - n2.order;
                });
                //populate the form with the correct input processors
                let /** @type {?} */ inputProcessorFields = this.fieldConfigurationState.getFieldsForInput(this.processor.id);
                if (inputProcessorFields.length == 0) {
                    inputProcessorFields = this.fieldConfigurationState.getFieldsForNonInput(this.processor.id);
                }
                this.fieldGroups[0].fields = inputProcessorFields;
                this.dynamicFormService.addToFormGroup(inputProcessorFields, this.form);
            }
            else {
                this.fieldGroups = [new FieldGroup()];
            }
        }
    }
}
ProcessorFormComponent.decorators = [
    { type: Component, args: [{
                selector: "processor-form",
                template: `
      <dynamic-form [form]="form" [fieldGroups]="fieldGroups" [readonly]="readonly"></dynamic-form>
    `
            },] },
];
/** @nocollapse */
ProcessorFormComponent.ctorParameters = () => [
    { type: DynamicFormService, },
];
ProcessorFormComponent.propDecorators = {
    "addSectionHeader": [{ type: Input },],
    "form": [{ type: Input },],
    "processor": [{ type: Input },],
    "properties": [{ type: Input },],
    "readonly": [{ type: Input },],
};
function ProcessorFormComponent_tsickle_Closure_declarations() {
    /** @type {!Array<{type: !Function, args: (undefined|!Array<?>)}>} */
    ProcessorFormComponent.decorators;
    /**
     * @nocollapse
     * @type {function(): !Array<(null|{type: ?, decorators: (undefined|!Array<{type: !Function, args: (undefined|!Array<?>)}>)})>}
     */
    ProcessorFormComponent.ctorParameters;
    /** @type {!Object<string,!Array<{type: !Function, args: (undefined|!Array<?>)}>>} */
    ProcessorFormComponent.propDecorators;
    /** @type {?} */
    ProcessorFormComponent.prototype.addSectionHeader;
    /** @type {?} */
    ProcessorFormComponent.prototype.form;
    /** @type {?} */
    ProcessorFormComponent.prototype.processor;
    /** @type {?} */
    ProcessorFormComponent.prototype.properties;
    /** @type {?} */
    ProcessorFormComponent.prototype.readonly;
    /** @type {?} */
    ProcessorFormComponent.prototype.fieldGroups;
    /** @type {?} */
    ProcessorFormComponent.prototype.fieldConfigurationState;
    /** @type {?} */
    ProcessorFormComponent.prototype.dynamicFormService;
}
//# sourceMappingURL=processor-form.component.js.map