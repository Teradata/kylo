import { OnChanges, SimpleChanges } from "@angular/core";
import { FormGroup } from "@angular/forms";
import { FieldConfig } from "../../dynamic-form/model/FieldConfig";
import { FieldGroup } from "../../dynamic-form/model/FieldGroup";
import { DynamicFormFieldGroupBuilder } from "../../dynamic-form/services/dynamic-form-field-group-builder";
import { DynamicFormService } from "../../dynamic-form/services/dynamic-form.service";
import { Property } from "../model/property";
import { ProcessorRef } from "./processor-ref";
export declare class FieldConfigurationState {
    formFieldOrder: number;
    /**
     * Map of the inputProcessorId to the controls related to that input
     */
    inputFieldsMap: {
        [key: string]: FieldConfig<any>[];
    };
    /**
     * Map of all processors other inputs to array of configs
     */
    processorFieldMap: {
        [key: string]: FieldConfig<any>[];
    };
    /**
     * Return all fieldconfig objects for a given input processor
     */
    getFieldsForInput(processorId: string, emptyArrayIfNull?: boolean): FieldConfig<any>[];
    /**
     * are there input fields defined for the processor
     * param {string} processorId
     * return {boolean}
     */
    hasInputFields(processorId: string): boolean;
    /**
     * are there fields defined for the processor
     * param {string} processorId
     * return {boolean}
     */
    hasNonInputFields(processorId: string): boolean;
    /**
     * Return all fieldconfig objects for a given input processor
     */
    getFieldsForNonInput(processorId: string, emptyArrayIfNull?: boolean): FieldConfig<any>[];
    getAndIncrementFieldOrder(): number;
    incrementAndGetFieldOrder(): number;
    reset(): void;
}
export declare class FieldConfigurationBuilder {
    private state;
    private addSectionHeader;
    formGroupBuilder: DynamicFormFieldGroupBuilder;
    constructor(state: FieldConfigurationState, addSectionHeader: boolean);
    createFormFields(processor: ProcessorRef, properties?: Property[]): FieldConfig<any>[];
    private buildField;
    /**
     * convert the property allowable values to label,value objects
     */
    private getSelectOptions;
    private toFieldConfig;
    private isInputText;
}
export declare class ProcessorFormComponent implements OnChanges {
    private dynamicFormService;
    addSectionHeader: boolean | string;
    form: FormGroup;
    processor: ProcessorRef;
    properties: Property | Property[];
    readonly: boolean;
    fieldGroups: FieldGroup[];
    private fieldConfigurationState;
    constructor(dynamicFormService: DynamicFormService);
    ngOnChanges(changes: SimpleChanges): void;
}
