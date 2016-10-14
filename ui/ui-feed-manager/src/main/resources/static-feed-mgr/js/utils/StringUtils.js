/**
 * A collection of utility functions for strings.
 */
var StringUtils = (function() {
    function StringUtils() {
    }

    /**
     * Escapes the specified string using Scala String rules.
     *
     * @param str the string to escape
     * @returns {string} the string with escaped values
     */
    StringUtils.escapeScala = function(str) {
        return str.replace(/\\/g, "\\\\").replace(/"/g, "\\\"").replace(/\t/g, "\\t").replace(/\n/g, "\\n").replace(/\r/g, "\\r");
    };

    /**
     * Escapes parentheses in the specified string.
     *
     * @param {string} str the string to be escaped
     * @returns {string} the string with parentheses escaped
     */
    StringUtils.quote = function(str) {
        return str.replace(/\\/g, "\\\\").replace(/"/g, "\\\"");
    };

    /**
     * Escapes backticks in SQL identifiers.
     *
     * @param str the identifier
     * @returns {string} the identifier with backticks escaped
     */
    StringUtils.quoteSql = function(str) {
        return str.replace(/`/g, "``");
    };

    /**
     *
     * @param str
     * @returns {boolean}
     */
    StringUtils.isBlank = function(str) {
        return (!str || str.length === 0 || !str.trim());
    }
    return StringUtils;
})();
