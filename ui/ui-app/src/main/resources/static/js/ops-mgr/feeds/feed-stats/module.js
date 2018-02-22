define(["require", "exports", "angular", "./module-name", "../../../kylo-utils/LazyLoadUtil", "../../../constants/AccessConstants", "kylo-common", "kylo-services", "kylo-opsmgr", "angular-nvd3"], function (require, exports, angular, module_name_1, LazyLoadUtil_1, AccessConstants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.lazyLoadController = function (path) {
                return LazyLoadUtil_1.default.lazyLoadController(path, ['ops-mgr/feeds/feed-stats/module-require']);
            };
            this.lazyLoad = function () {
                return LazyLoadUtil_1.default.lazyLoad(['ops-mgr/feeds/feed-stats/module-require']);
            };
            this.module = angular.module(module_name_1.moduleName, ['nvd3']);
            this.module.config(['$stateProvider', '$compileProvider', this.configFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider, $compileProvider) {
            //preassign modules until directives are rewritten to use the $onInit method.
            //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
            $compileProvider.preAssignBindingsEnabled(true);
            $stateProvider.state(AccessConstants_1.default.UI_STATES.FEED_STATS.state, {
                url: '/feed-stats/{feedName}',
                params: {
                    feedName: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/ops-mgr/feeds/feed-stats/feed-stats.html',
                        controller: "FeedStatsController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['ops-mgr/feeds/feed-stats/feed-stats'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Feed Stats',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.FEED_STATS.permissions
                }
            });
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map