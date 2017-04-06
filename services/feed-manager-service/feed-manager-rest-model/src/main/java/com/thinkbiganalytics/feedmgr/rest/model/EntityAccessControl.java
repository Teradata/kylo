package com.thinkbiganalytics.feedmgr.rest.model;

import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.RoleMembership;
import com.thinkbiganalytics.security.rest.model.RoleMembershipChange;

import java.util.ArrayList;
import java.util.List;


public class EntityAccessControl {

    private ActionGroup allowedActions;

    private List<RoleMembership> roleMemberships;

    public ActionGroup getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(ActionGroup allowedActions) {
        this.allowedActions = allowedActions;
    }

    public List<RoleMembership> getRoleMemberships() {
        if(roleMemberships == null){
            roleMemberships = new ArrayList<>();
        }
        return roleMemberships;
    }

    public void setRoleMemberships(List<RoleMembership> roleMemberships) {
        this.roleMemberships = roleMemberships;
    }

    public void addRoleMembership(RoleMembership roleMembership){
        getRoleMemberships().add(roleMembership);
    }

    public List<RoleMembershipChange> toRoleMembershipChangeList(){
        List<RoleMembershipChange> membershipChanges = new ArrayList<>();
        for(RoleMembership membership : getRoleMemberships()) {
            RoleMembershipChange roleMembershipChange = new RoleMembershipChange();
            roleMembershipChange.setRoleName(membership.getRole().getSystemName());
            roleMembershipChange.setChange(RoleMembershipChange.ChangeType.REPLACE);
            membership.getGroups().stream().forEach(group -> roleMembershipChange.getGroups().add(group));
            membership.getUsers().stream().forEach(user -> roleMembershipChange.getUsers().add(user));
            membershipChanges.add(roleMembershipChange);
        }
        return membershipChanges;
    }
}
