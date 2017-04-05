define(['angular','feed-mgr/business-metadata/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants','kylo-feedmgr','kylo-common','kylo-services'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
    //LAZY LOADED into the application
    var module = angular.module(moduleName, []);

    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.BUSINESS_METADATA.state,{
            url:'/business-metadata',
            params: {},
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/business-metadata/business-metadata.html',
                    controller:'BusinessMetadataController',
                    controllerAs:'vm'
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/business-metadata/BusinessMetadataController'])
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Business Metadata',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.BUSINESS_METADATA.permissions
            }
        });




    }]);

    function lazyLoadController(path){
        return lazyLoadUtil.lazyLoadController(path,'feed-mgr/business-metadata/module-require');
    }






});