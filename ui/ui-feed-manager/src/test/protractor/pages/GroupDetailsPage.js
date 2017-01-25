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
