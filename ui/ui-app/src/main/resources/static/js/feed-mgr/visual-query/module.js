define(["angular", "feed-mgr/visual-query/module-name", "kylo-utils/LazyLoadUtil", "constants/AccessConstants", "kylo-common", "kylo-services", "kylo-feedmgr", "jquery",
        "feed-mgr/visual-query/flowchart/flowchart_directive"], function (angular, moduleName, lazyLoadUtil, AccessConstants) {
    var module = angular.module(moduleName, ["flowChart"]);

    /**
     * LAZY loaded in from /app.js
     */
    module.config(["$stateProvider", "$compileProvider", function ($stateProvider, $compileProvider) {
        //pre-assign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.VISUAL_QUERY.state, {
            url: "/visual-query/{engine}",
            params: {
                engine: null
            },
            views: {
                "content": {
                    templateUrl: "js/feed-mgr/visual-query/visual-query.template.html",
                    controller: "VisualQueryComponent",
                    controllerAs: "vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(["feed-mgr/visual-query/visual-query.component"])
            },
            data: {
                breadcrumbRoot: true,
                displayName: "Visual Query",
                module: moduleName,
                permissions: AccessConstants.UI_STATES.VISUAL_QUERY.permissions
            }
        });

        function lazyLoadController(path) {
            return lazyLoadUtil.lazyLoadController(path, "feed-mgr/visual-query/module-require", true);
        }
    }]);

    module.run(['$ocLazyLoad', function ($ocLazyLoad) {
        $ocLazyLoad.load({
            name: 'kylo', files: ["bower_components/fattable/fattable.css",
                                  "js/feed-mgr/visual-query/visual-query.component.css",
                                  "js/feed-mgr/visual-query/flowchart/flowchart.css"
            ]
        })
    }]);

    return module;
});
