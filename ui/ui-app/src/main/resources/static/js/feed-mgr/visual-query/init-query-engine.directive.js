define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    angular.module(moduleName).directive("kyloInitQueryEngine", ["VisualQueryEngineFactory", function (QueryEngineFactory) {
            return {
                restrict: "A",
                link: function ($scope, element, attrs) {
                    $scope.queryEngine = QueryEngineFactory.getEngine(attrs.kyloInitQueryEngine);
                }
            };
        }]);
});
//# sourceMappingURL=init-query-engine.directive.js.map