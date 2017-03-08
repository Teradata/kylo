define(['angular', 'feed-mgr/module-name', 'codemirror-require/module', 'angular-ui-grid', 'kylo-common', 'kylo-services'], function (angular, moduleName) {
    var module = angular.module(moduleName, ['ui.codemirror', 'ui.grid', 'ui.grid.resizeColumns', 'ui.grid.autoResize', 'ui.grid.moveColumns', 'ui.grid.pagination']);

    module.run(['$ocLazyLoad', function ($ocLazyLoad) {
        $ocLazyLoad.load({
            name: 'kylo', files: ['bower_components/angular-ui-grid/ui-grid.css', 'assets/ui-grid-material.css'
            ], serie: true
        })
    }]);
    return module;

});
