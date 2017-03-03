define(['angular','search/module-name','kylo-utils/LazyLoadUtil', 'kylo-services','kylo-feedmgr'], function (angular,moduleName,lazyLoadUtil) {

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
                module:moduleName
            }
        })


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,'search/module-require');
        }
    }]);







    return module;

});

