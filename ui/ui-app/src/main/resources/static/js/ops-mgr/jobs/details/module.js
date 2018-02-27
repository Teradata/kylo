define(["require", "exports", "angular", "./module-name", "../../../kylo-utils/LazyLoadUtil", "kylo-common", "kylo-services", "kylo-opsmgr", "../module"], function (require, exports, angular, module_name_1, LazyLoadUtil_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccessConstants = require("../../../constants/AccessConstants");
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.config(['$stateProvider', '$compileProvider', this.configFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider, $compileProvider) {
            //preassign modules until directives are rewritten to use the $onInit method.
            //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
            $compileProvider.preAssignBindingsEnabled(true);
            $stateProvider.state(AccessConstants.UI_STATES.JOB_DETAILS.state, {
                url: '/job-details/{executionId}',
                params: {
                    executionId: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/jobs/details/job-details.html',
                        controller: "JobDetailsController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/jobs/details/JobDetailsController'])
                },
                data: {
                    displayName: 'Job Details',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants.UI_STATES.JOB_DETAILS.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, ['ops-mgr/jobs/module-require', 'ops-mgr/jobs/details/module-require']);
        };
        ModuleFactory.prototype.lazyLoad = function () {
            return LazyLoadUtil_1.default.lazyLoad(['ops-mgr/jobs/module-require', 'ops-mgr/jobs/details/module-require']);
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map