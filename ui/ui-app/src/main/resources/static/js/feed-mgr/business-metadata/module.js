define(['angular','feed-mgr/business-metadata/module-name','kylo-utils/LazyLoadUtil','kylo-feedmgr','kylo-common','kylo-services'], function (angular,moduleName,lazyLoadUtil) {
    //LAZY LOADED into the application
    var module = angular.module(moduleName, []);

    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state('business-metadata',{
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
                module:moduleName
            }
        });




    }]);

    function lazyLoadController(path){
        console.log('lazyLoadProvider',lazyLoadUtil)
        return lazyLoadUtil.lazyLoadController(path,'feed-mgr/business-metadata/module-require');
    }






});