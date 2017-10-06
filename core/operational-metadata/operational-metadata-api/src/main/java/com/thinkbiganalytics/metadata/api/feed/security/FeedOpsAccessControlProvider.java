/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed.security;

/*-
 * #%L
 * kylo-operational-metadata-api
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

import java.security.Principal;
import java.util.List;
import java.util.Set;

import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 * A provider for granting and revoking visibility to feed operations by certain users.
 */
public interface FeedOpsAccessControlProvider {

    /**
     * Grants access to operations of a feed to one or more principals.
     * @param feedId the feed ID
     * @param principal a principal
     * @param more any additional principals
     */
    void grantAccess(Feed.ID feedId, Principal principal, Principal... more);
    
    /**
     * Grants access to operations of a feed to only the one or more principals
     * specified; revoking access to all others.
     * @param feedId the feed ID
     * @param principal a principal
     * @param more any additional principals
     */
    void grantAccessOnly(Feed.ID feedId, Principal principal, Principal... more);
    
    /**
     * Grants access to operations of a feed to a set of principals.
     * @param feedId the feed ID
     * @param principals the principals
     */
    void grantAccess(Feed.ID feedId, Set<Principal> principals); 
    
    /**
     * Grants access to operations of a feed to only specified set of principals;
     * revoking access to all others.
     * @param feedId the feed ID
     * @param principals the principals
     */
    void grantAccessOnly(Feed.ID feedId, Set<Principal> principals);
    
    /**
     * Revokes access to operations of a feed for one or more principals.
     * @param feedId the feed ID
     * @param principal a principal
     * @param more any additional principals
     */
    void revokeAccess(Feed.ID feedId, Principal principal, Principal... more);
    
    /**
     * Revokes access to operations of a feed for a set of principals.
     * @param feedId the feed ID
     * @param principals the principals
     */
    void revokeAccess(Feed.ID feedId, Set<Principal> principals);
    
    /**
     * Revokes access to operations of all feeds for one or more principals.
     * @param principal a principal
     * @param more any additional principals
     */
    void revokeAllAccess(Principal principal, Principal... more);
    
    /**
     * Revokes access to operations of all feeds for a set of principals.
     * @param principals the principals
     */
    void revokeAllAccess(Set<Principal> principals);
    
    /**
     * Revokes access to operations of a feed for all principals.
     * @param feedId the feed ID
     */
    void revokeAllAccess(Feed.ID feedId);
    
    
    /**
     * Returns the set of all principals that have been granted 
     * access to a feed's operations.
     * @param feedId the feed ID
     * @return all principals with access
     */
    Set<Principal> getPrincipals(Feed.ID feedId);

    List<? extends FeedOpsAclEntry> findAll();

    List<? extends FeedOpsAclEntry> findForFeed(String feedId);

}
