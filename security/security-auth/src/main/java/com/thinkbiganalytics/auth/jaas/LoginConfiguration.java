/**
 *
 */
package com.thinkbiganalytics.auth.jaas;

/*-
 * #%L
 * thinkbig-security-auth
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

import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;

import org.springframework.core.Ordered;

/**
 * Represents a JAAS login configuration of one or more {@link AppConfigurationEntry} instances for
 * applications.  When more than one LoginConfiguration bean exists then all of the 
 * entries from each bean are merged into a single, combined JAAS configuration.
 * <p>
 * Beans that implement this interface may use the @Ordered annotation to set a precedence order.
 * This order will used when more than LoginConfiguration bean exists and their entries are to be merged.  
 * Higher precedence (lower order numbered) LoginConfiguration entries
 * will be placed before lower precedence LoginConfiguration entries in the login sequence for a given 
 * application (i.e. "service", "UI", etc.)
 */
public interface LoginConfiguration {
    
    int DEFAULT_ORDER = 0;
    int LOWEST_ORDER = Integer.MIN_VALUE;
    int LOW_ORDER = DEFAULT_ORDER - 200;
    int HIGH_ORDER = DEFAULT_ORDER + 200;
    int HIGHEST_ORDER = Integer.MAX_VALUE;

    /**
     * Gets the ordered array of {@link AppConfigurationEntry} instances associated with the named
     * application ("service", "UI", etc.)
     * @param appName the name of the application
     * @return an array of AppConfigurationEntries
     */
    AppConfigurationEntry[] getApplicationEntries(String appName);

    /**
     * Gets a map of all {@link AppConfigurationEntry} instances grouped by application ("service", "UI", etc.)
     * @return a map with application name as keys and arrays of AppConfigurationEntries as values
     */
    Map<String, AppConfigurationEntry[]> getAllApplicationEntries();
}
