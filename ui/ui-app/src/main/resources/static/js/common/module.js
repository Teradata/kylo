define(['angular','common/module-name', 'kylo-services','common/dir-pagination/dirPagination-arrows','./common.module'], function (angular,moduleName) {

    var module = angular.module(moduleName, ['kylo.services','templates.navigate-before.html', 'templates.navigate-first.html', 'templates.navigate-last.html', 'templates.navigate-next.html']);
    //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6

    module.config(['$compileProvider',function ($compileProvider) {
        //pre-assign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);
    }]);
    return module;

});

