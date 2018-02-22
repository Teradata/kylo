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
            $stateProvider.state(AccessConstants_1.default.UI_STATES.SCHEDULER.state, {
                url: '/scheduler',
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/scheduler/scheduler.html',
                        controller: "SchedulerController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/scheduler/SchedulerController'])
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: 'Tasks',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.SCHEDULER.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, ["ops-mgr/scheduler/module-require"]);
        };
        ModuleFactory.prototype.lazyLoad = function () {
            return LazyLoadUtil_1.default.lazyLoad(['ops-mgr/scheduler/module-require']);
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map