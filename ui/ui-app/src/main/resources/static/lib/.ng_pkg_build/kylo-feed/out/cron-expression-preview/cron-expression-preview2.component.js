/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { Component, Injector, Input, OnInit } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { AbstractControl, AsyncValidatorFn, FormControl, ValidationErrors } from "@angular/forms";
import { catchError, map } from "rxjs/operators";
import { TranslateService } from "@ngx-translate/core";
import { Observable } from "rxjs/Observable";
import { of } from "rxjs/observable/of";
export class CronExpressionPreviewComponent {
    /**
     * @param {?} $$angularInjector
     * @param {?} http
     * @param {?} translateService
     */
    constructor($$angularInjector, http, translateService) {
        this.$$angularInjector = $$angularInjector;
        this.http = http;
        this.translateService = translateService;
        this.restUrlService = $$angularInjector.get("RestUrlService");
        this.labelNotAvailable = this.translateService.instant('views.cron-expression-preview.PreviewNotAvailable');
    }
    /**
     * @return {?}
     */
    ngOnInit() {
        this.control.setAsyncValidators(this.cronExpressionValidator());
        this.control.updateValueAndValidity(); //updateValueAndValidity so that we get preview of the initial default value
    }
    /**
     * @return {?}
     */
    cronExpressionValidator() {
        return (control) => {
            return this.http.get(this.restUrlService.PREVIEW_CRON_EXPRESSION_URL, { params: { cronExpression: control.value } }).pipe(map(response => {
                this.nextDates = response;
                return null;
            }), catchError(() => {
                this.nextDates = [this.labelNotAvailable];
                return of(true);
            }));
        };
    }
}
CronExpressionPreviewComponent.decorators = [
    { type: Component, args: [{
                selector: "cron-expression-preview2",
                styles: [`.cron-expression-preview-container{min-height:20px;padding-left:0;list-style-type:none}.cron-expression-preview-item{background-color:#fff;border:1px solid #ddd;border-top-right-radius:4px;border-top-left-radius:4px;display:block;padding:4px 10px;margin-bottom:-1px}`],
                template: `<!--
  #%L
  thinkbig-ui-feed-manager
  %%
  Copyright (C) 2017 ThinkBig Analytics
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  -->
<span *ngIf="nextDates && nextDates.length > 0" class="hint" translate>views.cron-expression-preview.CronPreview</span>
<div class="cron-expression-preview-container">
  <div *ngFor="let nextDate of nextDates" class="cron-expression-preview-item">
    {{nextDate}}
  </div>
</div>`
            },] },
];
/** @nocollapse */
CronExpressionPreviewComponent.ctorParameters = () => [
    { type: Injector, },
    { type: HttpClient, },
    { type: TranslateService, },
];
CronExpressionPreviewComponent.propDecorators = {
    "control": [{ type: Input },],
};
function CronExpressionPreviewComponent_tsickle_Closure_declarations() {
    /** @type {!Array<{type: !Function, args: (undefined|!Array<?>)}>} */
    CronExpressionPreviewComponent.decorators;
    /**
     * @nocollapse
     * @type {function(): !Array<(null|{type: ?, decorators: (undefined|!Array<{type: !Function, args: (undefined|!Array<?>)}>)})>}
     */
    CronExpressionPreviewComponent.ctorParameters;
    /** @type {!Object<string,!Array<{type: !Function, args: (undefined|!Array<?>)}>>} */
    CronExpressionPreviewComponent.propDecorators;
    /** @type {?} */
    CronExpressionPreviewComponent.prototype.control;
    /** @type {?} */
    CronExpressionPreviewComponent.prototype.nextDates;
    /** @type {?} */
    CronExpressionPreviewComponent.prototype.restUrlService;
    /** @type {?} */
    CronExpressionPreviewComponent.prototype.labelNotAvailable;
    /** @type {?} */
    CronExpressionPreviewComponent.prototype.$$angularInjector;
    /** @type {?} */
    CronExpressionPreviewComponent.prototype.http;
    /** @type {?} */
    CronExpressionPreviewComponent.prototype.translateService;
}
//# sourceMappingURL=cron-expression-preview2.component.js.map