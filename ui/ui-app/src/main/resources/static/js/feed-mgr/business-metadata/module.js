define(["require", "exports", "angular", "./module-name", "../../kylo-utils/LazyLoadUtil", "../../constants/AccessConstants", "kylo-feedmgr", "kylo-common"], function (require, exports, angular, module_name_1, LazyLoadUtil_1, AccessConstants_1) {
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
            $stateProvider.state(AccessConstants_1.default.UI_STATES.BUSINESS_METADATA.state, {
                url: '/business-metadata',
                params: {},
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/business-metadata/business-metadata.html',
                        controller: 'BusinessMetadataController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/business-metadata/BusinessMetadataController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Business Metadata',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.BUSINESS_METADATA.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, "feed-mgr/business-metadata/module-require");
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map