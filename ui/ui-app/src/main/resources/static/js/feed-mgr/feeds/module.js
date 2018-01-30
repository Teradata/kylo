define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccessConstants = require('../../constants/AccessConstants');
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
            $stateProvider.state(AccessConstants.UI_STATES.FEEDS.state, {
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
                    permissions: AccessConstants.UI_STATES.FEEDS.permissions
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