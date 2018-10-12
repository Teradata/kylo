import {AbstractControl, FormArray, FormControl, FormGroup} from "@angular/forms";
import {Injectable} from "@angular/core";
import * as _ from "underscore"

@Injectable()
export class FormGroupUtil {


    static touchFormControls(formGroup:FormGroup) {
        if (formGroup.controls) {
            (<any>Object).values(formGroup.controls).forEach((control: AbstractControl) => {

                control.markAsTouched();
                if (control instanceof FormGroup) {
                    FormGroupUtil.touchFormControls(control as FormGroup);
                }
                if (control instanceof FormArray) {
                    FormGroupUtil.touchFormArrayControls(control as FormArray);
                }
            });
        }
    }

    static touchFormArrayControls(formArray:FormArray) {
        if (formArray.controls) {
            formArray.controls.forEach((control: AbstractControl) => {
                control.markAsTouched();
                if (control instanceof FormGroup) {
                    FormGroupUtil.touchFormControls(control as FormGroup);
                }
                if (control instanceof FormArray) {
                    FormGroupUtil.touchFormArrayControls(control as FormArray);
                }
            });
        }
    }
}