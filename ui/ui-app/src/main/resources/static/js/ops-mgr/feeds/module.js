define(['angular','ops-mgr/feeds/module-name', 'kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-common', 'kylo-services','kylo-opsmgr','angular-nvd3'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
   var module = angular.module(moduleName, ['nvd3']);

    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state('ops-feed-details',{
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
                loadMyCtrl: lazyLoadController(['ops-mgr/feeds/FeedDetailsController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Feed Details',
                module:moduleName,
                permissions:AccessConstants.FEED_OPERATIONS_DETAIL_ACCESS
            }
        });


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['ops-mgr/jobs/module','ops-mgr/jobs/module-require','ops-mgr/feeds/module-require']);
        }

    }]);
    return module;
});




