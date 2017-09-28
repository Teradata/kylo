define(["angular", "feed-mgr/visual-query/module-name", "kylo-utils/LazyLoadUtil", "constants/AccessConstants", "kylo-common", "kylo-services", "kylo-feedmgr", "jquery",
        "feed-mgr/visual-query/build-query/flowchart/flowchart_directive"], function (angular, moduleName, lazyLoadUtil, AccessConstants) {
    var module = angular.module(moduleName, ["flowChart"]);

    /**
     * LAZY loaded in from /app.js
     */
    module.config(["$stateProvider", "$compileProvider", function ($stateProvider, $compileProvider) {
        $stateProvider.state(AccessConstants.UI_STATES.VISUAL_QUERY.state, {
            url: "/visual-query/{engine}",
            params: {
                engine: null
            },
            views: {
                "content": {
                    component: "visualQuery"
                }
            },
            resolve: {
                engine: function ($injector, $ocLazyLoad, $transition$) {
                    var engineName = (function (name) {
                        if (name === null) {
                            return "spark";
                        } else {
                            return name;
                        }
                    })($transition$.params().engine);
                    return $ocLazyLoad.load("feed-mgr/visual-query/services/query-engine-factory.service")
                        .then(function () {
                            return $injector.get("VisualQueryEngineFactory").getEngine(engineName);
                        });
                },
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
                                  "js/feed-mgr/visual-query/build-query/flowchart/flowchart.css",
                                  "js/feed-mgr/visual-query/transform-data/transform-data.component.css"
            ]
        })
    }]);

    return module;
});
