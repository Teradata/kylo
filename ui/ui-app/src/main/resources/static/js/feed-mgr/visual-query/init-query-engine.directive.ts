import {QueryEngineFactory} from "./wrangler/query-engine-factory.service";

declare const angular: angular.IAngularStatic;

import {moduleName} from "./module-name";

angular.module(moduleName).directive("kyloInitQueryEngine", ["VisualQueryEngineFactory", function (QueryEngineFactory: QueryEngineFactory) {
    return {
        restrict: "A",
        link: function ($scope: any, element: JQuery, attrs: { [k: string]: string }) {
            $scope.queryEngine = QueryEngineFactory.getEngine(attrs.kyloInitQueryEngine);
        }
    };
}]);
