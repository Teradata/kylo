import {Injectable} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {TranslateService} from "@ngx-translate/core";

import {FieldConfig} from '../model/FieldConfig';
import {DynamicFormUtil} from "./dynamic-form-util";

@Injectable()
export class DynamicFormService {
    constructor( private _translateService: TranslateService) {

    }

    toFormGroup(fields: FieldConfig<any>[] ) :FormGroup {
      return  DynamicFormUtil.toFormGroup(fields, this._translateService)
    }

    addToFormGroup(fields:FieldConfig<any>[], formGroup:FormGroup):FormControl[]{//: {[key: FieldConfig<any>]: FormControl}{
         return DynamicFormUtil.addToFormGroup(fields,formGroup,this._translateService)

    }

}
