import { Injectable }   from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { FieldConfig } from '../model/FieldConfig';
import {DynamicFormUtil} from "./dynamic-form-util";

@Injectable()
export class DynamicFormService {
    constructor() { }

    toFormGroup(fields: FieldConfig<any>[] ) :FormGroup {
      return  DynamicFormUtil.toFormGroup(fields)
    }

    addToFormGroup(fields:FieldConfig<any>[], formGroup:FormGroup):FormControl[]{//: {[key: FieldConfig<any>]: FormControl}{
   return DynamicFormUtil.addToFormGroup(fields,formGroup)

    }

    private toFormControl(field:FieldConfig<any>) : FormControl {

      return DynamicFormUtil.toFormControl(field);


    }
}