import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
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
                // loadMyCtrl: this.lazyLoadController(['feed-mgr/templates/RegisteredTemplatesController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "feeds.registered-templates.controller" */ './RegisteredTemplatesController')
                        .then(mod => {
                            console.log('imported RegisteredTemplatesController mod', mod);
                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load RegisteredTemplatesController, " + err);
                        });
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
                // loadMyCtrl: this.lazyLoadController(['feed-mgr/templates/new-template/RegisterNewTemplateController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "feeds.register-new-template.controller" */ './new-template/RegisterNewTemplateController')
                        .then(mod => {
                            console.log('imported RegisterNewTemplateController mod', mod);
                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load RegisterNewTemplateController, " + err);
                        });
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
                // loadMyCtrl: this.lazyLoadController(['feed-mgr/templates/template-stepper/RegisterTemplateController','@uirouter/angularjs'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "feeds.register-template.controller" */ './template-stepper/RegisterTemplateController')
                        .then(mod => {
                            console.log('imported RegisterTemplateController mod', mod);
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
            data: {
                breadcrumbRoot: false,
                displayName: 'Register Template',
                module:moduleName,
                permissions:AccessConstants.TEMPLATES_EDIT
            }
        });
    }  

   runFn($ocLazyLoad: any){
       return import(/* webpackChunkName: "categories.module" */ "./module-require")
           .then(mod => {
               $ocLazyLoad.load({name:moduleName});
           })
           .catch(err => {
               throw new Error("Failed to load ./feed-mgr/categories/module.js, " + err);
           });
   }
} 
const module = new ModuleFactory();
export default module;

