/**
 * Controls the Users page.
 *
 * @constructor
 */
var UsersPage = function() {
    var self = this;

    /**
     * Opens the Users page.
     */
    self.get = function() {
        browser.get("http://dladmin:thinkbig@localhost:8400/feed-mgr/index.html#/users");
    };

    /**
     * Gets the rows per page options.
     *
     * @returns {Array.<string>} the option names
     */
    self.getRowsPerPageOptions = function() {
        browser.findElement(by.css("md-select[ng-model=rowsPerPage]")).click();
        return element
                .all(by.css("div.md-select-menu-container.md-active md-option"))
                .map(function(optionElement) {
                    return optionElement.getAttribute("value");
                });
    };
};

module.exports = UsersPage;
