import {ScriptExpressionType} from "./script-expression-type";

/**
 * An expression in a script.
 */
export abstract class ScriptExpression {

    /**
     * Last column in the original expression
     */
    end: number;

    /**
     * Source code
     */
    source: any;

    /**
     * First column in the original expression
     */
    start: number;

    /**
     * Result type
     */
    type: ScriptExpressionType;
}
