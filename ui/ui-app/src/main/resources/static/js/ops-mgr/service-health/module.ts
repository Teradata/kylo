import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import AccessConstants from "../../constants/AccessConstants";
import "kylo-common";
import "kylo-services";
import "kylo-opsmgr";

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]);
        this.module.config(['$stateProvider','$compileProvider',this.configFn.bind(this)]);
    }
    configFn($stateProvider:any, $compileProvider: any) {
           //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.SERVICE_HEALTH.state,{
            url:'/service-health',
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/service-health/service-health.html'
                }
            },
            resolve: {
                loadPage: this.lazyLoad()
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Service Health',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_HEALTH.permissions
            }
        }).state(AccessConstants.UI_STATES.SERVICE_DETAILS.state,{
            url:'/service-details/:serviceName',
            params: {
                serviceName: null
            },
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/service-health/service-detail.html',
                    controller:"ServiceHealthDetailsController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/service-health/ServiceHealthDetailsController'])
            },
            data:{
                displayName:'Service Details',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_DETAILS.permissions
            }
        }).state(AccessConstants.UI_STATES.SERVICE_COMPONENT_DETAILS.state,{
            url:'/service-details/{serviceName}/{componentName}',
            params: {
                serviceName: null
            },
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/service-health/service-component-detail.html',
                    controller:"ServiceComponentHealthDetailsController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/service-health/ServiceComponentHealthDetailsController'])
            },
            data:{
                displayName:'Service Component',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_COMPONENT_DETAILS.permissions
            }
        });
    }  

    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,["ops-mgr/service-health/module-require"]);
    }    
    lazyLoad(){
        return lazyLoadUtil.lazyLoad(['ops-mgr/service-health/module-require']);
    }
      
} 

const module = new ModuleFactory();
export default module;


