import {ScriptBuilder} from "../script-builder";
import {TeradataExpression} from "./teradata-expression";
import {TeradataExpressionType} from "./teradata-expression-type";
import {Identifier} from "estree";
import {TeradataScript} from "./teradata-script";
import {TeradataQueryEngine} from "./teradata-query-engine";

/**
 * Parses an abstract syntax tree into a Teradata script.
 */
export class TeradataScriptBuilder extends ScriptBuilder<TeradataExpression, TeradataScript> {

    /**
     * Constructs a {@code TeradataScriptBuilder}.
     *
     * @param functions - ternjs functions
     * @param queryEngine - Teradata query engine
     */
    constructor(functions: any, private queryEngine: TeradataQueryEngine) {
        super(functions);
    }

    /**
     * Creates a script expression with the specified child expression appended to the parent expression.
     */
    protected appendChildExpression(parent: TeradataExpression, child: TeradataExpression): TeradataExpression {
        if (!TeradataExpressionType.SELECT.equals(parent.type) && parent.type.equals(child.type)) {
            return new TeradataExpression((parent.source as string) + (child.source as string), child.type, child.start, child.end);
        } else {
            const parentSource = (typeof parent.source === "string") ? this.prepareScript(parent) : parent.source;
            const childSource = (typeof child.source === "string") ? this.prepareScript(child) : child.source;
            const newSource: TeradataScript = {
                groupBy: childSource.groupBy ? childSource.groupBy : parentSource.groupBy,
                having: childSource.having ? childSource.having : parentSource.having,
                keywordList: childSource.keywordList ? childSource.keywordList : parentSource.keywordList,
                selectList: childSource.selectList ? childSource.selectList : parentSource.selectList,
                where: childSource.where ? childSource.where : parentSource.where
            };
            return new TeradataExpression(newSource, TeradataExpressionType.SELECT, child.start, child.end);
        }
    }

    /**
     * Creates a script expression for the specified AST node.
     */
    protected createScriptExpression(source: any, type: TeradataExpressionType, start: number, end: number): TeradataExpression {
        return new TeradataExpression(source, type, start, end);
    }

    /**
     * Creates a script expression from a function definition and AST node.
     */
    protected createScriptExpressionFromDefinition(definition: any, node: acorn.Node, ...var_args: TeradataExpression[]): TeradataExpression {
        return TeradataExpression.fromDefinition(definition, node, ...var_args);
    }

    /**
     * Indicates if the specified expression type is an object.
     */
    protected isObject(type: TeradataExpressionType): boolean {
        return TeradataExpressionType.isObject(type.toString());
    }

    /**
     * Parses an identifier into a script expression.
     */
    protected parseIdentifier(node: Identifier & acorn.Node): TeradataExpression {
        let label = StringUtils.quote(this.queryEngine.getColumnLabel(node.name));
        return new TeradataExpression("\"" + label + "\"", TeradataExpressionType.COLUMN, node.start, node.end);
    }

    /**
     * Parses an identifier into a script expression.
     */
    protected prepareScript(expression: TeradataExpression): TeradataScript {
        if (TeradataExpressionType.ARRAY.equals(expression.type) || TeradataExpressionType.COLUMN.equals(expression.type) || TeradataExpressionType.LITERAL.equals(expression.type)) {
            let selectList = TeradataExpression.needsColumnAlias(expression.source as string) ? TeradataExpression.addColumnAlias(expression.source as string) : expression.source as string;
            return {groupBy: null, having: null, keywordList: null, selectList: "*, " + selectList, where: null};
        }
        if (TeradataExpressionType.GROUP_BY.equals(expression.type)) {
            return {groupBy: expression.source as string, having: null, keywordList: null, selectList: null, where: null};
        }
        if (TeradataExpressionType.HAVING.equals(expression.type)) {
            return {groupBy: null, having: expression.source as string, keywordList: null, selectList: null, where: null};
        }
        if (TeradataExpressionType.KEYWORD.equals(expression.type)) {
            return {groupBy: null, having: null, keywordList: expression.source as string, selectList: null, where: null};
        }
        if (TeradataExpressionType.SELECT.equals(expression.type)) {
            if (typeof expression.source === "string") {
                return {groupBy: null, having: null, keywordList: null, selectList: expression.source, where: null};
            } else {
                return expression.source as TeradataScript;
            }
        }
        if (TeradataExpressionType.WHERE.equals(expression.type)) {
            return {groupBy: null, having: null, keywordList: null, selectList: null, where: expression.source as string};
        }
        throw new Error("Result type not supported: " + expression.type);
    }

    /**
     * Gets the Ternjs name of the specified expression type.
     */
    protected toTernjsName(type: TeradataExpressionType): string {
        return TeradataExpressionType.toTernjsName(type.toString());
    }
}
