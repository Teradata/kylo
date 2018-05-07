import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');


var directive = function() {
    return {
        restrict: "E",
        bindToController: {
            entity:'=',
            roleMembershipsProperty:'@?',
            allowOwnerChange:'=?',
            entityType:'@',
            theForm:'=?',
            readOnly:'=?',
            queryForEntityAccess:'=?'
        },
        controllerAs: 'vm',
        scope: {},
        templateUrl: 'js/feed-mgr/shared/entity-access-control/entity-access-control.html',
        controller: "EntityAccessControlController",
        link: function($scope:any, element:any, attrs:any, ngModel:any) {

        }
    };
};

export class EntityAccessControlController {

    enabled:any;
    readOnly:any;
    theForm:any;
    roleMembershipsProperty:any;
    allowOwnerChange:any;
    entity:any;
    entityRoleMemberships:any;
    queryForEntityAccess:any;
    rolesInitialized:any;
    ownerAutoComplete:any;
    queryUsers:any;
    onRemovedMember:any;
    queryUsersAndGroups:any;

    constructor($q:any,$http:any, UserGroupService:any,EntityAccessControlService:any, AccessControlService:any){

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
       
        if(angular.isUndefined(this.roleMembershipsProperty)){
    	   this.roleMembershipsProperty = "roleMemberships";
        }
        
        if(angular.isUndefined(this.allowOwnerChange)){
        	this.allowOwnerChange = true;
        }


        if(angular.isUndefined(this.entity[this.roleMembershipsProperty])){
        	this.entity[this.roleMembershipsProperty] = [];
        }

        this.entityRoleMemberships = this.entity[this.roleMembershipsProperty];


        if(angular.isUndefined(this.queryForEntityAccess)){
            this.queryForEntityAccess = false;
        }

        if(angular.isUndefined(this.entity.owner) || this.entity.owner == null){
            this.entity.owner = null;
            //assign it the current user
            var requests = {currentUser:UserGroupService.getCurrentUser(),allUsers:getAllUsers()};
            $q.all(requests).then(function(response:any){
                    var matchingUsers = filterCollection(response.allUsers,response.currentUser.systemName,['_lowerDisplayName','_lowerSystemName']);
                    if(matchingUsers){
                        self.entity.owner = matchingUsers[0];
                    }
            })
        }

        /**
         * Flag that the user has updated the role memberships
         * @type {boolean}
         */
        self.entity.roleMembershipsUpdated = angular.isUndefined(self.entity.roleMembershipsUpdated) ? false : self.entity.roleMembershipsUpdated;

        /**
         * Flag to indicate we should query for the roles from the server.
         * If the entity has already been marked as being updated then mark this as initialized so it doesnt loose the in-memory settings the user has applied
         * @type {boolean}
         */
        self.rolesInitialized = self.queryForEntityAccess == true ? false : (self.entity.roleMembershipsUpdated == true ? true : false);


        /**
         * Owner autocomplete model
         * @type {{searchText: string, searchTextChanged: controller.ownerAutoComplete.searchTextChanged, selectedItemChange: controller.ownerAutoComplete.selectedItemChange}}
         */
        this.ownerAutoComplete = {searchText:'',
            searchTextChanged:function(query:any){ },
            selectedItemChange:function(item:any) {
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
        var allGroups:any = null;

        /**
         * Cache of the user names for filtering
         * @type {null}
         */
        var allUsers:any = null;

        var roles = null;

        /**
         * Filter the groups or users based upon the supplied query
         * @param collection
         * @param query
         * @returns {Array}
         */
        var filterCollection = function(collection:any,query:any, keys:any){
            return query ? _.filter(collection,function(item) {
                                    var lowercaseQuery = angular.lowercase(query);
                                       var found = _.find(keys,function(key:any) {
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
        this.queryUsersAndGroups = function(query:any){
        	self.entity.roleMembershipsUpdated = true;
            var df = $q.defer();
            var request = {groups:getAllGroups(),users:getAllUsers()};
            $q.all(request).then(function(results:any){
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
         * If an attempt is made to remove a non-editable member of a role membership then
         * re-add that member to the membership set.
         */
        this.onRemovedMember = function(member:any, members:any) {
            if (member.editable == false) {
            	members.unshift(member);
            }
        };

        /**
         * Query users
         * @param query
         */
        this.queryUsers = function(query:any){
            var df = $q.defer();
            getAllUsers().then(function(users:any) {
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
                    .then(function(groups:any) {
                        allGroups =  _.map(groups,function (item:any) {
                            item._lowername = (item.title == null || angular.isUndefined(item.title)) ? item.systemName.toLowerCase() : item.title.toLowerCase();
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
                    .then(function(users:any) {
                        allUsers = _.map(users,function (user:any) {
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
            if(self.rolesInitialized == false) {
                $q.when(EntityAccessControlService.mergeRoleAssignments(self.entity, self["entityType"], self.entity[self.roleMembershipsProperty])).then(function(){
                    self.rolesInitialized == true
                    self.entityRoleMemberships = self.entity[self.roleMembershipsProperty];
                });
            }
        }

        init();
    };
    
}
angular.module(moduleName).controller('EntityAccessControlController', ["$q","$http","UserGroupService","EntityAccessControlService","AccessControlService",EntityAccessControlController]);
angular.module(moduleName).directive('entityAccessControl', directive);

