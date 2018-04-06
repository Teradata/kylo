(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('@angular/cdk/bidi'), require('@covalent/core/common'), require('@angular/common'), require('@angular/material/tooltip'), require('@angular/material/icon')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', '@angular/cdk/bidi', '@covalent/core/common', '@angular/common', '@angular/material/tooltip', '@angular/material/icon'], factory) :
	(factory((global.covalent = global.covalent || {}, global.covalent.core = global.covalent.core || {}, global.covalent.core['json-formatter'] = {}),global.ng.core,global.ng.cdk.bidi,global.covalent.core.common,global.ng.common,global.ng.material.tooltip,global.ng.material.icon));
}(this, (function (exports,core,bidi,common,common$1,tooltip,icon) { 'use strict';

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdJsonFormatterComponent = /** @class */ (function () {
    /**
     * @param {?} _changeDetectorRef
     * @param {?} _dir
     */
    function TdJsonFormatterComponent(_changeDetectorRef, _dir) {
        this._changeDetectorRef = _changeDetectorRef;
        this._dir = _dir;
        this._open = false;
        this._levelsOpen = 0;
    }
    Object.defineProperty(TdJsonFormatterComponent.prototype, "levelsOpen", {
        /**
         * @return {?}
         */
        get: function () {
            return this._levelsOpen;
        },
        /**
         * levelsOpen?: number
         * Levels opened by default when JS object is formatted and rendered.
         * @param {?} levelsOpen
         * @return {?}
         */
        set: function (levelsOpen) {
            if (!Number.isInteger(levelsOpen)) {
                throw new Error('[levelsOpen] needs to be an integer.');
            }
            this._levelsOpen = levelsOpen;
            this._open = levelsOpen > 0;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdJsonFormatterComponent.prototype, "open", {
        /**
         * @return {?}
         */
        get: function () {
            return this._open;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdJsonFormatterComponent.prototype, "key", {
        /**
         * @return {?}
         */
        get: function () {
            var /** @type {?} */ elipsis = this._key && this._key.length > TdJsonFormatterComponent.KEY_MAX_LENGTH ? '…' : '';
            return this._key ? this._key.substring(0, TdJsonFormatterComponent.KEY_MAX_LENGTH) + elipsis : this._key;
        },
        /**
         * key?: string
         * Tag to be displayed next to formatted object.
         * @param {?} key
         * @return {?}
         */
        set: function (key) {
            this._key = key;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdJsonFormatterComponent.prototype, "data", {
        /**
         * @return {?}
         */
        get: function () {
            return this._data;
        },
        /**
         * data: any
         * JS object to be formatted.
         * @param {?} data
         * @return {?}
         */
        set: function (data) {
            this._data = data;
            this.parseChildren();
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdJsonFormatterComponent.prototype, "children", {
        /**
         * @return {?}
         */
        get: function () {
            return this._children;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdJsonFormatterComponent.prototype, "isRTL", {
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
     * Refreshes json-formatter and rerenders [data]
     * @return {?}
     */
    TdJsonFormatterComponent.prototype.refresh = function () {
        this._changeDetectorRef.markForCheck();
    };
    /**
     * Toggles collapse/expanded state of component.
     * @return {?}
     */
    TdJsonFormatterComponent.prototype.toggle = function () {
        this._open = !this._open;
    };
    /**
     * @return {?}
     */
    TdJsonFormatterComponent.prototype.isObject = function () {
        return this.getType(this._data) === 'object';
    };
    /**
     * @return {?}
     */
    TdJsonFormatterComponent.prototype.isArray = function () {
        return Array.isArray(this._data);
    };
    /**
     * @return {?}
     */
    TdJsonFormatterComponent.prototype.hasChildren = function () {
        return this._children && this._children.length > 0;
    };
    /**
     * Gets parsed value depending on value type.
     * @param {?} value
     * @return {?}
     */
    TdJsonFormatterComponent.prototype.getValue = function (value) {
        var /** @type {?} */ type = this.getType(value);
        if (type === 'undefined' || (type === 'null')) {
            return type;
        }
        else if (type === 'date') {
            value = new Date(value).toString();
        }
        else if (type === 'string') {
            value = '"' + value + '"';
        }
        else if (type === 'function') {
            // Remove content of the function
            return value.toString()
                .replace(/[\r\n]/g, '')
                .replace(/\{.*\}/, '') + '{…}';
        }
        else if (Array.isArray(value)) {
            return this.getObjectName() + ' [' + value.length + ']';
        }
        return value;
    };
    /**
     * Gets type of object.
     * returns 'null' if object is null and 'date' if value is object and can be parsed to a date.
     * @param {?} object
     * @return {?}
     */
    TdJsonFormatterComponent.prototype.getType = function (object) {
        if (typeof object === 'object') {
            if (!object) {
                return 'null';
            }
            if (Array.isArray(object)) {
                return 'object';
            }
            var /** @type {?} */ date = new Date(object);
            if (Object.prototype.toString.call(date) === '[object Date]') {
                if (!Number.isNaN(date.getTime())) {
                    return 'date';
                }
            }
        }
        return typeof object;
    };
    /**
     * Generates string representation depending if its an object or function.
     * see: http://stackoverflow.com/a/332429
     * @return {?}
     */
    TdJsonFormatterComponent.prototype.getObjectName = function () {
        var /** @type {?} */ object = this._data;
        if (this.isObject() && !object.constructor) {
            return 'Object';
        }
        var /** @type {?} */ funcNameRegex = /function (.{1,})\(/;
        var /** @type {?} */ results = (funcNameRegex).exec((object).constructor.toString());
        if (results && results.length > 1) {
            return results[1];
        }
        else {
            return '';
        }
    };
    /**
     * Creates preview of nodes children to render in tooltip depending if its an array or an object.
     * @return {?}
     */
    TdJsonFormatterComponent.prototype.getPreview = function () {
        var _this = this;
        var /** @type {?} */ previewData;
        var /** @type {?} */ startChar = '{ ';
        var /** @type {?} */ endChar = ' }';
        if (this.isArray()) {
            var /** @type {?} */ previewArray = this._data.slice(0, TdJsonFormatterComponent.PREVIEW_LIMIT);
            previewData = previewArray.map(function (obj) {
                return _this.getValue(obj);
            });
            startChar = '[';
            endChar = ']';
        }
        else {
            var /** @type {?} */ previewKeys = this._children.slice(0, TdJsonFormatterComponent.PREVIEW_LIMIT);
            previewData = previewKeys.map(function (key) {
                return key + ': ' + _this.getValue(_this._data[key]);
            });
        }
        var /** @type {?} */ previewString = previewData.join(', ');
        var /** @type {?} */ ellipsis = previewData.length >= TdJsonFormatterComponent.PREVIEW_LIMIT ||
            previewString.length > TdJsonFormatterComponent.PREVIEW_STRING_MAX_LENGTH ? '…' : '';
        return startChar + previewString.substring(0, TdJsonFormatterComponent.PREVIEW_STRING_MAX_LENGTH) +
            ellipsis + endChar;
    };
    /**
     * @return {?}
     */
    TdJsonFormatterComponent.prototype.parseChildren = function () {
        if (this.isObject()) {
            this._children = [];
            for (var /** @type {?} */ key in this._data) {
                this._children.push(key);
            }
        }
    };
    return TdJsonFormatterComponent;
}());
/**
 * Max length for property names. Any names bigger than this get trunctated.
 */
TdJsonFormatterComponent.KEY_MAX_LENGTH = 30;
/**
 * Max length for preview string. Any names bigger than this get trunctated.
 */
TdJsonFormatterComponent.PREVIEW_STRING_MAX_LENGTH = 80;
/**
 * Max tooltip preview elements.
 */
TdJsonFormatterComponent.PREVIEW_LIMIT = 5;
TdJsonFormatterComponent.decorators = [
    { type: core.Component, args: [{
                changeDetection: core.ChangeDetectionStrategy.OnPush,
                selector: 'td-json-formatter',
                styles: [":host{\n  display:block; }\n.td-json-formatter-wrapper{\n  padding-top:2px;\n  padding-bottom:2px; }\n  .td-json-formatter-wrapper .td-key{\n    -webkit-box-sizing:border-box;\n            box-sizing:border-box;\n    display:-webkit-box;\n    display:-ms-flexbox;\n    display:flex;\n    -webkit-box-orient:horizontal;\n    -webkit-box-direction:normal;\n        -ms-flex-direction:row;\n            flex-direction:row;\n    -webkit-box-align:center;\n        -ms-flex-align:center;\n            align-items:center;\n    -ms-flex-line-pack:center;\n        align-content:center;\n    max-width:100%;\n    -webkit-box-pack:start;\n        -ms-flex-pack:start;\n            justify-content:start; }\n    .td-json-formatter-wrapper .td-key.td-key-node:hover{\n      cursor:pointer; }\n  .td-json-formatter-wrapper .td-object-children.ng-animating{\n    overflow:hidden; }\n  .td-json-formatter-wrapper .td-object-children .td-key,\n  .td-json-formatter-wrapper .td-object-children .td-object-children{\n    padding-left:24px; }\n    ::ng-deep [dir='rtl'] .td-json-formatter-wrapper .td-object-children .td-key, ::ng-deep [dir='rtl']\n    .td-json-formatter-wrapper .td-object-children .td-object-children{\n      padding-right:24px;\n      padding-left:0; }\n    .td-json-formatter-wrapper .td-object-children .td-key.td-key-leaf,\n    .td-json-formatter-wrapper .td-object-children .td-object-children.td-key-leaf{\n      padding-left:48px; }\n      ::ng-deep [dir='rtl'] .td-json-formatter-wrapper .td-object-children .td-key.td-key-leaf, ::ng-deep [dir='rtl']\n      .td-json-formatter-wrapper .td-object-children .td-object-children.td-key-leaf{\n        padding-right:48px;\n        padding-left:0; }\n  .td-json-formatter-wrapper .value{\n    margin-left:5px; }\n    ::ng-deep [dir='rtl'] .td-json-formatter-wrapper .value{\n      padding-right:5px;\n      padding-left:0; }\n    .td-json-formatter-wrapper .value .td-empty{\n      opacity:0.5;\n      text-decoration:line-through; }\n    .td-json-formatter-wrapper .value .string{\n      word-break:break-word; }\n    .td-json-formatter-wrapper .value .date{\n      word-break:break-word; }\n"],
                template: "<div class=\"td-json-formatter-wrapper\">\n  <a class=\"td-key\"\n     [class.td-key-node]=\"hasChildren()\"\n     [class.td-key-leaf]=\"!hasChildren()\"\n     [tabIndex]=\"isObject()? 0 : -1\"\n     (keydown.enter)=\"toggle()\"\n     (click)=\"toggle()\">\n    <mat-icon class=\"td-node-icon\" *ngIf=\"hasChildren()\">{{open? 'keyboard_arrow_down' : (isRTL ? 'keyboard_arrow_left' : 'keyboard_arrow_right')}}</mat-icon>\n    <span *ngIf=\"key\" class=\"key\">{{key}}:</span>\n    <span class=\"value\">\n      <span [class.td-empty]=\"!hasChildren()\" *ngIf=\"isObject()\" [matTooltip]=\"getPreview()\" matTooltipPosition=\"after\">\n        <span class=\"td-object-name\">{{getObjectName()}}</span>\n        <span class=\"td-array-length\" *ngIf=\"isArray()\">[{{data.length}}]</span>\n      </span>\n      <span *ngIf=\"!isObject()\" [class]=\"getType(data)\">{{getValue(data)}}</span>\n    </span>\n  </a>\n  <div class=\"td-object-children\" [@tdCollapse]=\"!(hasChildren() && open)\">\n    <ng-template let-key ngFor [ngForOf]=\"children\">\n      <td-json-formatter [key]=\"key\" [data]=\"data[key]\" [levelsOpen]=\"levelsOpen - 1\"></td-json-formatter>\n    </ng-template>\n  </div>\n</div>",
                animations: [
                    common.TdCollapseAnimation(),
                ],
            },] },
];
/** @nocollapse */
TdJsonFormatterComponent.ctorParameters = function () { return [
    { type: core.ChangeDetectorRef, },
    { type: bidi.Dir, decorators: [{ type: core.Optional },] },
]; };
TdJsonFormatterComponent.propDecorators = {
    "levelsOpen": [{ type: core.Input, args: ['levelsOpen',] },],
    "key": [{ type: core.Input, args: ['key',] },],
    "data": [{ type: core.Input, args: ['data',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var CovalentJsonFormatterModule = /** @class */ (function () {
    function CovalentJsonFormatterModule() {
    }
    return CovalentJsonFormatterModule;
}());
CovalentJsonFormatterModule.decorators = [
    { type: core.NgModule, args: [{
                imports: [
                    common$1.CommonModule,
                    tooltip.MatTooltipModule,
                    icon.MatIconModule,
                ],
                declarations: [
                    TdJsonFormatterComponent,
                ],
                exports: [
                    TdJsonFormatterComponent,
                ],
            },] },
];
/** @nocollapse */
CovalentJsonFormatterModule.ctorParameters = function () { return []; };

exports.CovalentJsonFormatterModule = CovalentJsonFormatterModule;
exports.TdJsonFormatterComponent = TdJsonFormatterComponent;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=covalent-core-json-formatter.umd.js.map
