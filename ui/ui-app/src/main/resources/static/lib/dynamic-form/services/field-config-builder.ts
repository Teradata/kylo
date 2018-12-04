import {GetErrorMessage, NgIfCallback, OnFieldChange} from "../model/FieldConfig";
import {Select} from "../model/Select";
import {Textarea} from "../model/Textarea";
import {RadioButton} from "../model/RadioButton";
import {InputText, InputType} from "../model/InputText";
import {Chip} from "../model/Chip";
import {SectionHeader} from "../model/SectionHeader";
import {Checkbox} from "../model/Checkbox";
import {DynamicFormFieldGroupBuilder} from "./dynamic-form-field-group-builder";
import {StaticText} from "../model/StaticText";
import {Icon} from '../model/Icon';
import {ObjectUtils} from '../../common/utils/object-utils';
import {ValidatorFn} from '@angular/forms';


export abstract class FieldConfigBuilder<T> {
    protected value: any;
    private key: string;
    private required: boolean;
    private order: number;
    private placeholder: string;
    private model?: any;
    private hint?: string;
    private readonlyValue: string;
    private modelValueProperty: string;
    private pattern?: string;
    private onModelChange?: OnFieldChange;
    private validators?: ValidatorFn[] | null;
    private disabled?: boolean;
    private placeHolderLocaleKey:string;
    private styleClass:string;
    private formGroupBuilder?:DynamicFormFieldGroupBuilder

    private ngIfCallback ?:NgIfCallback;

    private getErrorMessage ?:GetErrorMessage;



    protected constructor(formGroupBuilder?:DynamicFormFieldGroupBuilder) {
        this.formGroupBuilder = formGroupBuilder;
    }

    abstract getObjectType():any;


    done() : DynamicFormFieldGroupBuilder{
        return this.formGroupBuilder;
    }

    ngIf(callback:NgIfCallback, bind?:any){
        if(bind) {
            this.ngIfCallback = callback.bind(bind);
        }
        else {
            this.ngIfCallback = callback;
        }
        return this;
    }

    setStyleClass(value:string){
            this.styleClass = value;
            return this;
    }

    containsStyle(value:string):boolean {
        return (this.styleClass != undefined && this.styleClass.split(" ").find((v => v ==value.trim())) == undefined);
    }

    appendStyleClass(value:string){
        if(value) {
            if(this.styleClass == undefined || !this.containsStyle(value)) {
                if(this.styleClass == undefined){
                    this.styleClass = value.trim();
                }
                else {
                    this.styleClass += " "+value.trim();
                }
            }
        }
        return this;
    }
    setFormGroupBuilder(formGroupBuilder:DynamicFormFieldGroupBuilder){
        this.formGroupBuilder = this.formGroupBuilder;
        return this;
    }
    setKey(value: string)  {
        this.key = value;
        return this;
    }

    setValue(value:any){
        this.value = value;
        return this;
    }

    setPlaceholderLocaleKey(value:string){
        this.placeHolderLocaleKey = value;
        return this;
    }

    setRequired(value: boolean) {
        this.required = value;
        return this;
    }

    setOrder(value: number){
        this.order = value;
        return this;
    }

    setErrorMessageLookup(value:GetErrorMessage){
        this.getErrorMessage = value;
        return this;
    }

    setPlaceholder(value: string) {
        this.placeholder = value;
        return this;
    }

    getPlaceHolder(){
        return this.placeholder;
    }

    setHint(value: string) {
        this.hint = value;
        return this;
    }

    getHint(){
        return this.hint;
    }

    setModelValueProperty(value: string) {
        this.modelValueProperty = value;
        return this;
    }

    setPattern(value: string) {
        this.pattern = value;
        return this;
    }

    setDisabled(value: boolean) {
        this.disabled = value;
        return this;
    }

    onChange(value: OnFieldChange, bind?:any) {
        if(bind) {
            this.onModelChange = value.bind(bind);
        }
        else {
            this.onModelChange = value;
        }
        return this;
    }

    setValidators(value: ValidatorFn[]) {
        this.validators = value;
        return this;
    }

    setReadonlyValue(value: string) {
        this.readonlyValue = value;
        return this;
    }

    addValidator(value: ValidatorFn) {
        if (this.validators == undefined) {
            this.validators = [];
        }
        this.validators.push(value);
        return this;
    }


