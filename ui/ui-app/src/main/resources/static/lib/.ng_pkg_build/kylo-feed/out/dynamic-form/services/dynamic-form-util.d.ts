import { FormControl, FormGroup } from "@angular/forms";
import { FieldConfig } from "../model/FieldConfig";
import { TranslateService } from "@ngx-translate/core";
export declare class DynamicFormUtil {
    static toFormGroup(fields: FieldConfig<any>[], _translateService?: TranslateService): FormGroup;
    private static translate;
    static resolveLocaleKeys(field: FieldConfig<any>, _translateService?: TranslateService): void;
    static isTextOnlyField(field: FieldConfig<any>): boolean;
    static addToFormGroup(fields: FieldConfig<any>[], formGroup: FormGroup, _translateService?: TranslateService): FormControl[];
    static toFormControl(field: FieldConfig<any>, _translateService?: TranslateService): FormControl;
}
