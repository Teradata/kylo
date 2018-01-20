import {Expression} from "estree";

import {ParseException} from "../../wrangler/parse-exception";
import {SparkConstants} from "./spark-constants";
import {SparkExpressionType} from "./spark-expression-type";

declare const angular: angular.IAngularStatic;

/**
 * Context for formatting a Spark conversion string.
 */
interface FormatContext {

    /**
     * Format parameters
     */
    args: SparkExpression[];

    /**
     * Current position within {@code args}
     */
    index: number;
}

/**
 * An expression in a Spark script.
 */
export class SparkExpression {

    /** Regular expression for conversion strings */
    static FORMAT_REGEX = /%([?*,@]*)([bcdfors])/g;

    /** TernJS directive for the Spark code */
    static SPARK_DIRECTIVE = "!spark";

    /** TernJS directive for the return type */
    static TYPE_DIRECTIVE = "!sparkType";

    /**
     * Constructs a {@code SparkExpression}
     *
     * @param source - Spark source code
     * @param type - result type
     * @param start - column of the first character in the original expression
     * @param end - column of the last character in the original expression
     */
    constructor(public source: string | SparkExpression[], public type: SparkExpressionType, public start: number, public end: number) {
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
    static format(str: string, ...args: SparkExpression[]): string {
        // Convert arguments
        let context = {
            args: Array.prototype.slice.call(arguments, 1),
            index: 0
        } as FormatContext;
        let result = str.replace(SparkExpression.FORMAT_REGEX, angular.bind(str, SparkExpression.replace, context) as any);

        // Verify all arguments converted
        if (context.index >= context.args.length) {
            return result;
        } else {
            throw new ParseException("Too many arguments for conversion.");
        }
    }

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
    static fromDefinition(definition: any, node: acorn.Node, ...var_args: SparkExpression[]): SparkExpression {
        // Convert Spark string to code
        const args = [definition[SparkExpression.SPARK_DIRECTIVE]];
        Array.prototype.push.apply(args, Array.prototype.slice.call(arguments, 2));

        const source = SparkExpression.format.apply(SparkExpression, args);

        // Return expression
        return new SparkExpression(source, SparkExpressionType.valueOf(definition[SparkExpression.TYPE_DIRECTIVE]), node.start, node.end);
    }

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
    private static replace(context: FormatContext, match: string, flags: string, type: string): string {
        // Parse flags
        let arrayType = null;
        let comma = false;
        let end = context.index + 1;

        for (let i = 0; i < flags.length; ++i) {
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
        let first = true;
        let result = "";

        for (; context.index < end; ++context.index) {
            // Argument separator
            if (comma || !first) {
                result += ", ";
            } else {
                first = false;
            }

            // Conversion
            let arg = context.args[context.index];

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
    }

    /**
     * Converts the specified Spark expression to an array.
     *
     * @param expression - the Spark expression
     * @param type - the type specifier
     * @returns the Spark code for the array
     * @throws {ParseException} if the expression cannot be converted to an array
     */
    private static toArray(expression: SparkExpression, type: string): string {
        if (SparkExpressionType.ARRAY.equals(expression.type)) {
            let source = expression.source as SparkExpression[];
            return "Array(" + source
                .map(function (e) {
                    return SparkExpression.format("%" + type, e);
                })
                .join(", ") + ")";
        } else {
            throw new ParseException("Expression cannot be converted to an array: " + expression.type, expression.start);
        }
    }

    /**
     * Converts the specified Spark expression to a boolean literal.
     *
     * @param expression - the Spark expression
     * @returns the Spark code for the boolean
     * @throws {ParseException} if the expression cannot be converted to a boolean
     */
    private static toBoolean(expression: SparkExpression): string {
        if (SparkExpressionType.LITERAL.equals(expression.type) && (expression.source === "true" || expression.source === "false")) {
            return expression.source;
        } else {
            throw new ParseException("Expression cannot be converted to a boolean: " + expression.type, expression.start);
        }
    }

    /**
     * Converts the specified Spark expression to a Column type.
     *
     * @param expression - the Spark expression
     * @returns the Spark code for the new type
     * @throws {ParseException} if the expression cannot be converted to a column
     */
    private static toColumn(expression: SparkExpression): string {
        if (SparkExpressionType.COLUMN.equals(expression.type)) {
            return expression.source as string;
        }
        if (SparkExpressionType.LITERAL.equals(expression.type)) {
            let literal;
            if ((expression.source as string).charAt(0) === "\"" || (expression.source as string).charAt(0) === "'") {
                literal = SparkExpression.toString(expression);
            } else {
                literal = expression.source;
            }
            return "functions.lit(" + literal + ")";
        }
        throw new ParseException("Expression cannot be converted to a column: " + expression.type, expression.start);
    }

    /**
     * Converts the specified Spark expression to a DataFrame type.
     *
     * @param expression - the Spark expression
     * @returns the Spark code for the new type
     * @throws {ParseException} if the expression cannot be converted to a DataFrame
     */
    private static toDataFrame(expression: SparkExpression): string {
        if (SparkExpressionType.DATA_FRAME.equals(expression.type)) {
            return SparkConstants.DATA_FRAME_VARIABLE + expression.source;
        }
        throw new ParseException("Expression cannot be converted to a DataFrame: " + expression.type, expression.start);
    }

    /**
     * Converts the specified Spark expression to a double.
     *
     * @param expression - the Spark expression
     * @returns the Spark code for the double
     * @throws {ParseException} if the expression cannot be converted to a double
     */
    private static toDouble(expression: SparkExpression): string {
        if (SparkExpressionType.LITERAL.equals(expression.type) && (expression.source as string).match(/^(0|-?[1-9][0-9]*)(\.[0-9]+)?$/) !== null) {
            return expression.source as string;
        } else {
            throw new ParseException("Expression cannot be converted to an integer: " + expression.type, expression.start);
        }
    }

    /**
     * Converts the specified Spark expression to an integer.
     *
     * @param expression - the Spark expression
     * @returns the Spark code for the integer
     * @throws {ParseException} if the expression cannot be converted to a number
     */
    private static toInteger(expression: SparkExpression): string {
        if (SparkExpressionType.LITERAL.equals(expression.type) && (expression.source as string).match(/^(0|-?[1-9][0-9]*)$/) !== null) {
            return expression.source as string;
        } else {
            throw new ParseException("Expression cannot be converted to an integer: " + expression.type, expression.start);
        }
    }

    /**
     * Converts the specified Spark expression to an object.
     *
     * @param expression - the Spark expression
     * @returns the Spark code for the object
     * @throws {ParseException} if the expression cannot be converted to an object
     */
    private static toObject(expression: SparkExpression): string {
        if (SparkExpressionType.isObject(expression.type.toString())) {
            return expression.source as string;
        } else if (SparkExpressionType.LITERAL.equals(expression.type)) {
            if ((expression.source as string).charAt(0) === "\"" || (expression.source as string).charAt(0) === "'") {
                return SparkExpression.toString(expression);
            } else {
                return expression.source as string;
            }
        } else {
            throw new ParseException("Expression cannot be converted to an object: " + expression.type, expression.start);
        }
    }

    /**
     * Converts the specified Spark expression to a string literal.
     *
     * @param expression - the Spark expression
     * @returns the Spark code for the string literal
     * @throws {ParseException} if the expression cannot be converted to a string
     */
    private static toString(expression: SparkExpression): string {
        if (!SparkExpressionType.LITERAL.equals(expression.type)) {
            throw new ParseException("Expression cannot be converted to a string: " + expression.type, expression.start);
        }
        if ((expression.source as string).charAt(0) === "\"") {
            return expression.source as string;
        }
        if ((expression.source as string).charAt(0) === "'") {
            return "\"" + (expression.source as string).substr(1, expression.source.length - 2).replace(/"/g, "\\\"") + "\"";
        }
        return "\"" + expression.source + "\"";
    }
}
