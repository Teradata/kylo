define(['angular', 'feed-mgr/module-name', 'codemirror-require/module', 'kylo-common', 'kylo-services', 'jquery','angular-drag-and-drop-lists'], function (angular, moduleName) {
    var module = angular.module(moduleName, ['ui.codemirror','dndLists']);

    module.run(['$ocLazyLoad', function ($ocLazyLoad) {
        $ocLazyLoad.load({
            name: 'kylo', files: ['bower_components/angular-ui-grid/ui-grid.css', 'assets/ui-grid-material.css', 'js/feed-mgr/shared/cron-expression-preview/cron-expression-preview.css'
            ], serie: true
        })
    }]);
    return module;

});
