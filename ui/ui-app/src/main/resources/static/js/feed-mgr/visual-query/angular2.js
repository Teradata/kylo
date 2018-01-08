var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "@angular/upgrade/static", "./wrangler/query-engine"], function (require, exports, core_1, static_1, query_engine_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Angular 2 entry component for Visual Query page.
     */
    var VisualQueryComponent = /** @class */ (function () {
        function VisualQueryComponent() {
        }
        __decorate([
            core_1.Input(),
            __metadata("design:type", query_engine_1.QueryEngine)
        ], VisualQueryComponent.prototype, "engine", void 0);
        VisualQueryComponent = __decorate([
            core_1.Component({
                template: "\n      <visual-query [engine]=\"engine\"></visual-query>\n    "
            })
        ], VisualQueryComponent);
        return VisualQueryComponent;
    }());
    exports.VisualQueryComponent = VisualQueryComponent;
    /**
     * Upgrades Visual Query AngularJS entry component to Angular 2.
     */
    var VisualQueryDirective = /** @class */ (function (_super) {
        __extends(VisualQueryDirective, _super);
        function VisualQueryDirective(elementRef, injector) {
            return _super.call(this, 'visualQuery', elementRef, injector) || this;
        }
        __decorate([
            core_1.Input(),
            __metadata("design:type", query_engine_1.QueryEngine)
        ], VisualQueryDirective.prototype, "engine", void 0);
        VisualQueryDirective = __decorate([
            core_1.Directive({
                selector: 'visual-query'
            }),
            __metadata("design:paramtypes", [core_1.ElementRef, core_1.Injector])
        ], VisualQueryDirective);
        return VisualQueryDirective;
    }(static_1.UpgradeComponent));
    exports.VisualQueryDirective = VisualQueryDirective;
});
//# sourceMappingURL=angular2.js.map