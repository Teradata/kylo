define(['angular','ops-mgr/sla/module-name',  'kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-common', 'kylo-services', 'kylo-opsmgr'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
   var module = angular.module(moduleName, []);


    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
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
                loadMyCtrl: lazyLoadController(['ops-mgr/sla/ServiceLevelAssessmentsInitController'])
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
                loadMyCtrl: lazyLoadController(['ops-mgr/sla/service-level-assessment'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Service Level Assessment',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENT.permissions
            }
        });


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['ops-mgr/sla/module-require']);
        }

        function lazyLoad(){
            return lazyLoadUtil.lazyLoad(['ops-mgr/sla/module-require']);
        }

    }]);
    return module;







});




