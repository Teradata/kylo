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
