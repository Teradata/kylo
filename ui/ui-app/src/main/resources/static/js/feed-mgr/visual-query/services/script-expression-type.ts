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
     * Returns the constant with the specified name.
     *
     * @return the matching constant or {@code null} if not found
     */
    static valueOf(name: string): ScriptExpressionType {
        switch (name) {
            case "array":
                return ScriptExpressionType.ARRAY;

            case "Column":
            case "column":
                return ScriptExpressionType.COLUMN;

            case "literal":
                return ScriptExpressionType.LITERAL;

            default:
                return new ScriptExpressionType(name);
        }
    }

    /**
     * Constructs a {@code ScriptExpressionType} with the specified string value.
     */
    constructor(public value: string) {
    }

    /**
     * Returns true if the specified object is equal to this enum constant.
     */
    equals(other: ScriptExpressionType): boolean {
        return this.value === other.value;
    }

    /**
     * Returns the string value of this type.
     */
    toString() {
        return this.value;
    }
}
