import * as angular from 'angular';
import * as _ from 'underscore';
import {UserService} from "../../services/UserService";
import {moduleName} from "../../module-name";

export default class UserDetailsController implements ng.IComponentController {
    $error:any = {duplicateUser: false, missingGroup: false, missingUser: false}; 
    allowAdmin:boolean = false; //Indicates that admin operations are allowed  {boolean}
    editModel:any = {}; //User model for the edit view. {UserPrincipal}
    groupList:any[] = []; //List of group system names {Array.<string>}
    groupMap:any = {}; //Map of group system names to group objects {Object.<string, GroupPrincipal>}
    groupSearchText:string = ""; //Autocomplete search text for group input  {string}
    isEditable:boolean = false; //Indicates if the edit view is displayed {boolean}
    isValid:boolean = false; //Indicates if the edit form is valid {boolean}
    loading:boolean = true; //Indicates that the user is currently being loaded {boolean}
    model:any = {displayName: null, email: null, enabled: true, groups: [], systemName: null}; //User model for the read-only view {UserPrincipal}
    userMap:any = {}; //Lookup map for detecting duplicate user names {Object.<string, boolean>}
    ngOnInit(){}

    constructor(
        private $scope:angular.IScope,
        private $mdDialog:angular.material.IDialogService,
        private $mdToast:angular.material.IToastService,
        private $transition$: any,
        private AccessControlService:any,
        private UserService:UserService,
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
    
    $scope.$watch(// Update $error.missingGroup when the edit model changes
            () => {return this.editModel.groups},
            () => {this.$error.missingGroup = (angular.isUndefined(this.editModel.groups) || this.editModel.groups.length === 0)},
            true
    );

    $scope.$watch( // Update $error when the system name changes
            () => {return this.editModel.systemName},
            () => {
                this.$error.duplicateUser = (angular.isString(this.editModel.systemName) && this.userMap[this.editModel.systemName]);
                this.$error.missingUser = (!angular.isString(this.editModel.systemName) || this.editModel.systemName.length === 0);
            }
    );
    this.onLoad();
    } // end of constructor

    /**
     * Indicates if the user can be deleted. The main requirement is that the user exists.
     * @returns {boolean} {@code true} if the user can be deleted, or {@code false} otherwise
     */
    canDelete=()=> {
        return (this.model.systemName !== null);
    };       
    /**
     * Finds the substring of the title for the specified group that matches the query term.
     * @param group the group
     * @returns {string} the group title substring
     */
    findGroupSearchText=(group:any)=> {
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
     * @returns {Array.<string>} the group titles for this user
     */
    getGroupTitles() {
        return _.map(this.model.groups, this.getGroupTitle);
    };

    onCancel() { //Cancels the current edit operation. If a new user is being created then redirects to the users page
        if (this.model.systemName === null) {
            this.StateService.Auth().navigateToUsers();
        }
    };

    onDelete() { //Deletes the current user.
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

        onEdit() { //Creates a copy of the user model for editing.
            this.editModel = angular.copy(this.model);
        };

        onLoad () { //Loads the user details           
            this.UserService.getGroups() // Get the list of groups
                    .then((groups:any) => {
                        this.groupList = [];
                        this.groupMap = {};
                        angular.forEach(groups, (group) => {
                            this.groupList.push(group.systemName);
                            this.groupMap[group.systemName] = group;
                        });
                    });

            this.AccessControlService.getUserAllowedActions()// Load allowed permissions
                    .then((actionSet:any) => {
                        this.allowAdmin = this.AccessControlService.hasAction(this.AccessControlService.USERS_ADMIN, actionSet.actions);
                    });
            
            if (angular.isString(this.$transition$.params().userId)) {// Load the user details
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
 
        onSave() { //Saves the current user.
            var model: any = angular.copy(this.editModel);
            this.UserService.saveUser(model)
                    .then(() => {
                        this.model = model;
                    });
        };

        /**
         * Filters the list of groups to those matching the specified query.
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

angular.module(moduleName).controller('UserDetailsController', ["$scope","$mdDialog","$mdToast","$transition$","AccessControlService","UserService","StateService",UserDetailsController]);