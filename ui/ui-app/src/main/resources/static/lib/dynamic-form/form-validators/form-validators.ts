import {AbstractControl, ValidatorFn} from "@angular/forms";


function isEmpty(value: any): boolean {
    return value == null || value.length === 0;
}

export class FormValidators {




    /**
     * Validates 2 form fields and ensures that the min value is less than the max value
     * @param {string} minFormKey
     * @param {string} maxFormKey
     * @return {ValidatorFn}
     */
    static minMaxFormFieldValidator(minFormKey: string, maxFormKey: string, minFieldLabel?: string, maxFieldLabel?: string): ValidatorFn {

        return (control: AbstractControl): { [key: string]: any } | null => {
            const errorKey:string = "minMaxError";
            let minValueControl = control.parent && control.parent.get(minFormKey);
            let maxValueControl = control.parent && control.parent.get(maxFormKey);
            let minValue = minValueControl ? minValueControl.value : null;
            let maxValue = maxValueControl ? maxValueControl.value : null;
            if(isEmpty(minValue) || isEmpty(maxValue)){
                //Dont validate empty values
                return null;
            }
            const minFloatValue = parseFloat(minValue);
            const maxFloatValue = parseFloat(maxValue);

            if (minFloatValue > maxFloatValue) {
                let message: string = "";
                if (minFieldLabel && maxFieldLabel) {
                    message = minFieldLabel + " can't be greater than " + maxFieldLabel;
                }
                else {
                    message = "The min value '" + minValue + "' can't be greater than then max value '" + maxValue + "'";
                }
                let error = {};
                error[errorKey] = message;
                return error;
            }
            else {
                if(minValueControl && maxValueControl) {
                    let error = {};
                    error[errorKey] = null;
                    //clear the other validators if they are invalid
                    if(minValueControl.hasError(errorKey)){
                        minValueControl.setErrors(error)
                    }
                    if(maxValueControl.hasError(errorKey)){
                        maxValueControl.setErrors(error)
                    }

                    //revalidate??
                }
                return null;
            }
        }
    }
}

