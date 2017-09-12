define(["require", "exports", "./teradata-expression-type", "../parse-exception"], function (require, exports, teradata_expression_type_1, parse_exception_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * An expression in a Teradata SQL script.
     */
    var TeradataExpression = (function () {
        /**
         * Constructs a {@code TeradataExpression}
         *
         * @param source - Teradata code
         * @param type - result type
         * @param start - column of the first character in the original expression
         * @param end - column of the last character in the original expression
         */
        function TeradataExpression(source, type, start, end) {
            this.source = source;
            this.type = type;
            this.start = start;
            this.end = end;
        }
        /**
         * Adds an alias to the specified expression.
         */
        TeradataExpression.addColumnAlias = function (expression) {
            return expression + " AS \"" + expression.replace(/"/g, "") + "\"";
        };
        /**
         * Formats the specified string by replacing the type specifiers with the specified parameters.
         *
         * @param str - the Teradata conversion string to be formatted
         * @param requireAlias - true to ensure that all columns have an alias
         * @param args - the format parameters
         * @returns the formatted string
         * @throws {Error} if the conversion string is not valid
         * @throws {ParseException} if a format parameter cannot be converted to the specified type
         */
        TeradataExpression.format = function (str, requireAlias) {
            var args = [];
            for (var _i = 2; _i < arguments.length; _i++) {
                args[_i - 2] = arguments[_i];
            }
            // Convert arguments
            var context = {
                args: args,
                index: 0,
                requireAlias: requireAlias
            };
            var result = str.replace(TeradataExpression.FORMAT_REGEX, angular.bind(str, TeradataExpression.replace, context));
            // Verify all arguments converted
            if (context.index >= context.args.length) {
                return result;
            }
            else {
                throw new parse_exception_1.ParseException("Too many arguments for conversion.");
            }
        };
        /**
         * Creates a Teradata expression from a function definition.
         *
         * @param definition - the function definition
         * @param node - the source abstract syntax tree
         * @param var_args - the format parameters
         * @returns the Teradata expression
         * @throws {Error} if the function definition is not valid
         * @throws {ParseException} if a format parameter cannot be converted to the required type
         */
        TeradataExpression.fromDefinition = function (definition, node) {
            var var_args = [];
            for (var _i = 2; _i < arguments.length; _i++) {
                var_args[_i - 2] = arguments[_i];
            }
            // Convert Spark string to code
            var args = [definition[TeradataExpression.TERADATA_DIRECTIVE], definition[TeradataExpression.TYPE_DIRECTIVE] === "Select"];
            Array.prototype.push.apply(args, Array.prototype.slice.call(arguments, 2));
            var source = TeradataExpression.format.apply(TeradataExpression, args);
            // Return expression
            return new TeradataExpression(source, teradata_expression_type_1.TeradataExpressionType.valueOf(definition[TeradataExpression.TYPE_DIRECTIVE]), node.start, node.end);
        };
        /**
         * Indicates if the expression does not have a column alias.
         */
        TeradataExpression.needsColumnAlias = function (expression) {
            return !expression.match(/^"[^"]+"$| AS "[^"]+"$/);
        };
        /**
         * Converts the next argument to the specified type for a Teradata conversion string.
         *
         * @param context - the format context
         * @param match - the conversion specification
         * @param flags - the conversion flags
         * @param type - the type specifier
         * @returns the converted Teradata code
         * @throws {Error} if the type specifier is not supported
         * @throws {ParseException} if the format parameter cannot be converted to the specified type
         */
        TeradataExpression.replace = function (context, match, flags, type) {
            // Parse flags
            var arrayType = null;
            var comma = false;
            var end = context.index + 1;
            for (var i = 0; i < flags.length; ++i) {
                switch (flags.charAt(i)) {
                    case ",":
                        comma = true;
                        break;
                    case "?":
                        end = (context.index < context.args.length) ? end : 0;
                        break;
                    case "*":
                        end = context.args.length;
                        break;
                    case "@":
                        arrayType = type;
                        type = "@";
                        break;
                    default:
                        throw new Error("Unsupported conversion flag: " + flags.charAt(i));
                }
            }
            // Validate arguments
            if (end > context.args.length) {
                throw new parse_exception_1.ParseException("Not enough arguments for conversion");
            }
            // Convert to requested type
            var first = true;
            var result = "";
            for (; context.index < end; ++context.index) {
                // Argument separator
                if (comma || !first) {
                    result += ", ";
                }
                else {
                    first = false;
                }
                // Conversion
                var arg = context.args[context.index];
                switch (type) {
                    case "c":
                        result += TeradataExpression.toColumn(arg, context.requireAlias);
                        break;
                    case "s":
                        result += TeradataExpression.toString(arg);
                        break;
                    default:
                        throw new Error("Not a recognized conversion type: " + type);
                }
            }
            return result;
        };
        /**
         * Converts the specified Teradata expression to a Column type.
         *
         * @param expression - the Teradata expression
         * @param requireAlias - true to ensure that the column has an alias
         * @returns the Teradata code for the new type
         * @throws {ParseException} if the expression cannot be converted to a column
         */
        TeradataExpression.toColumn = function (expression, requireAlias) {
            if (teradata_expression_type_1.TeradataExpressionType.COLUMN.equals(expression.type) || teradata_expression_type_1.TeradataExpressionType.LITERAL.equals(expression.type)) {
                if (requireAlias && TeradataExpression.needsColumnAlias(expression.source)) {
                    return TeradataExpression.addColumnAlias(expression.source);
                }
                else {
                    return expression.source;
                }
            }
            throw new parse_exception_1.ParseException("Expression cannot be converted to a column: " + expression.type, expression.start);
        };
        /**
         * Converts the specified Teradata expression to a string literal.
         *
         * @param expression - the Teradata expression
         * @returns the Teradata code for the string literal
         * @throws {ParseException} if the expression cannot be converted to a string
         */
        TeradataExpression.toString = function (expression) {
            if (!teradata_expression_type_1.TeradataExpressionType.LITERAL.equals(expression.type)) {
                throw new parse_exception_1.ParseException("Expression cannot be converted to a string: " + expression.type, expression.start);
            }
            if (expression.source.charAt(0) === "'") {
                return expression.source.replace(/\\'/g, "''");
            }
            if (expression.source.charAt(0) === "\"") {
                return expression.source.replace(/\\"/g, "\"\"");
            }
            return "'" + expression.source + "'";
        };
        return TeradataExpression;
    }());
    /** Regular expression for conversion strings */
    TeradataExpression.FORMAT_REGEX = /%([?*,@]*)([cs])/g;
    /** TernJS directive for the Teradata code */
    TeradataExpression.TERADATA_DIRECTIVE = "!sql";
    /** TernJS directive for the return type */
    TeradataExpression.TYPE_DIRECTIVE = "!sqlType";
    exports.TeradataExpression = TeradataExpression;
});
//# sourceMappingURL=teradata-expression.js.map