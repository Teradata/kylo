import {FormControl, FormGroup} from "@angular/forms";
import {FieldConfig} from "../../dynamic-form/model/FieldConfig";

export class FormControlValidation {


    /**
     * gets the first error message for a given control key
     * param {FormGroup} form
     * param {string} key
     * return {string}
     */
    static getFieldConfigErrorMessage(form:FormGroup,field:FieldConfig<any>) {
        let control: FormControl = <FormControl>form.get(field.key);
        let errorMessage: string = "";
        if (control) {
            const controlErrors = control.errors;
            let firstError: any;
            if (controlErrors) {
                let firstKey = Object.keys(controlErrors)[0];
                firstError = controlErrors[firstKey];
                if(field.getErrorMessage){
                    errorMessage = field.getErrorMessage(firstKey,firstError,form)
                }
                if(errorMessage == undefined || errorMessage == '') {
                    //default error message
                    let label = field.placeholder;

                    if (typeof firstError == "boolean") {
                        errorMessage = (label || "This field") + " is " + firstKey;
                    }
                    else if (!(typeof firstError == "string")) {
                        errorMessage = (label || "This field") + " is invalid";
                    }
                    else {
                        errorMessage = firstError;
                    }
                }
            }
        }
        return errorMessage;
    }


    /**
     * gets the first error message for a given control key
     * param {FormGroup} form
     * param {string} key
     * return {string}
     */
    static getErrorMessage(form:FormGroup,key:string, label?:string) {
        let control: FormControl = <FormControl>form.get(key);
        let errorMessage: string = "";
        if (control) {
            const controlErrors = control.errors;
            let firstError: any;
            if (controlErrors) {
                let firstKey = Object.keys(controlErrors)[0];
                firstError = controlErrors[firstKey];
                if (typeof firstError == "boolean") {
                    errorMessage = (label || "This field") + " is " + firstKey;
                }
                else if (!(typeof firstError == "string")) {
                    errorMessage = (label || "This field") + " is invalid";
                }
                else {
                    errorMessage = firstError;
                }
            }
        }
        return errorMessage;
    }
}
