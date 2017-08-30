define(['angular','search/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants', 'kylo-services','kylo-feedmgr'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {

    var module = angular.module(moduleName, []);

    module.config(['$stateProvider',function ($stateProvider) {
        $stateProvider.state(AccessConstants.UI_STATES.SEARCH.state,{
            url:'/search',
            params: {
                bcExclude_globalSearchResetPaging: null
            },
            views: {
                'content': {
                    templateUrl: 'js/search/common/search.html',
                    controller: "SearchController",
                    controllerAs: "vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['search/common/SearchController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Search',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SEARCH.permissions
            }
        })


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,'search/module-require');
        }
    }]);







    return module;

});

