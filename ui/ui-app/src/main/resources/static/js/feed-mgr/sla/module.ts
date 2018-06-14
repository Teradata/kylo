import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import AccessConstants from "../../constants/AccessConstants";
import "kylo-common";
import "kylo-services";
import "kylo-feedmgr";
import "jquery";

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]);
   /* **
     * LAZY loaded in from /app.js
     **/
        this.module.config(['$stateProvider',this.configFn.bind(this)]);
    }
    configFn($stateProvider:any, $compileProvider: any) {
        $stateProvider.state(AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.state,{
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
                loadMyCtrl: this.lazyLoadController(['feed-mgr/sla/service-level-agreement','feed-mgr/sla/ServiceLevelAgreementInitController'])
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
                    component : "slaEmailTemplatesController"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['feed-mgr/sla/sla-email-templates/SlaEmailTemplatesController'])
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
                        component:"slaEmailTemplateController",
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['feed-mgr/sla/sla-email-templates/SlaEmailTemplateController'])
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

    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,["feed-mgr/sla/module-require"]);
    }
} 

const module = new ModuleFactory();
export default module;