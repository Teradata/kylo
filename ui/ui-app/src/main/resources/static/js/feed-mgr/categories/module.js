define(['angular','feed-mgr/categories/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants','app','@uirouter/angularjs','kylo-feedmgr', 'pascalprecht.translate'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
    //LAZY LOADED into the application
    var module = angular.module(moduleName, ['ui.router']);

    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider,$filter) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.default.UI_STATES.CATEGORIES.state,{
            url:'/categories',
            params: {
            },
            views: {
                'content': {
                    component: "categoriesController"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/categories/CategoriesController'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Categories',
                module:moduleName,
                permissions:AccessConstants.default.UI_STATES.CATEGORIES.permissions
            }
        }).state(AccessConstants.default.UI_STATES.CATEGORY_DETAILS.state,{
            url:'/category-details/{categoryId}',
            params: {
                categoryId:null
            },
            views: {
                'content': {
                    component: "categoryDetailsController"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/categories/category-details'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Category Details',
                module:moduleName,
                permissions:AccessConstants.default.UI_STATES.CATEGORY_DETAILS.permissions
            }
        })




    }]);

    function lazyLoadController(path){
        return lazyLoadUtil.default.lazyLoadController(path,'feed-mgr/categories/module-require');
    }






});
