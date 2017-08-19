define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Type of script expressions.
     */
    var ScriptExpressionType = (function () {
        /**
         * Constructs a {@code ScriptExpressionType} with the specified string value.
         */
        function ScriptExpressionType(value) {
            this.value = value;
        }
        /**
         * Returns the string value of this type.
         */
        ScriptExpressionType.prototype.toString = function () {
            return this.value;
        };
        return ScriptExpressionType;
    }());
    /**
     * Represents an array of objects.
     */
    ScriptExpressionType.ARRAY = new ScriptExpressionType("array");
    /**
     * Represents a SQL column.
     */
    ScriptExpressionType.COLUMN = new ScriptExpressionType("column");
    /**
     * Represents a number or string literal.
     */
    ScriptExpressionType.LITERAL = new ScriptExpressionType("literal");
    exports.ScriptExpressionType = ScriptExpressionType;
});
//# sourceMappingURL=script-expression-type.js.map