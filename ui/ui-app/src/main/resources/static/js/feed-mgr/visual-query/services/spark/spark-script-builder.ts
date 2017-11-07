import {Expression, Identifier} from "estree";

import {ScriptBuilder} from "../../wrangler/script-builder";
import {SparkConstants} from "./spark-constants";
import {SparkExpression} from "./spark-expression";
import {SparkExpressionType} from "./spark-expression-type";
import {SparkQueryEngine} from "./spark-query-engine";

/**
 * Parses an abstract syntax tree into a Spark script.
 */
export class SparkScriptBuilder extends ScriptBuilder<SparkExpression, string> {

    /**
     * Constructs a {@code SparkScriptBuilder}.
     *
     * @param functions - ternjs functions
     * @param queryEngine - Spark query engine
     */
    constructor(functions: any, private queryEngine: SparkQueryEngine) {
        super(functions);
    }

    /**
     * Creates a script expression with the specified child expression appended to the parent expression.
     */
    protected appendChildExpression(parent: SparkExpression, child: SparkExpression): SparkExpression {
        return this.createScriptExpression((parent.source as string) + (child.source as string), child.type, child.start, child.end);
    }

    /**
     * Creates a script expression for the specified AST node.
     */
    protected createScriptExpression(source: any, type: SparkExpressionType, start: number, end: number): SparkExpression {
        return new SparkExpression(source, type, start, end);
    }

    /**
     * Creates a script expression from a function definition and AST node.
     */
    protected createScriptExpressionFromDefinition(definition: any, node: acorn.Node, ...var_args: SparkExpression[]): SparkExpression {
        return SparkExpression.fromDefinition(definition, node, ...var_args);
    }

    /**
     * Indicates if the specified function definition can be converted to a script expression.
     */
    protected hasScriptExpression(definition: any): boolean {
        return definition[SparkExpression.SPARK_DIRECTIVE] != null;
    }

    /**
     * Indicates if the specified expression type is an object.
     */
    protected isObject(sparkType: SparkExpressionType): boolean {
        return SparkExpressionType.isObject(sparkType.toString());
    }

    /**
     * Parses an identifier into a script expression.
     */
    protected parseIdentifier(node: Identifier & acorn.Node): SparkExpression {
        let label = StringUtils.quote(this.queryEngine.getColumnLabel(node.name));
        return new SparkExpression(SparkConstants.DATA_FRAME_VARIABLE + "(\"" + label + "\")", SparkExpressionType.COLUMN, node.start, node.end);
    }

    /**
     * Converts the specified script expression to a transform script.
     */
    protected prepareScript(spark: SparkExpression): string {
        if (SparkExpressionType.COLUMN.equals(spark.type) || SparkExpressionType.CONDITION_CHAIN.equals(spark.type)) {
            return ".select(" + SparkConstants.DATA_FRAME_VARIABLE + "(\"*\"), " + spark.source + ")";
        }
        if (SparkExpressionType.DATA_FRAME.equals(spark.type)) {
            return spark.source as string;
        }
        if (SparkExpressionType.LITERAL.equals(spark.type)) {
            const column = SparkExpression.format("%c", spark);
            return ".select(" + SparkConstants.DATA_FRAME_VARIABLE + "(\"*\"), " + column + ")";
        }
        if (SparkExpressionType.TRANSFORM.equals(spark.type)) {
            return ".transform(" + spark.source + ")";
        }
        throw new Error("Result type not supported: " + spark.type);
    }

    /**
     * Gets the Ternjs name of the specified expression type.
     */
    protected toTernjsName(sparkType: SparkExpressionType): string {
        return SparkExpressionType.toTernjsName(sparkType.toString());
    }
}
