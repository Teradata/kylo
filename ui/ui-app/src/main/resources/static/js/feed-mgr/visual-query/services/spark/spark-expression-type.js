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
define(["require", "exports", "../../wrangler/query-engine-constants", "../../wrangler/script-expression-type"], function (require, exports, query_engine_constants_1, script_expression_type_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Types supported by {@link SparkExpression}.
     */
    var SparkExpressionType = /** @class */ (function (_super) {
        __extends(SparkExpressionType, _super);
        function SparkExpressionType() {
            return _super !== null && _super.apply(this, arguments) || this;
        }
        /**
         * Indicates if the specified type is an object type.
         *
         * @param sparkType - the Spark type
         * @returns true if the type is an object type, or false otherwise
         */
        SparkExpressionType.isObject = function (sparkType) {
            return (sparkType !== SparkExpressionType.ARRAY.toString() && sparkType !== SparkExpressionType.LITERAL.toString() && sparkType !== SparkExpressionType.TRANSFORM.toString());
        };
        /**
         * Gets the TernJS definition name for the specified type.
         *
         * @param sparkType - the Spark type
         */
        SparkExpressionType.toTernjsName = function (sparkType) {
            return (sparkType === SparkExpressionType.COLUMN.toString()) ? query_engine_constants_1.QueryEngineConstants.TERNJS_COLUMN_TYPE : sparkType;
        };
        /** Represents a chain of {@code when} function calls */
        SparkExpressionType.CONDITION_CHAIN = new SparkExpressionType("ConditionChain");
        /** Represents a Spark SQL DataFrame */
        SparkExpressionType.DATA_FRAME = new SparkExpressionType("dataframe");
        /** Represents a function that takes a DataFrame and returns a DataFrame */
        SparkExpressionType.TRANSFORM = new SparkExpressionType("transform");
        return SparkExpressionType;
    }(script_expression_type_1.ScriptExpressionType));
    exports.SparkExpressionType = SparkExpressionType;
});
//# sourceMappingURL=spark-expression-type.js.map