define(["require", "exports", "angular", "./module-name", "../../kylo-utils/LazyLoadUtil", "../../constants/AccessConstants", "kylo-common", "kylo-services", "kylo-opsmgr", "angular-nvd3", "pascalprecht.translate"], function (require, exports, angular, module_name_1, LazyLoadUtil_1, AccessConstants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleAlerts = require('../alerts/module');
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.config(['$compileProvider', this.configFn_init.bind(this)]);
            this.module.config(['$stateProvider', '$compileProvider', this.configFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn_init = function ($compileProvider) {
            //pre-assign modules until directives are rewritten to use the $onInit method.
            //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
            $compileProvider.preAssignBindingsEnabled(true);
        };
        /**
         * LAZY loaded in from /app.js
         */
        ModuleFactory.prototype.configFn = function ($stateProvider, $compileProvider) {
            //preassign modules until directives are rewritten to use the $onInit method.
            //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
            $compileProvider.preAssignBindingsEnabled(true);
            $stateProvider.state(AccessConstants_1.default.UI_STATES.DASHBOARD.state, {
                url: '/dashboard',
                params: {},
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/overview/overview.html',
                        controller: "OverviewController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/overview/OverviewController'])
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: 'Dashboard',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.DASHBOARD.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, ['ops-mgr/overview/module-require', 'ops-mgr/alerts/module-require']); //,true);
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map