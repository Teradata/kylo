import * as _ from "underscore";
import AccessConstants from '../../../constants/AccessConstants';
import {AccessControlService} from '../../../services/AccessControlService';
import { Injectable, Inject } from '@angular/core';
import { RestUrlService } from '../../services/RestUrlService';
import { ObjectUtils } from '../../../../lib/common/utils/object-utils';

@Injectable()
export class EntityAccessControlService extends AccessConstants{

    roleUrlsMap: any;
    public static entityRoleTypes: any ={ CATEGORY: "category", CATEGORY_FEED: "category-feed", FEED: "feed", TEMPLATE: "template", DATASOURCE: "datasource",  CONNECTOR: "connector" };

    constructor(private accessControlService: AccessControlService,
                private restUrlService: RestUrlService,
                @Inject("$injector") private $injector: any) {
        super();
        this.roleUrlsMap = {
            "feed": restUrlService.FEED_ROLES_URL,
            "category": restUrlService.CATEGORY_ROLES_URL,
            "category-feed": restUrlService.CATEGORY_FEED_ROLES_URL,
            "template": restUrlService.TEMPLATE_ROLES_URL,
            "datasource": restUrlService.DATASOURCE_ROLES_URL,
            "connector": restUrlService.CONNECTOR_ROLES_URL
        };
    }
    augmentRoleWithUiModel(roleMembership: any) {
        roleMembership.ui = { members: { selectedItem: '', searchText: '' } };
        if (ObjectUtils.isUndefined(roleMembership.members)) {
            roleMembership.members = [];
        }
    }
    queryForRoleAssignments(entity: any, membersType: any) {
        if (entity && entity.id && entity.id != null) {
            var url = '';
            if (membersType in this.roleUrlsMap) {
                var f = this.roleUrlsMap[membersType];
                url = f(entity.id);
            }
            return this.$injector.get("$http").get(url);
        }
        else {
            var deferred = this.$injector.get("$q").defer();
            deferred.resolve({ data: {} });
            return deferred.promise;
        }
    }
    // may be called by plugins
    addRoleAssignment (type: any, urlFunc: any) {
        this.roleUrlsMap[type] = urlFunc;
    }
    /**
         * Ensure the entity's roleMemberships.members are pushed back into the proper entity.roleMemberships.users and entity.roleMemberships.groups
         * @param entity the entity to save
         */
    updateRoleMembershipsForSave (roleMemberships: any) {

        if (roleMemberships) {
            _.each(roleMemberships, (roleMembership: any) => {
                var users: any = [];
                var groups: any = [];
                var update = false;
                if (roleMembership.members != null && roleMembership.members != undefined) {
                    //if the members is empty for the  entity we should update as the user cleared out memberships, otherwise we should update only if the member has a 'type' attr
                    var update = roleMembership.members.length == 0;
                    _.each(roleMembership.members, (member: any) => {
                        if (ObjectUtils.isDefined(member.type) && member.editable != false) {
                            if (member.type == 'user') {
                                users.push(member.systemName);
                            }
                            else if (member.type == 'group') {
                                groups.push(member.systemName);
                            }
                            update = true;
                        }
                    });
                }
                if (update) {
                    roleMembership.users = users;
                    roleMembership.groups = groups;
                }
            });

        }
    }
    /**
         * Merges all possible roles for this entity, with the assigned roles/memberships
         */
    mergeRoleAssignments (entity: any, membershipType: any, entityRoleMemberships: any) {
        var deferred = this.$injector.get("$q").defer();
        var existingModelRoleAssignments = {};
        this.queryForRoleAssignments(entity, membershipType).then((response: any) => {
            entityRoleMemberships.splice(0, entityRoleMemberships.length);

            // TODO: Consolidate the common behavior in the 2 loops below into a single function
            _.each(response.data.inherited, (roleMembership: any, roleName: any) => {
                entityRoleMemberships.push(roleMembership);
                existingModelRoleAssignments[roleMembership.role.systemName] = roleMembership;
                roleMembership.members = [];

                this.augmentRoleWithUiModel(roleMembership);
                _.each(roleMembership.groups, (group: any) => {
                    group.editable = false;
                    group.type = 'group';
                    group.title = (group.title == null || ObjectUtils.isUndefined(group.title)) ? group.systemName : group.title;
                    roleMembership.members.push(group)
                });
                _.each(roleMembership.users, (user: any) => {
                    user.editable = false;
                    user.type = 'user';
                    user.title = user.displayName;
                    roleMembership.members.push(user)
                })
            });
            _.each(response.data.assigned, (roleMembership: any, roleName: any) => {
                if (ObjectUtils.isUndefined(existingModelRoleAssignments[roleMembership.role.systemName])) {
                    existingModelRoleAssignments[roleMembership.role.systemName] = roleMembership;
                    entityRoleMemberships.push(roleMembership);
                }

                var existingMembership = existingModelRoleAssignments[roleMembership.role.systemName];

                this.augmentRoleWithUiModel(existingMembership);
                _.each(roleMembership.groups, (group: any) => {
                    group.editable = true;
                    group.type = 'group';
                    group.title = (group.title == null || ObjectUtils.isUndefined(group.title)) ? group.systemName : group.title;
                    existingMembership.members.push(group)
                });
                _.each(roleMembership.users, (user: any) => {
                    user.editable = true;
                    user.type = 'user';
                    user.title = user.displayName;
                    existingMembership.members.push(user)
                })
            });

            //get the available roles for this entity (might need to add a method to AccessControlService to getRolesForEntityType()
            this.accessControlService.getEntityRoles(membershipType).then((roles: any) => {
                _.each(roles, (role: any) => {
                    if (ObjectUtils.isUndefined(existingModelRoleAssignments[role.systemName])) {
                        var membership = { role: role };
                        this.augmentRoleWithUiModel(membership);
                        entityRoleMemberships.push(membership);
                    }
                });
                deferred.resolve(entityRoleMemberships);
            });

        });

        return deferred.promise;
    }
    /**
         * convert the model to a RoleMembershipChange object
         * @returns {Array}
         */
    //$scope.entity.roleMemberships
    toRoleMembershipChange (roleMemberships: any) {
        var roleMembershipChanges: any = [];
        _.each(roleMemberships, (roleMembership: any) => {
            var users = _.chain(roleMembership.members).filter((member) => {
                return member.type == 'user' && member.editable != false;
            }).map((user) => {
                return user.systemName;
            }).value();

            var groups = _.chain(roleMembership.members).filter((member) => {
                return member.type == 'group' && member.editable != false;
            }).map((group) => {
                return group.systemName;
            }).value();

            var obj = { "change": "REPLACE", roleName: roleMembership.role.systemName, users: users, groups: groups };
            roleMembershipChanges.push(obj);
        });
        return roleMembershipChanges;
    }
    /**
         * Save the Role Changes for this entity
         * @param $event
         */
    saveRoleMemberships (entityType: any, entityId: any, roleMemberships: any, callbackFn?: any) {
        var defer = this.$injector.get("$q").defer();
        var url = '';
        if (entityType === 'feed') {
            url = this.restUrlService.FEED_ROLES_URL(entityId);
        } else if (entityType === 'project') {
            // url = this.restUrlService.PROJECT_ROLES_URL(entityId);
        } else if (entityType === 'category') {
            url = this.restUrlService.CATEGORY_ROLES_URL(entityId);
        } else if (entityType === 'template') {
            url = this.restUrlService.TEMPLATE_ROLES_URL(entityId);
        } else if (entityType === "datasource") {
            url = this.restUrlService.DATASOURCE_ROLES_URL(entityId);
        }else if (entityType === "connector") {
            url = this.restUrlService.CONNECTOR_ROLES_URL(entityId);
        }
        //construct a RoleMembershipChange object
        var changes = this.toRoleMembershipChange(roleMemberships);
        var responses: any = [];
        _.each(changes, (roleMembershipChange) => {
            var promise = this.$injector.get("$http")({
                url: url,
                method: "POST",
                data: ObjectUtils.toJson(roleMembershipChange),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            });
            responses.push(promise);
        });

        this.$injector.get("$q").all(responses).then((resolvedResponses: any) => {
            var responses: any = [];
            _.each(resolvedResponses, (response: any) => {
                responses.push(response.data);
            })
            if (typeof callbackFn === "function") {
                callbackFn(responses);
            }
            defer.resolve(responses);
        }, (err: any) => defer.reject(err));
        return defer.promise;
    }
}
