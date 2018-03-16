define(["require", "exports", "angular", "../../kylo-utils/LazyLoadUtil", "../../constants/AccessConstants", "./module-name", "../../codemirror-require/module", "kylo-feedmgr", "kylo-common", "kylo-services"], function (require, exports, angular, LazyLoadUtil_1, AccessConstants_1, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            // Window.CodeMirror = CodeMirror;
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.config(["$stateProvider", "$compileProvider", this.configFn.bind(this)]);
            this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
        }
        ModuleFactory.prototype.runFn = function ($ocLazyLoad) {
            $ocLazyLoad.load({
                name: 'kylo',
                files: [
                    "js/feed-mgr/domain-types/codemirror-regex.css",
                    "js/feed-mgr/domain-types/details/matchers/regexp-editor.component.css"
                ]
            });
        };
        ModuleFactory.prototype.configFn = function ($stateProvider, $compileProvider) {
            //preassign modules until directives are rewritten to use the $onInit method.
            //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
            $compileProvider.preAssignBindingsEnabled(true);
            $stateProvider.state(AccessConstants_1.default.UI_STATES.DOMAIN_TYPES.state, {
                url: "/domain-types",
                params: {},
                views: {
                    content: {
                        templateUrl: "js/feed-mgr/domain-types/domain-types.html",
                        controller: "DomainTypesController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(["feed-mgr/domain-types/DomainTypesController"])
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: "Domain Types",
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.DOMAIN_TYPES.permissions
                }
            }).state(AccessConstants_1.default.UI_STATES.DOMAIN_TYPE_DETAILS.state, {
                url: "/domain-type-details/{domainTypeId}",
                params: {
                    domainTypeId: null
                },
                views: {
                    content: {
                        component: "domainTypeDetailsComponent"
                    }
                },
                resolve: {
                    model: function ($transition$, DomainTypesService) {
                        if (angular.isString($transition$.params().domainTypeId)) {
                            return DomainTypesService.findById($transition$.params().domainTypeId);
                        }
                        else {
                            return DomainTypesService.newDomainType();
                        }
                    },
                    loadMyCtrl: this.lazyLoadController(["feed-mgr/domain-types/details/details.component"])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: "Domain Type Details",
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.DOMAIN_TYPE_DETAILS.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, "feed-mgr/domain-types/module-require");
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map