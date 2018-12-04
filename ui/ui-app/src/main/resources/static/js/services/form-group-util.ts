import {AbstractControl, FormArray, FormControl, FormGroup} from "@angular/forms";
import {Injectable} from "@angular/core";
import * as _ from "underscore"

@Injectable()
export class FormGroupUtil {


    static disableFormControls(formGroup:FormGroup) {
        formGroup.disable()
        if (formGroup.controls) {
            (<any>Object).values(formGroup.controls).forEach((control: AbstractControl) => {

                control.disable();
                if (control instanceof FormGroup) {
                    FormGroupUtil.disableFormControls(control as FormGroup);
                }
                if (control instanceof FormArray) {
                    FormGroupUtil.disableFormArrayControls(control as FormArray);
                }
            });
        }
    }

    static disableFormArrayControls(formArray:FormArray) {
        if (formArray.controls) {
            formArray.controls.forEach((control: AbstractControl) => {
                control.disable();
                if (control instanceof FormGroup) {
                    FormGroupUtil.disableFormControls(control as FormGroup);
                }
                if (control instanceof FormArray) {
                    FormGroupUtil.disableFormArrayControls(control as FormArray);
                }
            });
        }
    }

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
    static findInvalidControls(formGroup:FormGroup) :FormControl[]{
        let controls:FormControl[] = [];
        if (formGroup.controls) {
            (<any>Object).values(formGroup.controls).forEach((control: AbstractControl) => {

                if(control instanceof FormControl && control.invalid){
                    controls.push(control);
                }

                if (control instanceof FormGroup) {
                let invalidControls =   FormGroupUtil.findInvalidControls(control as FormGroup);
                 controls = controls.concat(invalidControls)
                }
                if (control instanceof FormArray) {
                    let invalidControls =  FormGroupUtil.findInvalidFormArrayControls(control as FormArray);
                    controls = controls.concat(invalidControls)
                }
            });
        }
        return controls;
    }


    static findInvalidFormArrayControls(formArray:FormArray) {
        let controls:FormControl[] = [];
        if (formArray.controls) {
            formArray.controls.forEach((control: AbstractControl) => {
                if(control instanceof FormControl && control.invalid){
                    controls.push(control);
                }
                if (control instanceof FormGroup) {
                    let invalidControls =  FormGroupUtil.findInvalidControls(control as FormGroup);
                    controls = controls.concat(invalidControls)
                }
                if (control instanceof FormArray) {
                    let invalidControls =   FormGroupUtil.findInvalidFormArrayControls(control as FormArray);
                    controls = controls.concat(invalidControls)
                }
            });
        }
        return controls;
    }
}