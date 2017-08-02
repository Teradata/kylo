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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Set;

/**
 * Metadata for a user with access to Kylo.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    /**
     * Display name for this user
     */
    private String displayName;

    /**
     * Email address for this user
     */
    private String email;

    /**
     * Indicates if user is active or disabled
     */
    private boolean enabled;

    /**
     * System names of groups the user belongs to
     */
    private Set<String> groups;

    /**
     * Username for this user
     */
    private String systemName;
    
    public User() {
    }
    
    public User(String sysName) {
        this.systemName = sysName;
    }

    /**
     * Gets the display name for this user.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for this user.
     *
     * @param displayName the display name
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the email address for this user.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address for this user.
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Indicates that the user may access Kylo.
     *
     * @return {@code true} if the user may login, or {@code false} otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables access to Kylo for this user.
     *
     * @param enabled {@code true}
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the groups this user belongs to.
     *
     * @return the group system names
     */
    public Set<String> getGroups() {
        return groups;
    }

    /**
     * Sets the groups this user belongs to.
     *
     * @param groups the group system names
     */
    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    /**
     * Gets the login name for this user.
     *
     * @return the login name
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Sets the login name for this user.
     *
     * @param systemName the login name
     */
    public void setSystemName(final String systemName) {
        this.systemName = systemName;
    }
}
