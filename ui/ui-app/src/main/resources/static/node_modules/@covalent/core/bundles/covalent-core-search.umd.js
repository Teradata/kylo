(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('@angular/animations'), require('@angular/forms'), require('@angular/cdk/bidi'), require('@angular/material/input'), require('rxjs/operators/debounceTime'), require('rxjs/operators/skip'), require('@covalent/core/common'), require('@angular/common'), require('@angular/material/icon'), require('@angular/material/button')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', '@angular/animations', '@angular/forms', '@angular/cdk/bidi', '@angular/material/input', 'rxjs/operators/debounceTime', 'rxjs/operators/skip', '@covalent/core/common', '@angular/common', '@angular/material/icon', '@angular/material/button'], factory) :
	(factory((global.covalent = global.covalent || {}, global.covalent.core = global.covalent.core || {}, global.covalent.core.search = {}),global.ng.core,global.ng.animations,global.ng.forms,global.ng.cdk.bidi,global.ng.material.input,global.Rx.Observable.prototype,global.Rx.Observable.prototype,global.covalent.core.common,global.ng.common,global.ng.material.icon,global.ng.material.button));
}(this, (function (exports,core,animations,forms,bidi,input,debounceTime,skip,common,common$1,icon,button) { 'use strict';

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
var TdSearchInputBase = /** @class */ (function () {
    /**
     * @param {?} _changeDetectorRef
     */
    function TdSearchInputBase(_changeDetectorRef) {
        this._changeDetectorRef = _changeDetectorRef;
    }
    return TdSearchInputBase;
}());
/* tslint:disable-next-line */
var _TdSearchInputMixinBase = common.mixinControlValueAccessor(TdSearchInputBase);
var TdSearchInputComponent = /** @class */ (function (_super) {
    __extends(TdSearchInputComponent, _super);
    /**
     * @param {?} _dir
     * @param {?} _changeDetectorRef
     */
    function TdSearchInputComponent(_dir, _changeDetectorRef) {
        var _this = _super.call(this, _changeDetectorRef) || this;
        _this._dir = _dir;
        /**
         * showUnderline?: boolean
         * Sets if the input underline should be visible. Defaults to 'false'.
         */
        _this.showUnderline = false;
        /**
         * debounce?: number
         * Debounce timeout between keypresses. Defaults to 400.
         */
        _this.debounce = 400;
        /**
         * clearIcon?: string
         * The icon used to clear the search input.
         * Defaults to 'cancel' icon.
         */
        _this.clearIcon = 'cancel';
        /**
         * searchDebounce: function($event)
         * Event emitted after the [debounce] timeout.
         */
        _this.onSearchDebounce = new core.EventEmitter();
        /**
         * search: function($event)
         * Event emitted after the key enter has been pressed.
         */
        _this.onSearch = new core.EventEmitter();
        /**
         * clear: function()
         * Event emitted after the clear icon has been clicked.
         */
        _this.onClear = new core.EventEmitter();
        /**
         * blur: function()
         * Event emitted after the blur event has been called in underlying input.
         */
        _this.onBlur = new core.EventEmitter();
        return _this;
    }
    Object.defineProperty(TdSearchInputComponent.prototype, "isRTL", {
        /**
         * @return {?}
         */
        get: function () {
            if (this._dir) {
                return this._dir.dir === 'rtl';
            }
            return false;
        },
        enumerable: true,
        configurable: true
    });
    /**
     * @return {?}
     */
    TdSearchInputComponent.prototype.ngOnInit = function () {
        var _this = this;
        this._input.ngControl.valueChanges.pipe(debounceTime.debounceTime(this.debounce), skip.skip(1)).subscribe(function (value) {
            _this._searchTermChanged(value);
        });
    };
    /**
     * Method to focus to underlying input.
     * @return {?}
     */
    TdSearchInputComponent.prototype.focus = function () {
        this._input.focus();
    };
    /**
     * @return {?}
     */
    TdSearchInputComponent.prototype.handleBlur = function () {
        this.onBlur.emit(undefined);
    };
    /**
     * @param {?} event
     * @return {?}
     */
    TdSearchInputComponent.prototype.stopPropagation = function (event) {
        event.stopPropagation();
    };
    /**
     * @param {?} event
     * @return {?}
     */
    TdSearchInputComponent.prototype.handleSearch = function (event) {
        this.stopPropagation(event);
        this.onSearch.emit(this.value);
    };
    /**
     * Method to clear the underlying input.
     * @return {?}
     */
    TdSearchInputComponent.prototype.clearSearch = function () {
        this.value = '';
        this._changeDetectorRef.markForCheck();
        this.onClear.emit(undefined);
    };
    /**
     * @param {?} value
     * @return {?}
     */
    TdSearchInputComponent.prototype._searchTermChanged = function (value) {
        this.onSearchDebounce.emit(value);
    };
    return TdSearchInputComponent;
}(_TdSearchInputMixinBase));
TdSearchInputComponent.decorators = [
    { type: core.Component, args: [{
                providers: [{
                        provide: forms.NG_VALUE_ACCESSOR,
                        useExisting: core.forwardRef(function () { return TdSearchInputComponent; }),
                        multi: true,
                    }],
                selector: 'td-search-input',
                template: "<div class=\"td-search-input\">\n  <mat-form-field class=\"td-search-input-field\"\n                  [class.mat-hide-underline]=\"!showUnderline\"\n                  floatPlaceholder=\"never\">\n    <input matInput\n            #searchElement\n            type=\"search\"\n            [(ngModel)]=\"value\"\n            [placeholder]=\"placeholder\"\n            (blur)=\"handleBlur()\"\n            (search)=\"stopPropagation($event)\"\n            (keyup.enter)=\"handleSearch($event)\"/>\n  </mat-form-field>\n  <button mat-icon-button\n          class=\"td-search-input-clear\"\n          type=\"button\"\n          [@searchState]=\"(searchElement.value ?  'show' : (isRTL ? 'hide-left' : 'hide-right'))\"\n          (click)=\"clearSearch()\">\n    <mat-icon>{{clearIcon}}</mat-icon>\n  </button>\n</div>",
                styles: [".td-search-input{\n  overflow-x:hidden;\n  -webkit-box-sizing:border-box;\n          box-sizing:border-box;\n  display:-webkit-box;\n  display:-ms-flexbox;\n  display:flex;\n  -webkit-box-orient:horizontal;\n  -webkit-box-direction:normal;\n      -ms-flex-direction:row;\n          flex-direction:row;\n  -webkit-box-align:center;\n      -ms-flex-align:center;\n          align-items:center;\n  -ms-flex-line-pack:center;\n      align-content:center;\n  max-width:100%;\n  -webkit-box-pack:end;\n      -ms-flex-pack:end;\n          justify-content:flex-end; }\n  .td-search-input .td-search-input-field{\n    -webkit-box-flex:1;\n        -ms-flex:1;\n            flex:1; }\n  .td-search-input ::ng-deep mat-form-field.mat-hide-underline .mat-form-field-underline{\n    display:none; }\n  .td-search-input .td-search-input-clear{\n    -webkit-box-flex:0;\n        -ms-flex:0 0 auto;\n            flex:0 0 auto; }\n"],
                changeDetection: core.ChangeDetectionStrategy.OnPush,
                inputs: ['value'],
                animations: [
                    animations.trigger('searchState', [
                        animations.state('hide-left', animations.style({
                            transform: 'translateX(-150%)',
                            display: 'none',
                        })),
                        animations.state('hide-right', animations.style({
                            transform: 'translateX(150%)',
                            display: 'none',
                        })),
                        animations.state('show', animations.style({
                            transform: 'translateX(0%)',
                            display: 'block',
                        })),
                        animations.transition('* => show', animations.animate('200ms ease-in')),
                        animations.transition('show => *', animations.animate('200ms ease-out')),
                    ]),
                ],
            },] },
];
/** @nocollapse */
TdSearchInputComponent.ctorParameters = function () { return [
    { type: bidi.Dir, decorators: [{ type: core.Optional },] },
    { type: core.ChangeDetectorRef, },
]; };
TdSearchInputComponent.propDecorators = {
    "_input": [{ type: core.ViewChild, args: [input.MatInput,] },],
    "showUnderline": [{ type: core.Input, args: ['showUnderline',] },],
    "debounce": [{ type: core.Input, args: ['debounce',] },],
    "placeholder": [{ type: core.Input, args: ['placeholder',] },],
    "clearIcon": [{ type: core.Input, args: ['clearIcon',] },],
    "onSearchDebounce": [{ type: core.Output, args: ['searchDebounce',] },],
    "onSearch": [{ type: core.Output, args: ['search',] },],
    "onClear": [{ type: core.Output, args: ['clear',] },],
    "onBlur": [{ type: core.Output, args: ['blur',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdSearchBoxBase = /** @class */ (function () {
    /**
     * @param {?} _changeDetectorRef
     */
    function TdSearchBoxBase(_changeDetectorRef) {
        this._changeDetectorRef = _changeDetectorRef;
    }
    return TdSearchBoxBase;
}());
/* tslint:disable-next-line */
var _TdSearchBoxMixinBase = common.mixinControlValueAccessor(TdSearchBoxBase);
var TdSearchBoxComponent = /** @class */ (function (_super) {
    __extends(TdSearchBoxComponent, _super);
    /**
     * @param {?} _changeDetectorRef
     */
    function TdSearchBoxComponent(_changeDetectorRef) {
        var _this = _super.call(this, _changeDetectorRef) || this;
        _this._searchVisible = false;
        /**
         * backIcon?: string
         * The icon used to close the search toggle, only shown when [alwaysVisible] is false.
         * Defaults to 'search' icon.
         */
        _this.backIcon = 'search';
        /**
         * searchIcon?: string
         * The icon used to open/focus the search toggle.
         * Defaults to 'search' icon.
         */
        _this.searchIcon = 'search';
        /**
         * clearIcon?: string
         * The icon used to clear the search input.
         * Defaults to 'cancel' icon.
         */
        _this.clearIcon = 'cancel';
        /**
         * showUnderline?: boolean
         * Sets if the input underline should be visible. Defaults to 'false'.
         */
        _this.showUnderline = false;
        /**
         * debounce?: number
         * Debounce timeout between keypresses. Defaults to 400.
         */
        _this.debounce = 400;
        /**
         * alwaysVisible?: boolean
         * Sets if the input should always be visible. Defaults to 'false'.
         */
        _this.alwaysVisible = false;
        /**
         * searchDebounce: function($event)
         * Event emitted after the [debounce] timeout.
         */
        _this.onSearchDebounce = new core.EventEmitter();
        /**
         * search: function($event)
         * Event emitted after the key enter has been pressed.
         */
        _this.onSearch = new core.EventEmitter();
        /**
         * clear: function()
         * Event emitted after the clear icon has been clicked.
         */
        _this.onClear = new core.EventEmitter();
        return _this;
    }
    Object.defineProperty(TdSearchBoxComponent.prototype, "searchVisible", {
        /**
         * @return {?}
         */
        get: function () {
            return this._searchVisible;
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Method executed when the search icon is clicked.
     * @return {?}
     */
    TdSearchBoxComponent.prototype.searchClicked = function () {
        if (this.alwaysVisible || !this._searchVisible) {
            this._searchInput.focus();
        }
        this.toggleVisibility();
    };
    /**
     * @return {?}
     */
    TdSearchBoxComponent.prototype.toggleVisibility = function () {
        this._searchVisible = !this._searchVisible;
        this._changeDetectorRef.markForCheck();
    };
    /**
     * @param {?} value
     * @return {?}
     */
    TdSearchBoxComponent.prototype.handleSearchDebounce = function (value) {
        this.onSearchDebounce.emit(value);
    };
    /**
     * @param {?} value
     * @return {?}
     */
    TdSearchBoxComponent.prototype.handleSearch = function (value) {
        this.onSearch.emit(value);
    };
    /**
     * @return {?}
     */
    TdSearchBoxComponent.prototype.handleClear = function () {
        this.onClear.emit(undefined);
    };
    return TdSearchBoxComponent;
}(_TdSearchBoxMixinBase));
TdSearchBoxComponent.decorators = [
    { type: core.Component, args: [{
                providers: [{
                        provide: forms.NG_VALUE_ACCESSOR,
                        useExisting: core.forwardRef(function () { return TdSearchBoxComponent; }),
                        multi: true,
                    }],
                selector: 'td-search-box',
                template: "<div class=\"td-search-box\">\n  <button mat-icon-button type=\"button\" class=\"td-search-icon\" (click)=\"searchClicked()\">\n    <mat-icon *ngIf=\"searchVisible && !alwaysVisible\">{{backIcon}}</mat-icon>\n    <mat-icon *ngIf=\"!searchVisible || alwaysVisible\">{{searchIcon}}</mat-icon>\n  </button>\n  <td-search-input #searchInput\n                   [@inputState]=\"alwaysVisible || searchVisible\"\n                   [debounce]=\"debounce\"\n                   [(ngModel)]=\"value\"\n                   [showUnderline]=\"showUnderline\"\n                   [placeholder]=\"placeholder\"\n                   [clearIcon]=\"clearIcon\"\n                   (searchDebounce)=\"handleSearchDebounce($event)\"\n                   (search)=\"handleSearch($event)\"\n                   (clear)=\"handleClear(); toggleVisibility()\">\n  </td-search-input>\n</div>",
                styles: [":host{\n  display:block; }\n.td-search-box{\n  -webkit-box-sizing:border-box;\n          box-sizing:border-box;\n  display:-webkit-box;\n  display:-ms-flexbox;\n  display:flex;\n  -webkit-box-orient:horizontal;\n  -webkit-box-direction:normal;\n      -ms-flex-direction:row;\n          flex-direction:row;\n  -webkit-box-align:center;\n      -ms-flex-align:center;\n          align-items:center;\n  -ms-flex-line-pack:center;\n      align-content:center;\n  max-width:100%;\n  -webkit-box-pack:end;\n      -ms-flex-pack:end;\n          justify-content:flex-end; }\n  .td-search-box .td-search-icon{\n    -webkit-box-flex:0;\n        -ms-flex:0 0 auto;\n            flex:0 0 auto; }\n  .td-search-box td-search-input{\n    margin-left:12px; }\n    ::ng-deep [dir='rtl'] .td-search-box td-search-input{\n      margin-right:12px;\n      margin-left:0 !important; }\n"],
                changeDetection: core.ChangeDetectionStrategy.OnPush,
                inputs: ['value'],
                animations: [
                    animations.trigger('inputState', [
                        animations.state('0', animations.style({
                            width: '0%',
                            margin: '0px',
                        })),
                        animations.state('1', animations.style({
                            width: '100%',
                            margin: animations.AUTO_STYLE,
                        })),
                        animations.transition('0 => 1', animations.animate('200ms ease-in')),
                        animations.transition('1 => 0', animations.animate('200ms ease-out')),
                    ]),
                ],
            },] },
];
/** @nocollapse */
TdSearchBoxComponent.ctorParameters = function () { return [
    { type: core.ChangeDetectorRef, },
]; };
TdSearchBoxComponent.propDecorators = {
    "_searchInput": [{ type: core.ViewChild, args: [TdSearchInputComponent,] },],
    "backIcon": [{ type: core.Input, args: ['backIcon',] },],
    "searchIcon": [{ type: core.Input, args: ['searchIcon',] },],
    "clearIcon": [{ type: core.Input, args: ['clearIcon',] },],
    "showUnderline": [{ type: core.Input, args: ['showUnderline',] },],
    "debounce": [{ type: core.Input, args: ['debounce',] },],
    "alwaysVisible": [{ type: core.Input, args: ['alwaysVisible',] },],
    "placeholder": [{ type: core.Input, args: ['placeholder',] },],
    "onSearchDebounce": [{ type: core.Output, args: ['searchDebounce',] },],
    "onSearch": [{ type: core.Output, args: ['search',] },],
    "onClear": [{ type: core.Output, args: ['clear',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var CovalentSearchModule = /** @class */ (function () {
    function CovalentSearchModule() {
    }
    return CovalentSearchModule;
}());
CovalentSearchModule.decorators = [
    { type: core.NgModule, args: [{
                imports: [
                    forms.FormsModule,
                    common$1.CommonModule,
                    input.MatInputModule,
                    icon.MatIconModule,
                    button.MatButtonModule,
                ],
                declarations: [
                    TdSearchInputComponent,
                    TdSearchBoxComponent,
                ],
                exports: [
                    TdSearchInputComponent,
                    TdSearchBoxComponent,
                ],
            },] },
];
/** @nocollapse */
CovalentSearchModule.ctorParameters = function () { return []; };

exports.CovalentSearchModule = CovalentSearchModule;
exports.TdSearchBoxBase = TdSearchBoxBase;
exports._TdSearchBoxMixinBase = _TdSearchBoxMixinBase;
exports.TdSearchBoxComponent = TdSearchBoxComponent;
exports.TdSearchInputBase = TdSearchInputBase;
exports._TdSearchInputMixinBase = _TdSearchInputMixinBase;
exports.TdSearchInputComponent = TdSearchInputComponent;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=covalent-core-search.umd.js.map
