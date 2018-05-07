import * as angular from 'angular';
// import moduleName from '../../../auth/module-name';
import * as _ from 'underscore';

import {UserService} from "../../services/UserService";
//const moduleName = require('auth/module-name');

import {moduleName} from "../../module-name";

export default class UserDetailsController implements ng.IComponentController {

    ngOnInit(){
        
    }

    constructor(
        private $scope:angular.IScope,
        private $mdDialog:angular.material.IDialogService,
        private $mdToast:angular.material.IToastService,
        private $transition$: any,
        private AccessControlService:any,
        private UserService:any,
        private StateService:any,
        

    ){
        $scope.$watch(
            () => {return this.$error},
            () => {
                this.isValid = _.reduce(this.$error, 
                    (memo, value) => {
                    return memo && !value;
                }, true);
            },
            true
    );

    // Update $error.missingGroup when the edit model changes
    $scope.$watch(
            () => {return this.editModel.groups},
            () => {this.$error.missingGroup = (angular.isUndefined(this.editModel.groups) || this.editModel.groups.length === 0)},
            true
    );

    // Update $error when the system name changes
    $scope.$watch(
            () => {return this.editModel.systemName},
            () => {
                this.$error.duplicateUser = (angular.isString(this.editModel.systemName) && this.userMap[this.editModel.systemName]);
                this.$error.missingUser = (!angular.isString(this.editModel.systemName) || this.editModel.systemName.length === 0);
            }
    );
    this.onLoad();
    }

        $error:any = {duplicateUser: false, missingGroup: false, missingUser: false};

        /**
         * Indicates that admin operations are allowed.
         * @type {boolean}
         */
        allowAdmin:any = false;

        /**
         * User model for the edit view.
         * @type {UserPrincipal}
         */
        editModel:any = {};

        /**
         * List of group system names.
         * @type {Array.<string>}
         */
        groupList:any[] = [];

        /**
         * Map of group system names to group objects.
         * @type {Object.<string, GroupPrincipal>}
         */
        groupMap:any = {};

        /**
         * Autocomplete search text for group input.
         * @type {string}
         */
        groupSearchText:any = "";

        /**
         * Indicates if the edit view is displayed.
         * @type {boolean}
         */
        isEditable:any = false;

        /**
         * Indicates if the edit form is valid.
         * @type {boolean}
         */
        isValid:any = false;

        /**
         * Indicates that the user is currently being loaded.
         * @type {boolean}
         */
        loading:any = true;

        /**
         * User model for the read-only view.
         * @type {UserPrincipal}
         */
        model:any = {displayName: null, email: null, enabled: true, groups: [], systemName: null};

        /**
         * Lookup map for detecting duplicate user names.
         * @type {Object.<string, boolean>}
         */
        userMap:any = {};

        /**
         * Indicates if the user can be deleted. The main requirement is that the user exists.
         *
         * @returns {boolean} {@code true} if the user can be deleted, or {@code false} otherwise
         */
        canDelete() {
            return (this.model.systemName !== null);
        };       
                /**
         * Finds the substring of the title for the specified group that matches the query term.
         *
         * @param group the group
         * @returns {string} the group title substring
         */
        findGroupSearchText (group:any) {
            var safeQuery = this.groupSearchText.toLocaleUpperCase();
            if (angular.isString(this.groupMap[group].title)) {
                var titleIndex = this.groupMap[group].title.toLocaleUpperCase().indexOf(safeQuery);
                return (titleIndex > -1) ? this.groupMap[group].title.substr(titleIndex, safeQuery.length) : this.groupSearchText;
            } else {
                var nameIndex = group.toLocaleUpperCase().indexOf(safeQuery);
                return (nameIndex > -1) ? group.substr(nameIndex, safeQuery.length) : this.groupSearchText;
            }
        };

        /**
         * Gets the title for the specified group.
         *
         * @param group the group
         * @returns {string} the group title
         */
        getGroupTitle=(group:any)=> {
            if (angular.isDefined(this.groupMap[group]) && angular.isString(this.groupMap[group].title)) {
                return this.groupMap[group].title;
            } else {
                return group;
            }
        };

