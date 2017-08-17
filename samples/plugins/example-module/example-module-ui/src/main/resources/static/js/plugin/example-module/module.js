define(['angular', 'plugin/example-module/module-name', 'kylo-utils/LazyLoadUtil', 'constants/AccessConstants', 'kylo-common', 'kylo-services', 'kylo-opsmgr'], function (angular, moduleName, lazyLoadUtil, AccessConstants) {
    var module = angular.module(moduleName, []);


    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state('exampleModule',{
            url:'/exampleModule',
            params: {
                name: null
            },
            views: {
                'content': {
                    templateUrl: 'js/plugin/example-module/example-module.html',
                    controller:"ExampleModuleController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['plugin/example-module/ExampleModuleController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Example Page',
                module:moduleName,
                permissions:[]
            }
        });


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['plugin/example-module/module-require']);
        }

        function lazyLoad(){
            return lazyLoadUtil.lazyLoad(['plugin/example-module/module-require']);
        }




    }]);
    return module;







});
