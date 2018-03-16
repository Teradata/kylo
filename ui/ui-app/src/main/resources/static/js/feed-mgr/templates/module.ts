import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import AccessConstants from '../../constants/AccessConstants';
import "@uirouter/angularjs";
import "kylo-feedmgr";
import "kylo-common";
import {KyloServicesModule} from "../../services/services.module";
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
                    templateUrl: 'js/feed-mgr/templates/registered-templates.html',
                    controller:'RegisteredTemplatesController',
                    controllerAs:'vm'
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
                    templateUrl: 'js/feed-mgr/templates/new-template/register-new-template.html',
                    controller:'RegisterNewTemplateController',
                    controllerAs:'vm'
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
                    templateUrl: 'js/feed-mgr/templates/template-stepper/register-template.html',
                    controller:'RegisterTemplateController',
                    controllerAs:'vm'
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
                    templateUrl: 'js/feed-mgr/templates/template-stepper/register-template/register-template-complete.html',
                    controller:'RegisterTemplateCompleteController',
                    controllerAs:'vm'
                }
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Register Template',
                module:moduleName,
                permissions:AccessConstants.TEMPLATES_EDIT
            }
        }).state('import-template',{
            url:'/import-template',
            params: {
            },
            views: {
                'content': {
                    templateUrl:  'js/feed-mgr/templates/import-template/import-template.html',
                    controller:'ImportTemplateController',
                    controllerAs:'vm'
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['feed-mgr/templates/import-template/ImportTemplateController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Template Manager',
                module:moduleName,
                permissions:AccessConstants.TEMPLATES_IMPORT
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

