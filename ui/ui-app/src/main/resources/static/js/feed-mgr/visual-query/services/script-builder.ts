import {ArrayExpression, BinaryExpression, CallExpression, Expression, Identifier, Literal, LogicalExpression, Program, Statement, UnaryExpression} from "estree";
import {QueryEngineConstants} from "./query-engine-constants";
import {ParseException} from "./parse-exception";
import {ScriptExpression} from "./script-expression";
import {ScriptExpressionType} from "./script-expression-type";

/**
 * Builds a script from an abstract syntax tree.
 */
export abstract class ScriptBuilder<T extends ScriptExpression> {

    /**
     * Constructs a {@code ScriptBuilder}.
     *
     * @param functions the ternjs functions
     */
    constructor(private functions: any) {
    }

    /**
     * Converts the specified abstract syntax tree to a transform script.
     *
     * @param program - the program node
     * @returns the transform script
     * @throws {Error} if a function definition is not valid
     * @throws {ParseException} if the program is not valid
     */
    toScript(program: Program): string {
        // Check node parameters
        if (program.type !== "Program") {
            throw new Error("Cannot convert non-program");
        }
        if (program.body.length !== 1) {
            throw new Error("Program is too long");
        }

        // Convert to a script
        const expression = this.parseStatement(program.body[0] as Statement);
        return this.prepareScript(expression);
    }

    /**
     * Creates a script expression for the specified AST node.
     */
    protected abstract createScriptExpression<X extends ScriptExpressionType>(source: any, type: X, start: number, end: number): T;

    /**
     * Creates a script expression from a function definition and AST node.
     */
    protected abstract createScriptExpressionFromDefinition(definition: any, node: acorn.Node, ...var_args: T[]): T;

    /**
     * Indicates if the specified expression type is an object.
     */
    protected abstract isObject<X extends ScriptExpressionType>(sparkType: X): boolean;

    /**
     * Parses an identifier into a script expression.
     */
    protected abstract parseIdentifier(node: Identifier & acorn.Node): T;

    /**
     * Converts the specified script expression to a transform script.
     */
    protected abstract prepareScript(expression: T): string;

    /**
     * Gets the Ternjs name of the specified expression type.
     */
    protected abstract toTernjsName<X extends ScriptExpressionType>(sparkType: X): string;

    /**
     * Converts an array expression node to a script expression.
     *
     * @param node - the array expression node
     * @returns the script expression
     * @throws {Error} if a function definition is not valid
     * @throws {ParseException} if the node is not valid
     */
    private parseArrayExpression(node: ArrayExpression & acorn.Node): T {
        const source = node.elements.map(this.parseExpression.bind(this));
        return this.createScriptExpression(source, ScriptExpressionType.ARRAY, node.start, node.end);
    }

    /**
     * Converts a binary expression node to a script expression.
     *
     * @param node - the binary expression node
     * @returns the script expression
     * @throws {Error} if the function definition is not valid
     * @throws {ParseException} if a function argument cannot be converted to the required type
     */
    private parseBinaryExpression(node: BinaryExpression & acorn.Node): T {
        // Get the function definition
        let def = null;

        switch (node.operator) {
            case "+":
                def = this.functions.add;
                break;

            case "-":
                def = this.functions.subtract;
                break;

            case "*":
                def = this.functions.multiply;
                break;

            case "/":
                def = this.functions.divide;
                break;

            case "==":
                def = this.functions.equal;
                break;

            case "!=":
                def = this.functions.notEqual;
                break;

            case ">":
                def = this.functions.greaterThan;
                break;

            case ">=":
                def = this.functions.greaterThanOrEqual;
                break;

            case "<":
                def = this.functions.lessThan;
                break;

            case "<=":
                def = this.functions.lessThanOrEqual;
                break;

            default:
        }

        if (def == null) {
            throw new ParseException("Binary operator not supported: " + node.operator, node.start);
        }

        // Convert to a Spark expression
        const left = this.parseExpression(node.left);
        const right = this.parseExpression(node.right);
        return this.createScriptExpressionFromDefinition(def, node, left, right);
    }

