/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.category;

/*-
 * #%L
 * kylo-metadata-modeshape
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
import java.util.stream.Stream;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrAbstractRoleMembership;
import com.thinkbiganalytics.security.role.RoleMembership;

/**
 * The default role memberships for all feeds contained within a category.  These are independent 
 * and in addition to any role memberships maintained by each individual feed.
 */
public class JcrFeedDefaultRoleMembership extends JcrAbstractRoleMembership {
    
    private CategoryDetails category;
    
    
    public JcrFeedDefaultRoleMembership(Node node, CategoryDetails cat) {
        super(node);
        this.category = cat;
    }

    public JcrFeedDefaultRoleMembership(Node node, Node roleNode, CategoryDetails cat) {
        super(node, roleNode);
        this.category = cat;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.security.role.JcrAbstractRoleMembership#enable(java.security.Principal)
     */
    @Override
    protected void enable(Principal principal) {
        this.category.getFeeds().forEach(feed -> enableOnly(principal, streamAllRoleMemberships(feed), feed.getAllowedActions()));
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.security.role.JcrAbstractRoleMembership#disable(java.security.Principal)
     */
    @Override
    protected void disable(Principal principal) {
        // Since this method is called after a principal has been removed as a member, the enableOnly() call
        // below will only leave permissions enabled for the principal if it is a member of some other role,
        // otherwise all permissions will be left disabled for that principal.
        this.category.getFeeds().forEach(feed -> enableOnly(principal, streamAllRoleMemberships(feed), feed.getAllowedActions()));
    }

    /**
     * Streams all feed role memberships (category-level and feed-level) that apply to the specified feed.
     */
    protected Stream<RoleMembership> streamAllRoleMemberships(Feed feed) {
        return Stream.concat(this.category.getFeedRoleMemberships().stream(), feed.getRoleMemberships().stream());
    }
}
