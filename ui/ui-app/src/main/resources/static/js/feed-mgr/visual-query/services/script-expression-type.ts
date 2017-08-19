/**
 * Type of script expressions.
 */
export class ScriptExpressionType {

    /**
     * Represents an array of objects.
     */
    static ARRAY = new ScriptExpressionType("array");

    /**
     * Represents a SQL column.
     */
    static COLUMN = new ScriptExpressionType("column");

    /**
     * Represents a number or string literal.
     */
    static LITERAL = new ScriptExpressionType("literal");

    /**
     * Constructs a {@code ScriptExpressionType} with the specified string value.
     */
    constructor(public value: string) {
    }

    /**
     * Returns the string value of this type.
     */
    toString() {
        return this.value;
    }
}
