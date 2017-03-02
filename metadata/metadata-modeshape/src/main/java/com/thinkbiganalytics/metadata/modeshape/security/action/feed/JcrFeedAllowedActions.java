/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action.feed;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

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

import javax.jcr.Node;
import javax.jcr.security.Privilege;

import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * A type of allowed actions that applies to feeds.  It intercepts certain action enable/disable
 * calls related to visibility to update the underlying JCR node structure's ACL lists.
 */
public class JcrFeedAllowedActions extends JcrAllowedActions {
    
    private JcrFeed feed;

    /**
     * @param allowedActionsNode
     */
    public JcrFeedAllowedActions(JcrFeed feed, Node allowedActionsNode) {
        super(allowedActionsNode);
        this.feed = feed;
    }

    @Override
    public boolean enable(Principal principal, Set<Action> actions) {
        enableEntityAccess(principal, actions.stream());
        return super.enable(principal, actions);
    }

    @Override
    public boolean enableOnly(Principal principal, Set<Action> actions) {
        enableOnlyEntityAccess(principal, actions.stream());
        return super.enableOnly(principal, actions);
    }

    @Override
    public boolean enableOnly(Principal principal, AllowedActions actions) {
        enableOnlyEntityAccess(principal, actions.getAvailableActions().stream());
        return super.enableOnly(principal, actions);
    }

    @Override
    public boolean disable(Principal principal, Set<Action> actions) {
        disableEntityAccess(principal, actions.stream());
        return super.disable(principal, actions);
    }

    @Override
    public boolean disable(Principal principal, AllowedActions actions) {
        disableEntityAccess(principal, actions.getAvailableActions().stream());
        return super.disable(principal, actions);
    }

    protected void enableEntityAccess(Principal principal, Stream<? extends Action> actions) {
        actions.forEach(action -> {
            if (action.implies(FeedAccessControl.ACCESS_FEED)) {
                this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
            }
            if (action.implies(FeedAccessControl.ACCESS_DETAILS)) {
                this.feed.getFeedDetails().ifPresent(d -> JcrAccessControlUtil.addHierarchyPermissions(d.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
            }
        });
    }
    
    protected void enableOnlyEntityAccess(Principal principal, Stream<? extends Action> actions) {
        AtomicBoolean summary = new AtomicBoolean(false);
        AtomicBoolean details = new AtomicBoolean(false);
        
        actions.forEach(action -> {
            summary.compareAndSet(false, action.implies(FeedAccessControl.ACCESS_FEED));
            details.compareAndSet(false, action.implies(FeedAccessControl.ACCESS_DETAILS));
        });
        
        if (summary.get()) {
            this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
        } else {
            this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.removeHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
        }
        
        if (summary.get()) {
            this.feed.getFeedDetails().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
        } else {
            this.feed.getFeedDetails().ifPresent(s -> JcrAccessControlUtil.removeHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
        }
    }
    
    protected void disableEntityAccess(Principal principal, Stream<? extends Action> actions) {
        actions.forEach(action -> {
            if (action.implies(FeedAccessControl.ACCESS_FEED)) {
                this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.removeHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
            }
            if (action.implies(FeedAccessControl.ACCESS_DETAILS)) {
                this.feed.getFeedDetails().ifPresent(d -> JcrAccessControlUtil.removeHierarchyPermissions(d.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
            }
        });
    }
}
