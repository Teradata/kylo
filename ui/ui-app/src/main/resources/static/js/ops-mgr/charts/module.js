define(['angular','ops-mgr/charts/module-name', 'kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-common', 'kylo-services','kylo-opsmgr','jquery','jquery-ui','pivottable'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
   var module = angular.module(moduleName, []);



    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
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
                loadMyCtrl: lazyLoadController(['ops-mgr/charts/ChartsController','pivottable-c3-renderers'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Charts',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.CHARTS.permissions
            }
        });




        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['ops-mgr/charts/module-require']);
        }

        function lazyLoad(){
            return lazyLoadUtil.lazyLoad(['ops-mgr/charts/module-require']);
        }

    }]);

    module.run(['$ocLazyLoad',function($ocLazyLoad){
        $ocLazyLoad.load({name:'kylo',files:['bower_components/c3/c3.css',
                                             'js/ops-mgr/charts/pivot.css'
        ]})
    }])
    return module;
});




