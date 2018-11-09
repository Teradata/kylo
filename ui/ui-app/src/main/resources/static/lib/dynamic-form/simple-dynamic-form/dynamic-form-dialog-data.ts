import {FormConfig} from "../services/dynamic-form-builder";

export class DynamicFormDialogData{

    constructor(public  formConfig:FormConfig, public data?:any){

    }
}