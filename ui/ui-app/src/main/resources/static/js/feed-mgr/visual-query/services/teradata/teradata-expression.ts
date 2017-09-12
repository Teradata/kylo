import {TeradataExpressionType} from "./teradata-expression-type";
import {ParseException} from "../parse-exception";
import {TeradataScript} from "./teradata-script";

declare const angular: angular.IAngularStatic;

/**
 * Context for formatting a Teradata conversion string.
 */
interface FormatContext {

    /**
     * Format parameters
     */
    args: TeradataExpression[];

    /**
     * Current position within {@code args}
     */
    index: number;

    /**
     * Indicates that all columns should have an alias
     */
    requireAlias: boolean;
}

/**
 * Type of source for {@link TeradataExpression}
 */
type SourceType = string | TeradataScript;

/**
 * An expression in a Teradata SQL script.
 */
export class TeradataExpression {

    /** Regular expression for conversion strings */
    static FORMAT_REGEX = /%([?*,@]*)([cs])/g;

    /** TernJS directive for the Teradata code */
    static TERADATA_DIRECTIVE = "!sql";

    /** TernJS directive for the return type */
    static TYPE_DIRECTIVE = "!sqlType";

    /**
     * Constructs a {@code TeradataExpression}
     *
     * @param source - Teradata code
     * @param type - result type
     * @param start - column of the first character in the original expression
     * @param end - column of the last character in the original expression
     */
    constructor(public source: SourceType, public type: TeradataExpressionType, public start: number, public end: number) {
    }

    /**
     * Adds an alias to the specified expression.
     */
    static addColumnAlias(expression: string): string {
        return expression + " AS \"" + expression.replace(/"/g, "") + "\"";
    }

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
    static format(str: string, requireAlias: boolean, ...args: TeradataExpression[]): string {
        // Convert arguments
        const context: FormatContext = {
            args: args,
            index: 0,
            requireAlias: requireAlias
        };
        const result = str.replace(TeradataExpression.FORMAT_REGEX, angular.bind(str, TeradataExpression.replace, context) as any);

        // Verify all arguments converted
        if (context.index >= context.args.length) {
            return result;
        } else {
            throw new ParseException("Too many arguments for conversion.");
        }
    }

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
    static fromDefinition(definition: any, node: acorn.Node, ...var_args: TeradataExpression[]): TeradataExpression {
        // Convert Spark string to code
        const args = [definition[TeradataExpression.TERADATA_DIRECTIVE], definition[TeradataExpression.TYPE_DIRECTIVE] === "Select"];
        Array.prototype.push.apply(args, Array.prototype.slice.call(arguments, 2));

        const source = TeradataExpression.format.apply(TeradataExpression, args);

        // Return expression
        return new TeradataExpression(source, TeradataExpressionType.valueOf(definition[TeradataExpression.TYPE_DIRECTIVE]), node.start, node.end);
    }

    /**
     * Indicates if the expression does not have a column alias.
     */
    static needsColumnAlias(expression: string): boolean {
        return !expression.match(/^"[^"]+"$| AS "[^"]+"$/);
    }

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
    }

    /**
     * Converts the specified Teradata expression to a Column type.
     *
     * @param expression - the Teradata expression
     * @param requireAlias - true to ensure that the column has an alias
     * @returns the Teradata code for the new type
     * @throws {ParseException} if the expression cannot be converted to a column
     */
    private static toColumn(expression: TeradataExpression, requireAlias: boolean): SourceType {
        if (TeradataExpressionType.COLUMN.equals(expression.type) || TeradataExpressionType.LITERAL.equals(expression.type)) {
            if (requireAlias && TeradataExpression.needsColumnAlias(expression.source as string)) {
                return TeradataExpression.addColumnAlias(expression.source as string);
            } else {
                return expression.source;
            }
        }
        throw new ParseException("Expression cannot be converted to a column: " + expression.type, expression.start);
    }

    /**
     * Converts the specified Teradata expression to a string literal.
     *
     * @param expression - the Teradata expression
     * @returns the Teradata code for the string literal
     * @throws {ParseException} if the expression cannot be converted to a string
     */
    private static toString(expression: TeradataExpression): SourceType {
        if (!TeradataExpressionType.LITERAL.equals(expression.type)) {
            throw new ParseException("Expression cannot be converted to a string: " + expression.type, expression.start);
        }
        if ((expression.source as string).charAt(0) === "'") {
            return (expression.source as string).replace(/\\'/g, "''");
        }
        if ((expression.source as string).charAt(0) === "\"") {
            return (expression.source as string).replace(/\\"/g, "\"\"");
        }
        return "'" + expression.source + "'";
    }
}
