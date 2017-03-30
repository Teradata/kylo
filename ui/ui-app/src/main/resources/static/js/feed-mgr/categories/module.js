define(['angular','feed-mgr/categories/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants','app','angular-ui-router','kylo-feedmgr'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
    //LAZY LOADED into the application
    var module = angular.module(moduleName, ['ui.router']);

    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state('categories',{
            url:'/categories',
            params: {
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/categories/categories.html',
                    controller:'CategoriesController',
                    controllerAs:'vm'
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/categories/CategoriesController'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Categories',
                module:moduleName,
                permissions:AccessConstants.CATEGORIES_ACCESS
            }
        }).state('category-details',{
            url:'/category-details/{categoryId}',
            params: {
                categoryId:null
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/categories/category-details.html',
                    controller: 'CategoryDetailsController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/categories/category-details'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Category Details',
                module:moduleName,
                permissions:AccessConstants.CATEGORIES_ACCESS
            }
        })




    }]);

    function lazyLoadController(path){
        return lazyLoadUtil.lazyLoadController(path,'feed-mgr/categories/module-require');
    }






});