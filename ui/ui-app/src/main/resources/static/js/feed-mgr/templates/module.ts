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
                loadMyCtrl: this.lazyLoadController(['feed-mgr/templates/RegisteredTemplatesController'])
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
                loadMyCtrl: this.lazyLoadController(['feed-mgr/templates/new-template/RegisterNewTemplateController'])
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
                loadMyCtrl: this.lazyLoadController(['feed-mgr/templates/template-stepper/RegisterTemplateController','@uirouter/angularjs'])
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
        $ocLazyLoad.load({name:moduleName,files:['js/vendor/ment.io/styles.css','vendor/ment.io/templates']})
    }
    
    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,"feed-mgr/templates/module-require");
    }

} 
const module = new ModuleFactory();
export default module;

