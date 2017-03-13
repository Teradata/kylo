define(['angular','ops-mgr/module-name', 'kylo-common', 'kylo-services','jquery'], function (angular,moduleName) {
   var module = angular.module(moduleName, []);

    module.config(['$compileProvider',function ($compileProvider) {
        //pre-assign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);
    }]);

return module;

});




