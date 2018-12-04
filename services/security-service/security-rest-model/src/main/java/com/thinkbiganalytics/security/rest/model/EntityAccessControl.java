package com.thinkbiganalytics.security.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityAccessControl {

    private User owner;

    private ActionGroup allowedActions;

    private List<RoleMembership> roleMemberships;
    private List<RoleMembership> feedRoleMemberships;

    public ActionGroup getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(ActionGroup allowedActions) {
        this.allowedActions = allowedActions;
    }

    public List<RoleMembership> getRoleMemberships() {
        if (roleMemberships == null) {
            roleMemberships = new ArrayList<>();
        }
        return roleMemberships;
    }

    public void setRoleMemberships(List<RoleMembership> roleMemberships) {
        this.roleMemberships = roleMemberships;
    }

    public List<RoleMembership> getFeedRoleMemberships() {
        if (feedRoleMemberships == null) {
            feedRoleMemberships = new ArrayList<>();
        }
        return feedRoleMemberships;
    }

    public void setFeedRoleMemberships(List<RoleMembership> roleMemberships) {
        this.feedRoleMemberships = roleMemberships;
    }

    public void addRoleMembership(RoleMembership roleMembership) {
        getRoleMemberships().add(roleMembership);
    }

    public List<RoleMembershipChange> toRoleMembershipChangeList() {
        return toRoleMembershipChangeList(getRoleMemberships());
    }

    public List<RoleMembershipChange> toFeedRoleMembershipChangeList() {
        return toRoleMembershipChangeList(getFeedRoleMemberships());
    }

    private List<RoleMembershipChange> toRoleMembershipChangeList(Collection<RoleMembership> roleMemberships) {
        List<RoleMembershipChange> membershipChanges = new ArrayList<>();
        for (RoleMembership membership : roleMemberships) {
            RoleMembershipChange roleMembershipChange = new RoleMembershipChange();
            roleMembershipChange.setRoleName(membership.getRole().getSystemName());
            roleMembershipChange.setChange(RoleMembershipChange.ChangeType.REPLACE);
            for (UserGroup userGroup : membership.getGroups()) {
                roleMembershipChange.getGroups().add(userGroup.getSystemName());
            }
            for (User user : membership.getUsers()) {
                roleMembershipChange.getUsers().add(user.getSystemName());
            }
            membershipChanges.add(roleMembershipChange);
        }
        return membershipChanges;
    }

    public User getOwner() {
        return owner;
    }

    @JsonProperty  // allows overloaded method in Datasource to be ignored
    public void setOwner(User owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public boolean hasAction(String action) {
        return getAllowedActions() != null && getAllowedActions().hasAction(action);
    }
}
