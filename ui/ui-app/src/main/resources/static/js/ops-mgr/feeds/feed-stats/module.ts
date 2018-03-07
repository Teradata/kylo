import * as angular from 'angular';
import {moduleName} from "./module-name";

import lazyLoadUtil from "../../../kylo-utils/LazyLoadUtil";
import AccessConstants from "../../../constants/AccessConstants";
import "kylo-common";
import "kylo-services";
import "kylo-opsmgr";
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

        $stateProvider.state(AccessConstants.UI_STATES.FEED_STATS.state,{
            url:'/feed-stats/{feedName}',
            params:{
               feedName:null
            },
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/feeds/feed-stats/feed-stats.html',
                    controller:"FeedStatsController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/feeds/feed-stats/feed-stats'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Feed Stats',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.FEED_STATS.permissions
            }
        });
    }

     lazyLoadController=(path: any)=>{
            return lazyLoadUtil.lazyLoadController(path,['ops-mgr/feeds/feed-stats/module-require']);
        }

     lazyLoad=()=>{
            return lazyLoadUtil.lazyLoad(['ops-mgr/feeds/feed-stats/module-require']);
        }

} 

const module = new ModuleFactory();
export default module;


