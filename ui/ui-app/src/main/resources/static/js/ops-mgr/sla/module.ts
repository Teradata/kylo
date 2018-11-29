import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil, {Lazy} from "../../kylo-utils/LazyLoadUtil";
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
        $stateProvider.state(AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.state,{
            url:'/service-level-assessments',
            params: {
                filter: null
            },
            views: {
                'content': {
                    templateUrl: './assessments.html',
                    controller:"serviceLevelAssessmentsInitController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    const onModuleLoad = () => {
                        return import(/* webpackChunkName: "ops-mgr.slas.ServiceLevelAssessmentsInitController" */ "./ServiceLevelAssessmentsInitController")
                            .then(Lazy.onModuleImport($ocLazyLoad));
                    };
                    return import(/* webpackChunkName: "ops-mgr.slas.service-level-assessments" */ "./service-level-assessments").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
                }]
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Service Level Assessments',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.permissions
            }
        });


        $stateProvider.state(AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENT.state,{
            url:'/service-level-assessment/{assessmentId}',
            params: {
                assessmentId:null
            },
            views: {
                'content': {
                    templateUrl: './assessment.html',
                    controller:"serviceLevelAssessmentController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "opsmgr.sla.controller" */ './service-level-assessment')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load ServiceLevelAssessmentsInitController, " + err);
                        });
                }]
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Service Level Assessment',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENT.permissions
            }
        });
    }  

    // lazyLoadController(path:any){
    //     return lazyLoadUtil.lazyLoadController(path,["./module-require"]);
    // }
    // lazyLoad(){
    //     return lazyLoadUtil.lazyLoad(['./module-require']);
    // }
} 

const module = new ModuleFactory();
export default module;


