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
                    component:"tbaServiceHealth",
                    // templateUrl: './service-health.html'
                }
            },
            resolve: {
                loadPage: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "opsmgr.service-health-details.controller" */ './module-require')
                        .then(mod => {

                            return $ocLazyLoad.load({name: moduleName})
                        })
                        .catch(err => {
                            throw new Error("Failed to load ops-mgr/service-health/module-require, " + err);
                        });
                }]
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
                    // templateUrl: './service-detail.html',
                    component:"serviceHealthDetailsController",
                    // controllerAs:"vm"
                }
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['./ServiceHealthDetailsController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "opsmgr.service-health-details.controller" */ './ServiceHealthDetailsController')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load ServiceHealthDetailsController, " + err);
                        });
                }]
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
                    // templateUrl: './service-component-detail.html',
                    component:"serviceComponentHealthDetailsController",
                    // controllerAs:"vm"
                }
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['./ServiceComponentHealthDetailsController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "opsmgr.service-component-health-details.controller" */ './ServiceComponentHealthDetailsController')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load ServiceComponentHealthDetailsController, " + err);
                        });
                }]
            },
            data:{
                displayName:'Service Component',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_COMPONENT_DETAILS.permissions
            }
        });
    }  
}

const module = new ModuleFactory();
export default module;


