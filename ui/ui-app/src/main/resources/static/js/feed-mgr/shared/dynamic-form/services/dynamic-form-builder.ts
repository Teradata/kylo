import {FormGroup} from "@angular/forms";
import {FieldConfigBuilder} from "./field-config-builder";
import {FieldConfig} from "../model/FieldConfig";
import {FormFieldBuilder} from "./form-field-builder";
import {DynamicFormFieldGroupBuilder} from "./dynamic-form-field-group-builder";
import {FieldGroup, Layout} from "../model/FieldGroup"



export class FormConfig{

    title:string;
    message: string;
    fieldGroups:FieldGroup[];

    form:FormGroup;

    constructor(){

    }
}
export class DynamicFormBuilder {

    title:string;
    message: string;
    formFieldBuilders:DynamicFormFieldGroupBuilder[];
    form:FormGroup

    constructor(){
        this.formFieldBuilders = [];
    }

    setTitle(title:string):DynamicFormBuilder{
        this.title = title;
        return this;
    }
    setMessage(value:string):DynamicFormBuilder{
        this.message = value;
        return this;
    }

    row(){
        let rowBuilder = new DynamicFormFieldGroupBuilder(this,Layout.ROW);
        this.formFieldBuilders.push(rowBuilder);
        return rowBuilder;
    }

    column(){
        let columnBuilder = new DynamicFormFieldGroupBuilder(this,Layout.COLUMN);
        this.formFieldBuilders.push(columnBuilder);
        return columnBuilder;
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
        formConfig.title = this.title;
        if(this.form == undefined){
            this.form = new FormGroup({});
        }
        formConfig.form = this.form;
        let fieldGroups: FieldGroup[] = this.formFieldBuilders.map(builder => builder.build())

        formConfig.fieldGroups = fieldGroups;
        return formConfig;
    }
}