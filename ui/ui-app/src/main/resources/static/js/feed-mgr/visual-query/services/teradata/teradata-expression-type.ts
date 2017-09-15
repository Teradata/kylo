import {ScriptExpressionType} from "../script-expression-type";
import {QueryEngineConstants} from "../query-engine-constants";

/**
 * Types supported by {@link TeradataExpression}.
 */
export class TeradataExpressionType extends ScriptExpressionType {

    /**
     * GROUP BY clause
     */
    static GROUP_BY = new TeradataExpressionType("GroupBy");

    /**
     * HAVING or QUALIFY conditional-expression
     */
    static HAVING = new TeradataExpressionType("Having");

    /**
     * Keywords between 'SELECT' and column list
     */
    static KEYWORD = new TeradataExpressionType("Keyword");

    /**
     * List of expressions
     */
    static SELECT = new TeradataExpressionType("Select");

    /**
     * WHERE qualification
     */
    static WHERE = new TeradataExpressionType("Where");

    /**
     * Indicates if the specified type is an object type.
     *
     * @param teradataType - the Teradata type
     * @returns true if the type is an object type, or false otherwise
     */
    static isObject(teradataType: string): boolean {
        return (teradataType !== TeradataExpressionType.ARRAY.toString() && teradataType !== TeradataExpressionType.LITERAL.toString());
    }

    /**
     * Indicates if the specified type is a {@link TeradataScript} property.
     *
     * @param type - The Teradata type
     * @returns true if the type is a property, or false otherwise
     */
    static isScriptProperty(type: TeradataExpressionType): boolean {
        return TeradataExpressionType.GROUP_BY.equals(type) && TeradataExpressionType.HAVING.equals(type) && TeradataExpressionType.KEYWORD.equals(type) && TeradataExpressionType.SELECT.equals(type)
            && TeradataExpressionType.WHERE.equals(type);
    }

    /**
     * Gets the TernJS definition name for the specified type.
     *
     * @param teradataType - the Teradata type
     */
    static toTernjsName(teradataType: string): string {
        return (teradataType === TeradataExpressionType.COLUMN.toString()) ? QueryEngineConstants.TERNJS_COLUMN_TYPE : teradataType;
    }
}
