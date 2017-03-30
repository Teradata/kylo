define(['angular','feed-mgr/module-name'], function (angular,moduleName) {

    var directive = function() {
        return {
            restrict: "E",
            bindToController: {
                entity:'=',
                model:'=',
                theForm:'=?',
                readOnly:'=?'
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/shared/entity-access-control/entity-access-control.html',
            controller: "EntityAccessControlController",
            link: function($scope, element, attrs, ngModel) {

            }
        };
    };

    var controller = function($q, UserGroupService,AccessControlService){

        var self = this;


        if(angular.isUndefined(this.readOnly)){
            this.readOnly = false;
        }

       if(angular.isUndefined(this.theForm)){
           this.theForm = {};
       }

        if(angular.isUndefined(this.model)){
            this.model = {};
        }
        if(angular.isUndefined(this.model.roles)){
            this.model.roles = [];
        }

        if(angular.isUndefined(this.model.owner) || this.model.owner == null){
            this.model.owner = null;
            //assign it the current user
            var requests = {currentUser:UserGroupService.getCurrentUser(),allUsers:getAllUsers()};
            $q.all(requests).then(function(response){
                    var matchingUsers = filterCollection(response.allUsers,response.currentUser.systemName,['_lowerDisplayName','_lowerSystemName']);
                    if(matchingUsers){
                        self.model.owner = matchingUsers[0];
                    }
            })
            /*
            UserGroupService.getCurrentUser().then(function(user){
                self.model.owner = user;
            });
            */
        }

        /**
         * Owner autocomplete model
         * @type {{searchText: string, searchTextChanged: controller.ownerAutoComplete.searchTextChanged, selectedItemChange: controller.ownerAutoComplete.selectedItemChange}}
         */
        this.ownerAutoComplete = {searchText:'',
            searchTextChanged:function(query){ },
            selectedItemChange:function(item) {
                if(item != null && item != undefined) {
                    self.model.owner = item;
                }
                else {
                    self.model.owner = null;
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


        function newRole(name){
            var role = {systemName:'',name:name,members:[]};
            // addin ui related stuff
            augmentRoleWithUiModel(role);
            return role;
        }

        function augmentRoleWithUiModel(role){
            role.ui = {members:{selectedItem:'',searchText:''}};
            if(angular.isUndefined(role.members)){
                role.members = [];
            }
        }

        /**
         * Merges all possible roles for this entity, with the assigned roles/memberships
         */
        function mergeRoleAssignments(){
            var availableEntityRoles = {};

           var existingModelRoleAssignments = {};
               _.each(self.model.roles,function(role){
                   existingModelRoleAssignments[role.systemName] = role;
                   augmentRoleWithUiModel(role);
               });


            //get the available roles for this entity (might need to add a method to AccessControlService to getRolesForEntityType()
            AccessControlService.getRoles().then(function(roles) {
                _.each(roles, function (role) {
                    augmentRoleWithUiModel(role);
                    availableEntityRoles[role.systeName] = role;
                    if(angular.isUndefined(existingModelRoleAssignments[role.systemName])){
                        self.model.roles.push(role);
                    }
                });
            });
        }

        function init(){

            mergeRoleAssignments();
        }


        init();





        //populate the self.model.groups with the query for the group access for this feed entity




    };

    angular.module(moduleName).controller('EntityAccessControlController', ["$q","UserGroupService","AccessControlService",controller]);
    angular.module(moduleName).directive('entityAccessControl', directive);


});
