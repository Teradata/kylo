package com.thinkbiganalytics.metadata.api.category;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.api.MissingUserPropertyException;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.security.AccessControlled;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.security.RoleMembership;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A category is a collection of zero or more feeds.
 */
public interface Category extends AccessControlled {

    ID getId();

    List<? extends Feed> getFeeds();

    String getDisplayName();

    void setDisplayName(String displayName);

    String getSystemName();

    void setSystemName(String name);

    Integer getVersion();

    String getDescription();

    void setDescription(String description);

    DateTime getCreatedTime();

    void setCreatedTime(DateTime createdTime);

    DateTime getModifiedTime();

    void setModifiedTime(DateTime modifiedTime);

    /**
     * Gets the user-defined properties for this category.
     *
     * @return the user-defined properties
     * @since 0.3.0
     */
    @Nonnull
    Map<String, String> getUserProperties();

    /**
     * Replaces the user-defined properties for this category with the specified properties.
     *
     * <p>If the user-defined field descriptors are given then a check is made to ensure that all required properties are specified.</p>
     *
     * @param userProperties the new user-defined properties
     * @param userFields     the user-defined field descriptors
     * @throws MissingUserPropertyException if a required property is empty or missing
     * @see CategoryProvider#getUserFields() for the user-defined field descriptors
     * @since 0.4.0
     */
    void setUserProperties(@Nonnull Map<String, String> userProperties, @Nonnull Set<UserFieldDescriptor> userFields);

    List<? extends HadoopSecurityGroup> getSecurityGroups();

    void setSecurityGroups(List<? extends HadoopSecurityGroup> securityGroups);

    String getIcon();

    void setIcon(String icon);

    String getIconColor();

    void setIconColor(String iconColor);

    Set<RoleMembership> getFeedRoleMemberships();

    Optional<RoleMembership> getFeedRoleMembership(String roleName);
    
    
    interface ID extends Serializable {

    }

}
