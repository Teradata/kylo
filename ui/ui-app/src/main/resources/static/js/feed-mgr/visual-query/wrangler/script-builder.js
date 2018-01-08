define(["require", "exports", "./query-engine-constants", "./parse-exception", "./script-expression-type"], function (require, exports, query_engine_constants_1, parse_exception_1, script_expression_type_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Builds a script from an abstract syntax tree.
     */
    var ScriptBuilder = /** @class */ (function () {
        /**
         * Constructs a {@code ScriptBuilder}.
         *
         * @param functions the ternjs functions
         */
        function ScriptBuilder(functions) {
            this.functions = functions;
        }
        /**
         * Converts the specified abstract syntax tree to a transform script.
         *
         * @param program - the program node
         * @returns the transform script
         * @throws {Error} if a function definition is not valid
         * @throws {ParseException} if the program is not valid
         */
        ScriptBuilder.prototype.toScript = function (program) {
            // Check node parameters
            if (program.type !== "Program") {
                throw new Error("Cannot convert non-program");
            }
            if (program.body.length !== 1) {
                throw new Error("Program is too long");
            }
            // Convert to a script
            var expression = this.parseStatement(program.body[0]);
            return this.prepareScript(expression);
        };
        /**
         * Converts an array expression node to a script expression.
         *
         * @param node - the array expression node
         * @returns the script expression
         * @throws {Error} if a function definition is not valid
         * @throws {ParseException} if the node is not valid
         */
        ScriptBuilder.prototype.parseArrayExpression = function (node) {
            var source = node.elements.map(this.parseExpression.bind(this));
            return this.createScriptExpression(source, script_expression_type_1.ScriptExpressionType.ARRAY, node.start, node.end);
        };
        /**
         * Converts a binary expression node to a script expression.
         *
         * @param node - the binary expression node
         * @returns the script expression
         * @throws {Error} if the function definition is not valid
         * @throws {ParseException} if a function argument cannot be converted to the required type
         */
        ScriptBuilder.prototype.parseBinaryExpression = function (node) {
            // Get the function definition
            var def = null;
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
                throw new parse_exception_1.ParseException("Binary operator not supported: " + node.operator, node.start);
            }
            // Convert to a Spark expression
            var left = this.parseExpression(node.left);
            var right = this.parseExpression(node.right);
            return this.createScriptExpressionFromDefinition(def, node, left, right);
        };
        /**
         * Converts a call expression node to a script expression.
         *
         * @param node - the call expression node
         * @returns the script expression
         * @throws {Error} if the function definition is not valid
         * @throws {ParseException} if a function argument cannot be converted to the required type
         */
        ScriptBuilder.prototype.parseCallExpression = function (node) {
            // Get the function definition
            var def;
            var name;
            var parent = null;
            switch (node.callee.type) {
                case "Identifier":
                    def = this.functions[node.callee.name];
                    name = node.callee.name;
                    break;
                case "MemberExpression":
                    parent = this.parseExpression(node.callee.object);
                    // Find function definition
                    var ternjsName = this.toTernjsName(parent.type);
                    if (ternjsName !== null) {
                        def = this.functions[query_engine_constants_1.QueryEngineConstants.DEFINE_DIRECTIVE][ternjsName][node.callee.property.name];
                    }
                    else {
                        throw new parse_exception_1.ParseException("Result type has no members: " + parent.type);
                    }
                    break;
                default:
                    throw new parse_exception_1.ParseException("Function call type not supported: " + node.callee.type);
            }
            if (def == null) {
                throw new parse_exception_1.ParseException("Function is not defined: " + name);
            }
            // Convert to a Spark expression
            var args = [def, node].concat(node.arguments.map(this.parseExpression.bind(this)));
            var spark = this.createScriptExpressionFromDefinition.apply(this, args);
            return (parent !== null) ? this.appendChildExpression(parent, spark) : spark;
        };
        /**
         * Converts the specified expression to a script expression object.
         */
        ScriptBuilder.prototype.parseExpression = function (expression) {
            switch (expression.type) {
                case "ArrayExpression":
                    return this.parseArrayExpression(expression);
                case "BinaryExpression":
                    return this.parseBinaryExpression(expression);
                case "CallExpression":
                    return this.parseCallExpression(expression);
                case "Identifier":
                    return this.parseIdentifier(expression);
                case "Literal":
                    var literal = expression;
                    return this.createScriptExpression(literal.raw, script_expression_type_1.ScriptExpressionType.LITERAL, literal.start, literal.end);
                case "LogicalExpression":
                    return this.parseLogicalExpression(expression);
                case "MemberExpression":
                    return this.parseMemberExpression(expression);
                case "UnaryExpression":
                    return this.parseUnaryExpression(expression);
                default:
                    throw new Error("Unsupported expression type: " + expression.type);
            }
        };
        /**
         * Converts a logical expression node to a script expression.
         *
         * @param node - the logical expression node
         * @returns the script expression
         * @throws {Error} if the function definition is not valid
         * @throws {ParseException} if a function argument cannot be converted to the required type
         */
        ScriptBuilder.prototype.parseLogicalExpression = function (node) {
            // Get the function definition
            var def = null;
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
                throw new parse_exception_1.ParseException("Logical operator not supported: " + node.operator, node.start);
            }
            // Convert to a Spark expression
            var left = this.parseExpression(node.left);
            var right = this.parseExpression(node.right);
            return this.createScriptExpressionFromDefinition(def, node, left, right);
        };
        /**
         * Converts the specified member expression to a script expression object.
         *
         * @param node - the abstract syntax tree
         * @returns the script expression
         * @throws {Error} if a function definition is not valid
         * @throws {ParseException} if the node is not valid
         */
        ScriptBuilder.prototype.parseMemberExpression = function (node) {
            // Check object type
            if (node.object.type !== "Identifier") {
                throw new parse_exception_1.ParseException("Unexpected object type for member expression: " + node.object.type);
            }
            // Create child expression
            var parentDef = this.functions[node.object.name];
            var childDef = parentDef[node.property.name];
            var expression = this.createScriptExpressionFromDefinition(childDef, node);
            // Check for parent expression
            if (this.hasScriptExpression(parentDef)) {
                var parent_1 = this.createScriptExpressionFromDefinition(parentDef, node);
                return this.appendChildExpression(parent_1, expression);
            }
            else {
                return expression;
            }
        };
        /**
         * Converts the specified statement to a script expression object.
         *
         * @param statement - the abstract syntax tree
         * @returns the script expression
         * @throws {Error} if a function definition is not valid
         * @throws {ParseException} if the node is not valid
         */
        ScriptBuilder.prototype.parseStatement = function (statement) {
            if (statement.type === "ExpressionStatement") {
                return this.parseExpression(statement.expression);
            }
            else {
                throw new Error("Unsupported statement type: " + statement.type);
            }
        };
        /**
         * Converts a unary expression node to a script expression.
         *
         * @param node - the unary expression node
         * @returns the script expression
         * @throws {Error} if the function definition is not valid
         * @throws {ParseException} if a function argument cannot be converted to the required type
         */
        ScriptBuilder.prototype.parseUnaryExpression = function (node) {
            // Get the function definition
            var arg = this.parseExpression(node.argument);
            var def = null;
            switch (node.operator) {
                case "-":
                    if (script_expression_type_1.ScriptExpressionType.COLUMN.equals(arg.type)) {
                        def = this.functions.negate;
                    }
                    else if (script_expression_type_1.ScriptExpressionType.LITERAL.equals(arg.type)) {
                        return this.createScriptExpression("-" + arg.source, script_expression_type_1.ScriptExpressionType.LITERAL, arg.start, node.end);
                    }
                    break;
                case "!":
                    def = this.functions.not;
                    break;
                default:
            }
            if (def === null) {
                throw new parse_exception_1.ParseException("Unary operator not supported: " + node.operator, node.start);
            }
            // Convert to a Spark expression
            return this.createScriptExpressionFromDefinition(def, node, arg);
        };
        return ScriptBuilder;
    }());
    exports.ScriptBuilder = ScriptBuilder;
});
//# sourceMappingURL=script-builder.js.map