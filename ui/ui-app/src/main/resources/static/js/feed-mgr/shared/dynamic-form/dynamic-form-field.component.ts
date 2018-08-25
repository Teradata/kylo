import { Component, Input } from '@angular/core';
import { FormGroup }        from '@angular/forms';

import { FieldConfig }     from './model/FieldConfig';
import {SectionHeader} from "./model/SectionHeader";
import {FormControlValidation} from "../../../common/utils/form-control-validation";
import {StaticText} from "./model/StaticText";
import {PolicyInputFormService} from "../field-policies-angular2/policy-input-form.service";

@Component({
    selector: 'dynamic-form-field',
    styleUrls:['js/feed-mgr/shared/dynamic-form/dynamic-form-field.component.css'],
    templateUrl: 'js/feed-mgr/shared/dynamic-form/dynamic-form-field.component.html'
})
export class DynamicFormFieldComponent {
    @Input() field: FieldConfig<any>;
    @Input() form: FormGroup;
    @Input() readonly :boolean;

    constructor(private policyInputFormService: PolicyInputFormService){

    }

    get isValid() {
        if(this.field.controlType == SectionHeader.CONTROL_TYPE || this.field.controlType == StaticText.CONTROL_TYPE) {
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
        this.policyInputFormService.validateRequiredChips(this.form, this.field);
    }

}