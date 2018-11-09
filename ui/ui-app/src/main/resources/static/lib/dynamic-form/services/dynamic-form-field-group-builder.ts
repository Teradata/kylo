import {FormFieldBuilder} from "./form-field-builder";
import {
    CheckboxFieldBuilder,
    ChipsFieldBuilder,
    FieldConfigBuilder,
    IconFieldBuilder,
    InputTextFieldBuilder,
    RadioButtonFieldBuilder,
    SectionHeaderBuilder,
    SelectFieldBuilder,
    StaticTextBuilder,
    TextareaFieldBuilder
} from "./field-config-builder";
import {FieldGroup, Layout} from "../model/FieldGroup";
import {DynamicFormBuilder} from "./dynamic-form-builder";

export class DynamicFormFieldGroupBuilder {

    formFieldBuilder:FormFieldBuilder;

    layout:Layout;
    layoutAlign:string;

    constructor(public dynamicFormBuilder:DynamicFormBuilder,layout:Layout = Layout.COLUMN){
        this.formFieldBuilder = new FormFieldBuilder();
        this.layout = layout;
    }

    setLayoutAlign(layoutAlign: string): DynamicFormFieldGroupBuilder {
        this.layoutAlign = layoutAlign;
        return this;
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

    chips(){
        let builder = new ChipsFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }

    staticText(){
        let builder = new StaticTextBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }

    icon(){
        let builder = new IconFieldBuilder(this);
        this.formFieldBuilder.field(builder);
        return builder;
    }

    build():FieldGroup{
        let group = new FieldGroup(this.layout);
        group.setLayoutAlign(this.layoutAlign);
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
    castAs<T extends DynamicFormBuilder> (type: { new(): T ;}):T{
        return <T> this.dynamicFormBuilder;
    }


}
