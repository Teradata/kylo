define(['angular','feed-mgr/feeds/module-name', 'kylo-utils/LazyLoadUtil','constants/AccessConstants','angular-ui-router','kylo-feedmgr'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
    //LAZY LOADED into the application


    var module = angular.module(moduleName, []);


    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);
        $stateProvider.state('feeds', {
            url: '/feeds',
            params: {
                tab: null
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/feeds/feeds-table.html',
                    controller:'FeedsTableController',
                    controllerAs:'vm'
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController('feed-mgr/feeds/FeedsTableController')
            },
            data: {
                breadcrumbRoot: true,
                displayName: 'Feeds',
                module:moduleName,
                permissions:[AccessConstants.FEEDS_ACCESS]
            }
        });

        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['feed-mgr/feeds/module-require']);
        }




    }]);






});