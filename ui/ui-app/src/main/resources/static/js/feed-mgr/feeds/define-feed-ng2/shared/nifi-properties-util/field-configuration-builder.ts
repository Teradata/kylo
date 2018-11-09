import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";

import {DynamicFormFieldGroupBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-field-group-builder";
import {InputType} from "../../../../../../lib/dynamic-form/model/InputText";
import {Templates} from "../../../../../../lib/feed-mgr/services/TemplateTypes";
import {FieldConfig} from "../../../../../../lib/dynamic-form/model/FieldConfig";
import {FieldConfigurationState} from "./field-configuration-state";
import {ConfigurationFieldBuilder} from "../../../../../../lib/dynamic-form/services/field-config-builder";

export class FieldConfigurationBuilder {
    formGroupBuilder:DynamicFormFieldGroupBuilder

    constructor(private state:FieldConfigurationState ) {

    }


    public  createFormFields(processors:Templates.Processor[]) :FieldConfig<any>[] {
        this.formGroupBuilder = new DynamicFormBuilder().column()
        let elements :FieldConfig<any>[] = []
        processors.forEach((processor :Templates.Processor) => {
            let processorConfig :FieldConfig<any>[] = this.toFieldConfig(processor);
            elements = elements.concat(processorConfig);
        });
        return elements;
    }

    private  buildField(property:Templates.Property):FieldConfig<any> {
        let field:FieldConfig<any>;
        //build the generic options to be used by all fields
        let label = property.propertyDescriptor.displayName || property.propertyDescriptor.name;
        let configBuilder = new ConfigurationFieldBuilder().setKey(property.nameKey).setOrder(this.state.getAndIncrementFieldOrder()).setPlaceholder(label).setRequired(property.required).setValue(property.value).setModel(property).setHint(property.propertyDescriptor.description);

        if(this.isInputText(property)){
            //get the correct input type

            let type= property.renderType;
            if(property.sensitive) {
                type = "password";
            }
            let inputType:InputType = <InputType>InputType[type] || InputType.text;

            //create the field
            field = this.formGroupBuilder.text().update(configBuilder).setType(inputType).build();

        }
        else if(property.renderType == "select"){
            field = this.formGroupBuilder.select().update(configBuilder).setOptions(this.getSelectOptions(property)).build()

        }
        else if(property.renderType == "radio") {
            field = this.formGroupBuilder.radio().update(configBuilder).setOptions(this.getSelectOptions(property)).build();

        }
        else if(property.renderType == "checkbox-true-false" || property.renderType == "checkbox-custom") {
            let trueValue = property.renderOptions['trueValue'] || 'true';
            let falseValue = property.renderOptions['falseValue'] || 'false';
            field = this.formGroupBuilder.checkbox().update(configBuilder).setTrueValue(trueValue).setFalseValue(falseValue).build();
        }
        else if(property.renderType == "textarea") {
            field = this.formGroupBuilder.textarea().update(configBuilder).build();
        }
        return field;
    }

    /**
     * convert the property allowable values to label,value objects
     * @param {Templates.Property} property
     * @return {{label: string; value: string}[]}
     */
    private getSelectOptions(property:Templates.Property):({label:string,value:string})[]{
        //get the select options
        let options:({label:string,value:string})[] = [];

        if(property.propertyDescriptor.allowableValues && property.propertyDescriptor.allowableValues.length >0) {
            options = (<any[]>property.propertyDescriptor.allowableValues).map(allowableValue => {
                return {label: allowableValue.displayName,value: allowableValue.value}
            });
        }
        else if(property.renderOptions && property.renderOptions.selectOptions && property.renderOptions.selectOptions.length >0) {
            let selectOptions = JSON.parse(property.renderOptions.selectOptions)
            options = (<any[]>selectOptions).map(allowableValue => {
                return {label: allowableValue,value: allowableValue}
            });
        }
        //add in the not set value
        if(!property.required){
            options.unshift({label:"Not Set",value:""});
        }
        return options;
    }



    private toFieldConfig(processor:Templates.Processor):FieldConfig<any>[]{
        let elements :FieldConfig<any>[] = []
        let processorId = processor.id;


        processor.properties.filter((property:Templates.Property) => property.userEditable).map((property:Templates.Property) => {

            let fieldConfig:FieldConfig<any> = this.buildField(property);

            if(property.inputProperty){
                if(this.state.inputFieldsMap[processor.id] == undefined){
                    this.state.inputFieldsMap[processor.id] = [];
                }
                this.state.inputFieldsMap[processor.id].push(fieldConfig);
            }
            else {
                if(this.state.processorFieldMap[processor.id] == undefined){
                    this.state.processorFieldMap[processor.id] = [];
                    //add a new SectionHeader
                    let sectionHeader =this.formGroupBuilder.sectionHeader().setOrder(this.state.getAndIncrementFieldOrder()).setPlaceholder(processor.name).setShowDivider(true).build();
                    elements.push(sectionHeader);
                    this.state.processorFieldMap[processor.id].push(sectionHeader);
                    //increment the order
                    fieldConfig.order = this.state.getAndIncrementFieldOrder();
                }
                this.state.processorFieldMap[processor.id].push(fieldConfig);
            }

            elements.push(fieldConfig);
        });
        return elements;
    }

    private isInputText(property:Templates.Property){
        return (property.renderType == null || property.renderType == "text" || property.renderType == "email" || property.renderType == "number" || property.renderType == "password");
    }

}
