define(['angular', 'feed-mgr/tables/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-common', 'kylo-services','kylo-feedmgr','jquery'], function (angular,moduleName,lazyLoadUtil,AccessControl) {
    var module = angular.module(moduleName, []);

    /**
     * LAZY loaded in from /app.js
     */
    module.config(['$stateProvider',function ($stateProvider) {
        $stateProvider.state('tables',{
            url:'/tables',
            params: {
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/tables/tables.html',
                    controller:"TablesController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/tables/TablesController'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Tables',
                module:moduleName,
                permissions:AccessControl.TABLES_ACCESS
            }
        });

        $stateProvider.state('table',{
            url:'/tables/{schema}/{tableName}',
            params: {
                schema:null,
                tableName:null
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/tables/table.html',
                    controller:"TableController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/tables/TableController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Table Details',
                module:moduleName,
                permissions:AccessControl.TABLES_ACCESS
            }
        })


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,'feed-mgr/tables/module-require');
        }

    }]);










    return module;
});



