package com.thinkbiganalytics.metadata.modeshape.feed.security;

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

import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.feed.FeedDetails;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.security.Privilege;


/**
 * A type of allowed actions that applies to feeds.  It intercepts certain action enable/disable
 * calls related to visibility to update the underlying JCR node structure's ACL lists.
 */
public class JcrFeedAllowedActions extends JcrAllowedActions {

    private JcrFeed feed;

    public JcrFeedAllowedActions(Node allowedActionsNode) {
        super(allowedActionsNode);
        this.feed = JcrUtil.getJcrObject(JcrUtil.getParent(allowedActionsNode), JcrFeed.class);
    }

    public JcrFeedAllowedActions(Node allowedActionsNode, FeedOpsAccessControlProvider opsAccessProvider) {
        super(allowedActionsNode);
        this.feed = JcrUtil.getJcrObject(JcrUtil.getParent(allowedActionsNode), JcrFeed.class, opsAccessProvider);
    }

    @Override
    public boolean enable(Principal principal, Set<Action> actions) {
        boolean changed = super.enable(principal, actions);
        updateEntityAccess(principal, getEnabledActions(principal));
        return changed;
    }

    @Override
    public boolean enableOnly(Principal principal, Set<Action> actions) {
        // Never replace permissions of the owner
        if (! principal.equals(this.feed.getOwner())) {
            boolean changed = super.enableOnly(principal, actions);
            updateEntityAccess(principal, getEnabledActions(principal));
            return changed;
        } else {
            return false;
        }
    }

    @Override
    public boolean enableOnly(Principal principal, AllowedActions actions) {
        // Never replace permissions of the owner
        if (! principal.equals(this.feed.getOwner())) {
            boolean changed = super.enableOnly(principal, actions);
            updateEntityAccess(principal, getEnabledActions(principal));
            return changed;
        } else {
            return false;
        }
    }

    @Override
    public boolean disable(Principal principal, Set<Action> actions) {
        // Never disable permissions of the owner
        if (! principal.equals(this.feed.getOwner())) {
            boolean changed = super.disable(principal, actions);
            updateEntityAccess(principal, getEnabledActions(principal));
            return changed;
        } else {
            return false;
        }
    }

    @Override
    public void setupAccessControl(Principal owner) {
        enable(owner, FeedAccessControl.EDIT_DETAILS);
        enable(owner, FeedAccessControl.ACCESS_OPS);
        enable(JcrMetadataAccess.ADMIN, FeedAccessControl.EDIT_DETAILS);
        enable(JcrMetadataAccess.ADMIN, FeedAccessControl.ACCESS_OPS);

        super.setupAccessControl(owner);
    }
    
    @Override
    public void removeAccessControl(Principal owner) {
        super.removeAccessControl(owner);
        
        this.feed.getFeedDetails().ifPresent(d -> JcrAccessControlUtil.clearHierarchyPermissions(d.getNode(), feed.getNode()));
        this.feed.getFeedData().ifPresent(d -> JcrAccessControlUtil.clearHierarchyPermissions(d.getNode(), feed.getNode()));
    }

    @Override
    protected boolean isAdminAction(Action action) {
        return action.implies(FeedAccessControl.CHANGE_PERMS);
    }

