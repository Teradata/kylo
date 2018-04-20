(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('@angular/cdk/portal'), require('@angular/cdk/coercion'), require('@covalent/core/common'), require('@angular/common'), require('@angular/material/core'), require('@angular/material/icon')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', '@angular/cdk/portal', '@angular/cdk/coercion', '@covalent/core/common', '@angular/common', '@angular/material/core', '@angular/material/icon'], factory) :
	(factory((global.covalent = global.covalent || {}, global.covalent.core = global.covalent.core || {}, global.covalent.core['expansion-panel'] = {}),global.ng.core,global.ng.cdk.portal,global.ng.cdk.coercion,global.covalent.core.common,global.ng.common,global.ng.material.core,global.ng.material.icon));
}(this, (function (exports,core,portal,coercion,common,common$1,core$1,icon) { 'use strict';

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

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdExpansionPanelHeaderDirective = /** @class */ (function (_super) {
    __extends(TdExpansionPanelHeaderDirective, _super);
    /**
     * @param {?} templateRef
     * @param {?} viewContainerRef
     */
    function TdExpansionPanelHeaderDirective(templateRef, viewContainerRef) {
        return _super.call(this, templateRef, viewContainerRef) || this;
    }
    return TdExpansionPanelHeaderDirective;
}(portal.TemplatePortalDirective));
TdExpansionPanelHeaderDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[td-expansion-panel-header]ng-template',
            },] },
];
/** @nocollapse */
TdExpansionPanelHeaderDirective.ctorParameters = function () { return [
    { type: core.TemplateRef, },
    { type: core.ViewContainerRef, },
]; };
var TdExpansionPanelLabelDirective = /** @class */ (function (_super) {
    __extends(TdExpansionPanelLabelDirective, _super);
    /**
     * @param {?} templateRef
     * @param {?} viewContainerRef
     */
    function TdExpansionPanelLabelDirective(templateRef, viewContainerRef) {
        return _super.call(this, templateRef, viewContainerRef) || this;
    }
    return TdExpansionPanelLabelDirective;
}(portal.TemplatePortalDirective));
TdExpansionPanelLabelDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[td-expansion-panel-label]ng-template',
            },] },
];
/** @nocollapse */
TdExpansionPanelLabelDirective.ctorParameters = function () { return [
    { type: core.TemplateRef, },
    { type: core.ViewContainerRef, },
]; };
var TdExpansionPanelSublabelDirective = /** @class */ (function (_super) {
    __extends(TdExpansionPanelSublabelDirective, _super);
    /**
     * @param {?} templateRef
     * @param {?} viewContainerRef
     */
    function TdExpansionPanelSublabelDirective(templateRef, viewContainerRef) {
        return _super.call(this, templateRef, viewContainerRef) || this;
    }
    return TdExpansionPanelSublabelDirective;
}(portal.TemplatePortalDirective));
TdExpansionPanelSublabelDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[td-expansion-panel-sublabel]ng-template',
            },] },
];
/** @nocollapse */
TdExpansionPanelSublabelDirective.ctorParameters = function () { return [
    { type: core.TemplateRef, },
    { type: core.ViewContainerRef, },
]; };
var TdExpansionPanelSummaryComponent = /** @class */ (function () {
    function TdExpansionPanelSummaryComponent() {
    }
    return TdExpansionPanelSummaryComponent;
}());
TdExpansionPanelSummaryComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-expansion-summary',
                template: '<ng-content></ng-content>',
            },] },
];
/** @nocollapse */
TdExpansionPanelSummaryComponent.ctorParameters = function () { return []; };
var TdExpansionPanelBase = /** @class */ (function () {
    function TdExpansionPanelBase() {
    }
    return TdExpansionPanelBase;
}());
/* tslint:disable-next-line */
var _TdExpansionPanelMixinBase = common.mixinDisableRipple(common.mixinDisabled(TdExpansionPanelBase));
var TdExpansionPanelComponent = /** @class */ (function (_super) {
    __extends(TdExpansionPanelComponent, _super);
    /**
     * @param {?} _renderer
     * @param {?} _elementRef
     */
    function TdExpansionPanelComponent(_renderer, _elementRef) {
        var _this = _super.call(this) || this;
        _this._renderer = _renderer;
        _this._elementRef = _elementRef;
        _this._expand = false;
        /**
         * expanded?: function
         * Event emitted when [TdExpansionPanelComponent] is expanded.
         */
        _this.expanded = new core.EventEmitter();
        /**
         * collapsed?: function
         * Event emitted when [TdExpansionPanelComponent] is collapsed.
         */
        _this.collapsed = new core.EventEmitter();
        _this._renderer.addClass(_this._elementRef.nativeElement, 'td-expansion-panel');
        return _this;
    }
    Object.defineProperty(TdExpansionPanelComponent.prototype, "expand", {
        /**
         * @return {?}
         */
        get: function () {
            return this._expand;
        },
        /**
         * expand?: boolean
         * Toggles [TdExpansionPanelComponent] between expand/collapse.
         * @param {?} expand
         * @return {?}
         */
        set: function (expand) {
            this._setExpand(coercion.coerceBooleanProperty(expand));
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Method executed when [TdExpansionPanelComponent] is clicked.
     * @return {?}
     */
    TdExpansionPanelComponent.prototype.clickEvent = function () {
        this._setExpand(!this._expand);
    };
    /**
     * Toggle expand state of [TdExpansionPanelComponent]
     * retuns 'true' if successful, else 'false'.
     * @return {?}
     */
    TdExpansionPanelComponent.prototype.toggle = function () {
        return this._setExpand(!this._expand);
    };
    /**
     * Opens [TdExpansionPanelComponent]
     * retuns 'true' if successful, else 'false'.
     * @return {?}
     */
    TdExpansionPanelComponent.prototype.open = function () {
        return this._setExpand(true);
    };
    /**
     * Closes [TdExpansionPanelComponent]
     * retuns 'true' if successful, else 'false'.
     * @return {?}
     */
    TdExpansionPanelComponent.prototype.close = function () {
        return this._setExpand(false);
    };
    /**
     * Method executed when the disabled value changes
     * @param {?} v
     * @return {?}
     */
    TdExpansionPanelComponent.prototype.onDisabledChange = function (v) {
        if (v && this._expand) {
            this._expand = false;
            this._onCollapsed();
        }
    };
    /**
     * Method to change expand state internally and emit the [onExpanded] event if 'true' or [onCollapsed]
     * event if 'false'. (Blocked if [disabled] is 'true')
     * @param {?} newExpand
     * @return {?}
     */
    TdExpansionPanelComponent.prototype._setExpand = function (newExpand) {
        if (this.disabled) {
            return false;
        }
        if (this._expand !== newExpand) {
            this._expand = newExpand;
            if (newExpand) {
                this._renderer.addClass(this._elementRef.nativeElement, 'td-expanded');
                this._onExpanded();
            }
            else {
                this._renderer.removeClass(this._elementRef.nativeElement, 'td-expanded');
                this._onCollapsed();
            }
            return true;
        }
        return false;
    };
    /**
     * @return {?}
     */
    TdExpansionPanelComponent.prototype._onExpanded = function () {
        this.expanded.emit(undefined);
    };
    /**
     * @return {?}
     */
    TdExpansionPanelComponent.prototype._onCollapsed = function () {
        this.collapsed.emit(undefined);
    };
    return TdExpansionPanelComponent;
}(_TdExpansionPanelMixinBase));
TdExpansionPanelComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-expansion-panel',
                styles: [":host{\n  display:block; }\n  :host .td-expansion-panel-header{\n    position:relative;\n    outline:none; }\n    :host .td-expansion-panel-header:focus:not(.mat-disabled), :host .td-expansion-panel-header:hover:not(.mat-disabled){\n      cursor:pointer; }\n    :host .td-expansion-panel-header .td-expansion-panel-header-content{\n      height:48px;\n      padding:0 24px;\n      -webkit-box-sizing:border-box;\n              box-sizing:border-box;\n      display:-webkit-box;\n      display:-ms-flexbox;\n      display:flex;\n      -webkit-box-orient:horizontal;\n      -webkit-box-direction:normal;\n          -ms-flex-direction:row;\n              flex-direction:row;\n      -webkit-box-flex:1;\n          -ms-flex:1;\n              flex:1;\n      -webkit-box-pack:start;\n          -ms-flex-pack:start;\n              justify-content:start;\n      -webkit-box-align:center;\n          -ms-flex-align:center;\n              align-items:center;\n      -ms-flex-line-pack:center;\n          align-content:center;\n      max-width:100%; }\n      :host .td-expansion-panel-header .td-expansion-panel-header-content .td-expansion-label,\n      :host .td-expansion-panel-header .td-expansion-panel-header-content .td-expansion-sublabel{\n        -webkit-box-flex:1;\n            -ms-flex:1;\n                flex:1; }\n  :host .td-expansion-content.ng-animating,\n  :host .td-expansion-summary.ng-animating{\n    overflow:hidden; }\n.td-expansion-label,\n.td-expansion-sublabel{\n  white-space:nowrap;\n  overflow:hidden;\n  text-overflow:ellipsis;\n  margin-right:16px; }\n  ::ng-deep [dir='rtl'] .td-expansion-label, ::ng-deep [dir='rtl']\n  .td-expansion-sublabel{\n    margin-left:16px;\n    margin-right:inherit; }\n"],
                template: "<div class=\"td-expansion-panel-header\"\n      [class.mat-disabled]=\"disabled\"\n      matRipple\n      [matRippleDisabled]=\"disabled || disableRipple\"\n      [tabIndex]=\"disabled? -1 : 0\"\n      (keydown.enter)=\"clickEvent()\"\n      (click)=\"clickEvent()\">\n  <ng-template [cdkPortalHost]=\"expansionPanelHeader\"></ng-template>\n  <div class=\"td-expansion-panel-header-content\"\n        [class.mat-disabled]=\"disabled\"\n        *ngIf=\"!expansionPanelHeader\">\n    <div *ngIf=\"label || expansionPanelLabel\" class=\"td-expansion-label\">\n      <ng-template [cdkPortalHost]=\"expansionPanelLabel\"></ng-template>\n      <ng-template [ngIf]=\"!expansionPanelLabel\">{{label}}</ng-template>\n    </div>\n    <div *ngIf=\"sublabel || expansionPanelSublabel\" class=\"td-expansion-sublabel\">\n      <ng-template [cdkPortalHost]=\"expansionPanelSublabel\"></ng-template>\n      <ng-template [ngIf]=\"!expansionPanelSublabel\">{{sublabel}}</ng-template>\n    </div>\n    <mat-icon class=\"td-expand-icon\" *ngIf=\"!disabled\" [@tdRotate]=\"expand\">keyboard_arrow_down</mat-icon>\n  </div>\n</div>\n<div class=\"td-expansion-content\"\n      [@tdCollapse]=\"!expand\">\n  <ng-content></ng-content>\n</div>\n<div class=\"td-expansion-summary\"\n      [@tdCollapse]=\"expand\">\n  <ng-content select=\"td-expansion-summary\"></ng-content>\n</div>\n",
                inputs: ['disabled', 'disableRipple'],
                animations: [
                    common.TdCollapseAnimation(),
                    common.TdRotateAnimation({ anchor: 'tdRotate' }),
                ],
            },] },
];
/** @nocollapse */
TdExpansionPanelComponent.ctorParameters = function () { return [
    { type: core.Renderer2, },
    { type: core.ElementRef, },
]; };
TdExpansionPanelComponent.propDecorators = {
    "expansionPanelHeader": [{ type: core.ContentChild, args: [TdExpansionPanelHeaderDirective,] },],
    "expansionPanelLabel": [{ type: core.ContentChild, args: [TdExpansionPanelLabelDirective,] },],
    "expansionPanelSublabel": [{ type: core.ContentChild, args: [TdExpansionPanelSublabelDirective,] },],
    "label": [{ type: core.Input },],
    "sublabel": [{ type: core.Input },],
    "expand": [{ type: core.Input, args: ['expand',] },],
    "expanded": [{ type: core.Output },],
    "collapsed": [{ type: core.Output },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdExpansionPanelGroupComponent = /** @class */ (function () {
    /**
     * @param {?} _renderer
     * @param {?} _elementRef
     */
    function TdExpansionPanelGroupComponent(_renderer, _elementRef) {
        this._renderer = _renderer;
        this._elementRef = _elementRef;
        this._renderer.addClass(this._elementRef.nativeElement, 'td-expansion-panel-group');
    }
    return TdExpansionPanelGroupComponent;
}());
TdExpansionPanelGroupComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-expansion-panel-group',
                styles: [""],
                template: "<ng-content></ng-content>",
            },] },
];
/** @nocollapse */
TdExpansionPanelGroupComponent.ctorParameters = function () { return [
    { type: core.Renderer2, },
    { type: core.ElementRef, },
]; };
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TD_EXPANSION_PANEL = [
    TdExpansionPanelGroupComponent,
    TdExpansionPanelComponent,
    TdExpansionPanelHeaderDirective,
    TdExpansionPanelLabelDirective,
    TdExpansionPanelSublabelDirective,
    TdExpansionPanelSummaryComponent,
];
var CovalentExpansionPanelModule = /** @class */ (function () {
    function CovalentExpansionPanelModule() {
    }
    return CovalentExpansionPanelModule;
}());
CovalentExpansionPanelModule.decorators = [
    { type: core.NgModule, args: [{
                imports: [
                    common$1.CommonModule,
                    core$1.MatRippleModule,
                    icon.MatIconModule,
                    portal.PortalModule,
                ],
                declarations: [
                    TD_EXPANSION_PANEL,
                ],
                exports: [
                    TD_EXPANSION_PANEL,
                ],
            },] },
];
/** @nocollapse */
CovalentExpansionPanelModule.ctorParameters = function () { return []; };

exports.CovalentExpansionPanelModule = CovalentExpansionPanelModule;
exports.TdExpansionPanelHeaderDirective = TdExpansionPanelHeaderDirective;
exports.TdExpansionPanelLabelDirective = TdExpansionPanelLabelDirective;
exports.TdExpansionPanelSublabelDirective = TdExpansionPanelSublabelDirective;
exports.TdExpansionPanelSummaryComponent = TdExpansionPanelSummaryComponent;
exports.TdExpansionPanelBase = TdExpansionPanelBase;
exports._TdExpansionPanelMixinBase = _TdExpansionPanelMixinBase;
exports.TdExpansionPanelComponent = TdExpansionPanelComponent;
exports.TdExpansionPanelGroupComponent = TdExpansionPanelGroupComponent;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=covalent-core-expansion-panel.umd.js.map
