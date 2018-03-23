define(["require", "exports", "angular", "./module-name", "../../kylo-utils/LazyLoadUtil", "../../constants/AccessConstants", "kylo-common", "kylo-services", "kylo-opsmgr"], function (require, exports, angular, module_name_1, LazyLoadUtil_1, AccessConstants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.config(['$stateProvider', '$compileProvider', this.configFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider, $compileProvider) {
            $stateProvider.state(AccessConstants_1.default.UI_STATES.ALERTS.state, {
                url: '/alerts',
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/alerts/alerts-table.html',
                        controller: 'AlertsController',
                        controllerAs: 'vm'
                    }
                },
                params: {
                    query: null
                },
                resolve: {
                    loadPage: this.lazyLoad()
                },
                data: {
                    displayName: 'Alerts',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.ALERTS.permissions
                }
            }).state(AccessConstants_1.default.UI_STATES.ALERT_DETAILS.state, {
                url: "/alert-details/{alertId}",
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/alerts/alert-details.html',
                        controller: 'AlertDetailsController',
                        controllerAs: 'vm'
                    }
                },
                params: {
                    alertId: null
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/alerts/AlertDetailsController'])
                },
                data: {
                    displayName: 'Alert Details',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.ALERT_DETAILS.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, ["ops-mgr/alerts/module-require"]);
        };
        ModuleFactory.prototype.lazyLoad = function () {
            return LazyLoadUtil_1.default.lazyLoad(['ops-mgr/alerts/module-require']);
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map