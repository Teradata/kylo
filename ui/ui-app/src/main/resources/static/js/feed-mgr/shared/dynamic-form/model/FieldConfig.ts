import {AbstractControlOptions} from "@angular/forms/src/model";
import {AsyncValidatorFn, ValidatorFn} from "@angular/forms/src/directives/validators";

export class FieldConfig<T> {
    value: T;
    key: string;
    label: string;
    required: boolean;
    order: number;
    controlType: string;
    placeholder:string;
    model?:any;
    hint?:string;
    readonlyValue:string;
    modelValueProperty:string;
    pattern?:string;
    onModelChange?:Function;
    validators?: ValidatorFn[] | null;
    disabled?:boolean;

    constructor(options: {
        value?: T,
        key?: string,
        label?: string,
        required?: boolean,
        order?: number,
        controlType?: string,
        placeholder?:string,
        model?:any,
        hint?:string,
        readonlyValue?:string,
        modelValueProperty?:string,
        pattern?:string,
        disabled?:boolean
    } = {}) {
        this.modelValueProperty = options.modelValueProperty  || 'value'
        this.value = options.value;
        this.key = options.key || '';
        this.label = options.label || '';
        this.required = !!options.required;
        this.order = options.order === undefined ? 1 : options.order;
        this.controlType = options.controlType || '';
        this.placeholder = options.placeholder || '';
        this.model = (options.model && options.model[this.modelValueProperty]) ? options.model : this;
        this.hint = options.hint || '';
        this.readonlyValue = options.readonlyValue || this.model.value;
        this.pattern = options.pattern;
        this.disabled = options.disabled || false;

    }

    setModelValue(value:any){
        this.model[this.modelValueProperty]= value;
    }

    getModelValue():any {
        return this.model[this.modelValueProperty];
    }


}