import {ValidatorFn} from "@angular/forms/src/directives/validators";
import {FieldConfig} from "../model/FieldConfig";
import {Select} from "../model/Select";
import {Textarea} from "../model/Textarea";
import {RadioButton} from "../model/RadioButton";
import {InputText, InputType} from "../model/InputText";
import {Chip} from "../model/Chip";
import {SectionHeader} from "../model/SectionHeader";
import {Checkbox} from "../model/Checkbox";
import {ObjectUtils} from "../../../../common/utils/object-utils";

export abstract class FieldConfigBuilder<T> {
    private value: any;
    private key: string;
    private label: string;
    private required: boolean;
    private order: number;
    private placeholder: string;
    private model?: any;
    private hint?: string;
    private readonlyValue: string;
    private modelValueProperty: string;
    private pattern?: string;
    private onModelChange?: Function;
    private validators?: ValidatorFn[] | null;
    private disabled?: boolean;

    protected constructor() {

    }

    abstract getObjectType():any;



    setKey(value: string)  {
        this.key = value;
        return this;
    }

    setValue(value:any){
        this.value = value;
        return this;
    }

    setLabel(value: string){
        this.label = value;
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

    setControlType(value: string){
        this.label = value;
        return this;
    }

    setPlaceholder(value: string) {
        this.placeholder = value;
        return this;
    }

    setHint(value: string) {
        this.hint = value;
        return this;
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

    setOnModelChange(value: Function) {
        this.onModelChange = value;
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

    buildOptions(): any {
        let option: any = {
            key: this.key,
            label: this.label,
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
            disabled: this.disabled
        }
        return option;
    }


    build(): T {
        let options = this.buildOptions();
        return ObjectUtils.newType(options,this.getObjectType());
    }
}


    export class SelectFieldBuilder extends FieldConfigBuilder<Select> {

        options: {label: string, value: string}[] = [];
        constructor() {
            super();
        }

        getObjectType():any {
            return Select;
        }
        setOptions(options:{label: string, value: string}[]):SelectFieldBuilder {
            this.options = options;
            return this;
        }

        addOption(label: string, value: string): SelectFieldBuilder{
            this.options.push({label: label, value: value});
            return this;
        }

        buildOptions(){
            let options = super.buildOptions();
            options.options = this.options;
            return options;
        }



}


export class RadioButtonFieldBuilder extends FieldConfigBuilder<RadioButton> {
    options: {label: string, value: string}[] = [];

    constructor() {
        super();
    }

    getObjectType():any {
        return RadioButton;
    }

    setOptions(options:{label: string, value: string}[]):RadioButtonFieldBuilder {
        this.options = options;
        return this;
    }

    addOption(label: string, value: string): RadioButtonFieldBuilder{
        this.options.push({label: label, value: value});
        return this;
    }

    buildOptions(){
        let options = super.buildOptions();
        options.options = this.options;
        return options;
    }
}


export class CheckboxFieldBuilder extends FieldConfigBuilder<Checkbox> {

    trueValue:string;
    falseValue:string;
    checked:boolean;

    constructor() {
        super();
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

    buildOptions(){
        let options = super.buildOptions();
        options.trueValue = this.trueValue;
        options.falseValue = this.falseValue;
        return options;
    }

}

export class InputTextFieldBuilder extends FieldConfigBuilder<InputText> {

    type:InputType;

    constructor() {
        super();
    }

    getObjectType():any {
        return InputText;
    }

    setType(type:InputType):InputTextFieldBuilder{
        this.type = type;
        return this;
    }

    buildOptions(){
        let options = super.buildOptions();
        options.type = this.type;
        return options;
    }
}

export  class SectionHeaderBuilder extends FieldConfigBuilder<SectionHeader> {

    constructor() {
super();
    }

    getObjectType():any {
        return SectionHeader;
    }
}

export  class TextareaFieldBuilder extends FieldConfigBuilder<Textarea> {

    constructor() {
        super();
    }

    getObjectType():any {
        return Textarea;
    }

}