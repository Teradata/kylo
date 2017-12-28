var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/common", "@angular/core", "@uirouter/angular", "./angular2"], function (require, exports, common_1, core_1, angular_1, angular2_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccessConstants = require("../../constants/AccessConstants");
    var moduleName = require("./module-name");
    var VisualQueryModule = /** @class */ (function () {
        function VisualQueryModule(injector) {
            // Lazy load AngularJS module and entry component
            require("./module");
            injector.get("$ocLazyLoad").inject(moduleName);
            require("./module-require");
            require("./visual-query.component");
        }
        VisualQueryModule = __decorate([
            core_1.NgModule({
                declarations: [
                    angular2_1.VisualQueryComponent,
                    angular2_1.VisualQueryDirective
                ],
                imports: [
                    common_1.CommonModule,
                    angular_1.UIRouterModule.forChild({
                        states: [{
                                name: "visual-query",
                                url: "/visual-query/{engine}",
                                params: {
                                    engine: null
                                },
                                views: {
                                    "content": {
                                        component: angular2_1.VisualQueryComponent
                                    }
                                },
                                resolve: {
                                    engine: function ($injector, $ocLazyLoad, $transition$) {
                                        var engineName = $transition$.params().engine;
                                        if (engineName === null) {
                                            engineName = "spark";
                                        }
                                        return $ocLazyLoad.load("feed-mgr/visual-query/module-require")
                                            .then(function () {
                                            return $injector.get("VisualQueryEngineFactory").getEngine(engineName);
                                        });
                                    }
                                },
                                data: {
                                    breadcrumbRoot: true,
                                    displayName: "Visual Query",
                                    module: moduleName,
                                    permissions: AccessConstants.UI_STATES.VISUAL_QUERY.permissions
                                }
                            }]
                    })
                ]
            }),
            __metadata("design:paramtypes", [core_1.Injector])
        ], VisualQueryModule);
        return VisualQueryModule;
    }());
    exports.VisualQueryModule = VisualQueryModule;
});
//# sourceMappingURL=visual-query.module.js.map