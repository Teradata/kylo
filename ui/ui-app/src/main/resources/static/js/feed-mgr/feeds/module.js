define(["require", "exports", "angular", "../../constants/AccessConstants"], function (require, exports, angular, AccessConstants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var lazyLoadUtil = require('../../kylo-utils/LazyLoadUtil');
    var moduleName = require('./module-name');
    var feedManager = require('kylo-feedmgr');
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(moduleName, []);
            this.module.config(['$stateProvider', '$compileProvider', this.configFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider, $compileProvider) {
            $compileProvider.preAssignBindingsEnabled(true);
            $stateProvider.state(AccessConstants_1.default.UI_STATES.FEEDS.state, {
                url: '/feeds',
                params: {
                    tab: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/feeds/feeds-table.html',
                        controller: 'FeedsTableController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController('feed-mgr/feeds/FeedsTableController')
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: 'Feeds',
                    module: moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.FEEDS.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return lazyLoadUtil.lazyLoadController(path, ['feed-mgr/feeds/module-require']);
        };
        return ModuleFactory;
    }());
    exports.default = new ModuleFactory();
});
//# sourceMappingURL=module.js.map