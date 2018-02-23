define(["require", "exports", "angular", "./module-name", "../../kylo-utils/LazyLoadUtil", "kylo-common", "kylo-services", "kylo-opsmgr", "../module"], function (require, exports, angular, module_name_1, LazyLoadUtil_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccessConstants = require("../../constants/AccessConstants");
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.config(['$stateProvider', '$compileProvider', this.configFn.bind(this)]);
            this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
        }
        ModuleFactory.prototype.runFn = function ($ocLazyLoad) {
            $ocLazyLoad.load({ name: 'kylo', files: ['bower_components/c3/c3.css',
                    'js/ops-mgr/charts/pivot.css'
                ] });
        };
        ModuleFactory.prototype.configFn = function ($stateProvider, $compileProvider) {
            //preassign modules until directives are rewritten to use the $onInit method.
            //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
            $compileProvider.preAssignBindingsEnabled(true);
            $stateProvider.state(AccessConstants.UI_STATES.CHARTS.state, {
                url: '/charts',
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/charts/charts.html',
                        controller: "ChartsController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/charts/ChartsController', 'pivottable-c3-renderers'])
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: 'Charts',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants.UI_STATES.CHARTS.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, ['ops-mgr/charts/module-require']);
        };
        ModuleFactory.prototype.lazyLoad = function () {
            return LazyLoadUtil_1.default.lazyLoad(['ops-mgr/charts/module-require']);
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map