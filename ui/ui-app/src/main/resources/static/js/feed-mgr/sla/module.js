define(['angular', 'feed-mgr/sla/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-common', 'kylo-services','kylo-feedmgr','jquery'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
    var module = angular.module(moduleName, []);

    /**
     * LAZY loaded in from /app.js
     */
    module.config(['$stateProvider',function ($stateProvider) {
        $stateProvider.state(AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.state,{
            url:'/service-level-agreements/:slaId',
            params: {
                slaId:null
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/sla/service-level-agreements-view.html',
                    controller:"ServiceLevelAgreementInitController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/sla/service-level-agreement','feed-mgr/sla/ServiceLevelAgreementInitController'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Service Level Agreements',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.permissions
            }
        })

        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,'feed-mgr/sla/module-require');
        }

    }]);









    return module;
});



