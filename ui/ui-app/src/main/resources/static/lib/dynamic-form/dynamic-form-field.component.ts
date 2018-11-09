import {Component, Input} from '@angular/core';
import {FormGroup} from '@angular/forms';

import {FieldConfig} from './model/FieldConfig';
import {SectionHeader} from "./model/SectionHeader";
import {StaticText} from "./model/StaticText";
import {Icon} from './model/Icon';
import {FormControlValidation} from '../common/utils/form-control-validation';


@Component({
    selector: 'dynamic-form-field',
    styleUrls:['./dynamic-form-field.component.css'],
    templateUrl: './dynamic-form-field.component.html'
})
export class DynamicFormFieldComponent {
    @Input() field: FieldConfig<any>;
    @Input() form: FormGroup;
    @Input() readonly :boolean;

    get isValid() {
        if(this.field.controlType == SectionHeader.CONTROL_TYPE || this.field.controlType == StaticText.CONTROL_TYPE || this.field.controlType == Icon.CONTROL_TYPE) {
            return true;
        }
        else {
            return this.form && this.form.controls && this.form.controls[this.field.key] && this.form.controls[this.field.key].valid;
        }
    }

    getErrorMessage() {
        return FormControlValidation.getFieldConfigErrorMessage(this.form, this.field)
    }

    validateRequiredChips() {
        const theForm = this.form;
        const property: any = this.field;
        if(property instanceof FieldConfig){
            let propertyValue = (<FieldConfig<any>>property).getModelValue();
            if(property.required && propertyValue != null && propertyValue.length == 0){
                //INVALID
                //   (<FormGroup>theForm).get(property.key).
                //   theForm[property.formKey].$setValidity("required", false);
                //   theForm[property.formKey].$setDirty(true);
                (theForm as FormGroup).get((property as FieldConfig<any>).key).setValue('');
                return false;
            }
            else {
                (theForm as FormGroup).get((property as FieldConfig<any>).key).setValue(propertyValue);
                return true;
            }
        }
        else  if (property.required && property.values && property.values.length == 0) {
            //INVALID
            theForm[property.formKey].$setValidity("required", false);
            theForm[property.formKey].$setDirty(true);
            return false;
        }
        else {
            theForm[property.formKey].$setValidity("required", true);
            return true;
        }
    }

    hasControl(){
        return this.field.isStaticText() || this.form.contains(this.field.key);
    }

}
