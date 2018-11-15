/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { Injectable } from "@angular/core";
import { FormControl, FormGroup } from "@angular/forms";
import { TranslateService } from "@ngx-translate/core";
import { FieldConfig } from "../model/FieldConfig";
import { DynamicFormUtil } from "./dynamic-form-util";
export class DynamicFormService {
    /**
     * @param {?} _translateService
     */
    constructor(_translateService) {
        this._translateService = _translateService;
        console.log("BUILD FORM SERVICE!");
    }
    /**
     * @param {?} fields
     * @return {?}
     */
    toFormGroup(fields) {
        return DynamicFormUtil.toFormGroup(fields, this._translateService);
    }
    /**
     * @param {?} fields
     * @param {?} formGroup
     * @return {?}
     */
    addToFormGroup(fields, formGroup) {
        //: {[key: FieldConfig<any>]: FormControl}{
        return DynamicFormUtil.addToFormGroup(fields, formGroup, this._translateService);
    }
}
DynamicFormService.decorators = [
    { type: Injectable },
];
/** @nocollapse */
DynamicFormService.ctorParameters = () => [
    { type: TranslateService, },
];
function DynamicFormService_tsickle_Closure_declarations() {
    /** @type {!Array<{type: !Function, args: (undefined|!Array<?>)}>} */
    DynamicFormService.decorators;
    /**
     * @nocollapse
     * @type {function(): !Array<(null|{type: ?, decorators: (undefined|!Array<{type: !Function, args: (undefined|!Array<?>)}>)})>}
     */
    DynamicFormService.ctorParameters;
    /** @type {?} */
    DynamicFormService.prototype._translateService;
}
//# sourceMappingURL=dynamic-form.service.js.map