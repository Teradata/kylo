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
            $stateProvider.state(AccessConstants_1.default.UI_STATES.JOBS.state, {
                url: '/jobs',
                params: {
                    filter: null,
                    tab: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/jobs/jobs.html',
                        controller: "JobsPageController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/jobs/JobsPageController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Jobs',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.JOBS.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, ["ops-mgr/jobs/module-require"]);
        };
        ModuleFactory.prototype.lazyLoad = function () {
            return LazyLoadUtil_1.default.lazyLoad(['ops-mgr/jobs/module-require']);
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map