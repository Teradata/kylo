define(['angular','ops-mgr/jobs/module-name',  'kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-common', 'kylo-services', 'kylo-opsmgr'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
   var module = angular.module(moduleName, []);


    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state('jobs',{
            url:'/jobs',
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/jobs/jobs.html'
                }
            },
            resolve: {
                loadPage: lazyLoad()
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Jobs',
                module:moduleName,
                permissions:AccessConstants.JOBS_ACCESS
            }
        });


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['ops-mgr/jobs/module-require']);
        }

        function lazyLoad(){
            return lazyLoadUtil.lazyLoad(['ops-mgr/jobs/module-require']);
        }

    }]);
    return module;







});




