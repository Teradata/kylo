import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import AccessConstants from "../../constants/AccessConstants";
import "kylo-common";
import "kylo-services";
import "kylo-opsmgr";
import "../alerts/module";
import "../overview/module";
import 'angular-nvd3';

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,['nvd3']);
        this.module.config(['$stateProvider','$compileProvider',this.configFn.bind(this)]);
    }
    configFn($stateProvider: any,$compileProvider:any) {
      //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.OPS_FEED_DETAILS.state,{
            url:'/ops-feed-details/{feedName}',
            params: {
               feedName:null
            },
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/feeds/feed-details.html',
                    controller:"OpsManagerFeedDetailsController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/feeds/FeedDetailsController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Feed Details',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.OPS_FEED_DETAILS.permissions
            }
        });
    }

     lazyLoadController=(path: any)=>{
            return lazyLoadUtil.lazyLoadController(path,['ops-mgr/jobs/module','ops-mgr/jobs/module-require','ops-mgr/feeds/module-require','ops-mgr/alerts/module-require','ops-mgr/overview/module-require']);
        }
} 

const module = new ModuleFactory();
export default module;


