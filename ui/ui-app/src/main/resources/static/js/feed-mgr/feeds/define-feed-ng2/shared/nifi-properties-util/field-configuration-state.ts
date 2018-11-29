import {FieldConfig} from "../../../../../../lib/dynamic-form/model/FieldConfig";
import { Templates } from "../../../../../../lib/feed-mgr/services/TemplateTypes";

export class FieldConfigurationState {

    formFieldOrder:number = 0
    /**
     * Map of the inputProcessorId to the controls related to that input
     * @type {{}}
     */
    inputFieldsMap :{[key: string]: FieldConfig<any>[]} ={};

    /**
     * Map of all processors other inputs to array of configs
     * @type {{}}
     */
    processorFieldMap:{[key: string]: FieldConfig<any>[]} ={};


    /**
     * Return all fieldconfig objects for a given input processor
     * @param {string} processorId
     * @return {FieldConfig<any>[]}
     */
    getFieldsForInput(processorId:string, emptyArrayIfNull:boolean = true){
        if(!this.hasInputFields(processorId) && emptyArrayIfNull){
            return [];
        }
        else {
            return this.inputFieldsMap[processorId];
        }
    }

    /**
     * Update the static value for the fieldConfig
     * @param {Template.Processor} processor
     */
    updateFieldValues(processor:Templates.Processor){
        this.getFieldsForInput(processor.id).forEach((fieldConfig:FieldConfig<any>) => {
            let oldValue = fieldConfig.value;
            let newValue = fieldConfig.getModelValue();

            fieldConfig.value = fieldConfig.getModelValue()
        })
    }

    /**
     * are there input fields defined for the processor
     * @param {string} processorId
     * @return {boolean}
     */
    hasInputFields(processorId:string) {
        return this.inputFieldsMap[processorId] != undefined;
    }

    /**
     * are there fields defined for the processor
     * @param {string} processorId
     * @return {boolean}
     */
    hasNonInputFields(processorId:string) {
        return this.processorFieldMap[processorId] != undefined;
    }

    /**
     * Return all fieldconfig objects for a given input processor
     * @param {string} processorId
     * @return {FieldConfig<any>[]}
     */
    getFieldsForNonInput(processorId:string,emptyArrayIfNull:boolean = true){
        if(!this.hasNonInputFields(processorId) && emptyArrayIfNull){
            return [];
        }
        else {
            return this.processorFieldMap[processorId];
        }
    }

    getAndIncrementFieldOrder(){
        let order = this.formFieldOrder;
        this.formFieldOrder++;
        return order;
    }
    incrementAndGetFieldOrder(){
        this.formFieldOrder++;
        return this.formFieldOrder;
    }

    reset(){
        this.formFieldOrder = 0;
        this.inputFieldsMap = {}
        this.processorFieldMap = {}
    }
}
