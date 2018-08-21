import {FieldConfig} from "../model/FieldConfig";
import {CheckboxFieldBuilder, FieldConfigBuilder, InputTextFieldBuilder, RadioButtonFieldBuilder, SectionHeaderBuilder, SelectFieldBuilder, TextareaFieldBuilder} from "./field-config-builder";
import {InputText} from "../model/InputText";
import {Select} from "../model/Select";
import {RadioButton} from "../model/RadioButton";
import {Checkbox} from "../model/Checkbox";
import {Chip} from "../model/Chip";
import {SectionHeader} from "../model/SectionHeader";
import {Textarea} from "../model/Textarea";
import {FormControl, FormGroup} from "@angular/forms";

export class FormFieldBuilder {

    fields :FieldConfigBuilder<any>[] = [];

    constructor(){

    }
    currentBuilder:FieldConfigBuilder<any>;


    field(fieldBuilder:FieldConfigBuilder<any>){
        this.fields.push(fieldBuilder);
        return this;
    }




    /**
     * Builds the FieldConfig objects needed for a form
     * @return {FieldConfig[]}
     */
    build():FieldConfig<any>[] {
        let fieldConfiguration:FieldConfig<any>[] = [];
        this.fields.forEach((builder:FieldConfigBuilder<any>,i:number) =>{
            let fieldConfig = builder.build();
            if(fieldConfig.key == undefined || fieldConfig.key == ""){
                //auto add the key
                fieldConfig.key = "field-"+i
            }
            fieldConfiguration.push(fieldConfig);
        });
        return fieldConfiguration;
    }



}