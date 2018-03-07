import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../../kylo-utils/LazyLoadUtil";
import "kylo-common";
import "kylo-services";
import "kylo-opsmgr";
import "../module"; //ops-mgr/jobs/module
import AccessConstants from "../../../constants/AccessConstants";

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

        $stateProvider.state(AccessConstants.UI_STATES.JOB_DETAILS.state,{
            url:'/job-details/{executionId}',
            params: {
                executionId:null
            },
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/jobs/details/job-details.html',
                    controller:"JobDetailsController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/jobs/details/JobDetailsController'])
            },
            data:{
                displayName:'Job Details',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.JOB_DETAILS.permissions
            }
        });
    }  

    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,['ops-mgr/jobs/module-require','ops-mgr/jobs/details/module-require']);
    }    
    lazyLoad(){
        return lazyLoadUtil.lazyLoad(['ops-mgr/jobs/module-require','ops-mgr/jobs/details/module-require']);
    }
} 

const module = new ModuleFactory();
export default module;


