/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
