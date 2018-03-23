import * as angular from 'angular';
import * as _ from "underscore";
import AccessConstants from '../../../constants/AccessConstants';
const moduleName = require('feed-mgr/module-name');


export class EntityAccessControlService {

}
angular.module(moduleName).factory('EntityAccessControlDialogService', ["$mdDialog", function ($mdDialog:any) {

    var data = {
        showAccessControlDialog: function (entity:any, entityType:any, entityTitle:any, onSave:any, onCancel:any) {

            var callbackEvents = {onSave: onSave, onCancel: onCancel};
            return $mdDialog.show({
                controller: 'EntityAccessControlDialogController',
                templateUrl: 'js/feed-mgr/shared/entity-access-control/entity-access-control-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {entity: entity, entityType: entityType, entityTitle: entityTitle, callbackEvents: callbackEvents}
            }).then(function (msg:any) {
                //respond to action in dialog if necessary... currently dont need to do anything
            }, function () {

            });
        }
    };
    return data;
}]);

angular.module(moduleName).factory('EntityAccessControlService', ["$http", "$q", "AccessControlService", "RestUrlService", function ($http:any, $q:any, AccessControlService:any, RestUrlService:any) {

    function augmentRoleWithUiModel(roleMembership:any) {
        roleMembership.ui = {members: {selectedItem: '', searchText: ''}};
        if (angular.isUndefined(roleMembership.members)) {
            roleMembership.members = [];
        }
    }

    var roleUrlsMap = {
        "feed" : RestUrlService.FEED_ROLES_URL,
        "category" : RestUrlService.CATEGORY_ROLES_URL,
        "category-feed" : RestUrlService.CATEGORY_FEED_ROLES_URL,
        "template" : RestUrlService.TEMPLATE_ROLES_URL,
        "datasource" : RestUrlService.DATASOURCE_ROLES_URL
    }

    function queryForRoleAssignments(entity:any, membersType:any) {
        if (entity && entity.id && entity.id != null) {
            var url = '';
            if (membersType in roleUrlsMap ) {
                var f = roleUrlsMap[membersType];
                url = f(entity.id);
            }
            return $http.get(url);
        }
        else {
            var deferred = $q.defer();
            deferred.resolve({data: {}});
            return deferred.promise;
        }
    }

    function EntityAccessControlService() {
    }

    var svc = angular.extend(EntityAccessControlService.prototype, AccessConstants);

    var data = angular.extend(svc, {
        entityRoleTypes: {CATEGORY: "category", CATEGORY_FEED: "category-feed", FEED: "feed", TEMPLATE: "template", DATASOURCE: "datasource"},

        // may be called by plugins
        addRoleAssignment: function(type:any,urlFunc:any) {
            roleUrlsMap[type] = urlFunc;
        },

        /**
         * Ensure the entity's roleMemberships.members are pushed back into the proper entity.roleMemberships.users and entity.roleMemberships.groups
         * @param entity the entity to save
         */
        updateRoleMembershipsForSave: function (roleMemberships:any) {

            if (roleMemberships) {
                _.each(roleMemberships, function (roleMembership:any) {
                    var users:any = [];
                    var groups:any = [];
                    var update = false;
                    if (roleMembership.members != null && roleMembership.members != undefined) {
                        //if the members is empty for the  entity we should update as the user cleared out memberships, otherwise we should update only if the member has a 'type' attr
                        var update = roleMembership.members.length == 0;
                        _.each(roleMembership.members, function (member:any) {
                            if (angular.isDefined(member.type) && member.editable != false) {
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
        },

        /**
         * Merges all possible roles for this entity, with the assigned roles/memberships
         */
        mergeRoleAssignments: function (entity:any, membershipType:any, entityRoleMemberships:any) {
            var deferred = $q.defer();
            var existingModelRoleAssignments = {};
            queryForRoleAssignments(entity, membershipType).then(function (response:any) {
                entityRoleMemberships.splice(0, entityRoleMemberships.length);

                // TODO: Consolidate the common behavior in the 2 loops below into a single function
                _.each(response.data.inherited, function (roleMembership:any, roleName:any) {
                    entityRoleMemberships.push(roleMembership);
                    existingModelRoleAssignments[roleMembership.role.systemName] = roleMembership;
                    roleMembership.members = [];

                    augmentRoleWithUiModel(roleMembership);
                    _.each(roleMembership.groups, function (group:any) {
                        group.editable = false;
                        group.type = 'group';
                        group.title = (group.title == null || angular.isUndefined(group.title)) ? group.systemName : group.title;
                        roleMembership.members.push(group)
                    });
                    _.each(roleMembership.users, function (user:any) {
                        user.editable = false;
                        user.type = 'user';
                        user.title = user.displayName;
                        roleMembership.members.push(user)
                    })
                });
                _.each(response.data.assigned, function (roleMembership:any, roleName:any) {
                    if (angular.isUndefined(existingModelRoleAssignments[roleMembership.role.systemName])) {
                        existingModelRoleAssignments[roleMembership.role.systemName] = roleMembership;
                        entityRoleMemberships.push(roleMembership);
                    }

                    var existingMembership = existingModelRoleAssignments[roleMembership.role.systemName];

                    augmentRoleWithUiModel(existingMembership);
                    _.each(roleMembership.groups, function (group:any) {
                        group.editable = true;
                        group.type = 'group';
                        group.title = (group.title == null || angular.isUndefined(group.title)) ? group.systemName : group.title;
                        existingMembership.members.push(group)
                    });
                    _.each(roleMembership.users, function (user:any) {
                        user.editable = true;
                        user.type = 'user';
                        user.title = user.displayName;
                        existingMembership.members.push(user)
                    })
                });

                //get the available roles for this entity (might need to add a method to AccessControlService to getRolesForEntityType()
                AccessControlService.getEntityRoles(membershipType).then(function (roles:any) {
                    _.each(roles, function (role:any) {
                        if (angular.isUndefined(existingModelRoleAssignments[role.systemName])) {
                            var membership = {role: role};
                            augmentRoleWithUiModel(membership);
                            entityRoleMemberships.push(membership);
                        }
                    });
                    deferred.resolve(entityRoleMemberships);
                });

            });

            return deferred.promise;
        },
        /**
         * convert the model to a RoleMembershipChange object
         * @returns {Array}
         */
        //$scope.entity.roleMemberships
        toRoleMembershipChange: function (roleMemberships:any) {
            var roleMembershipChanges:any = [];
            _.each(roleMemberships, function (roleMembership:any) {
                var users = _.chain(roleMembership.members).filter(function (member) {
                    return member.type == 'user' && member.editable != false;
                }).map(function (user) {
                    return user.systemName;
                }).value();

                var groups = _.chain(roleMembership.members).filter(function (member) {
                    return member.type == 'group' && member.editable != false;
                }).map(function (group) {
                    return group.systemName;
                }).value();

                var obj = {"change": "REPLACE", roleName: roleMembership.role.systemName, users: users, groups: groups};
                roleMembershipChanges.push(obj);
            });
            return roleMembershipChanges;
        },

        /**
         * Save the Role Changes for this entity
         * @param $event
         */
        saveRoleMemberships: function (entityType:any,entityId:any, roleMemberships:any, callbackFn:any) {
            var defer = $q.defer();
            var url = '';
            if (entityType === 'feed') {
                url = RestUrlService.FEED_ROLES_URL(entityId);
            } else if (entityType === 'project') {
                url = RestUrlService.PROJECT_ROLES_URL(entityId);
            } else if (entityType === 'category') {
                url = RestUrlService.CATEGORY_ROLES_URL(entityId);
            } else if (entityType === 'template') {
                url = RestUrlService.TEMPLATE_ROLES_URL(entityId);
            } else if (entityType === "datasource") {
                url = RestUrlService.DATASOURCE_ROLES_URL(entityId);
            }
            //construct a RoleMembershipChange object
            var changes = data.toRoleMembershipChange(roleMemberships);
            var responses:any = [];
            _.each(changes, function (roleMembershipChange) {
                var promise = $http({
                    url: url,
                    method: "POST",
                    data: angular.toJson(roleMembershipChange),
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                });
                responses.push(promise);
            });

            $q.all(responses).then(function (resolvedResponses:any) {
                var responses:any = [];
                _.each(resolvedResponses,function(response:any){
                    responses.push(response.data);
                })
                if (angular.isFunction(callbackFn)) {
                    callbackFn(responses);
                }
                defer.resolve(responses);
            });
            return defer.promise;

        }
    });
    
    return data;
}]);

var controller = function ($scope:any, $mdDialog:any, $q:any, $http:any, RestUrlService:any
    ,EntityAccessControlService:any, entity:any, entityType:any, entityTitle:any, callbackEvents:any, FeedService:any) {

    $scope.entityTitle = entityTitle;

    $scope.entityType = entityType;

    /**
     * The Angular form for validation
     * @type {{}}
     */
    $scope.theForm = {}

    /**
     * The entity to provide Access Control
     */
    $scope.entity = entity;
    /**
    * Indexing control
    */
    $scope.allowIndexing = $scope.entity.allowIndexing;

    /**
     * convert the model to a RoleMembershipChange object
     * @returns {Array}
     */
    function toRoleMembershipChange() {
        return EntityAccessControlService.toRoleMembershipChange($scope.entity.roleMemberships);
    }

    /**
     * Save the Role Changes for this entity
     * @param $event
     */
    $scope.onSave = function ($event:any) {
        // var roleMembershipChanges =
        //     EntityAccessControlService.saveRoleMemberships($scope.entityType, $scope.entity.id, $scope.entity.roleMemberships, function (r:any) {
        //         if (angular.isFunction(callbackEvents.onSave)) {
        //             callbackEvents.onSave(r);
        //         }
                // $mdDialog.hide();
        if ($scope.allowIndexing != $scope.entity.allowIndexing) {
                            var indexInfoForDisplay = "";
                            if (!$scope.allowIndexing) {
                                indexInfoForDisplay = "Disabling indexing of metadata and schema...";
                            } else {
                                indexInfoForDisplay = "Enabling indexing of metadata and schema...";
                            }
                            FeedService.showFeedSavingDialog($event, indexInfoForDisplay, $scope.entity.feedName);
                            var copy = angular.copy(FeedService.editFeedModel);
                            copy.allowIndexing = $scope.allowIndexing;
                            FeedService.saveFeedModel(copy).then((response:any) => {
                                FeedService.hideFeedSavingDialog();
                                $scope.entity.allowIndexing = copy.allowIndexing;
                                var roleMembershipChanges =
                                    EntityAccessControlService.saveRoleMemberships($scope.entityType, $scope.entity.id, $scope.entity.roleMemberships, function (r:any) {
                                        if (angular.isFunction(callbackEvents.onSave)) {
                                            callbackEvents.onSave(r);
                                        }
                                        $mdDialog.hide();
                                    });
                            },  (response:any) => {
                                console.log("Error saving feed: " + $scope.entity.feedName);
                             });
                        } else {
                            var roleMembershipChanges =
                                EntityAccessControlService.saveRoleMemberships($scope.entityType, $scope.entity.id, $scope.entity.roleMemberships, function (r:any) {
                                    if (angular.isFunction(callbackEvents.onSave)) {
                                        callbackEvents.onSave(r);
                                    }
                                    $mdDialog.hide();
                               });
                        }
            // });
    };

    $scope.onCancel = function ($event:any) {
        $mdDialog.hide();
        if (angular.isFunction(callbackEvents.onCancel)) {
            callbackEvents.onCancel();
        }
    }

};

angular.module(moduleName).controller('EntityAccessControlDialogController',
    ["$scope", '$mdDialog', "$q", "$http", "RestUrlService", "EntityAccessControlService","entity", "entityType", "entityTitle", "callbackEvents","FeedService", controller]);

