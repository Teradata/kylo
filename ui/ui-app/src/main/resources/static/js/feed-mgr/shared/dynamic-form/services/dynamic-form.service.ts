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

    addToFormGroup(fields:FieldConfig<any>[], formGroup:FormGroup){//: {[key: FieldConfig<any>]: FormControl}{
      //  let group:  {[key: FieldConfig<any>]: FormControl} = {};

        fields.forEach(field => {
            let control:FormControl =this.toFormControl(field);
            formGroup.registerControl(field.key,control);
        //    group[field] = control;
        });
     //   return group;

    }

    private toFormControl(field:FieldConfig<any>) : FormControl {
        return  field.required ? new FormControl(field.value || '', Validators.required)
            : new FormControl(field.value || '');
    }
}