define(["require", "exports", "angular", "./module-name", "../../kylo-utils/LazyLoadUtil", "../../constants/AccessConstants", "kylo-common", "kylo-services", "kylo-feedmgr", "jquery"], function (require, exports, angular, module_name_1, LazyLoadUtil_1, AccessConstants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(module_name_1.moduleName, []);
            /* **
              * LAZY loaded in from /app.js
              **/
            this.module.config(['$stateProvider', this.configFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider, $compileProvider) {
            $stateProvider.state(AccessConstants_1.AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.state, {
                url: '/service-level-agreements/:slaId',
                params: {
                    slaId: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/sla/service-level-agreements-view.html',
                        controller: "ServiceLevelAgreementInitController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/sla/service-level-agreement', 'feed-mgr/sla/ServiceLevelAgreementInitController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'Service Level Agreements',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.permissions
                }
            });
            $stateProvider.state('sla-email-templates', {
                url: '/sla-email-templates',
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/sla/sla-email-templates/sla-email-templates.html',
                        controller: "SlaEmailTemplatesController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/sla/sla-email-templates/SlaEmailTemplatesController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'SLA Email Templates',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES.permissions
                }
            });
            $stateProvider.state('sla-email-template', {
                url: '/sla-email-template/:emailTemplateId',
                params: {
                    emailTemplateId: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/sla/sla-email-templates/sla-email-template.html',
                        controller: "SlaEmailTemplateController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/sla/sla-email-templates/SlaEmailTemplateController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: 'SLA Email Template',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, ["feed-mgr/sla/module-require"]);
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map