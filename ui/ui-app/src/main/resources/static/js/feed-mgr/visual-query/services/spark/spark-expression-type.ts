import {QueryEngineConstants} from "../../wrangler/query-engine-constants";
import {ScriptExpressionType} from "../../wrangler/script-expression-type";

/**
 * Types supported by {@link SparkExpression}.
 */
export class SparkExpressionType extends ScriptExpressionType {

    /** Represents a chain of {@code when} function calls */
    static CONDITION_CHAIN = new SparkExpressionType("ConditionChain");

    /** Represents a Spark SQL DataFrame */
    static DATA_FRAME = new SparkExpressionType("dataframe");

    /** Represents a function that takes a DataFrame and returns a DataFrame */
    static TRANSFORM = new SparkExpressionType("transform");

    /**
     * Indicates if the specified type is an object type.
     *
     * @param sparkType - the Spark type
     * @returns true if the type is an object type, or false otherwise
     */
    static isObject(sparkType: string): boolean {
        return (sparkType !== SparkExpressionType.ARRAY.toString() && sparkType !== SparkExpressionType.LITERAL.toString() && sparkType !== SparkExpressionType.TRANSFORM.toString())
    }

    /**
     * Gets the TernJS definition name for the specified type.
     *
     * @param sparkType - the Spark type
     */
    static toTernjsName(sparkType: string): string {
        return (sparkType === SparkExpressionType.COLUMN.toString()) ? QueryEngineConstants.TERNJS_COLUMN_TYPE : sparkType;
    }
}
