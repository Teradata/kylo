declare const StringUtils: StringUtilsStatic;

declare interface StringUtilsStatic {

    /**
     * Escapes the specified string using Scala String rules.
     *
     * @param str - the string to escape
     * @returns the string with escaped values
     */
    escapeScala(str: string): string;

    /**
     * Escapes parentheses in the specified string.
     *
     * @param str - the string to be escaped
     * @returns the string with parentheses escaped
     */
    quote(str: string): string;

    /**
     * Escapes backticks in SQL identifiers.
     *
     * @param str - the identifier
     * @returns the identifier with backticks escaped
     */
    quoteSql(str: string): string;

    isBlank(str: string): boolean;
}
