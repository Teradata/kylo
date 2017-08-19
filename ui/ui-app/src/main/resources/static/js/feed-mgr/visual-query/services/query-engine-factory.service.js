define(["require", "exports", "./spark/spark-query-engine"], function (require, exports, spark_query_engine_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    /**
     * A factory for creating {@code QueryEngine} objects.
     */
    var QueryEngineFactory = (function () {
        /**
         * Constructs a {@code QueryEngineFactory}.
         */
        function QueryEngineFactory($http, $timeout, DatasourcesService, HiveService, RestUrlService, VisualQueryService) {
            this.$http = $http;
            this.$timeout = $timeout;
            this.DatasourcesService = DatasourcesService;
            this.HiveService = HiveService;
            this.RestUrlService = RestUrlService;
            this.VisualQueryService = VisualQueryService;
        }
        /**
         * Creates a new engine of the specified type.
         *
         * @param name - the type of engine
         * @returns the query engine
         */
        QueryEngineFactory.prototype.getEngine = function (name) {
            var standardName = name.toLowerCase();
            if (standardName === "spark") {
                return new spark_query_engine_1.SparkQueryEngine(this.$http, this.$timeout, this.DatasourcesService, this.HiveService, this.RestUrlService, this.VisualQueryService);
            }
            else {
                throw new Error("Unsupported query engine: " + name);
            }
        };
        return QueryEngineFactory;
    }());
    exports.QueryEngineFactory = QueryEngineFactory;
    angular.module(moduleName).service("VisualQueryEngineFactory", ["$http", "$timeout", "DatasourcesService", "HiveService", "RestUrlService", "VisualQueryService",
        QueryEngineFactory]);
});
//# sourceMappingURL=query-engine-factory.service.js.map