    /**
     * Converts a call expression node to a script expression.
     *
     * @param node - the call expression node
     * @returns the script expression
     * @throws {Error} if the function definition is not valid
     * @throws {ParseException} if a function argument cannot be converted to the required type
     */
    private parseCallExpression(node: CallExpression): T {
        // Get the function definition
        let def;
        let name;
        let parent = null;

        switch (node.callee.type) {
            case "Identifier":
                def = this.functions[node.callee.name];
                name = node.callee.name;
                break;

            case "MemberExpression":
                parent = this.parseExpression(node.callee.object as Expression);

                // Find function definition
                let ternjsName = this.toTernjsName(new ScriptExpressionType(parent.type));

                if (ternjsName !== null) {
                    def = this.functions[QueryEngineConstants.DEFINE_DIRECTIVE][ternjsName][(node.callee.property as Identifier).name];
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
        const args = [def, node].concat(node.arguments.map(this.parseExpression.bind(this)));
        const spark = this.createScriptExpressionFromDefinition.apply(this, args);
        return (parent !== null) ? this.createScriptExpression(parent.source + spark.source, spark.type, spark.start, spark.end) : spark;
    }

    /**
     * Converts the specified expression to a script expression object.
     */
    private parseExpression(expression: Expression): T {
        switch (expression.type) {
            case "ArrayExpression":
                return this.parseArrayExpression(expression as ArrayExpression & acorn.Node);

            case "BinaryExpression":
                return this.parseBinaryExpression(expression as BinaryExpression & acorn.Node);

            case "CallExpression":
                return this.parseCallExpression(expression as CallExpression);

            case "Identifier":
                return this.parseIdentifier(expression as Identifier & acorn.Node);

            case "Literal":
                const literal = expression as Literal & acorn.Node;
                return this.createScriptExpression(literal.raw, ScriptExpressionType.LITERAL, literal.start, literal.end);

            case "LogicalExpression":
                return this.parseLogicalExpression(expression as LogicalExpression & acorn.Node);

            case "UnaryExpression":
                return this.parseUnaryExpression(expression as UnaryExpression & acorn.Node);

            default:
                throw new Error("Unsupported expression type: " + expression.type);
        }
    }

    /**
     * Converts a logical expression node to a script expression.
     *
     * @param node - the logical expression node
     * @returns the script expression
     * @throws {Error} if the function definition is not valid
     * @throws {ParseException} if a function argument cannot be converted to the required type
     */
    private parseLogicalExpression(node: LogicalExpression & acorn.Node): T {
        // Get the function definition
        let def = null;

        switch (node.operator) {
            case "&&":
                def = this.functions.and;
                break;

            case "||":
                def = this.functions.or;
                break;

            default:
        }

        if (def == null) {
            throw new ParseException("Logical operator not supported: " + node.operator, node.start);
        }

        // Convert to a Spark expression
        const left = this.parseExpression(node.left);
        const right = this.parseExpression(node.right);
        return this.createScriptExpressionFromDefinition(def, node, left, right);
    }

    /**
     * Converts the specified statement to a script expression object.
     *
     * @param statement - the abstract syntax tree
     * @returns the script expression
     * @throws {Error} if a function definition is not valid
     * @throws {ParseException} if the node is not valid
     */
    private parseStatement(statement: Statement): T {
        if (statement.type === "ExpressionStatement") {
            return this.parseExpression(statement.expression);
        } else {
            throw new Error("Unsupported statement type: " + statement.type);
        }
    }

    /**
     * Converts a unary expression node to a script expression.
     *
     * @param node - the unary expression node
     * @returns the script expression
     * @throws {Error} if the function definition is not valid
     * @throws {ParseException} if a function argument cannot be converted to the required type
     */
    private parseUnaryExpression(node: UnaryExpression & acorn.Node): T {
        // Get the function definition
        let arg = this.parseExpression(node.argument);
        let def = null;

        switch (node.operator) {
            case "-":
                if (arg.type === ScriptExpressionType.COLUMN.toString()) {
                    def = this.functions.negate;
                } else if (arg.type === ScriptExpressionType.LITERAL.toString()) {
                    return this.createScriptExpression("-" + arg.source, ScriptExpressionType.LITERAL, arg.start, node.end);
                }
                break;

            case "!":
                def = this.functions.not;
                break;

            default:
        }

        if (def === null) {
            throw new ParseException("Unary operator not supported: " + node.operator, node.start);
        }

        // Convert to a Spark expression
        return this.createScriptExpressionFromDefinition(def, node, arg);
    }
}
