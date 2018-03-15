import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import AccessConstants from "../../constants/AccessConstants";
import "kylo-common";
import "kylo-services";
import "kylo-opsmgr";
import "angular-nvd3";
import  "../alerts/module";
import "pascalprecht.translate";

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,['nvd3']);
        this.module.config(['$stateProvider','$compileProvider',this.configFn.bind(this)]);
    }
    
  
    /**
     * LAZY loaded in from /app.js
     */
    configFn($stateProvider:any, $compileProvider: any) {
       //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.DASHBOARD.state,{
            url:'/dashboard',
            params: {
            },
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/overview/overview.html',
                    controller:"OverviewController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/overview/OverviewController'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Dashboard',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.DASHBOARD.permissions
            }
        });
    }  

    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,['ops-mgr/overview/module-require','ops-mgr/alerts/module-require']);//,true);
    }    
      
} 

const module = new ModuleFactory();
export default module;


