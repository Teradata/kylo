"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var core_1 = require("@angular/core");
var common_1 = require("@angular/common");
var codemirror_component_1 = require("./codemirror.component");
/**
 * CodemirrorModule
 */
var CodemirrorModule = (function () {
    function CodemirrorModule() {
    }
    return CodemirrorModule;
}());
CodemirrorModule.decorators = [
    { type: core_1.NgModule, args: [{
                imports: [
                    common_1.CommonModule
                ],
                declarations: [
                    codemirror_component_1.CodemirrorComponent,
                ],
                exports: [
                    codemirror_component_1.CodemirrorComponent,
                ]
            },] },
];
/** @nocollapse */
CodemirrorModule.ctorParameters = function () { return []; };
exports.CodemirrorModule = CodemirrorModule;
//# sourceMappingURL=codemirror.module.js.map