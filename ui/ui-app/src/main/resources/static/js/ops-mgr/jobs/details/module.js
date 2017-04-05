define(['angular','ops-mgr/jobs/details/module-name', 'kylo-utils/LazyLoadUtil','constants/AccessConstants', 'kylo-common', 'kylo-services','kylo-opsmgr','ops-mgr/jobs/module'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
   var module = angular.module(moduleName, []);

    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
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
                loadMyCtrl: lazyLoadController(['ops-mgr/jobs/details/JobDetailsController'])
            },
            data:{
                displayName:'Job Details',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.JOB_DETAILS.permissions
            }
        });


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['ops-mgr/jobs/module-require','ops-mgr/jobs/details/module-require']);
        }

        function lazyLoad(){
            return lazyLoadUtil.lazyLoad(['ops-mgr/jobs/module-require','ops-mgr/jobs/details/module-require']);
        }

    }]);
    return module;
});




