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
         $stateProvider.state(AccessConstants.UI_STATES.ALERTS.state,{
            url:'/alerts',
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/alerts/alerts-table.html',
                    controller:'AlertsController',
                    controllerAs:'vm'
                }
            },
            params: {
                query: null
            },
            resolve: {
                loadPage: this.lazyLoad()
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
                    templateUrl: 'js/ops-mgr/alerts/alert-details.html',
                    controller:'AlertDetailsController',
                    controllerAs:'vm'
                }
            },
            params: {
                alertId: null
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/alerts/AlertDetailsController'])
            },
            data:{
                displayName:'Alert Details',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.ALERT_DETAILS.permissions
            }
        });
    }  

    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,["ops-mgr/alerts/module-require"]);
    }    
    lazyLoad(){
        return lazyLoadUtil.lazyLoad(['ops-mgr/alerts/module-require']);
    }
} 

const module = new ModuleFactory();
export default module;


