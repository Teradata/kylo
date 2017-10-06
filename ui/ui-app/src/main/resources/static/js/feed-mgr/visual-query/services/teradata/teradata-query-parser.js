var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
define(["require", "exports", "../query-parser", "../../../services/VisualQueryService"], function (require, exports, query_parser_1, VisualQueryService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Handles transformations from a visual query model to Teradata.
     */
    var TeradataQueryParser = (function (_super) {
        __extends(TeradataQueryParser, _super);
        /**
         * Constructs a {@code SparkQueryParser}.
         */
        function TeradataQueryParser(VisualQueryService) {
            return _super.call(this, VisualQueryService) || this;
        }
        /**
         * Generates a Spark script for the specified SQL query and optional data source.
         *
         * @param  sql - the SQL query
         * @param datasources - the data source
         * @returns the Spark script
         * @throws {Error} if there are too many data sources
         */
        TeradataQueryParser.prototype.fromSql = function (sql, datasources) {
            return sql;
        };
        /**
         * Generates a Spark script for the specified visual query model and data sources.
         *
         * @param visualQueryModel - the visual query model
         */
        TeradataQueryParser.prototype.fromVisualQueryModel = function (visualQueryModel) {
            return this.VisualQueryService.sqlBuilder(visualQueryModel, VisualQueryService_1.SqlDialect.TERADATA).build();
        };
        return TeradataQueryParser;
    }(query_parser_1.QueryParser));
    exports.TeradataQueryParser = TeradataQueryParser;
});
//# sourceMappingURL=teradata-query-parser.js.map