import {FormControl, FormGroup, Validators} from "@angular/forms";
import {FieldConfig} from "../model/FieldConfig";

export class DynamicFormUtil {

    static toFormGroup(fields: FieldConfig<any>[] ) :FormGroup {
        let group: any = {};

        fields.forEach(field => {
            group[field.key] = DynamicFormUtil.toFormControl(field);
        });
        return new FormGroup(group);
    }

    static addToFormGroup(fields:FieldConfig<any>[], formGroup:FormGroup):FormControl[]{//: {[key: FieldConfig<any>]: FormControl}{
        //  let group:  {[key: FieldConfig<any>]: FormControl} = {};
        let formControls:FormControl[] = [];

        fields.forEach(field => {
            let control:FormControl =DynamicFormUtil.toFormControl(field);
            formGroup.addControl(field.key,control);
            formControls.push(control);

            control.valueChanges.debounceTime(200).subscribe(value=> {
                if(field.model && field.modelValueProperty){
                    console.log("update the model for ",field, 'with ',value);
                    field.model[field.modelValueProperty] = value;
                    if(field.onModelChange){
                        field.onModelChange(field.model);
                    }
                }
            })
            //    group[field] = control;
        });
        //   return group;
        return formControls;

    }

    static toFormControl(field:FieldConfig<any>) : FormControl {

        let validatorOpts = field.validators || null;
        if(field.required){
            if(validatorOpts == null){
                validatorOpts = [];
            }
            validatorOpts.push(Validators.required)
        }

        return new FormControl(field.value || '', validatorOpts)


    }
}