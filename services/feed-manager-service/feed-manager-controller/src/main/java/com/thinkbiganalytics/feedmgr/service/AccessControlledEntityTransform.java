package com.thinkbiganalytics.feedmgr.service;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.rest.model.EntityAccessControl;
import com.thinkbiganalytics.feedmgr.rest.model.EntityAccessRoleMembership;
import com.thinkbiganalytics.metadata.api.security.AccessControlled;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.GroupPrincipal;
import com.thinkbiganalytics.security.rest.model.RoleMembership;
import com.thinkbiganalytics.security.rest.model.UserPrincipal;
import com.thinkbiganalytics.security.service.user.UserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;


/**
 * Transform an entities Role Membership f
 */
public class AccessControlledEntityTransform {

    @Inject
    private SecurityModelTransform actionsTransform;

    @Inject
    private UserService userService;

    public EntityAccessRoleMembership toEntityAccessRoleMembership(RoleMembership roleMembership){
        EntityAccessRoleMembership entityAccessRoleMembership = new EntityAccessRoleMembership(roleMembership.getRole().getSystemName(),roleMembership.getRole().getTitle(),roleMembership.getRole().getDescription());
        roleMembership.getUsers().stream().forEach(user -> {
                Optional<UserPrincipal> userPrincipal = userService.getUser(user);
                if(userPrincipal.isPresent()){
                    entityAccessRoleMembership.addUser(userPrincipal.get());
                }
                else {
                    entityAccessRoleMembership.addUser(user);
                }
        });

        roleMembership.getGroups().stream().forEach(group -> {
            Optional<GroupPrincipal> groupPrincipal = userService.getGroup(group);
            if(groupPrincipal.isPresent()){
                entityAccessRoleMembership.addGroup(groupPrincipal.get());
            }
            else {
                entityAccessRoleMembership.addGroup(group);
            }
        });
        return entityAccessRoleMembership;

    }


    /**
     * get the Access Control from the Domain Model and apply it to the rest model
     * @param domain the domain
     * @param restModel the rest model
     */
    public  void applyAccessControlToRestModel(AccessControlled domain, EntityAccessControl restModel){
        if(domain.getAllowedActions() != null &&  domain.getAllowedActions().getAvailableActions() != null){
            ActionGroup allowed = actionsTransform.toActionGroup(null).apply(domain.getAllowedActions());
            restModel.setAllowedActions(allowed);
        }

        if(domain.getRoleMemberships() != null ){
            Map<String, RoleMembership> roleAssignmentMap = new HashMap<>();
            domain.getRoleMemberships().stream().forEach(membership -> {


                String systemRoleName = membership.getRole().getSystemName();
                String name = membership.getRole().getTitle();
                String desc = membership.getRole().getDescription();

                membership.getMembers().stream().forEach(member -> {
                    roleAssignmentMap.putIfAbsent(systemRoleName, new RoleMembership(systemRoleName, name,desc));
                    RoleMembership accessRoleAssignment = roleAssignmentMap.get(systemRoleName);
                    if(member instanceof UsernamePrincipal){
                            accessRoleAssignment.addUser(member.getName());
                    }
                    else {
                                accessRoleAssignment.addGroup(member.getName());
                         }
                });

            });
                restModel.setRoleMemberships(Lists.newArrayList(roleAssignmentMap.values()));
        }
    }

}