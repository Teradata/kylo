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
define(["require", "exports", "../script-expression-type", "../query-engine-constants"], function (require, exports, script_expression_type_1, query_engine_constants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Types supported by {@link TeradataExpression}.
     */
    var TeradataExpressionType = (function (_super) {
        __extends(TeradataExpressionType, _super);
        function TeradataExpressionType() {
            return _super !== null && _super.apply(this, arguments) || this;
        }
        /**
         * Indicates if the specified type is an object type.
         *
         * @param teradataType - the Teradata type
         * @returns true if the type is an object type, or false otherwise
         */
        TeradataExpressionType.isObject = function (teradataType) {
            return (teradataType !== TeradataExpressionType.ARRAY.toString() && teradataType !== TeradataExpressionType.LITERAL.toString());
        };
        /**
         * Gets the TernJS definition name for the specified type.
         *
         * @param teradataType - the Teradata type
         */
        TeradataExpressionType.toTernjsName = function (teradataType) {
            return (teradataType === TeradataExpressionType.COLUMN.toString()) ? query_engine_constants_1.QueryEngineConstants.TERNJS_COLUMN_TYPE : teradataType;
        };
        return TeradataExpressionType;
    }(script_expression_type_1.ScriptExpressionType));
    /**
     * GROUP BY clause
     */
    TeradataExpressionType.GROUP_BY = new TeradataExpressionType("GroupBy");
    /**
     * HAVING or QUALIFY conditional-expression
     */
    TeradataExpressionType.HAVING = new TeradataExpressionType("Having");
    /**
     * Keywords between 'SELECT' and column list
     */
    TeradataExpressionType.KEYWORD = new TeradataExpressionType("Keyword");
    /**
     * List of expressions
     */
    TeradataExpressionType.SELECT = new TeradataExpressionType("Select");
    /**
     * WHERE qualification
     */
    TeradataExpressionType.WHERE = new TeradataExpressionType("Where");
    exports.TeradataExpressionType = TeradataExpressionType;
});
//# sourceMappingURL=teradata-expression-type.js.map