import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import "kylo-common";
import "kylo-services";
import "kylo-opsmgr";
import "jquery";
import "jquery-ui";
import "pivottable";
import AccessConstants from "../../constants/AccessConstants";
//const AccessConstants = require("../../constants/AccessConstants");

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

        $stateProvider.state(AccessConstants.UI_STATES.CHARTS.state,{
            url:'/charts',
            views: {
                'content': {
                    templateUrl: './charts.html',
                    controller:"ChartsController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "opsmgr.charts.controller" */ './ChartsController')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load ChartsController, " + err);
                        });
                }]
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Charts',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.CHARTS.permissions
            }
        });
    }  
}

const module = new ModuleFactory();
export default module;


