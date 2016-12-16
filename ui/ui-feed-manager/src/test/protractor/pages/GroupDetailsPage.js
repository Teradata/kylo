/**
 * Controls the Group Details page.
 *
 * @constructor
 */
var GroupDetailsPage = function() {
    var self = this;

    /**
     * Opens the Group Details page.
     */
    self.get = function() {
        browser.get("http://dladmin:thinkbig@localhost:8400/feed-mgr/index.html#/group-details/");
    };

    /**
     * Gets the messages for the group name input.
     *
     * @returns {Array.<string>} the list of messages
     */
    self.getGroupNameMessages = function() {
        return element
                .all(by.css("md-input-container[ng-if='vm.model.systemName === null'] div[ng-message]"))
                .map(function(messageElement) {
                    return messageElement.getText();
                });
    };

    /**
     * Sets the text for the group name input.
     *
     * @param {string} groupName the group name
     */
    self.setGroupName = function(groupName) {
        browser.findElement(by.css("input[name=systemName]")).sendKeys(groupName);
    };
};

module.exports = GroupDetailsPage;
