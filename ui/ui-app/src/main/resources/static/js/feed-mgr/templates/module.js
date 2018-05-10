define(["require", "exports", "angular", "./module-name", "../../kylo-utils/LazyLoadUtil", "../../constants/AccessConstants", "@uirouter/angularjs", "kylo-feedmgr", "kylo-common", "ment-io", "jquery", "angular-drag-and-drop-lists"], function (require, exports, angular, module_name_1, LazyLoadUtil_1, AccessConstants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.config(['$stateProvider', this.configFn.bind(this)]);
            this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider) {
            $stateProvider.state('registered-templates', {
                url: '/registered-templates',
                params: {},
                views: {
                    'content': {
                        component: "registeredTemplatesController"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/templates/RegisteredTemplatesController'])
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: 'Templates',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.TEMPLATES_ACCESS
                }
            });
            $stateProvider.state('register-new-template', {
                url: '/register-new-template',
                views: {
                    'content': {
                        component: "registerNewTemplateController"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/templates/new-template/RegisterNewTemplateController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Register Template',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.TEMPLATES_EDIT
                }
            });
            $stateProvider.state('register-template', {
                url: '/register-template',
                params: {
                    nifiTemplateId: null,
                    registeredTemplateId: null
                },
                views: {
                    'content': {
                        component: "registerTemplateController"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/templates/template-stepper/RegisterTemplateController', '@uirouter/angularjs'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Register Template',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.TEMPLATES_EDIT
                }
            }).state('register-template-complete', {
                url: '/register-template-complete',
                params: {
                    message: '',
                    templateModel: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/templates/template-stepper/register-template/register-template-complete.html',
                        controller: 'RegisterTemplateCompleteController',
                        controllerAs: 'vm'
                    }
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Register Template',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.TEMPLATES_EDIT
                }
            }).state('import-template', {
                url: '/import-template',
                params: {},
                views: {
                    'content': {
                        component: 'importTemplateController'
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/templates/import-template/ImportTemplateController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Template Manager',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.TEMPLATES_IMPORT
                }
            });
        };
        ModuleFactory.prototype.runFn = function ($ocLazyLoad) {
            $ocLazyLoad.load({ name: module_name_1.moduleName, files: ['js/vendor/ment.io/styles.css', 'vendor/ment.io/templates'] });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, "feed-mgr/templates/module-require");
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map