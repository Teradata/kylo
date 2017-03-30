define(['angular','ops-mgr/scheduler/module-name',  'kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-common', 'kylo-services', 'kylo-opsmgr'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
   var module = angular.module(moduleName, []);

    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state('scheduler',{
            url:'/scheduler',
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/scheduler/scheduler.html',
                    controller:"SchedulerController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['ops-mgr/scheduler/SchedulerController'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Scheduler',
                module:moduleName,
                permissions:AccessConstants.SLA_SCHEDULER_ACCESS
            }
        });

        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['ops-mgr/scheduler/module-require']);
        }

        function lazyLoad(){
            return lazyLoadUtil.lazyLoad(['ops-mgr/scheduler/module-require']);
        }

    }]);
    return module;

});




