import * as angular from 'angular';
import {moduleName} from "./module-name";
//const lazyLoadUtil = require('../kylo-utils/LazyLoadUtil');
import lazyLoadUtil from "../kylo-utils/LazyLoadUtil";
import AccessConstants from "../constants/AccessConstants";
import "../services/services.module";
const {KyloFeedManager} = require('../feed-mgr/module');

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]);
        this.module.config(['$stateProvider',this.configFn.bind(this)]);
    }
    configFn($stateProvider:any) {
            
      $stateProvider.state(AccessConstants.UI_STATES.SEARCH.state,{
            url:'/search',
            params: {
                bcExclude_globalSearchResetPaging: null
            },
            views: {
                'content': {
                   // templateUrl: 'js/search/common/search.html',
                    component: "searchController",
                    //controllerAs: "vm"
                }
            },
            resolve: {
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "kylo.search" */ "./common/SearchController")
                        .then(mod => {
                            //console.log('imported search controller module', mod);
                            return $ocLazyLoad.load(mod.default);
                        })
                        .catch(err => {
                            throw new Error("Failed to load search controller module, " + err);
                        });
                }]
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Search',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SEARCH.permissions
            }
        });
    }  

    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,"search/module-require");
    }    
} 

const module = new ModuleFactory();
export default module;


