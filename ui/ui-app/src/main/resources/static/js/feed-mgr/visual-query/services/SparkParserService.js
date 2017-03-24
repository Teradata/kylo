define(["angular", "feed-mgr/visual-query/module-name"], function (angular, moduleName) {
angular.module(moduleName).factory("SparkParserService", [function() {

    /** Name of the variable containing the DataFrame */
    var DATA_FRAME_VARIABLE = "df";

    /** TernJS directive for defined types */
    var DEFINE_DIRECTIVE = "!define";

    /** Regular expression for conversion strings */
    var FORMAT_REGEX = /%([?*,@]*)([bcdfors])/g;

    /** TernJS directive for the Spark code */
    var SPARK_DIRECTIVE = "!spark";

    /** Return type for columns in TernJS */
    var TERNJS_COLUMN_TYPE = "Column";

    /** TernJS directive for the return type */
    var TYPE_DIRECTIVE = "!sparkType";

    /**
     * Types supported by SparkExpression.
     *
     * @readonly
     * @enum {string}
     */
    var SparkType = {
        /** Represents a Scala array */
        ARRAY: "array",

        /** Represents a Spark SQL Column */
        COLUMN: "column",

        /** Represents a chain of {@code when} function calls */
        CONDITION_CHAIN: "ConditionChain",

        /** Represents a Spark SQL DataFrame */
        DATA_FRAME: "dataframe",

        /** Represents a Scala number or string literal */
        LITERAL: "literal",

        /** Represents a function that takes a DataFrame and returns a DataFrame */
        TRANSFORM: "transform",

        /**
         * Indicates if the specified type is an object type.
         *
         * @param {SparkType} sparkType the Spark type
         * @returns {boolean} true if the type is an object type, or false otherwise
         */
        isObject: function (sparkType) {
            return (sparkType !== SparkType.ARRAY && sparkType !== SparkType.LITERAL && sparkType !== SparkType.TRANSFORM)
        },

        /**
         * Gets the TernJS definition name for the specified type.
         *
         * @param {SparkType} sparkType the Spark type
         * @returns {string|null}
         */
        toTernjsName: function (sparkType) {
            return (sparkType === SparkType.COLUMN) ? TERNJS_COLUMN_TYPE : sparkType;
        }
    };

    /**
     * Thrown to indicate that the abstract syntax tree could not be parsed.
     *
     * @constructor
     * @param {string} message the error message
     * @param {number} [opt_col] the column number
     */
    function ParseException(message, opt_col) {
        this.name = "ParseException";
        this.message = message + (opt_col ? " at column number " + opt_col : "");
    }

    ParseException.prototype = Object.create(Error.prototype);

    /**
     * An expression in a Spark script.
     *
     * @constructor
     * @param {string} source the Spark code
     * @param {SparkType} type the result type
     * @param {number} start the first column in the original expression
     * @param {number} end the last column in the original expression
     */
    function SparkExpression(source, type, start, end) {
        /**
         * Spark source code.
         * @type {string|Array}
         */
        this.source = source;

        /**
         * Result type.
         * @type {SparkType}
         */
        this.type = type;

        /**
         * Column of the first character in the original expression.
         * @type {number}
         */
        this.start = start;

        /**
         * Column of the last character in the original expression.
         * @type {number}
         */
        this.end = end;
    }

    angular.extend(SparkExpression, {
        /**
         * Context for formatting a Spark conversion string.
         *
         * @typedef {Object} FormatContext
         * @property {SparkExpression[]} args the format parameters
         * @property {number} index the current position within {@code args}
         */

        /**
         * Formats the specified string by replacing the type specifiers with the specified parameters.
         *
         * @static
         * @param {string} str the Spark conversion string to be formatted
         * @param {...SparkExpression} var_args the format parameters
         * @returns {string} the formatted string
         * @throws {Error} if the conversion string is not valid
         * @throws {ParseException} if a format parameter cannot be converted to the specified type
         */
        format: function (str, var_args) {
            // Convert arguments
            var context = {
                args: Array.prototype.slice.call(arguments, 1),
                index: 0
            };
            var result = str.replace(FORMAT_REGEX, angular.bind(str, SparkExpression.replace, context));

            // Verify all arguments converted
            if (context.index >= context.args.length) {
                return result;
            } else {
                throw new ParseException("Too many arguments for conversion.");
            }
        },

        /**
         * Creates a Spark expression from a function definition.
         *
         * @static
         * @param {Object} definition the function definition
         * @param {acorn.Node} node the source abstract syntax tree
         * @param {...SparkExpression} var_args the format parameters
         * @returns {SparkExpression} the Spark expression
         * @throws {Error} if the function definition is not valid
         * @throws {ParseException} if a format parameter cannot be converted to the required type
         */
        fromDefinition: function (definition, node, var_args) {
            // Convert Spark string to code
            var args = [definition[SPARK_DIRECTIVE]];
            Array.prototype.push.apply(args, Array.prototype.slice.call(arguments, 2));

            var source = SparkExpression.format.apply(SparkExpression, args);

            // Return expression
            return new SparkExpression(source, definition[TYPE_DIRECTIVE], node.start, node.end);
        },

        /**
         * Converts the next argument to the specified type for a Spark conversion string.
         *
         * @private
         * @static
         * @param {FormatContext} context the format context
         * @param {string} match the conversion specification
         * @param {string} flags the conversion flags
         * @param {string} type the type specifier
         * @returns {string} the converted Spark code
         * @throws {Error} if the type specifier is not supported
         * @throws {ParseException} if the format parameter cannot be converted to the specified type
         */
        replace: function (context, match, flags, type) {
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
                throw new ParseException("Not enough arguments for conversion");
            }

            // Convert to requested type
            var first = true;
            var result = "";

            for (; context.index < end; ++context.index) {
                // Argument separator
                if (comma || !first) {
                    result += ", ";
                } else {
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
        },

        /**
         * Converts the specified Spark expression to an array.
         *
         * @private
         * @static
         * @param {SparkExpression} expression the Spark expression
         * @param {string} type the type specifier
         * @returns {string} the Spark code for the array
         * @throws {ParseException} if the expression cannot be converted to an array
         */
        toArray: function (expression, type) {
            if (expression.type === SparkType.ARRAY) {
                return "Array(" + expression.source
                        .map(function (e) {
                            return SparkExpression.format("%" + type, e);
                        })
                        .join(", ") + ")";
            } else {
                throw new ParseException("Expression cannot be converted to an array: " + expression.type, expression.start);
            }
        },

        /**
         * Converts the specified Spark expression to a boolean literal.
         *
         * @private
         * @static
         * @param {SparkExpression} expression the Spark expression
         * @returns {string} the Spark code for the boolean
         * @throws {ParseException} if the expression cannot be converted to a boolean
         */
        toBoolean: function (expression) {
            if (expression.type === SparkType.LITERAL && (expression.source === "true" || expression.source === "false")) {
                return expression.source;
            } else {
                throw new ParseException("Expression cannot be converted to a boolean: " + expression.type, expression.start);
            }
        },

        /**
         * Converts the specified Spark expression to a Column type.
         *
         * @private
         * @static
         * @param {SparkExpression} expression the Spark expression
         * @returns {string} the Spark code for the new type
         * @throws {ParseException} if the expression cannot be converted to a column
         */
        toColumn: function (expression) {
            switch (expression.type) {
                case SparkType.COLUMN:
                    return expression.source;

                case SparkType.LITERAL:
                    return "functions.lit(" + expression.source + ")";

                default:
                    throw new ParseException("Expression cannot be converted to a column: " + expression.type, expression.start);
            }
        },

        /**
         * Converts the specified Spark expression to a DataFrame type.
         *
         * @private
         * @static
         * @param {SparkExpression} expression the Spark expression
         * @returns {string} the Spark code for the new type
         * @throws {ParseException} if the expression cannot be converted to a DataFrame
         */
        toDataFrame: function (expression) {
            switch (expression.type) {
                case SparkType.DATA_FRAME:
                    return DATA_FRAME_VARIABLE + expression.source;

                default:
                    throw new ParseException("Expression cannot be converted to a DataFrame: " + expression.type, expression.start);
            }
        },

        /**
         * Converts the specified Spark expression to a double.
         *
         * @private
         * @static
         * @param {SparkExpression} expression the Spark expression
         * @returns {string} the Spark code for the double
         * @throws {ParseException} if the expression cannot be converted to a double
         */
        toDouble: function (expression) {
            if (expression.type === SparkType.LITERAL && expression.source.match(/^(0|-?[1-9][0-9]*)(\.[0-9]+)?$/) !== null) {
                return expression.source;
            } else {
                throw new ParseException("Expression cannot be converted to an integer: " + expression.type, expression.start);
            }
        },

        /**
         * Converts the specified Spark expression to an integer.
         *
         * @private
         * @static
         * @param {SparkExpression} expression the Spark expression
         * @returns {string} the Spark code for the integer
         * @throws {ParseException} if the expression cannot be converted to a number
         */
        toInteger: function (expression) {
            if (expression.type === SparkType.LITERAL && expression.source.match(/^(0|-?[1-9][0-9]*)$/) !== null) {
                return expression.source;
            } else {
                throw new ParseException("Expression cannot be converted to an integer: " + expression.type, expression.start);
            }
        },

        /**
         * Converts the specified Spark expression to an object.
         *
         * @private
         * @static
         * @param {SparkExpression} expression the Spark expression
         * @returns {string} the Spark code for the object
         * @throws {ParseException} if the expression cannot be converted to an object
         */
        toObject: function (expression) {
            if (SparkType.isObject(expression.type)) {
                return expression.source;
            } else if (expression.type == SparkType.LITERAL) {
                if (expression.source.charAt(0) === "\"" || expression.source.charAt(0) === "'") {
                    return SparkExpression.toString(expression);
                } else {
                    return expression.source;
                }
            } else {
                throw new ParseException("Expression cannot be converted to an object: " + expression.type, expression.start);
            }
        },

        /**
         * Converts the specified Spark expression to a string literal.
         *
         * @private
         * @static
         * @param {SparkExpression} expression the Spark expression
         * @returns {string} the Spark code for the string literal
         * @throws {ParseException} if the expression cannot be converted to a string
         */
        toString: function (expression) {
            if (expression.type !== SparkType.LITERAL) {
                throw new ParseException("Expression cannot be converted to a string: " + expression.type, expression.start);
            }
            if (expression.source.charAt(0) === "\"") {
                return expression.source;
            }
            if (expression.source.charAt(0) === "'") {
                return "\"" + expression.source.substr(1, expression.source.length - 2).replace(/"/g, "\\\"") + "\"";
            }
            return "\"" + expression.source + "\"";
        }
    });

    /**
     * Converts an array expression node to a Spark expression.
     *
     * @param {acorn.Node} node the array expression node
     * @param {SparkShellService} sparkShellService the Spark shell service
     * @returns {SparkExpression} the Spark expression
     * @throws {Error} if a function definition is not valid
     * @throws {ParseException} if the node is not valid
     */
    function parseArrayExpression(node, sparkShellService) {
        var source = node.elements
            .map(function (e) {
                return toSpark(e, sparkShellService);
            });
        return new SparkExpression(source, SparkType.ARRAY, node.start, node.end);
    }

    /**
     * Converts a binary expression node to a Spark expression.
     *
     * @param {acorn.Node} node the binary expression node
     * @param {SparkShellService} sparkShellService the Spark shell service
     * @returns {SparkExpression} the Spark expression
     * @throws {Error} if the function definition is not valid
     * @throws {ParseException} if a function argument cannot be converted to the required type
     */
    function parseBinaryExpression(node, sparkShellService) {
        // Get the function definition
        var def = null;

        switch (node.operator) {
            case "+":
                def = sparkShellService.getFunctionDefs().add;
                break;

            case "-":
                def = sparkShellService.getFunctionDefs().subtract;
                break;

            case "*":
                def = sparkShellService.getFunctionDefs().multiply;
                break;

            case "/":
                def = sparkShellService.getFunctionDefs().divide;
                break;

            case "==":
                def = sparkShellService.getFunctionDefs().equal;
                break;

            case "!=":
                def = sparkShellService.getFunctionDefs().notEqual;
                break;

            case ">":
                def = sparkShellService.getFunctionDefs().greaterThan;
                break;

            case ">=":
                def = sparkShellService.getFunctionDefs().greaterThanOrEqual;
                break;

            case "<":
                def = sparkShellService.getFunctionDefs().lessThan;
                break;

            case "<=":
                def = sparkShellService.getFunctionDefs().lessThanOrEqual;
                break;

            default:
        }

        if (def == null) {
            throw new ParseException("Binary operator not supported: " + node.operator, node.start);
        }

        // Convert to a Spark expression
        var left = toSpark(node.left, sparkShellService);
        var right = toSpark(node.right, sparkShellService);
        return SparkExpression.fromDefinition(def, node, left, right);
    }

    /**
     * Converts a call expression node to a Spark expression.
     *
     * @param {acorn.Node} node the call expression node
     * @param {SparkShellService} sparkShellService the Spark shell service
     * @returns {SparkExpression} the Spark expression
     * @throws {Error} if the function definition is not valid
     * @throws {ParseException} if a function argument cannot be converted to the required type
     */
    function parseCallExpression(node, sparkShellService) {
        // Get the function definition
        var def;
        var name;
        var parent = null;

        switch (node.callee.type) {
            case "Identifier":
                def = sparkShellService.getFunctionDefs()[node.callee.name];
                name = node.callee.name;
                break;

            case "MemberExpression":
                parent = toSpark(node.callee.object, sparkShellService);

                // Find function definition
                var ternjsName = SparkType.toTernjsName(parent.type);

                if (ternjsName !== null) {
                    def = sparkShellService.getFunctionDefs()[DEFINE_DIRECTIVE][ternjsName][node.callee.property.name];
                } else {
                    throw new ParseException("Result type has no members: " + parent.type);
                }
                break;

            default:
                throw new ParseException("Function call type not supported: " + node.callee.type);
        }

        if (def == null) {
            throw new ParseException("Function is not defined: " + name);
        }

        // Convert to a Spark expression
        var args = [def, node];

        angular.forEach(node.arguments, function (arg) {
            args.push(toSpark(arg, sparkShellService));
        });

        var spark = SparkExpression.fromDefinition.apply(SparkExpression, args);
        return (parent !== null) ? new SparkExpression(parent.source + spark.source, spark.type, spark.start, spark.end) : spark;
    }

    /**
     * Converts a logical expression node to a Spark expression.
     *
     * @param {acorn.Node} node the logical expression node
     * @param {SparkShellService} sparkShellService the Spark shell service
     * @returns {SparkExpression} the Spark expression
     * @throws {Error} if the function definition is not valid
     * @throws {ParseException} if a function argument cannot be converted to the required type
     */
    function parseLogicalExpression(node, sparkShellService) {
        // Get the function definition
        var def = null;

        switch (node.operator) {
            case "&&":
                def = sparkShellService.getFunctionDefs().and;
                break;

            case "||":
                def = sparkShellService.getFunctionDefs().or;
                break;

            default:
        }

        if (def == null) {
            throw new ParseException("Logical operator not supported: " + node.operator, node.start);
        }

        // Convert to a Spark expression
        var left = toSpark(node.left, sparkShellService);
        var right = toSpark(node.right, sparkShellService);
        return SparkExpression.fromDefinition(def, node, left, right);
    }

    /**
     * Converts a unary expression node to a Spark expression.
     *
     * @param {acorn.Node} node the unary expression node
     * @param {SparkShellService} sparkShellService the Spark shell service
     * @returns {SparkExpression} the Spark expression
     * @throws {Error} if the function definition is not valid
     * @throws {ParseException} if a function argument cannot be converted to the required type
     */
    function parseUnaryExpression(node, sparkShellService) {
        // Get the function definition
        var arg = toSpark(node.argument, sparkShellService);
        var def = null;

        switch (node.operator) {
            case "-":
                if (arg.type === SparkType.COLUMN) {
                    def = sparkShellService.getFunctionDefs().negate;
                } else if (arg.type === SparkType.LITERAL) {
                    return new SparkExpression("-" + arg.source, SparkType.LITERAL, arg.start, node.end);
                }
                break;

            case "!":
                def = sparkShellService.getFunctionDefs().not;
                break;

            default:
        }

        if (def === null) {
            throw new ParseException("Unary operator not supported: " + node.operator, node.start);
        }

        // Convert to a Spark expression
        return SparkExpression.fromDefinition(def, node, arg);
    }

    /**
     * Converts the specified abstract syntax tree to a Scala expression for a Spark script.
     *
     * @param {acorn.Node} program the program node
     * @param {SparkShellService} sparkShellService the spark shell service
     * @returns {string} the Scala expression
     * @throws {Error} if a function definition is not valid
     * @throws {ParseException} if the program is not valid
     */
    function toScript(program, sparkShellService) {
        // Check node parameters
        if (program.type !== "Program") {
            throw new Error("Cannot convert non-program to Spark");
        }
        if (program.body.length !== 1) {
            throw new Error("Program is too long");
        }

        // Convert to a DataFrame
        var spark = toSpark(program.body[0], sparkShellService);

        switch (spark.type) {
            case SparkType.COLUMN:
            case SparkType.CONDITION_CHAIN:
                return ".select(new Column(\"*\"), " + spark.source + ")";

            case SparkType.DATA_FRAME:
                return spark.source;

            case SparkType.LITERAL:
                var column = SparkExpression.format("%c", spark);
                return ".select(new Column(\"*\"), " + column + ")";

            case SparkType.TRANSFORM:
                return ".transform(" + spark.source + ")";

            default:
                throw new Error("Result type not supported: " + spark.type);
        }
    }

    /**
     * Converts the specified abstract syntax tree to a Spark expression object.
     *
     * @param {acorn.Node} node the abstract syntax tree
     * @param {SparkShellService} sparkShellService the spark shell service
     * @returns {SparkExpression} the Spark expression
     * @throws {Error} if a function definition is not valid
     * @throws {ParseException} if the node is not valid
     */
    function toSpark(node, sparkShellService) {
        switch (node.type) {
            case "ArrayExpression":
                return parseArrayExpression(node, sparkShellService);

            case "BinaryExpression":
                return parseBinaryExpression(node, sparkShellService);

            case "CallExpression":
                return parseCallExpression(node, sparkShellService);

            case "ExpressionStatement":
                return toSpark(node.expression, sparkShellService);

            case "Identifier":
                var label = StringUtils.quote(sparkShellService.getColumnLabel(node.name));
                return new SparkExpression("new Column(\"" + label + "\")", SparkType.COLUMN, node.start, node.end);

            case "Literal":
                return new SparkExpression(node.raw, SparkType.LITERAL, node.start, node.end);

            case "LogicalExpression":
                return parseLogicalExpression(node, sparkShellService);

            case "UnaryExpression":
                return parseUnaryExpression(node, sparkShellService);

            default:
                throw new Error("Unsupported node type: " + node.type);
        }
    }

    /**
     * Functions for parsing a TernJS tree into Spark code.
     *
     * @type {Object} SparkParserService
     */
    return {
        DATA_FRAME_VARIABLE: DATA_FRAME_VARIABLE,
        TERNJS_COLUMN_TYPE: TERNJS_COLUMN_TYPE,
        toScript: toScript
    }
}]);
});
