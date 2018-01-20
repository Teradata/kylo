define(["angular", "feed-mgr/visual-query/module-name", "kylo-utils/LazyLoadUtil", "constants/AccessConstants", "kylo-common", "kylo-services", "kylo-feedmgr", "jquery",
        "feed-mgr/visual-query/build-query/flowchart/flowchart_directive"], function (angular, moduleName, lazyLoadUtil, AccessConstants) {
    var module = angular.module(moduleName, ["kylo.common", "kylo.feedmgr", "kylo.services", "kylo.ui-codemirror", "flowChart"]);

    module.run(['$ocLazyLoad', function ($ocLazyLoad) {
        $ocLazyLoad.load({
            name: 'kylo', files: ["bower_components/fattable/fattable.css",
                                  "js/feed-mgr/visual-query/visual-query.component.css",
                                  "js/feed-mgr/visual-query/build-query/flowchart/flowchart.css",
                                  "js/feed-mgr/visual-query/transform-data/transform-data.component.css"
            ]
        })
    }]);

    return module;
});
