import { FormControl, FormGroup } from '@angular/forms';
import { TranslateService } from "@ngx-translate/core";
import { FieldConfig } from '../model/FieldConfig';
export declare class DynamicFormService {
    private _translateService;
    constructor(_translateService: TranslateService);
    toFormGroup(fields: FieldConfig<any>[]): FormGroup;
    addToFormGroup(fields: FieldConfig<any>[], formGroup: FormGroup): FormControl[];
}
