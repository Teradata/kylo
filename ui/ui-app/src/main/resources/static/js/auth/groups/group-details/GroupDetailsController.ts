import * as angular from 'angular';
import * as _ from 'underscore';
import {moduleName} from "../../module-name";
import UserService from "../../services/UserService";
import {StateService} from  "../../../services/StateService";
import {AccessControlService} from "../../../services/AccessControlService";
import PermissionsTableController from "../../shared/permissions-table/permissions-table";
import AccessConstants from "../../../constants/AccessConstants";
import "../../module";
import "../../module-require";
import {Transition} from "@uirouter/core";
import {LoadingDialogService} from "../../../common/loading-dialog/loading-dialog";

export class GroupDetailsController implements ng.IComponentController {
    $transition$: Transition;
    $error:any = {duplicateName: false, missingName: false };
    /**
     * Indicates that admin operations are allowed.
     * @type {boolean}
     */
    allowAdmin:boolean = false; 
     /**
     * User model for the edit view.
     * @type {UserPrincipal}
     */
    editModel:any = {};
    /**
     * Map of group system names to group objects.
     * @type {Object.<string, GroupPrincipal>}
     */
    groupMap:any = {};
    /**
     * Indicates if the edit view is displayed.
     * @type {boolean}
     */
    isEditable:boolean = false;
    /**
    * Indicates if the edit form is valid.
    * @type {boolean}
    */
    isValid:boolean = false; 
    groupId:any = this.$transition$.params().groupId;
    /**
    * Indicates that the user is currently being loaded.
    * @type {boolean}
    */
    loading:boolean = true;
    /**
     * User model for the read-only view.
     * @type {UserPrincipal}
     */
    model:any = {description: null, memberCount: 0, systemName: null, title: null};
     /**
     * List of actions allowed to the group.
     * @type {Array.<Action>}
     */
    actions: any[] = [];
    /**
     * Indicates that user operations are allowed.
     * @type boolean
     */
    allowUsers: boolean = false;
    /**
     * Editable list of actions allowed to the group.
     * @type {Array.<Action>}
     */
    editActions: any[] = [];
    /**
     * Indicates if the permissions edit view is displayed.
     * @type {boolean}
     */
   isPermissionsEditable: boolean = false;
    /**
     *  Users in the group.
     * @type {Array.<UserPrincipal>}
     */
    users: any[] = [];

    /**
     * flag set when a delete is in progress
     */
    deleting:boolean = false;
    
    ngOnInit(){}
    static readonly $inject = ["$scope","$mdDialog","$mdToast",//"$transition$",
                                "AccessControlService","UserService","StateService","LoadingDialogService"];
    constructor(
        private $scope:angular.IScope,
        private $mdDialog:angular.material.IDialogService,
        private $mdToast:angular.material.IToastService,
        //private $transition$: Transition,
        private accessControlService:AccessControlService,
        private UserService:UserService,
        private StateService:StateService,
        private loadingDialog:LoadingDialogService){
         // Update isValid when $error is updated
        $scope.$watch(
            () => {return this.$error},
            () => {this.isValid = _.reduce(this.$error,(memo, value) => {
                                                                    return memo && !value;
                                                                    }, true);
                    },
                    true
        );

        // Update $error when thes system name changes
        $scope.$watch(
                () => {return this.editModel.systemName},
                () => {
                    this.$error.duplicateName = (angular.isString(this.editModel.systemName) && this.groupMap[this.editModel.systemName]);
                    this.$error.missingName = (!angular.isString(this.editModel.systemName) || this.editModel.systemName.length === 0);
                }
        );
        this.onLoad();
    }
        /**
         * Gets the display name of the specified user. Defaults to the system name if the display name is blank.
         * @param user the user
         * @returns {string} the display name
         */
        getUserName(user: any) {
                return (angular.isString(user.displayName) && user.displayName.length > 0) ? user.displayName : user.systemName;
            };

