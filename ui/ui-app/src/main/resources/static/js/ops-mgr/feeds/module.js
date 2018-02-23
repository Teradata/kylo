define(["require", "exports", "angular", "./module-name", "../../kylo-utils/LazyLoadUtil", "../../constants/AccessConstants", "kylo-common", "kylo-services", "kylo-opsmgr", "../alerts/module", "../overview/module", "angular-nvd3"], function (require, exports, angular, module_name_1, LazyLoadUtil_1, AccessConstants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.lazyLoadController = function (path) {
                return LazyLoadUtil_1.default.lazyLoadController(path, ['ops-mgr/jobs/module', 'ops-mgr/jobs/module-require', 'ops-mgr/feeds/module-require', 'ops-mgr/alerts/module-require', 'ops-mgr/overview/module-require']);
            };
            this.module = angular.module(module_name_1.moduleName, ['nvd3']);
            this.module.config(['$stateProvider', '$compileProvider', this.configFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider, $compileProvider) {
            //preassign modules until directives are rewritten to use the $onInit method.
            //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
            $compileProvider.preAssignBindingsEnabled(true);
            $stateProvider.state(AccessConstants_1.default.UI_STATES.OPS_FEED_DETAILS.state, {
                url: '/ops-feed-details/{feedName}',
                params: {
                    feedName: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/feeds/feed-details.html',
                        controller: "OpsManagerFeedDetailsController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/feeds/FeedDetailsController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Feed Details',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.OPS_FEED_DETAILS.permissions
                }
            });
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map