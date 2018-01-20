define(["require", "exports", "./column-delegate", "./parse-exception", "./query-engine", "./query-engine-constants", "./query-engine-factory.service", "./query-parser", "./script-builder", "./script-expression", "./script-expression-type"], function (require, exports, column_delegate_1, parse_exception_1, query_engine_1, query_engine_constants_1, query_engine_factory_service_1, query_parser_1, script_builder_1, script_expression_1, script_expression_type_1) {
    "use strict";
    function __export(m) {
        for (var p in m) if (!exports.hasOwnProperty(p)) exports[p] = m[p];
    }
    Object.defineProperty(exports, "__esModule", { value: true });
    __export(column_delegate_1);
    __export(parse_exception_1);
    __export(query_engine_1);
    __export(query_engine_constants_1);
    __export(query_engine_factory_service_1);
    __export(query_parser_1);
    __export(script_builder_1);
    __export(script_expression_1);
    __export(script_expression_type_1);
    exports.StringUtils = window.StringUtils;
    /**
     * Supported SQL dialects
     */
    var SqlDialect;
    (function (SqlDialect) {
        /** Hive Query Language dialect */
        SqlDialect[SqlDialect["HIVE"] = 0] = "HIVE";
        /** Teradata SQL dialect */
        SqlDialect[SqlDialect["TERADATA"] = 1] = "TERADATA";
    })(SqlDialect = exports.SqlDialect || (exports.SqlDialect = {}));
    /**
     * Field of a schema object such as a table or file.
     */
    var SchemaField = /** @class */ (function () {
        function SchemaField() {
        }
        return SchemaField;
    }());
    exports.SchemaField = SchemaField;
});
//# sourceMappingURL=index.js.map