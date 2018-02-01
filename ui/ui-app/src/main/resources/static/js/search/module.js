define(["require", "exports", "angular", "./module-name", "../services/services.module"], function (require, exports, angular, module_name_1, services_module_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var lazyLoadUtil = require('../kylo-utils/LazyLoadUtil');
    var AccessConstants = require('../constants/AccessConstants');
    exports.KyloServicesModule = services_module_1.KyloServicesModule;
    var KyloFeedManager = require('../feed-mgr/module').KyloFeedManager;
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.config(['$stateProvider', this.configFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider) {
            $stateProvider.state(AccessConstants.UI_STATES.SEARCH.state, {
                url: '/search',
                params: {
                    bcExclude_globalSearchResetPaging: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/search/common/search.html',
                        controller: "SearchController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['search/common/SearchController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Search',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants.UI_STATES.SEARCH.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return lazyLoadUtil.lazyLoadController(path, "search/module-require");
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map