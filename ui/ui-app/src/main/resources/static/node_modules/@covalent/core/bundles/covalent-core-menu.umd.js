(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('@angular/common'), require('@angular/material/menu'), require('@angular/material/divider')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', '@angular/common', '@angular/material/menu', '@angular/material/divider'], factory) :
	(factory((global.covalent = global.covalent || {}, global.covalent.core = global.covalent.core || {}, global.covalent.core.menu = {}),global.ng.core,global.ng.common,global.ng.material.menu,global.ng.material.divider));
}(this, (function (exports,core,common,menu,divider) { 'use strict';

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdMenuComponent = /** @class */ (function () {
    function TdMenuComponent() {
    }
    return TdMenuComponent;
}());
TdMenuComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-menu',
                template: "<ng-content select=\"[td-menu-header]\"></ng-content>\n<mat-divider></mat-divider>\n<div class=\"td-menu-content\">\n  <ng-content></ng-content>\n</div>\n<mat-divider></mat-divider>\n<ng-content select=\"[td-menu-footer]\"></ng-content>",
                styles: [":host{\n  margin-top:-8px;\n  margin-bottom:-8px;\n  -webkit-box-sizing:border-box;\n          box-sizing:border-box;\n  display:-webkit-box;\n  display:-ms-flexbox;\n  display:flex;\n  -webkit-box-orient:vertical;\n  -webkit-box-direction:normal;\n      -ms-flex-direction:column;\n          flex-direction:column; }\n:host ::ng-deep [td-menu-header]{\n  padding:8px;\n  text-align:center; }\n:host ::ng-deep mat-list a[mat-list-item].mat-2-line,\n:host ::ng-deep mat-list mat-list-item.mat-2-line,\n:host ::ng-deep mat-list[dense] a[mat-list-item].mat-2-line,\n:host ::ng-deep mat-list[dense] mat-list-item.mat-2-line,\n:host ::ng-deep mat-nav-list a[mat-list-item].mat-2-line,\n:host ::ng-deep mat-nav-list mat-list-item.mat-2-line,\n:host ::ng-deep mat-nav-list[dense] a[mat-list-item].mat-2-line,\n:host ::ng-deep mat-nav-list[dense] mat-list-item.mat-2-line{\n  height:auto; }\n  :host ::ng-deep mat-list a[mat-list-item].mat-2-line .mat-list-item-content,\n  :host ::ng-deep mat-list mat-list-item.mat-2-line .mat-list-item-content,\n  :host ::ng-deep mat-list[dense] a[mat-list-item].mat-2-line .mat-list-item-content,\n  :host ::ng-deep mat-list[dense] mat-list-item.mat-2-line .mat-list-item-content,\n  :host ::ng-deep mat-nav-list a[mat-list-item].mat-2-line .mat-list-item-content,\n  :host ::ng-deep mat-nav-list mat-list-item.mat-2-line .mat-list-item-content,\n  :host ::ng-deep mat-nav-list[dense] a[mat-list-item].mat-2-line .mat-list-item-content,\n  :host ::ng-deep mat-nav-list[dense] mat-list-item.mat-2-line .mat-list-item-content{\n    height:auto;\n    padding:8px; }\n    :host ::ng-deep mat-list a[mat-list-item].mat-2-line .mat-list-item-content .mat-list-text,\n    :host ::ng-deep mat-list mat-list-item.mat-2-line .mat-list-item-content .mat-list-text,\n    :host ::ng-deep mat-list[dense] a[mat-list-item].mat-2-line .mat-list-item-content .mat-list-text,\n    :host ::ng-deep mat-list[dense] mat-list-item.mat-2-line .mat-list-item-content .mat-list-text,\n    :host ::ng-deep mat-nav-list a[mat-list-item].mat-2-line .mat-list-item-content .mat-list-text,\n    :host ::ng-deep mat-nav-list mat-list-item.mat-2-line .mat-list-item-content .mat-list-text,\n    :host ::ng-deep mat-nav-list[dense] a[mat-list-item].mat-2-line .mat-list-item-content .mat-list-text,\n    :host ::ng-deep mat-nav-list[dense] mat-list-item.mat-2-line .mat-list-item-content .mat-list-text{\n      padding-right:0; }\n      [dir='rtl'] :host ::ng-deep mat-list a[mat-list-item].mat-2-line .mat-list-item-content .mat-list-text, [dir='rtl']\n      :host ::ng-deep mat-list mat-list-item.mat-2-line .mat-list-item-content .mat-list-text, [dir='rtl']\n      :host ::ng-deep mat-list[dense] a[mat-list-item].mat-2-line .mat-list-item-content .mat-list-text, [dir='rtl']\n      :host ::ng-deep mat-list[dense] mat-list-item.mat-2-line .mat-list-item-content .mat-list-text, [dir='rtl']\n      :host ::ng-deep mat-nav-list a[mat-list-item].mat-2-line .mat-list-item-content .mat-list-text, [dir='rtl']\n      :host ::ng-deep mat-nav-list mat-list-item.mat-2-line .mat-list-item-content .mat-list-text, [dir='rtl']\n      :host ::ng-deep mat-nav-list[dense] a[mat-list-item].mat-2-line .mat-list-item-content .mat-list-text, [dir='rtl']\n      :host ::ng-deep mat-nav-list[dense] mat-list-item.mat-2-line .mat-list-item-content .mat-list-text{\n        padding-left:0;\n        padding-right:16px; }\n    :host ::ng-deep mat-list a[mat-list-item].mat-2-line .mat-list-item-content [matLine] + [matLine],\n    :host ::ng-deep mat-list mat-list-item.mat-2-line .mat-list-item-content [matLine] + [matLine],\n    :host ::ng-deep mat-list[dense] a[mat-list-item].mat-2-line .mat-list-item-content [matLine] + [matLine],\n    :host ::ng-deep mat-list[dense] mat-list-item.mat-2-line .mat-list-item-content [matLine] + [matLine],\n    :host ::ng-deep mat-nav-list a[mat-list-item].mat-2-line .mat-list-item-content [matLine] + [matLine],\n    :host ::ng-deep mat-nav-list mat-list-item.mat-2-line .mat-list-item-content [matLine] + [matLine],\n    :host ::ng-deep mat-nav-list[dense] a[mat-list-item].mat-2-line .mat-list-item-content [matLine] + [matLine],\n    :host ::ng-deep mat-nav-list[dense] mat-list-item.mat-2-line .mat-list-item-content [matLine] + [matLine]{\n      margin-top:4px; }\n.td-menu-content{\n  max-height:calc(50vh);\n  overflow-y:auto; }\n"],
            },] },
];
/** @nocollapse */
TdMenuComponent.ctorParameters = function () { return []; };
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TD_MENU = [
    TdMenuComponent,
];
var CovalentMenuModule = /** @class */ (function () {
    function CovalentMenuModule() {
    }
    return CovalentMenuModule;
}());
CovalentMenuModule.decorators = [
    { type: core.NgModule, args: [{
                imports: [
                    common.CommonModule,
                    menu.MatMenuModule,
                    divider.MatDividerModule,
                ],
                declarations: [
                    TD_MENU,
                ],
                exports: [
                    TD_MENU,
                ],
            },] },
];
/** @nocollapse */
CovalentMenuModule.ctorParameters = function () { return []; };

exports.CovalentMenuModule = CovalentMenuModule;
exports.TdMenuComponent = TdMenuComponent;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=covalent-core-menu.umd.js.map
