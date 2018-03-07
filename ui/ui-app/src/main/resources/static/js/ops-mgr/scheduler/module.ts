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

        $stateProvider.state(AccessConstants.UI_STATES.SCHEDULER.state,{
            url:'/scheduler',
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/scheduler/scheduler.html',
                    controller:"SchedulerController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/scheduler/SchedulerController'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Tasks',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SCHEDULER.permissions
            }
        });
    }  

    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,["ops-mgr/scheduler/module-require"]);
    }    
    lazyLoad(){
        return lazyLoadUtil.lazyLoad(['ops-mgr/scheduler/module-require']);
    }
      
} 

const module = new ModuleFactory();
export default module;


