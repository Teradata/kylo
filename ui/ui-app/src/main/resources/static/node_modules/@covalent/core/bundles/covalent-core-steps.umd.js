(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('@angular/cdk/portal'), require('@angular/cdk/coercion'), require('@covalent/core/common'), require('@angular/common'), require('@angular/cdk/scrolling'), require('@angular/material/icon'), require('@angular/material/core')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', '@angular/cdk/portal', '@angular/cdk/coercion', '@covalent/core/common', '@angular/common', '@angular/cdk/scrolling', '@angular/material/icon', '@angular/material/core'], factory) :
	(factory((global.covalent = global.covalent || {}, global.covalent.core = global.covalent.core || {}, global.covalent.core.steps = {}),global.ng.core,global.ng.cdk.portal,global.ng.cdk.coercion,global.covalent.core.common,global.ng.common,global.ng.cdk.scrolling,global.ng.material.icon,global.ng.material.core));
}(this, (function (exports,core,portal,coercion,common,common$1,scrolling,icon,core$1) { 'use strict';

/*! *****************************************************************************
Copyright (c) Microsoft Corporation. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at http://www.apache.org/licenses/LICENSE-2.0
THIS CODE IS PROVIDED ON AN *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED
WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE,
MERCHANTABLITY OR NON-INFRINGEMENT.
See the Apache Version 2.0 License for specific language governing permissions
and limitations under the License.
***************************************************************************** */
/* global Reflect, Promise */
var extendStatics = Object.setPrototypeOf ||
    ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
    function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
function __extends(d, b) {
    extendStatics(d, b);
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
}









function __read(o, n) {
    var m = typeof Symbol === "function" && o[Symbol.iterator];
    if (!m) return o;
    var i = m.call(o), r, ar = [], e;
    try {
        while ((n === void 0 || n-- > 0) && !(r = i.next()).done) ar.push(r.value);
    }
    catch (error) { e = { error: error }; }
    finally {
        try {
            if (r && !r.done && (m = i["return"])) m.call(i);
        }
        finally { if (e) throw e.error; }
    }
    return ar;
}
function __spread() {
    for (var ar = [], i = 0; i < arguments.length; i++)
        ar = ar.concat(__read(arguments[i]));
    return ar;
}

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
/** @enum {string} */
var StepState = {
    None: 'none',
    Required: 'required',
    Complete: 'complete',
};
var TdStepLabelDirective = /** @class */ (function (_super) {
    __extends(TdStepLabelDirective, _super);
    /**
     * @param {?} templateRef
     * @param {?} viewContainerRef
     */
    function TdStepLabelDirective(templateRef, viewContainerRef) {
        return _super.call(this, templateRef, viewContainerRef) || this;
    }
    return TdStepLabelDirective;
}(portal.TemplatePortalDirective));
TdStepLabelDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[td-step-label]ng-template',
            },] },
];
/** @nocollapse */
TdStepLabelDirective.ctorParameters = function () { return [
    { type: core.TemplateRef, },
    { type: core.ViewContainerRef, },
]; };
var TdStepActionsDirective = /** @class */ (function (_super) {
    __extends(TdStepActionsDirective, _super);
    /**
     * @param {?} templateRef
     * @param {?} viewContainerRef
     */
    function TdStepActionsDirective(templateRef, viewContainerRef) {
        return _super.call(this, templateRef, viewContainerRef) || this;
    }
    return TdStepActionsDirective;
}(portal.TemplatePortalDirective));
TdStepActionsDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[td-step-actions]ng-template',
            },] },
];
/** @nocollapse */
TdStepActionsDirective.ctorParameters = function () { return [
    { type: core.TemplateRef, },
    { type: core.ViewContainerRef, },
]; };
var TdStepSummaryDirective = /** @class */ (function (_super) {
    __extends(TdStepSummaryDirective, _super);
    /**
     * @param {?} templateRef
     * @param {?} viewContainerRef
     */
    function TdStepSummaryDirective(templateRef, viewContainerRef) {
        return _super.call(this, templateRef, viewContainerRef) || this;
    }
    return TdStepSummaryDirective;
}(portal.TemplatePortalDirective));
TdStepSummaryDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[td-step-summary]ng-template',
            },] },
];
/** @nocollapse */
TdStepSummaryDirective.ctorParameters = function () { return [
    { type: core.TemplateRef, },
    { type: core.ViewContainerRef, },
]; };
var TdStepBase = /** @class */ (function () {
    function TdStepBase() {
    }
    return TdStepBase;
}());
/* tslint:disable-next-line */
var _TdStepMixinBase = common.mixinDisableRipple(common.mixinDisabled(TdStepBase));
var TdStepComponent = /** @class */ (function (_super) {
    __extends(TdStepComponent, _super);
    /**
     * @param {?} _viewContainerRef
     */
    function TdStepComponent(_viewContainerRef) {
        var _this = _super.call(this) || this;
        _this._viewContainerRef = _viewContainerRef;
        _this._active = false;
        _this._state = StepState.None;
        /**
         * activated?: function
         * Event emitted when [TdStepComponent] is activated.
         */
        _this.onActivated = new core.EventEmitter();
        /**
         * deactivated?: function
         * Event emitted when [TdStepComponent] is deactivated.
         */
        _this.onDeactivated = new core.EventEmitter();
        return _this;
    }
    Object.defineProperty(TdStepComponent.prototype, "stepContent", {
        /**
         * @return {?}
         */
        get: function () {
            return this._contentPortal;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdStepComponent.prototype, "active", {
        /**
         * @return {?}
         */
        get: function () {
            return this._active;
        },
        /**
         * active?: boolean
         * Toggles [TdStepComponent] between active/deactive.
         * @param {?} active
         * @return {?}
         */
        set: function (active) {
            this._setActive(coercion.coerceBooleanProperty(active));
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdStepComponent.prototype, "state", {
        /**
         * @return {?}
         */
        get: function () {
            return this._state;
        },
        /**
         * state?: StepState or ['none' | 'required' | 'complete']
         * Sets state of [TdStepComponent] depending on value.
         * Defaults to [StepState.None | 'none'].
         * @param {?} state
         * @return {?}
         */
        set: function (state) {
            switch (state) {
                case StepState.Complete:
                    this._state = StepState.Complete;
                    break;
                case StepState.Required:
                    this._state = StepState.Required;
                    break;
                default:
                    this._state = StepState.None;
                    break;
            }
        },
        enumerable: true,
        configurable: true
    });
    /**
     * @return {?}
     */
    TdStepComponent.prototype.ngOnInit = function () {
        this._contentPortal = new portal.TemplatePortal(this._content, this._viewContainerRef);
    };
    /**
     * Toggle active state of [TdStepComponent]
     * retuns 'true' if successful, else 'false'.
     * @return {?}
     */
    TdStepComponent.prototype.toggle = function () {
        return this._setActive(!this._active);
    };
    /**
     * Opens [TdStepComponent]
     * retuns 'true' if successful, else 'false'.
     * @return {?}
     */
    TdStepComponent.prototype.open = function () {
        return this._setActive(true);
    };
    /**
     * Closes [TdStepComponent]
     * retuns 'true' if successful, else 'false'.
     * @return {?}
     */
    TdStepComponent.prototype.close = function () {
        return this._setActive(false);
    };
    /**
     * Returns 'true' if [state] equals to [StepState.Complete | 'complete'], else 'false'.
     * @return {?}
     */
    TdStepComponent.prototype.isComplete = function () {
        return this._state === StepState.Complete;
    };
    /**
     * Method executed when the disabled value changes
     * @param {?} v
     * @return {?}
     */
    TdStepComponent.prototype.onDisabledChange = function (v) {
        if (v && this._active) {
            this._active = false;
            this._onDeactivated();
        }
    };
    /**
     * Method to change active state internally and emit the [onActivated] event if 'true' or [onDeactivated]
     * event if 'false'. (Blocked if [disabled] is 'true')
     * returns true if successfully changed state
     * @param {?} newActive
     * @return {?}
     */
    TdStepComponent.prototype._setActive = function (newActive) {
        if (this.disabled) {
            return false;
        }
        if (this._active !== newActive) {
            this._active = newActive;
            if (newActive) {
                this._onActivated();
            }
            else {
                this._onDeactivated();
            }
            return true;
        }
        return false;
    };
    /**
     * @return {?}
     */
    TdStepComponent.prototype._onActivated = function () {
        this.onActivated.emit(undefined);
    };
    /**
     * @return {?}
     */
    TdStepComponent.prototype._onDeactivated = function () {
        this.onDeactivated.emit(undefined);
    };
    return TdStepComponent;
}(_TdStepMixinBase));
TdStepComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-step',
                inputs: ['disabled', 'disableRipple'],
                template: "<ng-template>\n  <ng-content></ng-content>\n</ng-template>",
            },] },
];
/** @nocollapse */
TdStepComponent.ctorParameters = function () { return [
    { type: core.ViewContainerRef, },
]; };
TdStepComponent.propDecorators = {
    "_content": [{ type: core.ViewChild, args: [core.TemplateRef,] },],
    "stepLabel": [{ type: core.ContentChild, args: [TdStepLabelDirective,] },],
    "stepActions": [{ type: core.ContentChild, args: [TdStepActionsDirective,] },],
    "stepSummary": [{ type: core.ContentChild, args: [TdStepSummaryDirective,] },],
    "label": [{ type: core.Input, args: ['label',] },],
    "sublabel": [{ type: core.Input, args: ['sublabel',] },],
    "active": [{ type: core.Input, args: ['active',] },],
    "state": [{ type: core.Input, args: ['state',] },],
    "onActivated": [{ type: core.Output, args: ['activated',] },],
    "onDeactivated": [{ type: core.Output, args: ['deactivated',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
/**
 * @record
 */
/** @enum {string} */
var StepMode = {
    Vertical: 'vertical',
    Horizontal: 'horizontal',
};
var TdStepsComponent = /** @class */ (function () {
    function TdStepsComponent() {
        this._mode = StepMode.Vertical;
        /**
         * stepChange?: function
         * Method to be executed when [onStepChange] event is emitted.
         * Emits an [IStepChangeEvent] implemented object.
         */
        this.onStepChange = new core.EventEmitter();
    }
    Object.defineProperty(TdStepsComponent.prototype, "stepsContent", {
        /**
         * @param {?} steps
         * @return {?}
         */
        set: function (steps) {
            if (steps) {
                this._steps = steps;
                this._registerSteps();
            }
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdStepsComponent.prototype, "steps", {
        /**
         * @return {?}
         */
        get: function () {
            return this._steps.toArray();
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdStepsComponent.prototype, "mode", {
        /**
         * @return {?}
         */
        get: function () {
            return this._mode;
        },
        /**
         * mode?: StepMode or ["vertical" | "horizontal"]
         * Defines if the mode of the [TdStepsComponent].  Defaults to [StepMode.Vertical | "vertical"]
         * @param {?} mode
         * @return {?}
         */
        set: function (mode) {
            switch (mode) {
                case StepMode.Horizontal:
                    this._mode = StepMode.Horizontal;
                    break;
                default:
                    this._mode = StepMode.Vertical;
            }
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Executed after content is initialized, loops through any [TdStepComponent] children elements,
     * assigns them a number and subscribes as an observer to their [onActivated] event.
     * @return {?}
     */
    TdStepsComponent.prototype.ngAfterContentInit = function () {
        this._registerSteps();
    };
    /**
     * Unsubscribes from [TdStepComponent] children elements when component is destroyed.
     * @return {?}
     */
    TdStepsComponent.prototype.ngOnDestroy = function () {
        this._deregisterSteps();
    };
    /**
     * Returns 'true' if [mode] equals to [StepMode.Horizontal | 'horizontal'], else 'false'.
     * @return {?}
     */
    TdStepsComponent.prototype.isHorizontal = function () {
        return this._mode === StepMode.Horizontal;
    };
    /**
     * Returns 'true' if [mode] equals to [StepMode.Vertical | 'vertical'], else 'false'.
     * @return {?}
     */
    TdStepsComponent.prototype.isVertical = function () {
        return this._mode === StepMode.Vertical;
    };
    /**
     * @return {?}
     */
    TdStepsComponent.prototype.areStepsActive = function () {
        return this._steps.filter(function (step) {
            return step.active;
        }).length > 0;
    };
    /**
     * Wraps previous and new [TdStepComponent] numbers in an object that implements [IStepChangeEvent]
     * and emits [onStepChange] event.
     * @param {?} step
     * @return {?}
     */
    TdStepsComponent.prototype._onStepSelection = function (step) {
        if (this.prevStep !== step) {
            var /** @type {?} */ prevStep = this.prevStep;
            this.prevStep = step;
            var /** @type {?} */ event = {
                newStep: step,
                prevStep: prevStep,
            };
            this._deactivateAllBut(step);
            this.onStepChange.emit(event);
        }
    };
    /**
     * Loops through [TdStepComponent] children elements and deactivates them ignoring the one passed as an argument.
     * @param {?} activeStep
     * @return {?}
     */
    TdStepsComponent.prototype._deactivateAllBut = function (activeStep) {
        this._steps.filter(function (step) { return step !== activeStep; })
            .forEach(function (step) {
            step.active = false;
        });
    };
    /**
     * @return {?}
     */
    TdStepsComponent.prototype._registerSteps = function () {
        var _this = this;
        this._subcriptions = [];
        this._steps.toArray().forEach(function (step) {
            var /** @type {?} */ subscription = step.onActivated.asObservable().subscribe(function () {
                _this._onStepSelection(step);
            });
            _this._subcriptions.push(subscription);
        });
    };
    /**
     * @return {?}
     */
    TdStepsComponent.prototype._deregisterSteps = function () {
        if (this._subcriptions) {
            this._subcriptions.forEach(function (subs) {
                subs.unsubscribe();
            });
            this._subcriptions = undefined;
        }
    };
    return TdStepsComponent;
}());
TdStepsComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-steps',
                styles: [".td-line-wrapper,\n.td-step{\n  position:relative; }\n.td-steps-header{\n  -webkit-box-sizing:border-box;\n          box-sizing:border-box;\n  display:-webkit-box;\n  display:-ms-flexbox;\n  display:flex;\n  -webkit-box-orient:horizontal;\n  -webkit-box-direction:normal;\n      -ms-flex-direction:row;\n          flex-direction:row; }\n.td-line-wrapper{\n  width:24px;\n  min-height:1px; }\n.td-horizontal-line{\n  border-bottom-width:1px;\n  border-bottom-style:solid;\n  height:1px;\n  position:relative;\n  top:36px;\n  min-width:15px;\n  -webkit-box-flex:1;\n      -ms-flex:1;\n          flex:1;\n  -webkit-box-sizing:border-box;\n          box-sizing:border-box; }\n  ::ng-deep :not([dir='rtl']) .td-horizontal-line{\n    left:-6px;\n    right:-3px; }\n  ::ng-deep [dir='rtl'] .td-horizontal-line{\n    left:-3px;\n    right:-6px; }\n.td-vertical-line{\n  position:absolute;\n  bottom:-16px;\n  top:-16px;\n  border-left-width:1px;\n  border-left-style:solid; }\n  ::ng-deep :not([dir='rtl']) .td-vertical-line{\n    left:20px;\n    right:auto; }\n  ::ng-deep [dir='rtl'] .td-vertical-line{\n    left:auto;\n    right:20px; }\n"],
                template: "<div *ngIf=\"isHorizontal()\" class=\"td-steps-header\">\n  <ng-template let-step let-index=\"index\" let-last=\"last\" ngFor [ngForOf]=\"steps\">\n    <td-step-header class=\"td-step-horizontal-header\"\n                    (keydown.enter)=\"step.open()\"\n                    [number]=\"index + 1\"\n                    [active]=\"step.active\"\n                    [disableRipple]=\"step.disableRipple\"\n                    [disabled]=\"step.disabled\"\n                    [state]=\"step.state\"\n                    (click)=\"step.open()\">\n      <ng-template td-step-header-label [cdkPortalHost]=\"step.stepLabel\"></ng-template>\n      <ng-template td-step-header-label [ngIf]=\"!step.stepLabel\">{{step.label}}</ng-template>\n      <ng-template td-step-header-sublabel [ngIf]=\"true\">{{step.sublabel | truncate:30}}</ng-template>\n    </td-step-header>\n    <span *ngIf=\"!last\" class=\"td-horizontal-line\"></span>\n  </ng-template>\n</div>\n<div *ngFor=\"let step of steps; let index = index; let last = last\" class=\"td-step\">\n  <td-step-header class=\"td-step-vertical-header\"\n                  (keydown.enter)=\"step.toggle()\"\n                  [number]=\"index + 1\"\n                  [active]=\"step.active\"\n                  [disabled]=\"step.disabled\"\n                  [disableRipple]=\"step.disableRipple\"\n                  [state]=\"step.state\"\n                  (click)=\"step.toggle()\"\n                  *ngIf=\"isVertical()\">\n    <ng-template td-step-header-label [cdkPortalHost]=\"step.stepLabel\"></ng-template>\n    <ng-template td-step-header-label [ngIf]=\"!step.stepLabel\">{{step.label}}</ng-template>\n    <ng-template td-step-header-sublabel [ngIf]=\"true\">{{step.sublabel}}</ng-template>\n  </td-step-header>\n  <ng-template [ngIf]=\"isVertical() || step.active || (!areStepsActive() && prevStep === step)\">\n    <td-step-body [active]=\"step.active\" [state]=\"step.state\">\n      <div *ngIf=\"isVertical()\" class=\"td-line-wrapper\">\n        <div *ngIf=\"!last\" class=\"td-vertical-line\"></div>\n      </div>\n      <ng-template td-step-body-content [cdkPortalHost]=\"step.stepContent\"></ng-template>\n      <ng-template td-step-body-actions [cdkPortalHost]=\"step.stepActions\"></ng-template>\n      <ng-template td-step-body-summary [cdkPortalHost]=\"step.stepSummary\"></ng-template>\n    </td-step-body>\n  </ng-template>\n</div>\n",
            },] },
];
/** @nocollapse */
TdStepsComponent.ctorParameters = function () { return []; };
TdStepsComponent.propDecorators = {
    "stepsContent": [{ type: core.ContentChildren, args: [TdStepComponent,] },],
    "mode": [{ type: core.Input, args: ['mode',] },],
    "onStepChange": [{ type: core.Output, args: ['stepChange',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdStepHeaderBase = /** @class */ (function () {
    function TdStepHeaderBase() {
    }
    return TdStepHeaderBase;
}());
/* tslint:disable-next-line */
var _TdStepHeaderMixinBase = common.mixinDisableRipple(common.mixinDisabled(TdStepHeaderBase));
var TdStepHeaderComponent = /** @class */ (function (_super) {
    __extends(TdStepHeaderComponent, _super);
    function TdStepHeaderComponent() {
        var _this = _super.apply(this, __spread(arguments)) || this;
        /**
         * state?: StepState or ['none' | 'required' | 'complete']
         * Sets styles for state of header.
         * Defaults to [StepState.None | 'none'].
         */
        _this.state = StepState.None;
        return _this;
    }
    /**
     * Returns 'true' if [state] equals to [StepState.Complete | 'complete'], else 'false'.
     * @return {?}
     */
    TdStepHeaderComponent.prototype.isComplete = function () {
        return this.state === StepState.Complete;
    };
    /**
     * Returns 'true' if [state] equals to [StepState.Required | 'required'], else 'false'.
     * @return {?}
     */
    TdStepHeaderComponent.prototype.isRequired = function () {
        return this.state === StepState.Required;
    };
    return TdStepHeaderComponent;
}(_TdStepHeaderMixinBase));
TdStepHeaderComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-step-header',
                inputs: ['disabled', 'disableRipple'],
                styles: [".td-step-header{\n  position:relative;\n  outline:none;\n  height:72px;\n  -webkit-box-orient:horizontal;\n  -webkit-box-direction:normal;\n      -ms-flex-direction:row;\n          flex-direction:row;\n  -webkit-box-sizing:border-box;\n          box-sizing:border-box;\n  display:-webkit-box;\n  display:-ms-flexbox;\n  display:flex;\n  -webkit-box-flex:1;\n      -ms-flex:1;\n          flex:1;\n  -webkit-box-pack:start;\n      -ms-flex-pack:start;\n          justify-content:start;\n  -webkit-box-align:center;\n      -ms-flex-align:center;\n          align-items:center;\n  -ms-flex-line-pack:center;\n      align-content:center;\n  max-width:100%; }\n  .td-step-header:hover:not(.mat-disabled){\n    cursor:pointer; }\n  .td-step-header mat-icon.td-edit-icon{\n    margin:0 8px; }\n  .td-step-header mat-icon.mat-warn{\n    font-size:24px;\n    height:24px;\n    width:24px; }\n  .td-step-header mat-icon.mat-complete{\n    position:relative;\n    left:-2px;\n    top:2px;\n    font-size:28px;\n    height:24px;\n    width:24px; }\n  .td-step-header .td-circle{\n    height:24px;\n    width:24px;\n    line-height:24px;\n    border-radius:99%;\n    text-align:center;\n    -webkit-box-flex:0;\n        -ms-flex:none;\n            flex:none; }\n    .td-step-header .td-circle mat-icon{\n      margin-top:2px;\n      font-weight:bold; }\n  .td-step-header .td-triangle > mat-icon{\n    font-size:25px; }\n  .td-step-header .td-complete{\n    font-size:0; }\n  ::ng-deep :not([dir='rtl']) .td-step-header .td-circle, ::ng-deep :not([dir='rtl'])\n  .td-step-header .td-triangle, ::ng-deep :not([dir='rtl'])\n  .td-step-header .td-complete{\n    margin-left:8px;\n    margin-right:0; }\n  ::ng-deep [dir='rtl'] .td-step-header .td-circle, ::ng-deep [dir='rtl']\n  .td-step-header .td-triangle, ::ng-deep [dir='rtl']\n  .td-step-header .td-complete{\n    margin-left:0;\n    margin-right:8px; }\n  .td-step-header .td-circle,\n  .td-step-header .td-complete{\n    font-size:14px; }\n  .td-step-header .td-step-label-wrapper{\n    padding-left:8px;\n    padding-right:8px; }\n  .td-step-header .td-step-header-separator{\n    -webkit-box-flex:1;\n        -ms-flex:1;\n            flex:1;\n    -webkit-box-sizing:border-box;\n            box-sizing:border-box; }\n"],
                template: "<div class=\"td-step-header\"\n      [class.mat-disabled]=\"disabled\"\n      matRipple\n      [matRippleDisabled]=\"disabled || disableRipple\"\n      [tabIndex]=\"disabled ? -1 : 0\">\n  <div class=\"td-circle\"\n      [class.mat-inactive]=\"(!active && !isComplete()) || disabled\"\n      [class.mat-active]=\"active && !disabled\"\n      *ngIf=\"!isRequired() && !isComplete()\">\n    <span *ngIf=\"(active || !isComplete())\">{{number || ''}}</span>\n  </div>\n  <div class=\"td-complete\" *ngIf=\"isComplete()\">\n    <mat-icon class=\"mat-complete\">check_circle</mat-icon>\n  </div>\n  <div class=\"td-triangle\"\n      [class.bg-muted]=\"disabled\"\n      *ngIf=\"isRequired()\">\n    <mat-icon class=\"mat-warn\">warning</mat-icon>\n  </div>\n  <div class=\"td-step-label-wrapper\"\n        [class.mat-inactive]=\"(!active && !isComplete()) || disabled\"\n        [class.mat-warn]=\"isRequired() && !disabled\">\n    <div class=\"td-step-label\">\n      <ng-content select=\"[td-step-header-label]\"></ng-content>\n    </div>\n    <div class=\"td-step-sublabel\">\n      <ng-content select=\"[td-step-header-sublabel]\"></ng-content>\n    </div>\n  </div>\n  <span class=\"td-step-header-separator\"></span>\n  <mat-icon class=\"td-edit-icon\" *ngIf=\"isComplete() && !active && !disabled\">mode_edit</mat-icon>\n</div>",
            },] },
];
/** @nocollapse */
TdStepHeaderComponent.ctorParameters = function () { return []; };
TdStepHeaderComponent.propDecorators = {
    "number": [{ type: core.Input, args: ['number',] },],
    "active": [{ type: core.Input, args: ['active',] },],
    "state": [{ type: core.Input, args: ['state',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdStepBodyComponent = /** @class */ (function () {
    function TdStepBodyComponent() {
        /**
         * state?: StepState or ['none' | 'required' | 'complete']
         * Sets styles for state of body.
         * Defaults to [StepState.None | 'none'].
         */
        this.state = StepState.None;
    }
    Object.defineProperty(TdStepBodyComponent.prototype, "hasContent", {
        /**
         * @return {?}
         */
        get: function () {
            return this.contentRef &&
                (this.contentRef.nativeElement.children.length > 0 || !!this.contentRef.nativeElement.textContent.trim());
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdStepBodyComponent.prototype, "hasActions", {
        /**
         * @return {?}
         */
        get: function () {
            return this.actionsRef &&
                (this.actionsRef.nativeElement.children.length > 0 || !!this.actionsRef.nativeElement.textContent.trim());
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdStepBodyComponent.prototype, "hasSummary", {
        /**
         * @return {?}
         */
        get: function () {
            return this.summaryRef &&
                (this.summaryRef.nativeElement.children.length > 0 || !!this.summaryRef.nativeElement.textContent.trim());
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Returns 'true' if [state] equals to [StepState.Complete | 'complete'], else 'false'.
     * @return {?}
     */
    TdStepBodyComponent.prototype.isComplete = function () {
        return this.state === StepState.Complete;
    };
    return TdStepBodyComponent;
}());
TdStepBodyComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-step-body',
                styles: [":host{\n  -webkit-box-sizing:border-box;\n          box-sizing:border-box;\n  display:-webkit-box;\n  display:-ms-flexbox;\n  display:flex;\n  -webkit-box-orient:horizontal;\n  -webkit-box-direction:normal;\n      -ms-flex-direction:row;\n          flex-direction:row; }\n  :host .td-step-body{\n    overflow-x:hidden;\n    -webkit-box-flex:1;\n        -ms-flex:1;\n            flex:1;\n    -webkit-box-sizing:border-box;\n            box-sizing:border-box; }\n    :host .td-step-body .td-step-summary.ng-animating,\n    :host .td-step-body .td-step-content-wrapper.ng-animating{\n      overflow:hidden; }\n    :host .td-step-body .td-step-content{\n      overflow-x:auto; }\n    :host .td-step-body .td-step-actions{\n      -webkit-box-sizing:border-box;\n              box-sizing:border-box;\n      display:-webkit-box;\n      display:-ms-flexbox;\n      display:flex;\n      -webkit-box-orient:horizontal;\n      -webkit-box-direction:normal;\n          -ms-flex-direction:row;\n              flex-direction:row; }\n"],
                template: "<ng-content></ng-content>\n<div class=\"td-step-body\">\n  <div class=\"td-step-content-wrapper\"\n       [@tdCollapse]=\"!active\">\n    <div #contentRef cdkScrollable [class.td-step-content]=\"hasContent\">\n      <ng-content select=\"[td-step-body-content]\"></ng-content>\n    </div>\n    <div #actionsRef\n         [class.td-step-actions]=\"hasActions\">\n      <ng-content select=\"[td-step-body-actions]\"></ng-content>\n    </div>\n  </div>\n  <div #summaryRef\n       [@tdCollapse]=\"active || !isComplete()\"\n       [class.td-step-summary]=\"hasSummary\">\n    <ng-content select=\"[td-step-body-summary]\"></ng-content>\n  </div>\n</div>",
                animations: [
                    common.TdCollapseAnimation(),
                ],
            },] },
];
/** @nocollapse */
TdStepBodyComponent.ctorParameters = function () { return []; };
TdStepBodyComponent.propDecorators = {
    "contentRef": [{ type: core.ViewChild, args: ['contentRef', { read: core.ElementRef },] },],
    "actionsRef": [{ type: core.ViewChild, args: ['actionsRef', { read: core.ElementRef },] },],
    "summaryRef": [{ type: core.ViewChild, args: ['summaryRef', { read: core.ElementRef },] },],
    "active": [{ type: core.Input, args: ['active',] },],
    "state": [{ type: core.Input, args: ['state',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TD_STEPS = [
    TdStepsComponent,
    TdStepComponent,
    TdStepHeaderComponent,
    TdStepBodyComponent,
    TdStepLabelDirective,
    TdStepActionsDirective,
    TdStepSummaryDirective,
];
var CovalentStepsModule = /** @class */ (function () {
    function CovalentStepsModule() {
    }
    return CovalentStepsModule;
}());
CovalentStepsModule.decorators = [
    { type: core.NgModule, args: [{
                imports: [
                    common$1.CommonModule,
                    icon.MatIconModule,
                    core$1.MatRippleModule,
                    portal.PortalModule,
                    scrolling.ScrollDispatchModule,
                    common.CovalentCommonModule,
                ],
                declarations: [
                    TD_STEPS,
                ],
                exports: [
                    TD_STEPS,
                ],
            },] },
];
/** @nocollapse */
CovalentStepsModule.ctorParameters = function () { return []; };

exports.CovalentStepsModule = CovalentStepsModule;
exports.StepState = StepState;
exports.TdStepLabelDirective = TdStepLabelDirective;
exports.TdStepActionsDirective = TdStepActionsDirective;
exports.TdStepSummaryDirective = TdStepSummaryDirective;
exports.TdStepBase = TdStepBase;
exports._TdStepMixinBase = _TdStepMixinBase;
exports.TdStepComponent = TdStepComponent;
exports.StepMode = StepMode;
exports.TdStepsComponent = TdStepsComponent;
exports.TdStepBodyComponent = TdStepBodyComponent;
exports.TdStepHeaderBase = TdStepHeaderBase;
exports._TdStepHeaderMixinBase = _TdStepHeaderMixinBase;
exports.TdStepHeaderComponent = TdStepHeaderComponent;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=covalent-core-steps.umd.js.map
