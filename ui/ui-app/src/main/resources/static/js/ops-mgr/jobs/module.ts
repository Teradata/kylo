import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import "kylo-common";
import "kylo-services";
import "kylo-opsmgr";
import AccessConstants from "../../constants/AccessConstants";

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

        $stateProvider.state(AccessConstants.UI_STATES.JOBS.state,{
            url:'/jobs',
            params: {
                filter: null,
                tab:null
            },
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/jobs/jobs.html',
                    controller:"JobsPageController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/jobs/JobsPageController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Jobs',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.JOBS.permissions
            }
        });
    }  

    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,["ops-mgr/jobs/module-require"]);
    }    
    lazyLoad(){
        return lazyLoadUtil.lazyLoad(['ops-mgr/jobs/module-require']);
    }
} 

const module = new ModuleFactory();
export default module;


