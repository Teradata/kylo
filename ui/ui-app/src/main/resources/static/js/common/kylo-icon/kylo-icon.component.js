var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "angular", "rxjs/Subject"], function (require, exports, core_1, angular, Subject_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Displays an SVG icon using AngularJS's ng-md-icon.
     */
    var KyloIconComponent = /** @class */ (function () {
        function KyloIconComponent(element) {
            this.element = element;
            this.iconObserver = new Subject_1.Subject();
            this.sizeObserver = new Subject_1.Subject();
        }
        KyloIconComponent.prototype.ngOnInit = function () {
            var $element = angular.element(this.element.nativeElement);
            angular.element(document.body).injector().get("ngMdIconDirective")[0].link(this, $element, this);
        };
        KyloIconComponent.prototype.ngOnChanges = function (changes) {
            if (changes.icon) {
                this.iconObserver.next(changes.icon.currentValue);
            }
            if (changes.size) {
                this.sizeObserver.next(changes.size.currentValue);
            }
        };
        KyloIconComponent.prototype.$observe = function (name, fn) {
            if (name === "icon") {
                this.iconObserver.subscribe(fn);
            }
            else if (name === "size") {
                this.sizeObserver.subscribe(fn);
            }
        };
        __decorate([
            core_1.Input(),
            __metadata("design:type", String)
        ], KyloIconComponent.prototype, "icon", void 0);
        __decorate([
            core_1.Input(),
            __metadata("design:type", String)
        ], KyloIconComponent.prototype, "size", void 0);
        KyloIconComponent = __decorate([
            core_1.Component({
                selector: "kylo-icon,ng-md-icon",
                template: ""
            }),
            __metadata("design:paramtypes", [core_1.ElementRef])
        ], KyloIconComponent);
        return KyloIconComponent;
    }());
    exports.KyloIconComponent = KyloIconComponent;
});
//# sourceMappingURL=kylo-icon.component.js.map