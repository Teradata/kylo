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
define(["require", "exports", "../script-builder", "./spark-expression", "./spark-expression-type", "./spark-constants"], function (require, exports, script_builder_1, spark_expression_1, spark_expression_type_1, spark_constants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Parses an abstract syntax tree into a Spark script.
     */
    var SparkScriptBuilder = (function (_super) {
        __extends(SparkScriptBuilder, _super);
        /**
         * Constructs a {@code SparkScriptBuilder}.
         *
         * @param functions - ternjs functions
         * @param queryEngine - Spark query engine
         */
        function SparkScriptBuilder(functions, queryEngine) {
            var _this = _super.call(this, functions) || this;
            _this.queryEngine = queryEngine;
            return _this;
        }
        /**
         * Creates a script expression for the specified AST node.
         */
        SparkScriptBuilder.prototype.createScriptExpression = function (source, type, start, end) {
            return new spark_expression_1.SparkExpression(source, type, start, end);
        };
        /**
         * Creates a script expression from a function definition and AST node.
         */
        SparkScriptBuilder.prototype.createScriptExpressionFromDefinition = function (definition, node) {
            var var_args = [];
            for (var _i = 2; _i < arguments.length; _i++) {
                var_args[_i - 2] = arguments[_i];
            }
            return spark_expression_1.SparkExpression.fromDefinition.apply(spark_expression_1.SparkExpression, [definition, node].concat(var_args));
        };
        /**
         * Indicates if the specified expression type is an object.
         */
        SparkScriptBuilder.prototype.isObject = function (sparkType) {
            return spark_expression_type_1.SparkExpressionType.isObject(sparkType.toString());
        };
        /**
         * Parses an identifier into a script expression.
         */
        SparkScriptBuilder.prototype.parseIdentifier = function (node) {
            var label = StringUtils.quote(this.queryEngine.getColumnLabel(node.name));
            return new spark_expression_1.SparkExpression(spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + "(\"" + label + "\")", spark_expression_type_1.SparkExpressionType.COLUMN, node.start, node.end);
        };
        /**
         * Converts the specified script expression to a transform script.
         */
        SparkScriptBuilder.prototype.prepareScript = function (spark) {
            switch (spark.type.toString()) {
                case spark_expression_type_1.SparkExpressionType.COLUMN.value:
                case spark_expression_type_1.SparkExpressionType.CONDITION_CHAIN.value:
                    return ".select(" + spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + "(\"*\"), " + spark.source + ")";
                case spark_expression_type_1.SparkExpressionType.DATA_FRAME.value:
                    return spark.source;
                case spark_expression_type_1.SparkExpressionType.LITERAL.value:
                    var column = spark_expression_1.SparkExpression.format("%c", spark);
                    return ".select(" + spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + "(\"*\"), " + column + ")";
                case spark_expression_type_1.SparkExpressionType.TRANSFORM.value:
                    return ".transform(" + spark.source + ")";
                default:
                    throw new Error("Result type not supported: " + spark.type);
            }
        };
        /**
         * Gets the Ternjs name of the specified expression type.
         */
        SparkScriptBuilder.prototype.toTernjsName = function (sparkType) {
            return spark_expression_type_1.SparkExpressionType.toTernjsName(sparkType.toString());
        };
        return SparkScriptBuilder;
    }(script_builder_1.ScriptBuilder));
    exports.SparkScriptBuilder = SparkScriptBuilder;
});
//# sourceMappingURL=spark-script-builder.js.map