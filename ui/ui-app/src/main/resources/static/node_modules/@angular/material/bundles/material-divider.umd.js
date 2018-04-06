/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('@angular/cdk/coercion'), require('@angular/common'), require('@angular/material/core')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', '@angular/cdk/coercion', '@angular/common', '@angular/material/core'], factory) :
	(factory((global.ng = global.ng || {}, global.ng.material = global.ng.material || {}, global.ng.material.divider = global.ng.material.divider || {}),global.ng.core,global.ng.cdk.coercion,global.ng.common,global.ng.material.core));
}(this, (function (exports,_angular_core,_angular_cdk_coercion,_angular_common,_angular_material_core) { 'use strict';

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */

var MatDivider = /** @class */ (function () {
    function MatDivider() {
        this._vertical = false;
        this._inset = false;
    }
    Object.defineProperty(MatDivider.prototype, "vertical", {
        get: /**
         * Whether the divider is vertically aligned.
         * @return {?}
         */
        function () { return this._vertical; },
        set: /**
         * @param {?} value
         * @return {?}
         */
        function (value) { this._vertical = _angular_cdk_coercion.coerceBooleanProperty(value); },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(MatDivider.prototype, "inset", {
        get: /**
         * Whether the divider is an inset divider.
         * @return {?}
         */
        function () { return this._inset; },
        set: /**
         * @param {?} value
         * @return {?}
         */
        function (value) { this._inset = _angular_cdk_coercion.coerceBooleanProperty(value); },
        enumerable: true,
        configurable: true
    });
    MatDivider.decorators = [
        { type: _angular_core.Component, args: [{selector: 'mat-divider',
                    host: {
                        'role': 'separator',
                        '[attr.aria-orientation]': 'vertical ? "vertical" : "horizontal"',
                        '[class.mat-divider-vertical]': 'vertical',
                        '[class.mat-divider-inset]': 'inset',
                        'class': 'mat-divider'
                    },
                    template: '',
                    styles: [".mat-divider{display:block;margin:0;border-top-width:1px;border-top-style:solid}.mat-divider.mat-divider-vertical{border-top:0;border-right-width:1px;border-right-style:solid}.mat-divider.mat-divider-inset{margin-left:80px}[dir=rtl] .mat-divider.mat-divider-inset{margin-left:auto;margin-right:80px}"],
                    encapsulation: _angular_core.ViewEncapsulation.None,
                    changeDetection: _angular_core.ChangeDetectionStrategy.OnPush,
                    preserveWhitespaces: false,
                },] },
    ];
    /** @nocollapse */
    MatDivider.ctorParameters = function () { return []; };
    MatDivider.propDecorators = {
        "vertical": [{ type: _angular_core.Input },],
        "inset": [{ type: _angular_core.Input },],
    };
    return MatDivider;
}());

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */

var MatDividerModule = /** @class */ (function () {
    function MatDividerModule() {
    }
    MatDividerModule.decorators = [
        { type: _angular_core.NgModule, args: [{
                    imports: [_angular_material_core.MatCommonModule, _angular_common.CommonModule],
                    exports: [
                        MatDivider,
                        _angular_material_core.MatCommonModule,
                    ],
                    declarations: [
                        MatDivider,
                    ],
                },] },
    ];
    /** @nocollapse */
    MatDividerModule.ctorParameters = function () { return []; };
    return MatDividerModule;
}());

exports.MatDivider = MatDivider;
exports.MatDividerModule = MatDividerModule;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=material-divider.umd.js.map
