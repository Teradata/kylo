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
        this.module.run(['$ocLazyLoad', this.runFn.bind(this)]); 
       
    }

    runFn($ocLazyLoad: any){
        $ocLazyLoad.load({name:'kylo',files:['bower_components/c3/c3.css',
                                             'js/ops-mgr/charts/pivot.css'
        ]})
    }
    
    configFn($stateProvider:any, $compileProvider: any) {
 //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.CHARTS.state,{
            url:'/charts',
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/charts/charts.html',
                    controller:"ChartsController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/charts/ChartsController','pivottable-c3-renderers'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Charts',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.CHARTS.permissions
            }
        });
    }  

    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,['ops-mgr/charts/module-require']);
    }    
    lazyLoad(){
        return lazyLoadUtil.lazyLoad(['ops-mgr/charts/module-require']);
    }
} 

const module = new ModuleFactory();
export default module;


