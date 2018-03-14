define(["require", "exports", "angular", "../../kylo-utils/LazyLoadUtil", "../../constants/AccessConstants", "./module-name", "kylo-feedmgr", "kylo-common", "kylo-services", "jquery"], function (require, exports, angular, LazyLoadUtil_1, AccessConstants_1, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            // Window.CodeMirror = CodeMirror;
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.config(["$stateProvider", this.configFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider) {
            $stateProvider.state(AccessConstants_1.default.UI_STATES.CATALOG.state, {
                url: '/catalog',
                params: {},
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/tables/catalog.html',
                        controller: "CatalogController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/tables/CatalogController'])
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: 'Catalog',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.CATALOG.permissions
                }
            });
            $stateProvider.state(AccessConstants_1.default.UI_STATES.SCHEMAS.state, {
                url: '/catalog/{datasource}/schemas',
                params: {
                    datasource: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/tables/schemas.html',
                        controller: "SchemasController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/tables/SchemasController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Schemas',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.SCHEMAS.permissions
                }
            });
            $stateProvider.state(AccessConstants_1.default.UI_STATES.TABLES.state, {
                url: '/catalog/{datasource}/schemas/{schema}',
                params: {
                    datasource: null,
                    schema: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/tables/tables.html',
                        controller: "TablesController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/tables/TablesController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Tables',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.TABLES.permissions
                }
            });
            $stateProvider.state(AccessConstants_1.default.UI_STATES.TABLE.state, {
                url: '/catalog/{datasource}/schemas/{schema}/{tableName}',
                params: {
                    datasource: null,
                    schema: null,
                    tableName: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/tables/table.html',
                        controller: "TableController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/tables/TableController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Table Details',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.TABLE.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, "feed-mgr/tables/module-require");
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map