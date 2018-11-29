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
                    // templateUrl: './overview.html',
                    component:"overviewController",
                    // controllerAs:"vm"
                }
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['./OverviewController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "opsmgr.overview.controller" */ './OverviewController')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load OverviewController, " + err);
                        });
                }]
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Dashboard',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.DASHBOARD.permissions
            }
        });
    }  
}

const module = new ModuleFactory();
export default module;


