import { Injectable }   from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { FieldConfig } from '../model/FieldConfig';
import {DynamicFormUtil} from "./dynamic-form-util";
import {TranslateService} from "@ngx-translate/core";

@Injectable()
export class DynamicFormService {
    constructor( private _translateService: TranslateService) {
        console.log("BUILD FORM SERVICE!")
    }

    toFormGroup(fields: FieldConfig<any>[] ) :FormGroup {
      return  DynamicFormUtil.toFormGroup(fields, this._translateService)
    }

    addToFormGroup(fields:FieldConfig<any>[], formGroup:FormGroup):FormControl[]{//: {[key: FieldConfig<any>]: FormControl}{
         return DynamicFormUtil.addToFormGroup(fields,formGroup,this._translateService)

    }

}