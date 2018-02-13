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
            $stateProvider.state(AccessConstants_1.default.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.state, {
                url: '/service-level-assessments',
                params: {
                    filter: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/sla/assessments.html',
                        controller: "ServiceLevelAssessmentsInitController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/sla/ServiceLevelAssessmentsInitController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Service Level Assessments',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.permissions
                }
            });
            $stateProvider.state(AccessConstants_1.default.UI_STATES.SERVICE_LEVEL_ASSESSMENT.state, {
                url: '/service-level-assessment/{assessmentId}',
                params: {
                    assessmentId: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/sla/assessment.html',
                        controller: "ServiceLevelAssessmentController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/sla/service-level-assessment'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Service Level Assessment',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.SERVICE_LEVEL_ASSESSMENT.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, ["ops-mgr/sla/module-require"]);
        };
        ModuleFactory.prototype.lazyLoad = function () {
            return LazyLoadUtil_1.default.lazyLoad(['ops-mgr/sla/module-require']);
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map