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

        function queryForRoleAssignments(entity, entityType) {
            if (entity && entity.id && entity.id != null) {
                var url = '';
                if (entityType === 'feed') {
                    url = RestUrlService.FEED_ROLES_URL(entity.id);
                } else if (entityType === 'category') {
                    url = RestUrlService.CATEGORY_ROLES_URL(entity.id);
                } else if (entityType === 'template') {
                    url = RestUrlService.TEMPLATE_ROLES_URL(entity.id);
                } else if (entityType === "datasource") {
                    url = RestUrlService.DATASOURCE_ROLES_URL(entity.id);
                }
                return $http.get(url, {params: {verbose: true}});
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

        var data= angular.extend(svc, {
            entityTypes: {CATEGORY: "category", FEED: "feed", TEMPLATE: "template", DATASOURCE: "datasource"},


            /**
             * ensure the entity.roleMemberships.members are pushed back into the proper entity.roleMemberships.users and entity.roleMemberships.groups
             * @param entity the entity to save
             */
            updateEntityForSave: function (entity) {

               if(entity.roleMemberships){
                   _.each(entity.roleMemberships,function(roleMembership){
                       var users = [];
                       var groups = [];
                       var update = false;
                       if(roleMembership.members != null && roleMembership.members != undefined) {
                           //if the members is empty for the  entity we should update as the user cleared out memberships, otherwise we should update only if the member has a 'type' attr
                           var update = roleMembership.members.length == 0;
                           _.each(roleMembership.members, function (member) {
                               if (angular.isDefined(member.type)) {
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
                       if(update) {
                           roleMembership.users = users;
                           roleMembership.groups = groups;
                       }
                   });

                }
            },
            /**
             * Merges all possible roles for this entity, with the assigned roles/memberships
             */
            mergeRoleAssignments: function (entity, entityType) {
                var deferred = $q.defer();
                var existingModelRoleAssignments = {};
                queryForRoleAssignments(entity, entityType).then(function (response) {
                    entity.roleMemberships = [];
                    _.each(response.data, function (roleMembership, roleName) {
                        entity.roleMemberships.push(roleMembership);

                        existingModelRoleAssignments[roleMembership.role.systemName] = roleMembership;
                        augmentRoleWithUiModel(roleMembership);
                        roleMembership.members = [];
                        _.each(roleMembership.groups, function (group) {
                            group.type = 'group';
                            roleMembership.members.push(group)
                        });
                        _.each(roleMembership.users, function (user) {
                            user.type = 'user';
                            user.title = user.displayName;
                            roleMembership.members.push(user)
                        })
                    });

                    //get the available roles for this entity (might need to add a method to AccessControlService to getRolesForEntityType()
                    AccessControlService.getEntityRoles(entityType).then(function (roles) {
                        _.each(roles, function (role) {
                            if (angular.isUndefined(existingModelRoleAssignments[role.systemName])) {
                                var membership = {role: role};
                                augmentRoleWithUiModel(membership);
                                entity.roleMemberships.push(membership);
                            }
                        });
                        deferred.resolve(entity.roleMemberships);
                    });

                });

                return deferred.promise;
            }

        });
        return data;
    }]);

    var controller = function ($scope, $mdDialog, $q, $http, RestUrlService, entity, entityType, entityTitle, callbackEvents) {

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
            var roleMembershipChanges = [];
            _.each($scope.entity.roleMemberships, function (roleMembership) {
                var users = _.chain(roleMembership.members).filter(function (member) {
                    return member.type == 'user'
                }).map(function (user) {
                    return user.systemName;
                }).value();

                var groups = _.chain(roleMembership.members).filter(function (member) {
                    return member.type == 'group'
                }).map(function (group) {
                    return group.systemName;
                }).value();

                var obj = {"change": "REPLACE", roleName: roleMembership.role.systemName, users: users, groups: groups};
                roleMembershipChanges.push(obj);
            });
            return roleMembershipChanges;
        }

        /**
         * Save the Role Changes for this entity
         * @param $event
         */
        $scope.onSave = function ($event) {
            var url = '';
            if ($scope.entityType === 'feed') {
                url = RestUrlService.FEED_ROLES_URL(entity.id);
            } else if ($scope.entityType === 'category') {
                url = RestUrlService.CATEGORY_ROLES_URL(entity.id);
            } else if ($scope.entityType === 'template') {
                url = RestUrlService.TEMPLATE_ROLES_URL(entity.id);
            } else if ($scope.entityType === "datasource") {
                url = RestUrlService.DATASOURCE_ROLES_URL(entity.id);
            }
            //construct a RoleMembershipChange object
            var changes = toRoleMembershipChange();
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

            $q.all(responses).then(function () {
                if (angular.isFunction(callbackEvents.onSave)) {
                    callbackEvents.onSave();
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
        ["$scope", '$mdDialog', "$q", "$http", "RestUrlService", "entity", "entityType", "entityTitle", "callbackEvents", controller]);

});