    protected void updateEntityAccess(Principal principal, Set<? extends Action> actions) {
        Set<String> detailPrivs = new HashSet<>();
        Set<String> dataPrivs = new HashSet<>();
        Set<String> summaryPrivs = new HashSet<>();
        
        // Enable/disable feed ops access
        if (actions.stream().filter(action -> action.implies(FeedAccessControl.ACCESS_OPS)).findFirst().isPresent()) {
            this.feed.getOpsAccessProvider().ifPresent(provider -> provider.grantAccess(feed.getId(), principal));
        } else {
            this.feed.getOpsAccessProvider().ifPresent(provider -> provider.revokeAccess(feed.getId(), principal));
        }

        // Collect all JCR privilege changes based on the specified actions.
        actions.forEach(action -> {
            if (action.implies(FeedAccessControl.CHANGE_PERMS)) {
                Collections.addAll(summaryPrivs, Privilege.JCR_READ_ACCESS_CONTROL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
                Collections.addAll(detailPrivs, Privilege.JCR_READ_ACCESS_CONTROL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
                Collections.addAll(dataPrivs, Privilege.JCR_READ_ACCESS_CONTROL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
            } else if (action.implies(FeedAccessControl.EDIT_DETAILS)) {
                //also add read to the category summary
                final AllowedActions categoryAllowedActions = feed.getCategory().getAllowedActions();
                if (categoryAllowedActions.hasPermission(CategoryAccessControl.CHANGE_PERMS)) {
                    categoryAllowedActions.enable(principal, CategoryAccessControl.ACCESS_DETAILS);
                }
                //If a user has Edit access for the feed, they need to be able to also Read the template
                this.feed.getFeedDetails()
                    .map(FeedDetails::getTemplate)
                    .map(FeedManagerTemplate::getAllowedActions)
                    .filter(allowedActions -> allowedActions.hasPermission(TemplateAccessControl.CHANGE_PERMS))
                    .ifPresent(allowedActions -> allowedActions.enable(principal, TemplateAccessControl.ACCESS_TEMPLATE));
                
                summaryPrivs.add(Privilege.JCR_ALL);                
                detailPrivs.add(Privilege.JCR_ALL);
                dataPrivs.add(Privilege.JCR_ALL);                
            } else if (action.implies(FeedAccessControl.EDIT_SUMMARY)) {
                //also add read to the category summary
                final AllowedActions categoryAllowedActions = feed.getCategory().getAllowedActions();
                if (categoryAllowedActions.hasPermission(CategoryAccessControl.CHANGE_PERMS)) {
                    categoryAllowedActions.enable(principal, CategoryAccessControl.ACCESS_CATEGORY);
                }
                
                summaryPrivs.add(Privilege.JCR_ALL);                
            } else if (action.implies(FeedAccessControl.ACCESS_DETAILS)) {
                //also add read to the category summary
                final AllowedActions categoryAllowedActions = feed.getCategory().getAllowedActions();
                if (categoryAllowedActions.hasPermission(CategoryAccessControl.CHANGE_PERMS)) {
                    categoryAllowedActions.enable(principal, CategoryAccessControl.ACCESS_DETAILS);
                }
                //If a user has Read access for the feed, they need to be able to also Read the template
                this.feed.getFeedDetails()
                    .map(FeedDetails::getTemplate)
                    .map(FeedManagerTemplate::getAllowedActions)
                    .filter(allowedActions -> allowedActions.hasPermission(TemplateAccessControl.CHANGE_PERMS))
                    .ifPresent(allowedActions -> allowedActions.enable(principal, TemplateAccessControl.ACCESS_TEMPLATE));
                
                summaryPrivs.add(Privilege.JCR_READ);
                detailPrivs.add(Privilege.JCR_READ);                
                dataPrivs.add(Privilege.JCR_READ);                
            } else if (action.implies(FeedAccessControl.ACCESS_FEED)) {
                //also add read to the category summary
                final AllowedActions categoryAllowedActions = feed.getCategory().getAllowedActions();
                if (categoryAllowedActions.hasPermission(CategoryAccessControl.CHANGE_PERMS)) {
                    categoryAllowedActions.enable(principal, CategoryAccessControl.ACCESS_CATEGORY);
                }
                
                summaryPrivs.add(Privilege.JCR_READ);
            }
        });
        
        JcrAccessControlUtil.setPermissions(this.feed.getNode(), principal, summaryPrivs);
        this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.setPermissions(s.getNode(), principal, summaryPrivs));
        this.feed.getFeedDetails().ifPresent(d -> JcrAccessControlUtil.setPermissions(d.getNode(), principal, detailPrivs));
        this.feed.getFeedData().ifPresent(d -> JcrAccessControlUtil.setPermissions(d.getNode(), principal, dataPrivs));
    }
}
