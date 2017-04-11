define(['angular','ops-mgr/service-health/module-name', 'kylo-utils/LazyLoadUtil', 'constants/AccessConstants','kylo-common', 'kylo-services','kylo-opsmgr'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
   var module = angular.module(moduleName, []);



    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.SERVICE_HEALTH.state,{
            url:'/service-health',
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/service-health/service-health.html'
                }
            },
            resolve: {
                loadPage: lazyLoad()
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Service Health',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_HEALTH.permissions
            }
        }).state(AccessConstants.UI_STATES.SERVICE_DETAILS.state,{
            url:'/service-details/:serviceName',
            params: {
                serviceName: null
            },
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/service-health/service-detail.html',
                    controller:"ServiceHealthDetailsController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['ops-mgr/service-health/ServiceHealthDetailsController'])
            },
            data:{
                displayName:'Service Details',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_DETAILS.permissions
            }
        }).state(AccessConstants.UI_STATES.SERVICE_COMPONENT_DETAILS.state,{
            url:'/service-details/{serviceName}/{componentName}',
            params: {
                serviceName: null
            },
            views: {
                'content': {
                    templateUrl: 'js/ops-mgr/service-health/service-component-detail.html',
                    controller:"ServiceComponentHealthDetailsController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['ops-mgr/service-health/ServiceComponentHealthDetailsController'])
            },
            data:{
                displayName:'Service Component',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_COMPONENT_DETAILS.permissions
            }
        });


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['ops-mgr/service-health/module-require']);
        }

        function lazyLoad(){
            return lazyLoadUtil.lazyLoad(['ops-mgr/service-health/module-require']);
        }

    }]);
    return module;









});




