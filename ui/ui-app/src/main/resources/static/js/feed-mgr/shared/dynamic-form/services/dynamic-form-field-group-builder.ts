import {FormFieldBuilder} from "./form-field-builder";
import {CheckboxFieldBuilder, FieldConfigBuilder, InputTextFieldBuilder, RadioButtonFieldBuilder, SectionHeaderBuilder, SelectFieldBuilder, TextareaFieldBuilder} from "./field-config-builder";
import {FieldGroup, Layout} from "../model/FieldGroup";
import {DynamicFormBuilder} from "./dynamic-form-builder";

export class DynamicFormFieldGroupBuilder {

    formFieldBuilder:FormFieldBuilder;

    layout:Layout;

    constructor(private dynamicFormBuilder:DynamicFormBuilder,layout:Layout = Layout.COLUMN){
        this.formFieldBuilder = new FormFieldBuilder();
        this.layout = layout;
    }

    field(fieldBuilder:FieldConfigBuilder<any>){
        fieldBuilder.setFormGroupBuilder(this);
        this.formFieldBuilder.field(fieldBuilder);
        return this;
    }

    select(){
        let builder = new SelectFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    text(){
        let builder = new InputTextFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    textarea(){
        let builder = new TextareaFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    radio(){
        let builder = new RadioButtonFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    checkbox(){
        let builder = new CheckboxFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }
    sectionHeader(){
        let builder = new SectionHeaderBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }

    build():FieldGroup{
        let group = new FieldGroup(this.layout)
        group.fields = this.formFieldBuilder.build();
        return group;
    }

    rowComplete(){
        return this.dynamicFormBuilder;
    }

    columnComplete(){
        return this.dynamicFormBuilder;
    }

    completeGroup(){
        return this.dynamicFormBuilder;
    }

}
