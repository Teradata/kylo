define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Type of script expressions.
     */
    var ScriptExpressionType = /** @class */ (function () {
        /**
         * Constructs a {@code ScriptExpressionType} with the specified string value.
         */
        function ScriptExpressionType(value) {
            this.value = value;
        }
        /**
         * Returns the constant with the specified name.
         *
         * @return the matching constant or {@code null} if not found
         */
        ScriptExpressionType.valueOf = function (name) {
            switch (name) {
                case "array":
                    return ScriptExpressionType.ARRAY;
                case "Column":
                case "column":
                    return ScriptExpressionType.COLUMN;
                case "literal":
                    return ScriptExpressionType.LITERAL;
                default:
                    return new ScriptExpressionType(name);
            }
        };
        /**
         * Returns true if the specified object is equal to this enum constant.
         */
        ScriptExpressionType.prototype.equals = function (other) {
            return this.value === other.value;
        };
        /**
         * Returns the string value of this type.
         */
        ScriptExpressionType.prototype.toString = function () {
            return this.value;
        };
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
        return ScriptExpressionType;
    }());
    exports.ScriptExpressionType = ScriptExpressionType;
});
//# sourceMappingURL=script-expression-type.js.map