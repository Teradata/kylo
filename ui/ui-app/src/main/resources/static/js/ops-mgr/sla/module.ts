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
        $stateProvider.state(AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.state,{
            url:'/service-level-assessments',
            params: {
                filter: null
            },
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/sla/assessments.html',
                    controller:"ServiceLevelAssessmentsInitController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/sla/ServiceLevelAssessmentsInitController'])
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
                    templateUrl: 'js/ops-mgr/sla/assessment.html',
                    controller:"ServiceLevelAssessmentController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['ops-mgr/sla/service-level-assessment'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Service Level Assessment',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENT.permissions
            }
        });
    }  

    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,["ops-mgr/sla/module-require"]);
    }    
    lazyLoad(){
        return lazyLoadUtil.lazyLoad(['ops-mgr/sla/module-require']);
    }
} 

const module = new ModuleFactory();
export default module;


