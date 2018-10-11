/**
 * A collection of utility functions for strings.
 */
export class StringUtils {

    constructor() {
    }

    /**
     * Escapes the specified string using Scala String rules.
     *
     * @param str the string to escape
     * @returns {string} the string with escaped values
     */
    static escapeScala(str: string): string {
        return str.replace(/\\/g, "\\\\").replace(/"/g, "\\\"").replace(/\t/g, "\\t").replace(/\n/g, "\\n").replace(/\r/g, "\\r");
    };

    /**
     * Escapes parentheses in the specified string.
     *
     * @param {string} str the string to be escaped
     * @returns {string} the string with parentheses escaped
     */
    static quote(str: string): string {
        return str.replace(/\\/g, "\\\\").replace(/"/g, "\\\"");
    };

    /**
     * Escapes single quote in the specified string.
     *
     * @param {string} str the string to be escaped
     * @returns {string} the string with parentheses escaped
     */
    static singleQuote = function (str: string): string {
        return str.replace(/\\/g, "\\\\").replace(/'/g, "\\\'");
    };

    /**
     * Escapes backticks in SQL identifiers.
     *
     * @param str - the identifier
     * @param quoteChar - the quote char for the SQL dialect
     * @param escapeChar - the escape char for the SQL dialect
     * @returns the identifier with backticks escaped
     */
    static quoteSql(str: string, quoteChar: string = "`", escapeChar: string = "`"): string {
        return str.replace(RegExp(quoteChar, "g"), `${escapeChar}${quoteChar}`);
    };

    /**
     * Indicates if the specified string is blank.
     */
    static isBlank(str: string): boolean {
        return (!str || str.length === 0 || !str.trim());
    };


    static replaceSpaces(str: string, replacement: string) {
        if (str != undefined) {
            return str.replace(/ /g, replacement);
        }
        else {
            return '';
        }
    };

    static stringify = function (v: any): string {
        const cache = new Map();
        return JSON.stringify(v, function (key, value) {
            if (typeof value === 'object' && value !== null) {
                if (cache.get(value)) {
                    // Circular reference found, discard key
                    return;
                }
                // Store value in our map
                cache.set(value, true);
            }
            return value;
        });
    };
}
