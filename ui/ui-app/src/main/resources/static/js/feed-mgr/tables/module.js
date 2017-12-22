define(['angular', 'feed-mgr/tables/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-common', 'kylo-services','kylo-feedmgr','jquery'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
    var module = angular.module(moduleName, []);

    /**
     * LAZY loaded in from /app.js
     */
    module.config(['$stateProvider',function ($stateProvider) {
        $stateProvider.state(AccessConstants.UI_STATES.SCHEMAS.state,{
            url:'/schemas',
            params: {
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/tables/schemas.html',
                    controller:"SchemasController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/tables/SchemasController'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Schemas',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SCHEMAS.permissions
            }
        });

        $stateProvider.state(AccessConstants.UI_STATES.TABLES.state,{
            url:'/schemas/{schema}',
            params: {
                schema:null
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
                breadcrumbRoot:false,
                displayName:'Tables',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.TABLES.permissions
            }
        });

        $stateProvider.state(AccessConstants.UI_STATES.TABLE.state,{
            url:'/schemas/{schema}/{tableName}',
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
                permissions:AccessConstants.UI_STATES.TABLE.permissions
            }
        });


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,'feed-mgr/tables/module-require');
        }

    }]);










    return module;
});



