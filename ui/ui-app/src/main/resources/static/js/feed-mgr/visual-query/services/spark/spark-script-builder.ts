import {ScriptBuilder} from "../script-builder";
import {SparkExpression} from "./spark-expression";
import {Expression, Identifier} from "estree";
import {SparkExpressionType} from "./spark-expression-type";
import {SparkConstants} from "./spark-constants";
import {SparkQueryEngine} from "./spark-query-engine";

/**
 * Parses an abstract syntax tree into a Spark script.
 */
export class SparkScriptBuilder extends ScriptBuilder<SparkExpression> {

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
        switch (spark.type.toString()) {
            case SparkExpressionType.COLUMN.value:
            case SparkExpressionType.CONDITION_CHAIN.value:
                return ".select(" + SparkConstants.DATA_FRAME_VARIABLE + "(\"*\"), " + spark.source + ")";

            case SparkExpressionType.DATA_FRAME.value:
                return spark.source as string;

            case SparkExpressionType.LITERAL.value:
                const column = SparkExpression.format("%c", spark);
                return ".select(" + SparkConstants.DATA_FRAME_VARIABLE + "(\"*\"), " + column + ")";

            case SparkExpressionType.TRANSFORM.value:
                return ".transform(" + spark.source + ")";

            default:
                throw new Error("Result type not supported: " + spark.type);
        }
    }

    /**
     * Gets the Ternjs name of the specified expression type.
     */
    protected toTernjsName(sparkType: SparkExpressionType): string {
        return SparkExpressionType.toTernjsName(sparkType.toString());
    }
}
