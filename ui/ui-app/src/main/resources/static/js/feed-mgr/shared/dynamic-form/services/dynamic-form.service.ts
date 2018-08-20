import { Injectable }   from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { FieldConfig } from '../model/FieldConfig';

@Injectable()
export class DynamicFormService {
    constructor() { }

    toFormGroup(fields: FieldConfig<any>[] ) :FormGroup {
        let group: any = {};

        fields.forEach(field => {
            group[field.key] = this.toFormControl(field);
        });
        return new FormGroup(group);
    }

    addToFormGroup(fields:FieldConfig<any>[], formGroup:FormGroup):FormControl[]{//: {[key: FieldConfig<any>]: FormControl}{
      //  let group:  {[key: FieldConfig<any>]: FormControl} = {};
        let formControls:FormControl[] = [];

        fields.forEach(field => {
            let control:FormControl =this.toFormControl(field);
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

    private toFormControl(field:FieldConfig<any>) : FormControl {

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