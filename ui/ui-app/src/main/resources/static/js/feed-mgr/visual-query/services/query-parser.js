define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Parses a query and generates a transform script.
     */
    var QueryParser = (function () {
        /**
         * Constructs a {@code QueryParser}.
         */
        function QueryParser(VisualQueryService) {
            this.VisualQueryService = VisualQueryService;
        }
        /**
         * Generates a Spark script for the specified visual query model and data sources.
         *
         * @param source - the SQL query or visual query model
         * @param datasources - the list of datasources used
         * @returns the Spark script
         * @throws {Error} if the source or datasources are not valid
         */
        QueryParser.prototype.toScript = function (source, datasources) {
            if (typeof source === "string") {
                return this.fromSql(source, datasources);
            }
            else if (typeof source === "object") {
                return this.fromVisualQueryModel(source);
            }
        };
        /**
         * Generates a transform script for the specified select statement and data sources.
         *
         * @param tree - the select statement
         * @returns  the transform script
         * @throws {Error} if the tree is not valid
         */
        QueryParser.prototype.fromSelectStmt = function (tree) {
            throw new Error("method not supported");
        };
        /**
         * Generates a Spark script for the specified visual query model and data sources.
         *
         * @param visualQueryModel - the visual query model
         * @returns the transform script
         * @throws {Error} if the model is not valid
         */
        QueryParser.prototype.fromVisualQueryModel = function (visualQueryModel) {
            var tree = this.VisualQueryService.sqlBuilder(visualQueryModel).buildTree();
            return this.fromSelectStmt(tree);
        };
        return QueryParser;
    }());
    exports.QueryParser = QueryParser;
});
//# sourceMappingURL=query-parser.js.map