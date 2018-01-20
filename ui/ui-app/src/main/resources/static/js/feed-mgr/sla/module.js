define(['angular', 'feed-mgr/sla/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-common', 'kylo-services','kylo-feedmgr','jquery'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
    var module = angular.module(moduleName, []);

    /**
     * LAZY loaded in from /app.js
     */
    module.config(['$stateProvider',function ($stateProvider) {
        $stateProvider.state(AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.state,{
            url:'/service-level-agreements/:slaId',
            params: {
                slaId:null
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/sla/service-level-agreements-view.html',
                    controller:"ServiceLevelAgreementInitController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/sla/service-level-agreement','feed-mgr/sla/ServiceLevelAgreementInitController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Service Level Agreements',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.permissions
            }
        })

        $stateProvider.state('sla-email-templates',{
            url:'/sla-email-templates',
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/sla/sla-email-templates/sla-email-templates.html',
                    controller:"SlaEmailTemplatesController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/sla/sla-email-templates/SlaEmailTemplatesController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'SLA Email Templates',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES.permissions
            }
        })



        $stateProvider.state('sla-email-template',{
                url:'/sla-email-template/:emailTemplateId',
                params:{
                    emailTemplateId:null
                },
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/sla/sla-email-templates/sla-email-template.html',
                        controller:"SlaEmailTemplateController",
                        controllerAs:"vm"
                    }
                },
                resolve: {
                    loadMyCtrl: lazyLoadController(['feed-mgr/sla/sla-email-templates/SlaEmailTemplateController'])
                },
                data:{
                    breadcrumbRoot:false,
                    displayName:'SLA Email Template',
                    module:moduleName,
                    permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES.permissions
                }
            })



        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,'feed-mgr/sla/module-require');
        }

    }]);









    return module;
});



