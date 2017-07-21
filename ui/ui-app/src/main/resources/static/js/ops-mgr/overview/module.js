define(['angular','ops-mgr/overview/module-name', 'kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-common', 'kylo-services','kylo-opsmgr','angular-nvd3','ops-mgr/alerts/module'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
   var module = angular.module(moduleName, []);

    module.config(['$compileProvider',function ($compileProvider) {
        //pre-assign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);
    }]);


    /**
     * LAZY loaded in from /app.js
     */
    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.DASHBOARD.state,{
            url:'/dashboard',
            params: {
            },
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/overview/overview.html',
                    controller:"OverviewController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['ops-mgr/overview/OverviewController'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Dashboard',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.DASHBOARD.permissions
            }
        });



        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['ops-mgr/overview/module-require','ops-mgr/alerts/module-require'],true);
        }

    }]);

    return module;

});




