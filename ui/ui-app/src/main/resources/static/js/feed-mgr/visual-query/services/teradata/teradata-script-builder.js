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
define(["require", "exports", "../script-builder", "./teradata-expression", "./teradata-expression-type"], function (require, exports, script_builder_1, teradata_expression_1, teradata_expression_type_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Parses an abstract syntax tree into a Teradata script.
     */
    var TeradataScriptBuilder = (function (_super) {
        __extends(TeradataScriptBuilder, _super);
        /**
         * Constructs a {@code TeradataScriptBuilder}.
         *
         * @param functions - ternjs functions
         * @param queryEngine - Teradata query engine
         */
        function TeradataScriptBuilder(functions, queryEngine) {
            var _this = _super.call(this, functions) || this;
            _this.queryEngine = queryEngine;
            return _this;
        }
        /**
         * Creates a script expression with the specified child expression appended to the parent expression.
         */
        TeradataScriptBuilder.prototype.appendChildExpression = function (parent, child) {
            if (!teradata_expression_type_1.TeradataExpressionType.SELECT.equals(parent.type) && parent.type.equals(child.type)) {
                return new teradata_expression_1.TeradataExpression(parent.source + child.source, child.type, child.start, child.end);
            }
            else {
                var parentSource = (typeof parent.source === "string") ? this.prepareScript(parent) : parent.source;
                var childSource = (typeof child.source === "string") ? this.prepareScript(child) : child.source;
                var newSource = {
                    groupBy: childSource.groupBy ? childSource.groupBy : parentSource.groupBy,
                    having: childSource.having ? childSource.having : parentSource.having,
                    keywordList: childSource.keywordList ? childSource.keywordList : parentSource.keywordList,
                    selectList: childSource.selectList ? childSource.selectList : parentSource.selectList,
                    where: childSource.where ? childSource.where : parentSource.where
                };
                return new teradata_expression_1.TeradataExpression(newSource, teradata_expression_type_1.TeradataExpressionType.SELECT, child.start, child.end);
            }
        };
        /**
         * Creates a script expression for the specified AST node.
         */
        TeradataScriptBuilder.prototype.createScriptExpression = function (source, type, start, end) {
            return new teradata_expression_1.TeradataExpression(source, type, start, end);
        };
        /**
         * Creates a script expression from a function definition and AST node.
         */
        TeradataScriptBuilder.prototype.createScriptExpressionFromDefinition = function (definition, node) {
            var var_args = [];
            for (var _i = 2; _i < arguments.length; _i++) {
                var_args[_i - 2] = arguments[_i];
            }
            return teradata_expression_1.TeradataExpression.fromDefinition.apply(teradata_expression_1.TeradataExpression, [definition, node].concat(var_args));
        };
        /**
         * Indicates if the specified expression type is an object.
         */
        TeradataScriptBuilder.prototype.isObject = function (type) {
            return teradata_expression_type_1.TeradataExpressionType.isObject(type.toString());
        };
        /**
         * Parses an identifier into a script expression.
         */
        TeradataScriptBuilder.prototype.parseIdentifier = function (node) {
            var label = StringUtils.quote(this.queryEngine.getColumnLabel(node.name));
            return new teradata_expression_1.TeradataExpression("\"" + label + "\"", teradata_expression_type_1.TeradataExpressionType.COLUMN, node.start, node.end);
        };
        /**
         * Parses an identifier into a script expression.
         */
        TeradataScriptBuilder.prototype.prepareScript = function (expression) {
            if (teradata_expression_type_1.TeradataExpressionType.ARRAY.equals(expression.type) || teradata_expression_type_1.TeradataExpressionType.COLUMN.equals(expression.type) || teradata_expression_type_1.TeradataExpressionType.LITERAL.equals(expression.type)) {
                var selectList = teradata_expression_1.TeradataExpression.needsColumnAlias(expression.source) ? teradata_expression_1.TeradataExpression.addColumnAlias(expression.source) : expression.source;
                return { groupBy: null, having: null, keywordList: null, selectList: "*, " + selectList, where: null };
            }
            if (teradata_expression_type_1.TeradataExpressionType.GROUP_BY.equals(expression.type)) {
                return { groupBy: expression.source, having: null, keywordList: null, selectList: null, where: null };
            }
            if (teradata_expression_type_1.TeradataExpressionType.HAVING.equals(expression.type)) {
                return { groupBy: null, having: expression.source, keywordList: null, selectList: null, where: null };
            }
            if (teradata_expression_type_1.TeradataExpressionType.KEYWORD.equals(expression.type)) {
                return { groupBy: null, having: null, keywordList: expression.source, selectList: null, where: null };
            }
            if (teradata_expression_type_1.TeradataExpressionType.SELECT.equals(expression.type)) {
                if (typeof expression.source === "string") {
                    return { groupBy: null, having: null, keywordList: null, selectList: expression.source, where: null };
                }
                else {
                    return expression.source;
                }
            }
            if (teradata_expression_type_1.TeradataExpressionType.WHERE.equals(expression.type)) {
                return { groupBy: null, having: null, keywordList: null, selectList: null, where: expression.source };
            }
            throw new Error("Result type not supported: " + expression.type);
        };
        /**
         * Gets the Ternjs name of the specified expression type.
         */
        TeradataScriptBuilder.prototype.toTernjsName = function (type) {
            return teradata_expression_type_1.TeradataExpressionType.toTernjsName(type.toString());
        };
        return TeradataScriptBuilder;
    }(script_builder_1.ScriptBuilder));
    exports.TeradataScriptBuilder = TeradataScriptBuilder;
});
//# sourceMappingURL=teradata-script-builder.js.map