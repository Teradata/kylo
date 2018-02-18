define(["require", "exports", "angular", "../kylo-utils/LazyLoadUtil", "./module-name", "../codemirror-require/module"], function (require, exports, angular, LazyLoadUtil_1, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccessConstants = require('../constants/AccessConstants');
    //export * from "../codemirror-require/module"; 
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.config(['$stateProvider', this.configFn.bind(this)]);
            this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider) {
            $stateProvider.state('jcr-query', {
                url: '/admin/jcr-query',
                views: {
                    'content': {
                        templateUrl: 'js/admin/jcr/jcr-query.html',
                        controller: "JcrQueryController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['admin/jcr/JcrQueryController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'JCR Admin',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants.UI_STATES.JCR_ADMIN.permissions
                }
            });
            $stateProvider.state('cluster', {
                url: '/admin/cluster',
                views: {
                    'content': {
                        templateUrl: 'js/admin/cluster/cluster-test.html',
                        controller: "ClusterController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['admin/cluster/ClusterController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Kylo Cluster',
                    module: module_name_1.moduleName,
                    permissions: []
                }
            });
        };
        ModuleFactory.prototype.runFn = function ($ocLazyLoad) {
            $ocLazyLoad.load({
                name: 'kylo',
                files: ['bower_components/angular-ui-grid/ui-grid.css', 'assets/ui-grid-material.css'],
                serie: true
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, "admin/module-require");
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map