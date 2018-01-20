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
import com.thinkbiganalytics.metadata.api.security.RoleMembership;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrAbstractRoleMembership;

/**
 *
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
        this.category.getFeeds().forEach(feed -> enableOnly(principal, streamAllRoleMemberships(feed), feed.getAllowedActions()));
    }

    /**
     * Streams all feed role memberships (category-level and feed-level) that apply to the specified feed.
     */
    protected Stream<RoleMembership> streamAllRoleMemberships(Feed feed) {
        return Stream.concat(this.category.getFeedRoleMemberships().stream(), feed.getRoleMemberships().stream());
    }
}