    setModel(value: any) {
        this.model = value;
        return this;
    }
    
    update<B extends FieldConfigBuilder<any>>(builder:B){
             this.key = builder.key;
             this.required = builder.required;
             this.placeholder = builder.placeholder;
             this.value = builder.value;
             this.hint = builder.hint;
             this.validators = builder.validators;
             this.model = builder.model;
             this.modelValueProperty = builder.modelValueProperty;
             this.onModelChange = builder.onModelChange;
             this.pattern = builder.pattern;
             this.order = builder.order;
             this.readonlyValue = builder.readonlyValue;
             this.disabled = builder.disabled;
             this.placeHolderLocaleKey = builder.placeHolderLocaleKey;
             this.styleClass =builder.styleClass;
             this.ngIfCallback =builder.ngIfCallback;
             this.getErrorMessage =builder.getErrorMessage
        return this;
    }

    protected buildOptions(): any {
        let option: any = {
            key: this.key,
            required: this.required,
            placeholder: this.placeholder,
            value: this.value,
            hint: this.hint,
            validators: this.validators,
            model: this.model,
            modelValueProperty: this.modelValueProperty,
            onModelChange: this.onModelChange,
            pattern: this.pattern,
            order: this.order,
            readonlyValue: this.readonlyValue,
            disabled: this.disabled,
            placeholderLocaleKey:this.placeHolderLocaleKey,
            styleClass:this.styleClass,
            ngIf:this.ngIfCallback,
            getErrorMessage:this.getErrorMessage
        }
        return option;
    }




    build(): T {
        if(this.formGroupBuilder.dynamicFormBuilder.styleClassStrategy){
            this.formGroupBuilder.dynamicFormBuilder.styleClassStrategy.applyStyleClass(this);
        }
        let options = this.buildOptions();
        return ObjectUtils.newType(options,this.getObjectType());
    }
}

export class ConfigurationFieldBuilder extends FieldConfigBuilder<any> {

    public constructor(formGroupBuilder?:DynamicFormFieldGroupBuilder) {
        super(formGroupBuilder)
    }

    getObjectType(): any {
        return "ConfigurationFieldBuilder";
    }

    build():any{
        //override.
        console.log("ConfigurationFieldBuilder cant be built ")
    }

}

export class SelectFieldBuilder extends FieldConfigBuilder<Select> {

    options: {label: string, value: string}[] = [];
    public constructor(formGroupBuilder?:DynamicFormFieldGroupBuilder) {
       super(formGroupBuilder)
    }

    getObjectType():any {
        return Select;
    }
    setOptions(options:{label: string, value: string}[]):SelectFieldBuilder {
        this.options = options;
        return this;
    }

    setOptionsArray(options:any[]){
        this.options = options.map(item => {
            return {label:item,value:item};
        });
        return this;
    }

    addOption(label: string, value: string): SelectFieldBuilder{
        this.options.push({label: label, value: value});
        return this;
    }

    protected buildOptions(){
        let options = super.buildOptions();
        options.options = this.options;
        return options;
    }
}


export class RadioButtonFieldBuilder extends FieldConfigBuilder<RadioButton> {
    options: {label: string, value: string}[] = [];

    public constructor(formGroupBuilder?:DynamicFormFieldGroupBuilder) {
        super(formGroupBuilder)
    }

    getObjectType():any {
        return RadioButton;
    }

    setOptions(options:{label: string, value: string}[]):RadioButtonFieldBuilder {
        this.options = options;
        return this;
    }

    setOptionsArray(options:any[]){
        this.options = options.map(item => {
            return {label:item,value:item};
        });
        return this;
    }

    addOption(label: string, value: string): RadioButtonFieldBuilder{
        this.options.push({label: label, value: value});
        return this;
    }

    protected buildOptions(){
        let options = super.buildOptions();
        options.options = this.options;
        return options;
    }
}

export class ChipsFieldBuilder extends FieldConfigBuilder<Chip> {
    items:any[]
    stacked:boolean = false;

    public constructor(formGroupBuilder?:DynamicFormFieldGroupBuilder) {
        super(formGroupBuilder)
    }

    getObjectType():any {
        return Chip;
    }

