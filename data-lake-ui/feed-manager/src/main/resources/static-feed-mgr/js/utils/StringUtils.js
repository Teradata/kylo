/**
 * A collection of utility functions for strings.
 */
var StringUtils = (function () {
    function StringUtils ()
    {}

    /**
     * Escapes parentheses in the specified string.
     *
     * @param {string} str the string to be escaped
     * @returns {string} the string with parentheses escaped
     */
    StringUtils.quote = function (str) {
        return str.replace(/\\/g, "\\\\").replace(/"/g, "\\\"");
    };

    /**
     * Escapes backticks in SQL identifiers.
     *
     * @param str the identifier
     * @returns {string} the identifier with backticks escaped
     */
    StringUtils.quoteSql = function (str) {
        return str.replace(/`/g, "``");
    };

    return StringUtils;
})();
