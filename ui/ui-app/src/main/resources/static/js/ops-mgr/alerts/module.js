define(['angular','ops-mgr/alerts/module-name', 'kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-common', 'kylo-services','kylo-opsmgr'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
   var module = angular.module(moduleName, []);

    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.ALERTS.state,{
            url:'/alerts',
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/alerts/alerts-table.html',
                    controller:'AlertsController',
                    controllerAs:'vm'
                }
            },
            params: {
                query: null
            },
            resolve: {
                loadPage: lazyLoad()
            },
            data:{
                displayName:'Alerts',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.ALERTS.permissions
            }
        }).state(AccessConstants.UI_STATES.ALERT_DETAILS.state,{
            url:"/alert-details/{alertId}",
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/alerts/alert-details.html',
                    controller:'AlertDetailsController',
                    controllerAs:'vm'
                }
            },
            params: {
                alertId: null
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['ops-mgr/alerts/AlertDetailsController'])
            },
            data:{
                displayName:'Alert Details',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.ALERT_DETAILS.permissions
            }
        })

        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['ops-mgr/alerts/module-require']);
        }

        function lazyLoad(){
            return lazyLoadUtil.lazyLoad(['ops-mgr/alerts/module-require']);
        }

    }]);
    return module;


});