    setItems(values:any):ChipsFieldBuilder {
        this.items = values;
        return this;
    }

    setStacked(stacked:boolean):ChipsFieldBuilder {
        this.stacked = stacked;
        return this;
    }


    protected buildOptions(){
        let options = super.buildOptions();
        options.items = this.items;
        options.values = this.value;
        options.stacked = this.stacked;
        return options;
    }
}


export class CheckboxFieldBuilder extends FieldConfigBuilder<Checkbox> {

    trueValue:string;
    falseValue:string;
    checked:boolean;

    public constructor(formGroupBuilder?:DynamicFormFieldGroupBuilder) {
        super(formGroupBuilder)
    }

    getObjectType():any {
        return Checkbox;
    }

    setTrueValue(value:string) :CheckboxFieldBuilder{
        this.trueValue = value;
        return this;
    }

    setFalseValue(value:string) :CheckboxFieldBuilder{
        this.falseValue = value;
        return this;
    }

    setChecked(value:boolean) :CheckboxFieldBuilder{
        this.checked = value;
        return this;
    }

    protected buildOptions(){
        let options = super.buildOptions();
        options.trueValue = this.trueValue;
        options.falseValue = this.falseValue;
        return options;
    }

}

export class InputTextFieldBuilder extends FieldConfigBuilder<InputText> {

    type:InputType;
    private readonly:boolean;

    public constructor(formGroupBuilder?:DynamicFormFieldGroupBuilder) {
        super(formGroupBuilder)
    }

    getObjectType():any {
        return InputText;
    }


    setReadonly(value:boolean){
        this.readonly = value;
        return this;
    }
    setType(type:InputType):InputTextFieldBuilder{
        this.type = type;
        return this;
    }

    protected buildOptions(){
        let options = super.buildOptions();
        options.type = this.type;
        options.readonly = this.readonly;
        return options;
    }
}

export  class SectionHeaderBuilder extends FieldConfigBuilder<SectionHeader> {

    showDivider:boolean = true;

    public constructor(formGroupBuilder?:DynamicFormFieldGroupBuilder) {
        super(formGroupBuilder)
    }

    setShowDivider(showDivider:boolean){
        this.showDivider = showDivider;
        return this;
    }
    getObjectType():any {
        return SectionHeader;
    }

    protected buildOptions(){
        let options = super.buildOptions();
        options.showDivider = this.showDivider;
        return options;
    }
}

export  class StaticTextBuilder extends FieldConfigBuilder<StaticText> {

    showDivider:boolean = true;
    staticText:string;
    textStyleClass:string

    public constructor(formGroupBuilder?:DynamicFormFieldGroupBuilder) {
        super(formGroupBuilder)
    }

    setShowDivider(showDivider:boolean){
        this.showDivider = showDivider;
        return this;
    }


    setText(text:string){
        this.staticText = text;
        return this;
    }

    setTextStyleClass(value:string){
        this.textStyleClass = value;
        return this;
    }


    getObjectType():any {
        return StaticText;
    }

    protected buildOptions(){
        let options = super.buildOptions();
        options.showDivider = this.showDivider;
        options.staticText = this.staticText;
        options.textStyleClass = this.textStyleClass;
        return options;
    }
}

export  class TextareaFieldBuilder extends FieldConfigBuilder<Textarea> {

    private readonly:boolean;

    public constructor(formGroupBuilder?:DynamicFormFieldGroupBuilder) {
        super(formGroupBuilder)
    }

    getObjectType():any {
        return Textarea;
    }

    setReadonly(value:boolean) {
        this.readonly = value;
        return this;
    }

    protected buildOptions(){
        let options = super.buildOptions();
        options.readonly = this.readonly;
        return options;
    }

}

export class IconFieldBuilder extends FieldConfigBuilder<Icon> {

    private name: string;
    private size: number;

    public constructor(formGroupBuilder?:DynamicFormFieldGroupBuilder) {
        super(formGroupBuilder)
    }

    getObjectType():any {
        return Icon;
    }

    setName(name:string){
        this.name = name;
        return this;
    }

    setSize(size:number){
        this.size = size;
        return this;
    }

    protected buildOptions(){
        let options = super.buildOptions();
        options.name = this.name;
        options.size = this.size;
        return options;
    }
}
