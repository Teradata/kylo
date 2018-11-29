import {Component, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {FormGroup} from "@angular/forms";
import * as _ from "underscore"

import {Templates} from "../../feed-mgr/services/TemplateTypes";
import {FieldConfig} from "../../dynamic-form/model/FieldConfig";
import {FieldGroup} from "../../dynamic-form/model/FieldGroup";
import {InputType} from "../../dynamic-form/model/InputText";
import {DynamicFormBuilder} from "../../dynamic-form/services/dynamic-form-builder";
import {DynamicFormFieldGroupBuilder} from "../../dynamic-form/services/dynamic-form-field-group-builder";
import {DynamicFormService} from "../../dynamic-form/services/dynamic-form.service";
import {ConfigurationFieldBuilder} from "../../dynamic-form/services/field-config-builder";
import {Property} from "../model/property";
import {ProcessorRef} from "./processor-ref";

export class FieldConfigurationState {

    formFieldOrder = 0;

    /**
     * Map of the inputProcessorId to the controls related to that input
     */
    inputFieldsMap: { [key: string]: FieldConfig<any>[] } = {};

    /**
     * Map of all processors other inputs to array of configs
     */
    processorFieldMap: { [key: string]: FieldConfig<any>[] } = {};

    /**
     * Return all fieldconfig objects for a given input processor
     */
    getFieldsForInput(processorId: string, emptyArrayIfNull: boolean = true): FieldConfig<any>[] {
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
     */
    hasInputFields(processorId: string) {
        return this.inputFieldsMap[processorId] != undefined;
    }

    /**
     * are there fields defined for the processor
     * param {string} processorId
     * return {boolean}
     */
    hasNonInputFields(processorId: string) {
        return this.processorFieldMap[processorId] != undefined;
    }

    /**
     * Return all fieldconfig objects for a given input processor
     */
    getFieldsForNonInput(processorId: string, emptyArrayIfNull: boolean = true): FieldConfig<any>[] {
        if (!this.hasNonInputFields(processorId) && emptyArrayIfNull) {
            return [];
        }
        else {
            return this.processorFieldMap[processorId];
        }
    }

    getAndIncrementFieldOrder() {
        let order = this.formFieldOrder;
        this.formFieldOrder++;
        return order;
    }

    incrementAndGetFieldOrder() {
        this.formFieldOrder++;
        return this.formFieldOrder;
    }

    reset() {
        this.formFieldOrder = 0;
        this.inputFieldsMap = {};
        this.processorFieldMap = {}
    }
}

export class FieldConfigurationBuilder {

    formGroupBuilder: DynamicFormFieldGroupBuilder;

    constructor(private state: FieldConfigurationState, private addSectionHeader: boolean) {
    }

    public createFormFields(processor: ProcessorRef, properties?: Property[]): FieldConfig<any>[] {
        this.formGroupBuilder = new DynamicFormBuilder().column();
        return this.toFieldConfig(processor as any, (properties != null) ? properties : processor.processor.properties);
    }

    private buildField(property: Templates.Property): FieldConfig<any> {
        let field: FieldConfig<any>;
        //build the generic options to be used by all fields
        let label = property.propertyDescriptor.displayName || property.propertyDescriptor.name;
        let configBuilder = new ConfigurationFieldBuilder().setKey(property.nameKey).setOrder(this.state.getAndIncrementFieldOrder()).setPlaceholder(label).setRequired(property.required).setValue(property.value).setModel(property).setHint(property.propertyDescriptor.description);
        if(property.displayValue){
            configBuilder.setReadonlyValue(property.displayValue)
        }

        if (this.isInputText(property)) {
            //get the correct input type

            let type = property.renderType;
            if (property.sensitive) {
                type = "password";
            }
            let inputType: InputType = <InputType>InputType[type] || InputType.text;

            //create the field
            field = this.formGroupBuilder.text().update(configBuilder).setType(inputType).build();

        }
        else if (property.renderType == "select") {
            field = this.formGroupBuilder.select().update(configBuilder).setOptions(this.getSelectOptions(property)).build()

        }
        else if (property.renderType == "radio") {
            field = this.formGroupBuilder.radio().update(configBuilder).setOptions(this.getSelectOptions(property)).build();

        }
        else if (property.renderType == "checkbox-true-false" || property.renderType == "checkbox-custom") {
            let trueValue = property.renderOptions['trueValue'] || 'true';
            let falseValue = property.renderOptions['falseValue'] || 'false';
            field = this.formGroupBuilder.checkbox().update(configBuilder).setTrueValue(trueValue).setFalseValue(falseValue).build();
        }
        else if (property.renderType == "textarea") {
            field = this.formGroupBuilder.textarea().update(configBuilder).build();
        }
        return field;
    }

    /**
     * convert the property allowable values to label,value objects
     */
    private getSelectOptions(property: Templates.Property): { label: string, value: string }[] {
        //get the select options
        let options: ({ label: string, value: string })[] = [];

        if (property.propertyDescriptor.allowableValues && property.propertyDescriptor.allowableValues.length > 0) {
            options = (<any[]>property.propertyDescriptor.allowableValues).map(allowableValue => {
                return {label: allowableValue.displayName, value: allowableValue.value}
            });
        }
        else if (property.renderOptions && property.renderOptions.selectOptions && property.renderOptions.selectOptions.length > 0) {
            let selectOptions = JSON.parse(property.renderOptions.selectOptions);
            options = (<any[]>selectOptions).map(allowableValue => {
                return {label: allowableValue, value: allowableValue}
            });
        }
        //add in the not set value
        if (!property.required) {
            options.unshift({label: "Not Set", value: ""});
        }
        return options;
    }

    private toFieldConfig(processor: Templates.Processor, properties: Property[]): FieldConfig<any>[] {
        let elements: FieldConfig<any>[] = [];

        properties.filter((property: Templates.Property) => property.userEditable).map((property: Templates.Property) => {

            if (!property.uniqueId) {
                property.uniqueId = _.uniqueId("property-");
            }
            let fieldConfig: FieldConfig<any> = this.buildField(property);

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
                        let sectionHeader = this.formGroupBuilder.sectionHeader().setOrder(this.state.getAndIncrementFieldOrder()).setPlaceholder(processor.name).setShowDivider(true).build();
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

    private isInputText(property: Templates.Property) {
        return (property.renderType == null || property.renderType == "text" || property.renderType == "email" || property.renderType == "number" || property.renderType == "password");
    }
}

@Component({
    selector: "processor-form",
    template: `
      <dynamic-form [form]="form" [fieldGroups]="fieldGroups" [readonly]="readonly"></dynamic-form>
    `
})
export class ProcessorFormComponent implements OnChanges, OnInit {

    @Input()
    addSectionHeader: boolean | string;

    @Input()
    form: FormGroup;

    @Input()
    processor: ProcessorRef;

    @Input()
    properties: Property | Property[];

    @Input()
    readonly: boolean;

    fieldGroups = [new FieldGroup()];

    private fieldConfigurationState: FieldConfigurationState = new FieldConfigurationState();

    constructor(private dynamicFormService: DynamicFormService) {
    }

    ngOnInit(): void {

    }

    ngOnChanges(changes: SimpleChanges): void {

        if ((changes.processor || changes.properties || changes.readonly)) {
            if (this.processor != null) {
                this.fieldConfigurationState.reset();

                new FieldConfigurationBuilder(this.fieldConfigurationState, (this.addSectionHeader !== "false" && this.addSectionHeader !== false))
                    .createFormFields(this.processor, (typeof this.properties === "undefined" || Array.isArray(this.properties)) ? this.properties as any : [this.properties])
                    .sort((n1, n2) => {
                        return n1.order - n2.order;
                    });

                //populate the form with the correct input processors
                let inputProcessorFields = this.fieldConfigurationState.getFieldsForInput(this.processor.id);
                if (inputProcessorFields.length == 0) {
                    inputProcessorFields = this.fieldConfigurationState.getFieldsForNonInput(this.processor.id);
                }
                this.fieldGroups[0].fields = inputProcessorFields;

                this.dynamicFormService.addToFormGroup(inputProcessorFields, this.form);
            } else {
                this.fieldGroups = [new FieldGroup()];
            }
        }
    }
}