        /**
         * Indicates if the user can be deleted. The main requirement is that the user exists.
         *
         * @returns {boolean} {@code true} if the user can be deleted, or {@code false} otherwise
         */
        canDelete() {
            return (!this.deleting && this.model.systemName !== null);
        };       

        /**
         * Cancels the current edit operation. If a new user is being created then redirects to the users page.
         */
        onCancel() {
            if (this.model.systemName === null) {
                this.StateService.Auth.navigateToGroups();
            }
        };
        testLoading(){
            this.loadingDialog.showDialog();
        }

        /**
         * Deletes the current group.
         */
        onDelete() {
            this.deleting = true;
            this.loadingDialog.showDialog();
            var name = (angular.isString(this.model.title) && this.model.title.length > 0) ? this.model.title : this.model.systemName;
             this.UserService.deleteGroup(this.model.systemName)
                    .then(() => {
                        this.deleting = false;
                        this.loadingDialog.hideDialog()
                        this.$mdToast.show(
                                this.$mdToast.simple()
                                        .textContent("Successfully deleted the group " + name)
                                        .hideDelay(3000)
                        );
                        this.StateService.Auth.navigateToGroups();
                    }, () => {
                        this.deleting = false
                        this.loadingDialog.hideDialog()
                        this.$mdDialog.show(
                                this.$mdDialog.alert()
                                        .clickOutsideToClose(true)
                                        .title("Delete Failed")
                                        .textContent("The group " + name + " could not be deleted. " )//+ err.data.message
                                        .ariaLabel("Failed to delete group")
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
             * Creates a copy of the permissions for editing.
             */
        onEditPermissions() {
           this.editActions = angular.copy(this.actions);
        };

        /**
         * Loads the user details.
         */
        onLoad=()=>{
            // Load allowed permissions
            this.accessControlService.getUserAllowedActions()
                    .then((actionSet:any)=>{
                       this.allowAdmin = this.accessControlService.hasAction(AccessConstants.GROUP_ADMIN, actionSet.actions);
                       this.allowUsers = this.accessControlService.hasAction(AccessConstants.USERS_ACCESS, actionSet.actions);
                  });

          // Fetch group details
                if (angular.isString(this.$transition$.params().groupId)) {
                    this.UserService.getGroup(this.$transition$.params().groupId)
                            .then((group: any)=> {
                                this.model = group;
                                this.loading = false; // _this
                            });
                    this.UserService.getUsersByGroup(this.$transition$.params().groupId)
                            .then((users: any)=> {
                                this.users = users;
                            });
                    this.accessControlService.getAllowedActions(null, null, this.$transition$.params().groupId)
                            .then((actionSet: any)=> {
                                this.actions = actionSet.actions;
                            });
                } else {
                    this.onEdit();
                    this.isEditable = true;
                    this.loading = false;
                    this.UserService.getGroups().then(function(groups: any) {
                                this.groupMap = {};
                                angular.forEach(groups, function(group: any) {
                                    this.groupMap[group.systemName] = true;
                                });
                            });
                }
        };
        
    /**
         * Saves the current group.
         */
    onSave() { 
        var model = angular.copy(this.editModel);
        this.UserService.saveGroup(model)
                .then((updated: any) => {
                    this.model = updated;
                    this.groupId = this.model.systemName;
                });
    };
     /**
     * Saves the current permissions.
     */
    onSavePermissions() {
            var actions = angular.copy(this.editActions);
            this.accessControlService.setAllowedActions(null, null, this.model.systemName, actions)
                    .then((actionSet: any) =>{
                        this.actions = actionSet.actions;
                    });
    };

    /**
     * Navigates to the details page for the specified user.
     *
     * @param user the user
     */
    onUserClick(user: any) {
        this.StateService.Auth.navigateToUserDetails(user.systemName);
    };
}

const module = angular.module(moduleName)
.component("groupDetailsController", { 
        bindings: {
            $transition$: '<'
        },
        controller: GroupDetailsController,
        controllerAs: "vm",
        templateUrl: "./group-details.html"
    });
export default module;
