import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil, {Lazy} from "../../kylo-utils/LazyLoadUtil";
import AccessConstants from '../../constants/AccessConstants';
import "@uirouter/angularjs";
import "kylo-feedmgr";
import "kylo-common";
import "ment-io";
import "jquery";
import "angular-drag-and-drop-lists";
import '../../vendor/ment.io/styles.css';
import '../../vendor/ment.io/templates.js';

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]); 
        this.module.config(['$stateProvider',this.configFn.bind(this)]);
        this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
    }
    configFn($stateProvider:any) {
      $stateProvider.state('registered-templates',{
            url:'/registered-templates',
            params: {
            },
            views: {
                'content': {
                    component : "registeredTemplatesController"
                }
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['./RegisteredTemplatesController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    const onModuleLoad = () => {
                        return import(/* webpackChunkName: "feeds.registered-templates.controller" */ './RegisteredTemplatesController')
                            .then(Lazy.onModuleImport($ocLazyLoad));
                    };

                    return import(/* webpackChunkName: "feed-mgr.module-require" */ "../module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
                }]
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Templates',
                module:moduleName,
                permissions:AccessConstants.TEMPLATES_ACCESS

            }
        })


        $stateProvider.state('register-new-template',{
            url:'/register-new-template',
            views: {
                'content': {
                    component: "registerNewTemplateController"
                }
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['./new-template/RegisterNewTemplateController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    const onModuleLoad = () => {
                        return import(/* webpackChunkName: "feeds.register-new-template.controller" */ './new-template/RegisterNewTemplateController')
                            .then(Lazy.onModuleImport($ocLazyLoad));
                    };

                    return import(/* webpackChunkName: "feed-mgr.module-require" */ "../module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
                }]
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Register Template',
                module:moduleName,
                permissions:AccessConstants.TEMPLATES_EDIT
            }
        });

        $stateProvider.state('register-template',{
            url:'/register-template',
            params:{
                nifiTemplateId:null,
                registeredTemplateId:null
            },
            views: {
                'content': {
                    component : "registerTemplateController"
                }
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['./template-stepper/RegisterTemplateController','@uirouter/angularjs'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "feeds.register-template.controller" */ './template-stepper/RegisterTemplateController')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load RegisterTemplateController, " + err);
                        });
                }]
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Register Template',
                module:moduleName,
                permissions:AccessConstants.TEMPLATES_EDIT
            }
        }).state('register-template-complete', {
            url: '/register-template-complete',
            params: {
                message: '',
                templateModel: null
            },
            views: {
                'content': {
                    component : "registerTemplateCompleteController"
                }
            },
            resolve: {
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "feeds.register-template-complete-complete.controller" */ './template-stepper/register-template/register-template-step')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load registerTemplateCompleteController, " + err);
                        });
                }]
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Register Template',
                module:moduleName,
                permissions:AccessConstants.TEMPLATES_EDIT
            }
        });
    }  

   runFn($ocLazyLoad: any){
       return import(/* webpackChunkName: "templates.module-require" */ "./module-require")
           .then(mod => {
               $ocLazyLoad.load({name:moduleName});
           })
           .catch(err => {
               throw new Error("Failed to load templates.module-require, " + err);
           });
   }
} 
const module = new ModuleFactory();
export default module;

