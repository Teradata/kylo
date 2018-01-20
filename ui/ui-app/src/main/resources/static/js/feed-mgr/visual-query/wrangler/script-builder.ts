import {ArrayExpression, BinaryExpression, CallExpression, Expression, Identifier, Literal, LogicalExpression, MemberExpression, Program, Statement, UnaryExpression} from "estree";
import {QueryEngineConstants} from "./query-engine-constants";
import {ParseException} from "./parse-exception";
import {ScriptExpression} from "./script-expression";
import {ScriptExpressionType} from "./script-expression-type";

/**
 * Builds a script from an abstract syntax tree.
 */
export abstract class ScriptBuilder<E extends ScriptExpression, T> {

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
    toScript(program: Program): T {
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
     * Creates a script expression with the specified child expression appended to the parent expression.
     */
    protected abstract appendChildExpression(parent: E, child: E): E;

    /**
     * Creates a script expression for the specified AST node.
     */
    protected abstract createScriptExpression<X extends ScriptExpressionType>(source: any, type: X, start: number, end: number): E;

    /**
     * Creates a script expression from a function definition and AST node.
     */
    protected abstract createScriptExpressionFromDefinition(definition: any, node: acorn.Node, ...var_args: E[]): E;

    /**
     * Indicates if the specified function definition can be converted to a script expression.
     */
    protected abstract hasScriptExpression(definition: any): boolean;

    /**
     * Indicates if the specified expression type is an object.
     */
    protected abstract isObject<X extends ScriptExpressionType>(sparkType: X): boolean;

    /**
     * Parses an identifier into a script expression.
     */
    protected abstract parseIdentifier(node: Identifier & acorn.Node): E;

    /**
     * Converts the specified script expression to a transform script.
     */
    protected abstract prepareScript(expression: E): T;

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
    private parseArrayExpression(node: ArrayExpression & acorn.Node): E {
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
    private parseBinaryExpression(node: BinaryExpression & acorn.Node): E {
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
    private parseCallExpression(node: CallExpression): E {
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
                let ternjsName = this.toTernjsName(parent.type);

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
        return (parent !== null) ? this.appendChildExpression(parent, spark) : spark;
    }

    /**
     * Converts the specified expression to a script expression object.
     */
    private parseExpression(expression: Expression): E {
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

            case "MemberExpression":
                return this.parseMemberExpression(expression as MemberExpression & acorn.Node);

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
    private parseLogicalExpression(node: LogicalExpression & acorn.Node): E {
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
     * Converts the specified member expression to a script expression object.
     *
     * @param node - the abstract syntax tree
     * @returns the script expression
     * @throws {Error} if a function definition is not valid
     * @throws {ParseException} if the node is not valid
     */
    private parseMemberExpression(node: MemberExpression & acorn.Node): E {
        // Check object type
        if (node.object.type !== "Identifier") {
            throw new ParseException("Unexpected object type for member expression: " + node.object.type);
        }

        // Create child expression
        const parentDef = this.functions[node.object.name];
        const childDef = parentDef[(node.property as Identifier).name];
        const expression = this.createScriptExpressionFromDefinition(childDef, node);

        // Check for parent expression
        if (this.hasScriptExpression(parentDef)) {
            const parent = this.createScriptExpressionFromDefinition(parentDef, node);
            return this.appendChildExpression(parent, expression);
        } else {
            return expression;
        }
    }

    /**
     * Converts the specified statement to a script expression object.
     *
     * @param statement - the abstract syntax tree
     * @returns the script expression
     * @throws {Error} if a function definition is not valid
     * @throws {ParseException} if the node is not valid
     */
    private parseStatement(statement: Statement): E {
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
    private parseUnaryExpression(node: UnaryExpression & acorn.Node): E {
        // Get the function definition
        let arg = this.parseExpression(node.argument);
        let def = null;

        switch (node.operator) {
            case "-":
                if (ScriptExpressionType.COLUMN.equals(arg.type)) {
                    def = this.functions.negate;
                } else if (ScriptExpressionType.LITERAL.equals(arg.type)) {
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
