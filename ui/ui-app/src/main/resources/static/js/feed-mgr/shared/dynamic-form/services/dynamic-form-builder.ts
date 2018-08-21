import {FormGroup} from "@angular/forms";
import {FieldConfigBuilder} from "./field-config-builder";
import {FieldConfig} from "../model/FieldConfig";
import {FormFieldBuilder} from "./form-field-builder";


export class FormConfig{

    title:string;
    message: string;
    fields:FieldConfig<any>[];

    form:FormGroup;

    constructor(){

    }
}
export class DynamicFormBuilder {

    title:string;
    message: string;
    formFieldBuilder:FormFieldBuilder;
    form:FormGroup

    constructor(){
        this.formFieldBuilder = new FormFieldBuilder();
    }

    setTitle(title:string):DynamicFormBuilder{
        this.title = title;
        return this;
    }
    setMessage(value:string):DynamicFormBuilder{
        this.message = value;
        return this;
    }

    field(fieldBuilder:FieldConfigBuilder<any>){
        this.formFieldBuilder.field(fieldBuilder);
        return this;
    }

    done(){
        return this;
    }

    setForm(value:FormGroup):DynamicFormBuilder{
        this.form = value;
        return this;
    }

    build():FormConfig{
        let formConfig = new FormConfig();
        if(this.form == undefined){
            this.form = new FormGroup({});
        }
        formConfig.form = this.form;

        let fields = this.formFieldBuilder.build();
        formConfig.fields = fields;
        return formConfig;
    }
}