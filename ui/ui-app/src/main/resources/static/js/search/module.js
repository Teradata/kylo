define(["require", "exports", "angular", "./module-name", "../kylo-utils/LazyLoadUtil", "../services/services.module"], function (require, exports, angular, module_name_1, LazyLoadUtil_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    //const AccessConstants = require('../constants/AccessConstants');
    var AccessConstants = require("../constants/AccessConstants");
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
            return LazyLoadUtil_1.default.lazyLoadController(path, "search/module-require");
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map