define(["require", "exports", "angular", "./module-name", "../../kylo-utils/LazyLoadUtil", "../../constants/AccessConstants", "kylo-common", "kylo-services", "kylo-opsmgr"], function (require, exports, angular, module_name_1, LazyLoadUtil_1, AccessConstants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.config(['$stateProvider', '$compileProvider', this.configFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider, $compileProvider) {
            //preassign modules until directives are rewritten to use the $onInit method.
            //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
            $compileProvider.preAssignBindingsEnabled(true);
            $stateProvider.state(AccessConstants_1.default.UI_STATES.SERVICE_HEALTH.state, {
                url: '/service-health',
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/service-health/service-health.html'
                    }
                },
                resolve: {
                    loadPage: this.lazyLoad()
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: 'Service Health',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.SERVICE_HEALTH.permissions
                }
            }).state(AccessConstants_1.default.UI_STATES.SERVICE_DETAILS.state, {
                url: '/service-details/:serviceName',
                params: {
                    serviceName: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/service-health/service-detail.html',
                        controller: "ServiceHealthDetailsController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/service-health/ServiceHealthDetailsController'])
                },
                data: {
                    displayName: 'Service Details',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.SERVICE_DETAILS.permissions
                }
            }).state(AccessConstants_1.default.UI_STATES.SERVICE_COMPONENT_DETAILS.state, {
                url: '/service-details/{serviceName}/{componentName}',
                params: {
                    serviceName: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/service-health/service-component-detail.html',
                        controller: "ServiceComponentHealthDetailsController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/service-health/ServiceComponentHealthDetailsController'])
                },
                data: {
                    displayName: 'Service Component',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.SERVICE_COMPONENT_DETAILS.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, ["ops-mgr/service-health/module-require"]);
        };
        ModuleFactory.prototype.lazyLoad = function () {
            return LazyLoadUtil_1.default.lazyLoad(['ops-mgr/service-health/module-require']);
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map