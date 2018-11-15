/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { FieldConfig } from "../model/FieldConfig";
import { TranslateService } from "@ngx-translate/core";
import { SectionHeader } from "../model/SectionHeader";
import { StaticText } from "../model/StaticText";
import { Checkbox } from "../model/Checkbox";
import { Chip } from "../model/Chip";
export class DynamicFormUtil {
    /**
     * @param {?} fields
     * @param {?=} _translateService
     * @return {?}
     */
    static toFormGroup(fields, _translateService) {
        let /** @type {?} */ group = {};
        fields.filter(field => !DynamicFormUtil.isTextOnlyField(field)).forEach(field => {
            group[field.key] = DynamicFormUtil.toFormControl(field, _translateService);
        });
        return new FormGroup(group);
    }
    /**
     * @param {?} key
     * @param {?} defaultValue
     * @param {?} _translateService
     * @return {?}
     */
    static translate(key, defaultValue, _translateService) {
        let /** @type {?} */ trans = _translateService.instant(key);
        if (trans == key && defaultValue) {
            return defaultValue;
        }
        else {
            return trans;
        }
    }
    /**
     * @param {?} field
     * @param {?=} _translateService
     * @return {?}
     */
    static resolveLocaleKeys(field, _translateService) {
        if (_translateService) {
            if (field.placeholderLocaleKey != undefined) {
                let /** @type {?} */ placeholder = DynamicFormUtil.translate(field.placeholderLocaleKey, field.placeholder, _translateService);
                field.placeholder = placeholder;
            }
        }
    }
    /**
     * @param {?} field
     * @return {?}
     */
    static isTextOnlyField(field) {
        return SectionHeader.CONTROL_TYPE == field.controlType || StaticText.CONTROL_TYPE == field.controlType;
    }
    /**
     * @param {?} fields
     * @param {?} formGroup
     * @param {?=} _translateService
     * @return {?}
     */
    static addToFormGroup(fields, formGroup, _translateService) {
        //: {[key: FieldConfig<any>]: FormControl}{
        //  let group:  {[key: FieldConfig<any>]: FormControl} = {};
        let /** @type {?} */ formControls = [];
        fields.filter(field => !DynamicFormUtil.isTextOnlyField(field)).forEach(field => {
            let /** @type {?} */ control = DynamicFormUtil.toFormControl(field, _translateService);
            formGroup.addControl(field.key, control);
            formControls.push(control);
            control.valueChanges.debounceTime(200).subscribe(value => {
                if (field.model && field.modelValueProperty) {
                    field.model[field.modelValueProperty] = value;
                    if (field.onModelChange) {
                        field.onModelChange(value, formGroup, field.model);
                    }
                }
                field.value = value;
            });
        });
        //   return group;
        return formControls;
    }
    /**
     * @param {?} field
     * @param {?=} _translateService
     * @return {?}
     */
    static toFormControl(field, _translateService) {
        if (_translateService) {
            DynamicFormUtil.resolveLocaleKeys(field, _translateService);
        }
        let /** @type {?} */ validatorOpts = field.validators || null;
        if (field.required) {
            if (validatorOpts == null) {
                validatorOpts = [];
            }
            validatorOpts.push(Validators.required);
        }
        let /** @type {?} */ value = field.value || '';
        if (field.controlType == "checkbox") {
            value = (/** @type {?} */ (field)).checked;
        }
        if (field.controlType == Chip.CONTROL_TYPE) {
            value = (/** @type {?} */ (field)).selectedItems;
        }
        return new FormControl(value, validatorOpts);
    }
}
//# sourceMappingURL=dynamic-form-util.js.map