        /**
         * Gets the titles for every group this user belongs to.
         *
         * @returns {Array.<string>} the group titles for this user
         */
        getGroupTitles() {
            return _.map(this.model.groups, this.getGroupTitle);
        };

        /**
         * Cancels the current edit operation. If a new user is being created then redirects to the users page.
         */
        onCancel() {
            if (this.model.systemName === null) {
                this.StateService.Auth().navigateToUsers();
            }
        };

        /**
         * Deletes the current user.
         */
        onDelete() {
            var name = (angular.isString(this.model.displayName) && this.model.displayName.length > 0) ? this.model.displayName : this.model.systemName;
            this.UserService.deleteUser(encodeURIComponent(this.model.systemName))
                    .then(() => {
                        this.$mdToast.show(
                                this.$mdToast.simple()
                                        .textContent("Successfully deleted the user " + name)
                                        .hideDelay(3000)
                        );
                        this.StateService.Auth().navigateToUsers();
                    }, () => {
                        this.$mdDialog.show(
                                this.$mdDialog.alert()
                                        .clickOutsideToClose(true)
                                        .title("Delete Failed")
                                        .textContent("The user " + name + " could not be deleted. " )//+ err.data.message
                                        .ariaLabel("Failed to delete user")
                                        .ok("Got it!")
                        );
                    });
        };

        /**
        //  * Creates a copy of the user model for editing.
         */
        onEdit() {
            this.editModel = angular.copy(this.model);
        };

        /**
         * Loads the user details.
         */
        onLoad () {
            // Get the list of groups
            this.UserService.getGroups()
                    .then((groups:any) => {
                        this.groupList = [];
                        this.groupMap = {};
                        angular.forEach(groups, (group) => {
                            this.groupList.push(group.systemName);
                            this.groupMap[group.systemName] = group;
                        });
                    });

            // Load allowed permissions
            this.AccessControlService.getUserAllowedActions()
                    .then((actionSet:any) => {
                        this.allowAdmin = this.AccessControlService.hasAction(this.AccessControlService.USERS_ADMIN, actionSet.actions);
                    });

            // Load the user details
            if (angular.isString(this.$transition$.params().userId)) {
                this.UserService.getUser(this.$transition$.params().userId)
                        .then((user:any) => {
                            this.model = user;
                            this.loading = false;
                        });
                       
            } else {
                this.onEdit();
                this.isEditable = true;
                this.loading = false;

                this.UserService.getUsers()
                        .then((users:any) => {
                            this.userMap = {};
                            angular.forEach(users, (user:any) => {
                                this.userMap[user.systemName] = true;
                            });
                        });
            }
        };
 

        
        /**
         * Saves the current user.
         */
        onSave() {
            var model = angular.copy(this.editModel);
            this.UserService.saveUser(model)
                    .then(() => {
                        this.model = model;
                    });
        };

        /**
         * Filters the list of groups to those matching the specified query.
         *
         * @param {string} query the query string
         * @returns {Array.<string>} the list of matching groups
         */
        queryGroups (query:any) {
            var safeQuery = query.toLocaleUpperCase();
            return this.groupList
                    // Filter groups that are already selected
                    .filter((group:any) => {
                        return (this.editModel.groups.indexOf(group) === -1);
                    })
                    // Find position of query term
                    .map((group) => {
                        var nameIndex = group.toLocaleUpperCase().indexOf(safeQuery);
                        var titleIndex = angular.isString(this.groupMap[group].title) ? this.groupMap[group].title.toLocaleUpperCase().indexOf(safeQuery) : -1;
                        var index = (titleIndex > -1 && (nameIndex === -1 || nameIndex > titleIndex)) ? titleIndex : nameIndex;
                        return {name: group, index: index};
                    })
                    // Filter groups without query term
                    .filter((item) => {
                        return item.index > -1;
                    })
                    // Sort based on position of query term
                    .sort((a, b) => {
                        return a.index - b.index;
                    })
                    // Map back to just the name
                    .map((item) => {
                        return item.name;
                    });
        };


}

angular.module(moduleName)
.controller('UserDetailsController', ["$scope","$mdDialog","$mdToast","$transition$","AccessControlService","UserService","StateService",UserDetailsController]);

