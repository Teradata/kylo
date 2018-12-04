import {FieldConfig} from "../model/FieldConfig";
import {FieldConfigBuilder} from "./field-config-builder";

export class FormFieldBuilder {

    fields :FieldConfigBuilder<any>[] = [];

    constructor(){

    }
    field(fieldBuilder:FieldConfigBuilder<any>){
        this.fields.push(fieldBuilder);
        return this;
    }

    /**
     * Builds the FieldConfig objects needed for a form
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
