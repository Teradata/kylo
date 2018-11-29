import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import AccessConstants from "../../constants/AccessConstants";
import "kylo-common";
import "kylo-services";
import "kylo-opsmgr";
import {StateProvider} from "@uirouter/angularjs";

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]);
        this.module.config(['$stateProvider','$compileProvider',this.configFn.bind(this)]);
    }
    configFn($stateProvider:StateProvider, $compileProvider: angular.ICompileProvider) {
         $stateProvider.state(AccessConstants.UI_STATES.ALERTS.state,{
            url:'/alerts',
            views: {
                'content': {
                   // templateUrl: './alerts-table.html',
                    component:'alertsController',
                    //controllerAs:'vm'
                }
            },
            params: {
                query: null
            },
            resolve: {
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "opsmgr.alert-details.controller" */ './AlertDetailsController')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load AlertDetailsController, " + err);
                        });
                }]
            },
            data:{
                displayName:'Alerts',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.ALERTS.permissions
            }
        }).state(AccessConstants.UI_STATES.ALERT_DETAILS.state,{
            url:"/alert-details/{alertId}",
            views: {
                'content': {
                    //templateUrl: './alert-details.html',
                    component:'alertDetailsController',
                    //controllerAs:'vm'
                }
            },
            params: {
                alertId: null
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['./AlertDetailsController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "opsmgr.alert-details.controller" */ './AlertDetailsController')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load AlertDetailsController, " + err);
                        });
                }]
            },
            data:{
                displayName:'Alert Details',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.ALERT_DETAILS.permissions
            }
        });
    }  
}

const module = new ModuleFactory();
export default module;


