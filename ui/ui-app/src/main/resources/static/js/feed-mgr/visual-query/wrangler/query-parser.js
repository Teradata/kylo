define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Parses a query and generates a transform script.
     */
    var QueryParser = /** @class */ (function () {
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
        return QueryParser;
    }());
    exports.QueryParser = QueryParser;
});
//# sourceMappingURL=query-parser.js.map