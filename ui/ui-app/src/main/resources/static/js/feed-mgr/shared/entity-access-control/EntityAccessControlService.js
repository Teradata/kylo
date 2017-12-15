define(['angular', 'feed-mgr/module-name','constants/AccessConstants'], function (angular, moduleName, AccessConstants) {
    angular.module(moduleName).factory('EntityAccessControlDialogService', ["$mdDialog", function ($mdDialog) {

        var data = {
            showAccessControlDialog: function (entity, entityType, entityTitle, onSave, onCancel) {

                var callbackEvents = {onSave: onSave, onCancel: onCancel};
                return $mdDialog.show({
                    controller: 'EntityAccessControlDialogController',
                    templateUrl: 'js/feed-mgr/shared/entity-access-control/entity-access-control-dialog.html',
                    parent: angular.element(document.body),
                    clickOutsideToClose: false,
                    fullscreen: true,
                    locals: {entity: entity, entityType: entityType, entityTitle: entityTitle, callbackEvents: callbackEvents}
                }).then(function (msg) {
                    //respond to action in dialog if necessary... currently dont need to do anything
                }, function () {

                });
            }
        };
        return data;
    }]);

    angular.module(moduleName).factory('EntityAccessControlService', ["$http", "$q", "AccessControlService", "RestUrlService", function ($http, $q, AccessControlService, RestUrlService) {

        function augmentRoleWithUiModel(roleMembership) {
            roleMembership.ui = {members: {selectedItem: '', searchText: ''}};
            if (angular.isUndefined(roleMembership.members)) {
                roleMembership.members = [];
            }
        }

        function queryForRoleAssignments(entity, membersType) {
            if (entity && entity.id && entity.id != null) {
                var url = '';
                if (membersType === 'feed') {
                    url = RestUrlService.FEED_ROLES_URL(entity.id);
                } else if (membersType === 'category') {
                    url = RestUrlService.CATEGORY_ROLES_URL(entity.id);
                } else if (membersType === 'category-feed') {
                	url = RestUrlService.CATEGORY_FEED_ROLES_URL(entity.id);
                } else if (membersType === 'template') {
                    url = RestUrlService.TEMPLATE_ROLES_URL(entity.id);
                } else if (membersType === "datasource") {
                    url = RestUrlService.DATASOURCE_ROLES_URL(entity.id);
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
            /**
             * Ensure the entity's roleMemberships.members are pushed back into the proper entity.roleMemberships.users and entity.roleMemberships.groups
             * @param entity the entity to save
             */
            updateRoleMembershipsForSave: function (roleMemberships) {

                if (roleMemberships) {
                    _.each(roleMemberships, function (roleMembership) {
                        var users = [];
                        var groups = [];
                        var update = false;
                        if (roleMembership.members != null && roleMembership.members != undefined) {
                            //if the members is empty for the  entity we should update as the user cleared out memberships, otherwise we should update only if the member has a 'type' attr
                            var update = roleMembership.members.length == 0;
                            _.each(roleMembership.members, function (member) {
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
            mergeRoleAssignments: function (entity, membershipType, entityRoleMemberships) {
                var deferred = $q.defer();
                var existingModelRoleAssignments = {};
                queryForRoleAssignments(entity, membershipType).then(function (response) {
                    entityRoleMemberships.splice(0, entityRoleMemberships.length);

                    // TODO: Consolidate the common behavior in the 2 loops below into a single function
                    _.each(response.data.inherited, function (roleMembership, roleName) {
                        entityRoleMemberships.push(roleMembership);
                        existingModelRoleAssignments[roleMembership.role.systemName] = roleMembership;
                        roleMembership.members = [];

                        augmentRoleWithUiModel(roleMembership);
                        _.each(roleMembership.groups, function (group) {
                            group.editable = false;
                            group.type = 'group';
                            group.title = (group.title == null || angular.isUndefined(group.title)) ? group.systemName : group.title;
                            roleMembership.members.push(group)
                        });
                        _.each(roleMembership.users, function (user) {
                            user.editable = false;
                            user.type = 'user';
                            user.title = user.displayName;
                            roleMembership.members.push(user)
                        })
                    });
                    _.each(response.data.assigned, function (roleMembership, roleName) {
                        if (angular.isUndefined(existingModelRoleAssignments[roleMembership.role.systemName])) {
                            existingModelRoleAssignments[roleMembership.role.systemName] = roleMembership;
                            entityRoleMemberships.push(roleMembership);
                        }

                        var existingMembership = existingModelRoleAssignments[roleMembership.role.systemName];

                        augmentRoleWithUiModel(existingMembership);
                        _.each(roleMembership.groups, function (group) {
                            group.editable = true;
                            group.type = 'group';
                            group.title = (group.title == null || angular.isUndefined(group.title)) ? group.systemName : group.title;
                            existingMembership.members.push(group)
                        });
                        _.each(roleMembership.users, function (user) {
                            user.editable = true;
                            user.type = 'user';
                            user.title = user.displayName;
                            existingMembership.members.push(user)
                        })
                    });

                    //get the available roles for this entity (might need to add a method to AccessControlService to getRolesForEntityType()
                    AccessControlService.getEntityRoles(membershipType).then(function (roles) {
                        _.each(roles, function (role) {
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
            toRoleMembershipChange: function (roleMemberships) {
                var roleMembershipChanges = [];
                _.each(roleMemberships, function (roleMembership) {
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
            saveRoleMemberships: function (entityType,entityId, roleMemberships, callbackFn) {
                var defer = $q.defer();
                var url = '';
                if (entityType === 'feed') {
                    url = RestUrlService.FEED_ROLES_URL(entityId);
                } else if (entityType === 'category') {
                    url = RestUrlService.CATEGORY_ROLES_URL(entityId);
                } else if (entityType === 'template') {
                    url = RestUrlService.TEMPLATE_ROLES_URL(entityId);
                } else if (entityType === "datasource") {
                    url = RestUrlService.DATASOURCE_ROLES_URL(entityId);
                }
                //construct a RoleMembershipChange object
                var changes = data.toRoleMembershipChange(roleMemberships);
                var responses = [];
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

                $q.all(responses).then(function (resolvedResponses) {
                    var responses = [];
                    _.each(resolvedResponses,function(response){
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

    var controller = function ($scope, $mdDialog, $q, $http, RestUrlService,EntityAccessControlService, entity, entityType, entityTitle, callbackEvents) {

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
        $scope.onSave = function ($event) {
            var roleMembershipChanges =
                EntityAccessControlService.saveRoleMemberships($scope.entityType, $scope.entity.id, $scope.entity.roleMemberships, function (r) {
                    if (angular.isFunction(callbackEvents.onSave)) {
                        callbackEvents.onSave(r);
                    }
                    $mdDialog.hide();
                });
        };

        $scope.onCancel = function ($event) {
            $mdDialog.hide();
            if (angular.isFunction(callbackEvents.onCancel)) {
                callbackEvents.onCancel();
            }
        }

    };

    angular.module(moduleName).controller('EntityAccessControlDialogController',
        ["$scope", '$mdDialog', "$q", "$http", "RestUrlService", "EntityAccessControlService","entity", "entityType", "entityTitle", "callbackEvents", controller]);

});
