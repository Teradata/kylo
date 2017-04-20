define(['angular','feed-mgr/module-name'], function (angular,moduleName) {

    var directive = function() {
        return {
            restrict: "E",
            bindToController: {
                entity:'=',
                entityType:'@',
                theForm:'=?',
                readOnly:'=?',
                queryForEntityAccess:'=?'
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/shared/entity-access-control/entity-access-control.html',
            controller: "EntityAccessControlController",
            link: function($scope, element, attrs, ngModel) {

            }
        };
    };

    var controller = function($q,$http, UserGroupService,EntityAccessControlService, AccessControlService){

        var self = this;

        /**
         * are we using Entity access control
         * @type {boolean}
         */
        this.enabled = false;

        $q.when(AccessControlService.checkEntityAccessControlled()).then(function(){
            self.enabled = AccessControlService.isEntityAccessControlled();
        });


        if(angular.isUndefined(this.readOnly)){
            this.readOnly = false;
        }

       if(angular.isUndefined(this.theForm)){
           this.theForm = {};
       }


        if(angular.isUndefined(this.entity.roleMemberships)){
            this.entity.roleMemberships = [];
        }

        if(angular.isUndefined(this.entity.owner) || this.entity.owner == null){
            this.entity.owner = null;
            //assign it the current user
            var requests = {currentUser:UserGroupService.getCurrentUser(),allUsers:getAllUsers()};
            $q.all(requests).then(function(response){
                    var matchingUsers = filterCollection(response.allUsers,response.currentUser.systemName,['_lowerDisplayName','_lowerSystemName']);
                    if(matchingUsers){
                        self.entity.owner = matchingUsers[0];
                    }
            })
        }


        /**
         * Owner autocomplete model
         * @type {{searchText: string, searchTextChanged: controller.ownerAutoComplete.searchTextChanged, selectedItemChange: controller.ownerAutoComplete.selectedItemChange}}
         */
        this.ownerAutoComplete = {searchText:'',
            searchTextChanged:function(query){ },
            selectedItemChange:function(item) {
                if(item != null && item != undefined) {
                    self.entity.owner = item;
                }
                else {
                    self.entity.owner = null;
                }

            }
        }


        /**
         * Cache of the group names for filtering
         * @type {null}
         */
        var allGroups = null;

        /**
         * Cache of the user names for filtering
         * @type {null}
         */
        var allUsers = null;

        var roles = null;

        /**
         * Filter the groups or users based upon the supplied query
         * @param collection
         * @param query
         * @returns {Array}
         */
        var filterCollection = function(collection,query, keys){
            return query ? _.filter(collection,function(item) {
                                    var lowercaseQuery = angular.lowercase(query);
                                       var found = _.find(keys,function(key) {
                                            return (item[key].indexOf(lowercaseQuery) === 0);
                                        });
                                       if(found != undefined){
                                           return true;
                                       }
                                       else {
                                           return false;
                                       }
                                }) : [];
        }

        /**
         * Query users and groups
         * @param query
         */
        this.queryUsersAndGroups = function(query){
            /**
             * Flag that the user has updated the role memberships
             * @type {boolean}
             */
            self.entity.roleMembershipsUpdated = true;

            var df = $q.defer();
            var request = {groups:getAllGroups(),users:getAllUsers()};
            $q.all(request).then(function(results){
                var groups = results.groups;
                var users = results.users;
                var matchingGroups = filterCollection(groups,query,['_lowername']);
                var matchingUsers = filterCollection(users,query,['_lowerDisplayName','_lowerSystemName']);
                var arr = matchingGroups.concat(matchingUsers);
                df.resolve(arr);
            });
            return df.promise;
        }

        /**
         * Query users
         * @param query
         */
        this.queryUsers = function(query){
            var df = $q.defer();
            getAllUsers().then(function(users){

                var matchingUsers = filterCollection(users,query,['_lowerDisplayName','_lowerSystemName']);
                df.resolve(matchingUsers);
            });
            return df.promise;
        }


        var getAllGroups = function(){
            var df = $q.defer();
            if(allGroups == null ) {
                // Get the list of groups
                UserGroupService.getGroups()
                    .then(function(groups) {
                        allGroups =  _.map(groups,function (item) {
                            item._lowername = item.title.toLowerCase();
                            item.type = 'group'
                            return item;
                        });
                        df.resolve(allGroups);
                    });
            }
            else {
                df.resolve(allGroups);
            }
            return df.promise;
        }

        function getAllUsers(){
            var df = $q.defer();
            if(allUsers == null ) {
                // Get the list of groups
                UserGroupService.getUsers()
                    .then(function(users) {
                        allUsers = _.map(users,function (user) {
                            var name = (angular.isString(user.displayName) && user.displayName.length > 0) ? user.displayName : user.systemName;
                            user.name = name;
                            user.displayName = name;
                            user.title = name;
                            user.type = 'user';
                            user._lowername = name.toLowerCase();
                            user._lowerSystemName = user.systemName.toLowerCase()
                            user._lowerDisplayName = angular.isString(user.displayName) ?  user.displayName.toLowerCase() : '';
                            return user;
                        });
                   //     var result = filterCollection(allUserNamesLowerCase,query,['_lowerDisplayName','_lowerSystemName']);
                        df.resolve(allUsers);
                    });
            }
            else {
                df.resolve(allUsers);
            }
            return df.promise;
        }

        function init(){
            if(angular.isUndefined(self.entity.roleMembershipsUpdated) || self.entity.roleMembershipsUpdated == false) {
                EntityAccessControlService.mergeRoleAssignments(self.entity, self.entityType);
            }
        }

        init();
    };

    angular.module(moduleName).controller('EntityAccessControlController', ["$q","$http","UserGroupService","EntityAccessControlService","AccessControlService",controller]);
    angular.module(moduleName).directive('entityAccessControl', directive);


});
