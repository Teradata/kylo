import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import AccessConstants from "../../constants/AccessConstants";
import "kylo-common";
import "kylo-services";
import "kylo-feedmgr";
import "jquery";
import SlaEmailTemplateService from './sla-email-templates/SlaEmailTemplateService';

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]);
   /* **
     * LAZY loaded in from /app.js
     **/
        this.module.config(['$stateProvider',this.configFn.bind(this)]);
        this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
    }
    configFn($stateProvider:any, $compileProvider: any) {
        $stateProvider.state({
            name: AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.state,
            url:'/service-level-agreements',
            views: {
                'content': {
                    component: "thinkbigServiceLevelAgreements"
                }
            },
            resolve: {
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "feedmgr.sla.service-level-agreements" */ "./service-level-agreements")
                        .then(mod => {
                            console.log('imported ./service-level-agreement mod', mod);
                            return $ocLazyLoad.load(mod.default);
                        })
                        .catch(err => {
                            throw new Error("Failed to load ./service-level-agreements, " + err);
                        });
                }]
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Service Level Agreements',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.permissions
            }
        });

        $stateProvider.state({
            name: 'service-level-agreement',
            url:'/service-level-agreements/:slaId',
            params: {
                slaId:null
            },
            views: {
                'content': {
                    component: "serviceLevelAgreementInitComponent"
                }
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['feed-mgr/sla/service-level-agreement','feed-mgr/sla/ServiceLevelAgreementInitController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    import(/* webpackChunkName: "feedmgr.sla.controller" */ "./service-level-agreement")
                        .then(mod => {
                            console.log('imported ./service-level-agreement mod', mod);
                            $ocLazyLoad.load(mod.default);
                        })
                        .catch(err => {
                            throw new Error("Failed to load ./service-level-agreement, " + err);
                        });

                    return import(/* webpackChunkName: "feedmgr.sla.initController" */ './ServiceLevelAgreementInitController')
                        .then(mod => {
                            console.log('imported ServiceLevelAgreementInitController mod', mod);
                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load ServiceLevelAgreementInitController, " + err);
                        });
                }]
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Service Level Agreements',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.permissions
            }
        });

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
                // loadMyCtrl: this.lazyLoadController(['feed-mgr/sla/sla-email-templates/SlaEmailTemplatesController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "feedmgr.sla.email-templates.controller" */ './sla-email-templates/SlaEmailTemplatesController')
                        .then(mod => {
                            console.log('imported SlaEmailTemplatesController mod', mod);
                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load SlaEmailTemplatesController, " + err);
                        });
                }]
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
                        controllerAs: "vm",
                        controller:"slaEmailTemplateController"
                    }
                },
                resolve: {
                    // loadMyCtrl: this.lazyLoadController(['feed-mgr/sla/sla-email-templates/SlaEmailTemplateController'])
                    loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                        return import(/* webpackChunkName: "feedmgr.sla.email-template.controller" */ './sla-email-templates/SlaEmailTemplateController')
                            .then(mod => {
                                console.log('imported SlaEmailTemplateController mod', mod);
                                return $ocLazyLoad.load(mod.default)
                            })
                            .catch(err => {
                                throw new Error("Failed to load SlaEmailTemplateController, " + err);
                            });
                    }]
                },
                data:{
                    breadcrumbRoot:false,
                    displayName:'SLA Email Template',
                    module:moduleName,
                    permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES.permissions
                }
            })
        ;
    }

    runFn($ocLazyLoad: any){
        // import(/* webpackChunkName: "feedmgr.sla.module-require" */ "./module-require")
        //     .then(mod => {
        //         $ocLazyLoad.load({name:moduleName});
        //     })
        //     .catch(err => {
        //         throw new Error("Failed to load feed-mgr/sla/module-require, " + err);
        //     });
    }
}

const module = new ModuleFactory();
export default module;