import { Component, Input } from '@angular/core';
import { FormGroup }        from '@angular/forms';

import { FieldConfig }     from './model/FieldConfig';
import {SectionHeader} from "./model/SectionHeader";

@Component({
    selector: 'dynamic-form-field',
    styleUrls:['js/feed-mgr/shared/dynamic-form/dynamic-form-field.component.css'],
    templateUrl: 'js/feed-mgr/shared/dynamic-form/dynamic-form-field.component.html'
})
export class DynamicFormFieldComponent {
    @Input() field: FieldConfig<any>;
    @Input() form: FormGroup;
    @Input() readonly :boolean;

    constructor(){

    }

    get isValid() {
        if(this.field.controlType == SectionHeader.CONTROL_TYPE) {
        return true;
        }
        else {
            return this.form && this.form.controls && this.form.controls[this.field.key].valid;
        }
    }


    getErrorMessage() {
        let control = this.form.get(this.field.key);
        const controlErrors = control.errors;
        let firstError :any;
        let errorMessage:string = "";
        if(control && controlErrors) {
            let firstKey = Object.keys(controlErrors)[0];
            firstError = controlErrors[firstKey];
            if(typeof firstError == "boolean"){
                errorMessage = this.field.label+" is "+firstKey;
            }
            else if(!(typeof firstError == "string")){
                errorMessage = this.field.label +" is invalid";
            }
            else {
                errorMessage = firstError;
            }
        }
        return errorMessage;
    }
}