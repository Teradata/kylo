define(["require", "exports", "../../wrangler/parse-exception", "./spark-constants", "./spark-expression-type"], function (require, exports, parse_exception_1, spark_constants_1, spark_expression_type_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * An expression in a Spark script.
     */
    var SparkExpression = /** @class */ (function () {
        /**
         * Constructs a {@code SparkExpression}
         *
         * @param source - Spark source code
         * @param type - result type
         * @param start - column of the first character in the original expression
         * @param end - column of the last character in the original expression
         */
        function SparkExpression(source, type, start, end) {
            this.source = source;
            this.type = type;
            this.start = start;
            this.end = end;
        }
        /**
         * Formats the specified string by replacing the type specifiers with the specified parameters.
         *
         * @param str - the Spark conversion string to be formatted
         * @param args - the format parameters
         * @returns the formatted string
         * @throws {Error} if the conversion string is not valid
         * @throws {ParseException} if a format parameter cannot be converted to the specified type
         */
        SparkExpression.format = function (str) {
            var args = [];
            for (var _i = 1; _i < arguments.length; _i++) {
                args[_i - 1] = arguments[_i];
            }
            // Convert arguments
            var context = {
                args: Array.prototype.slice.call(arguments, 1),
                index: 0
            };
            var result = str.replace(SparkExpression.FORMAT_REGEX, angular.bind(str, SparkExpression.replace, context));
            // Verify all arguments converted
            if (context.index >= context.args.length) {
                return result;
            }
            else {
                throw new parse_exception_1.ParseException("Too many arguments for conversion.");
            }
        };
        /**
         * Creates a Spark expression from a function definition.
         *
         * @param definition - the function definition
         * @param node - the source abstract syntax tree
         * @param var_args - the format parameters
         * @returns the Spark expression
         * @throws {Error} if the function definition is not valid
         * @throws {ParseException} if a format parameter cannot be converted to the required type
         */
        SparkExpression.fromDefinition = function (definition, node) {
            var var_args = [];
            for (var _i = 2; _i < arguments.length; _i++) {
                var_args[_i - 2] = arguments[_i];
            }
            // Convert Spark string to code
            var args = [definition[SparkExpression.SPARK_DIRECTIVE]];
            Array.prototype.push.apply(args, Array.prototype.slice.call(arguments, 2));
            var source = SparkExpression.format.apply(SparkExpression, args);
            // Return expression
            return new SparkExpression(source, spark_expression_type_1.SparkExpressionType.valueOf(definition[SparkExpression.TYPE_DIRECTIVE]), node.start, node.end);
        };
        /**
         * Converts the next argument to the specified type for a Spark conversion string.
         *
         * @param context - the format context
         * @param match - the conversion specification
         * @param flags - the conversion flags
         * @param type - the type specifier
         * @returns the converted Spark code
         * @throws {Error} if the type specifier is not supported
         * @throws {ParseException} if the format parameter cannot be converted to the specified type
         */
        SparkExpression.replace = function (context, match, flags, type) {
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
                    case "b":
                        result += SparkExpression.toBoolean(arg);
                        break;
                    case "c":
                        result += SparkExpression.toColumn(arg);
                        break;
                    case "d":
                        result += SparkExpression.toInteger(arg);
                        break;
                    case "f":
                        result += SparkExpression.toDouble(arg);
                        break;
                    case "o":
                        result += SparkExpression.toObject(arg);
                        break;
                    case "r":
                        result += SparkExpression.toDataFrame(arg);
                        break;
                    case "s":
                        result += SparkExpression.toString(arg);
                        break;
                    case "@":
                        result += SparkExpression.toArray(arg, arrayType);
                        break;
                    default:
                        throw new Error("Not a recognized conversion type: " + type);
                }
            }
            return result;
        };
        /**
         * Converts the specified Spark expression to an array.
         *
         * @param expression - the Spark expression
         * @param type - the type specifier
         * @returns the Spark code for the array
         * @throws {ParseException} if the expression cannot be converted to an array
         */
        SparkExpression.toArray = function (expression, type) {
            if (spark_expression_type_1.SparkExpressionType.ARRAY.equals(expression.type)) {
                var source = expression.source;
                return "Array(" + source
                    .map(function (e) {
                    return SparkExpression.format("%" + type, e);
                })
                    .join(", ") + ")";
            }
            else {
                throw new parse_exception_1.ParseException("Expression cannot be converted to an array: " + expression.type, expression.start);
            }
        };
        /**
         * Converts the specified Spark expression to a boolean literal.
         *
         * @param expression - the Spark expression
         * @returns the Spark code for the boolean
         * @throws {ParseException} if the expression cannot be converted to a boolean
         */
        SparkExpression.toBoolean = function (expression) {
            if (spark_expression_type_1.SparkExpressionType.LITERAL.equals(expression.type) && (expression.source === "true" || expression.source === "false")) {
                return expression.source;
            }
            else {
                throw new parse_exception_1.ParseException("Expression cannot be converted to a boolean: " + expression.type, expression.start);
            }
        };
        /**
         * Converts the specified Spark expression to a Column type.
         *
         * @param expression - the Spark expression
         * @returns the Spark code for the new type
         * @throws {ParseException} if the expression cannot be converted to a column
         */
        SparkExpression.toColumn = function (expression) {
            if (spark_expression_type_1.SparkExpressionType.COLUMN.equals(expression.type)) {
                return expression.source;
            }
            if (spark_expression_type_1.SparkExpressionType.LITERAL.equals(expression.type)) {
                var literal = void 0;
                if (expression.source.charAt(0) === "\"" || expression.source.charAt(0) === "'") {
                    literal = SparkExpression.toString(expression);
                }
                else {
                    literal = expression.source;
                }
                return "functions.lit(" + literal + ")";
            }
            throw new parse_exception_1.ParseException("Expression cannot be converted to a column: " + expression.type, expression.start);
        };
        /**
         * Converts the specified Spark expression to a DataFrame type.
         *
         * @param expression - the Spark expression
         * @returns the Spark code for the new type
         * @throws {ParseException} if the expression cannot be converted to a DataFrame
         */
        SparkExpression.toDataFrame = function (expression) {
            if (spark_expression_type_1.SparkExpressionType.DATA_FRAME.equals(expression.type)) {
                return spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + expression.source;
            }
            throw new parse_exception_1.ParseException("Expression cannot be converted to a DataFrame: " + expression.type, expression.start);
        };
        /**
         * Converts the specified Spark expression to a double.
         *
         * @param expression - the Spark expression
         * @returns the Spark code for the double
         * @throws {ParseException} if the expression cannot be converted to a double
         */
        SparkExpression.toDouble = function (expression) {
            if (spark_expression_type_1.SparkExpressionType.LITERAL.equals(expression.type) && expression.source.match(/^(0|-?[1-9][0-9]*)(\.[0-9]+)?$/) !== null) {
                return expression.source;
            }
            else {
                throw new parse_exception_1.ParseException("Expression cannot be converted to an integer: " + expression.type, expression.start);
            }
        };
        /**
         * Converts the specified Spark expression to an integer.
         *
         * @param expression - the Spark expression
         * @returns the Spark code for the integer
         * @throws {ParseException} if the expression cannot be converted to a number
         */
        SparkExpression.toInteger = function (expression) {
            if (spark_expression_type_1.SparkExpressionType.LITERAL.equals(expression.type) && expression.source.match(/^(0|-?[1-9][0-9]*)$/) !== null) {
                return expression.source;
            }
            else {
                throw new parse_exception_1.ParseException("Expression cannot be converted to an integer: " + expression.type, expression.start);
            }
        };
        /**
         * Converts the specified Spark expression to an object.
         *
         * @param expression - the Spark expression
         * @returns the Spark code for the object
         * @throws {ParseException} if the expression cannot be converted to an object
         */
        SparkExpression.toObject = function (expression) {
            if (spark_expression_type_1.SparkExpressionType.isObject(expression.type.toString())) {
                return expression.source;
            }
            else if (spark_expression_type_1.SparkExpressionType.LITERAL.equals(expression.type)) {
                if (expression.source.charAt(0) === "\"" || expression.source.charAt(0) === "'") {
                    return SparkExpression.toString(expression);
                }
                else {
                    return expression.source;
                }
            }
            else {
                throw new parse_exception_1.ParseException("Expression cannot be converted to an object: " + expression.type, expression.start);
            }
        };
        /**
         * Converts the specified Spark expression to a string literal.
         *
         * @param expression - the Spark expression
         * @returns the Spark code for the string literal
         * @throws {ParseException} if the expression cannot be converted to a string
         */
        SparkExpression.toString = function (expression) {
            if (!spark_expression_type_1.SparkExpressionType.LITERAL.equals(expression.type)) {
                throw new parse_exception_1.ParseException("Expression cannot be converted to a string: " + expression.type, expression.start);
            }
            if (expression.source.charAt(0) === "\"") {
                return expression.source;
            }
            if (expression.source.charAt(0) === "'") {
                return "\"" + expression.source.substr(1, expression.source.length - 2).replace(/"/g, "\\\"") + "\"";
            }
            return "\"" + expression.source + "\"";
        };
        /** Regular expression for conversion strings */
        SparkExpression.FORMAT_REGEX = /%([?*,@]*)([bcdfors])/g;
        /** TernJS directive for the Spark code */
        SparkExpression.SPARK_DIRECTIVE = "!spark";
        /** TernJS directive for the return type */
        SparkExpression.TYPE_DIRECTIVE = "!sparkType";
        return SparkExpression;
    }());
    exports.SparkExpression = SparkExpression;
});
//# sourceMappingURL=spark-expression.js.map