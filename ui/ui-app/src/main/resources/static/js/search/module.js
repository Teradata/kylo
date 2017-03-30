define(['angular','search/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants', 'kylo-services','kylo-feedmgr'], function (angular,moduleName,lazyLoadUtil,AccessControl) {

    var module = angular.module(moduleName, []);

    module.config(['$stateProvider',function ($stateProvider) {
        $stateProvider.state('search',{
            url:'/search',
            params: {
            },
            views: {
                'content': {
                    templateUrl: 'js/search/elastic-search/elastic-search.html',
                    controller: "ElasticSearchController",
                    controllerAs: "vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['search/elastic-search/ElasticSearchController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Search',
                module:moduleName,
                permissions:AccessControl.SEARCH_ACCESS
            }
        })


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,'search/module-require');
        }
    }]);







    return module;

});

