package com.thinkbiganalytics.security.rest.model;

/*-
 * #%L
 * thinkbig-security-rest-model
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

public class UserGroup {

    /**
     * A human-readable summary
     */
    private String description;

    /**
     * Number of users and groups within this group
     */
    private int memberCount;

    /**
     * Unique name
     */
    private String systemName;

    /**
     * Human-readable name
     */
    private String title;
    
    public UserGroup() {
    }

    public UserGroup(String systemName) {
        this.systemName = systemName;
    }

    /**
     * Gets a human-readable description of this group.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets a human-readable description of this group.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the number of users and groups contained within this group.
     *
     * @return the member count
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * Sets the number of users and groups contained within this group.
     *
     * @param memberCount the member count
     */
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    /**
     * Gets the unique name for this group.
     *
     * @return the unique name
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Sets the unique name for this group.
     *
     * @param systemName the unique name
     */
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    /**
     * Gets the human-readable name for this group.
     *
     * @return the human-readable name
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the human-readable name for this group.
     *
     * @param title the human-readable name
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
