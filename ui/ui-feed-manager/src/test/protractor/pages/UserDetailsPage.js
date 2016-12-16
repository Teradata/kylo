/**
 * Controls the User Details page.
 *
 * @constructor
 */
var UserDetailsPage = function() {
    var self = this;

    /**
     * Adds the specified group to the list of groups.
     *
     * @param {string} group the name of the group to add
     * @throws {Error} if the group cannot be found
     */
    self.addGroup = function(group) {
        self.setGroupText(group);
        element
                .all(by.css("md-virtual-repeat-container.md-autocomplete-suggestions-container li"))
                .map(function(listItem) {
                    return listItem
                            .element(by.css("span[md-highlight-text]"))
                            .getText()
                            .then(function(text) {
                                console.log(text);
                                if (text === group) {
                                    listItem.click();
                                    return true;
                                }
                                return false;
                            });
                })
                .then(function(results) {
                    var match = false;
                    for (var i=0; i < results.length; ++i) {
                        match |= results[i];
                    }

                    if (!match) {
                        throw new Error("Failed to add group: " + group);
                    }
                });
    };

    /**
     * Opens the User Details page.
     */
    self.get = function() {
        browser.get("http://dladmin:thinkbig@localhost:8400/feed-mgr/index.html#/user-details/");
    };

    /**
     * Gets the list of group autocomplete options.
     *
     * @returns {Array.<string>} the list of group names
     */
    self.getGroupAutocomplete = function() {
        return element
                .all(by.css("md-virtual-repeat-container.md-autocomplete-suggestions-container md-autocomplete-parent-scope > span"))
                .map(function(groupElement) {
                    return groupElement.getText();
                });
    };

    /**
     * Gets the messages for the username input.
     *
     * @returns {Array.<string>} the list of messages
     */
    self.getUsernameMessages = function() {
        return element
                .all(by.css("md-input-container[ng-if='vm.model.systemName === null'] div[ng-message]"))
                .map(function(messageElement) {
                    return messageElement.getText();
                });
    };

    /**
     * Sets the text for the group input.
     *
     * @param {string} text the group names
     */
    self.setGroupText = function(text) {
        var input = browser.findElement(by.css("md-chips[ng-model='vm.editModel.groups'] input"));
        input.clear();
        input.sendKeys(text);
    };

    /**
     * Sets the text for the username input.
     *
     * @param {string} username the username
     */
    self.setUsername = function(username) {
        browser.findElement(by.css("input[name=username]")).sendKeys(username);
    };
};

module.exports = UserDetailsPage;
