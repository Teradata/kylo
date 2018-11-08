import * as _ from 'underscore';
import UserService from "../../services/UserService";
import AccessControlService from "../../../services/AccessControlService";
import AccessConstants from "../../../constants/AccessConstants";
import { StateService } from "@uirouter/core";
import { Component } from '@angular/core';
import { ViewContainerRef } from '@angular/core';
import { TdDialogService } from '@covalent/core/dialogs';
import {FormControl, Validators, FormGroupDirective, NgForm} from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ObjectUtils } from '../../../common/utils/object-utils';
import { CloneUtil } from '../../../common/utils/clone-util';



@Component({
    templateUrl: "js/auth/users/user-details/user-details.html",
    selector: 'user-Details',
    styles : [' .block { display : block; margin: 18px;}']
})
export default class UserDetailsComponent {

    $error: any = { duplicateUser: false, missingGroup: false, missingUser: false };

    /**
     * Indicates that admin operations are allowed.
     * @type {boolean}
     */
    allowAdmin: any = false;

    /**
     * User model for the edit view.
     * @type {UserPrincipal}
     */
    editModel: any = {};

    /**
     * List of group system names.
     * @type {Array.<string>}
     */
    groupList: any[] = [];

    /**
     * Map of group system names to group objects.
     * @type {Object.<string, GroupPrincipal>}
     */
    groupMap: any = {};

    /**
     * Autocomplete search text for group input.
     * @type {string}
     */
    groupSearchText: any = "";

    /**
     * Indicates if the edit view is displayed.
     * @type {boolean}
     */
    isEditable: any = false;

    /**
     * Indicates if the edit form is valid.
     * @type {boolean}
     */
    isValid: any = true;

    /**
     * Indicates that the user is currently being loaded.
     * @type {boolean}
     */
    loading: any = true;

    /**
     * User model for the read-only view.
     * @type {UserPrincipal}
     */
    model: any = { displayName: null, email: null, enabled: true, groups: [], systemName: null };

    /**
     * Lookup map for detecting duplicate user names.
     * @type {Object.<string, boolean>}
     */
    userMap: any = {};

    emailFormControl = new FormControl('', [
        Validators.email,
      ]);


    enableEdit : boolean = false;
    constructor(
        private accessControlService: AccessControlService,
        private UserService: UserService,
        private stateService: StateService,
        private _dialogService: TdDialogService,
        private _viewContainerRef: ViewContainerRef,
        private snackBar: MatSnackBar
    ) {
        this.onLoad();
      }
    /**
         * Indicates if the user can be deleted. The main requirement is that the user exists.
         *
         * @returns {boolean} {@code true} if the user can be deleted, or {@code false} otherwise
     */
    canDelete () {
        return (this.model.systemName !== null);
    };
    /**
        * Finds the substring of the title for the specified group that matches the query term.
        *
        * @param group the group
        * @returns {string} the group title substring
    */
    findGroupSearchText (group: any) {
        var safeQuery = this.groupSearchText.toLocaleUpperCase();
        if (ObjectUtils.isString(this.groupMap[group].title)) {
            var titleIndex = this.groupMap[group].title.toLocaleUpperCase().indexOf(safeQuery);
            return (titleIndex > -1) ? this.groupMap[group].title.substr(titleIndex, safeQuery.length) : this.groupSearchText;
        } else {
            var nameIndex = group.toLocaleUpperCase().indexOf(safeQuery);
            return (nameIndex > -1) ? group.substr(nameIndex, safeQuery.length) : this.groupSearchText;
        }
    };

    isUserNameEmpty () {
       return !ObjectUtils.isString(this.editModel.systemName) || this.editModel.systemName.length === 0;
    }

    isUserNameDuplicate () {
        return ObjectUtils.isString(this.editModel.systemName) && this.userMap[this.editModel.systemName];
    }

    isMissingGroup () {
        return ObjectUtils.isUndefined(this.editModel.groups) || this.editModel.groups.length === 0;
    }

    isFormValid () {
        return !this.isUserNameDuplicate() && !this.isUserNameEmpty() && !this.isMissingGroup() && (this.editModel.email === null || this.editModel.email === "" || !this.emailFormControl.hasError('email'));
    }

    /**
     * Gets the title for the specified group.
     *
     * @param group the group
     * @returns {string} the group title
     */
    getGroupTitle (group: any) {
        if (ObjectUtils.isDefined(this.groupMap[group]) && ObjectUtils.isString(this.groupMap[group].title)) {
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
    getGroupTitles () {
        return _.map(this.model.groups, (group :any) => {this.getGroupTitle(group)});
    };

    /**
     * Cancels the current edit operation. If a new user is being created then redirects to the users page.
     */
    onCancel ()  {
        this.enableEdit = false;
        if (this.model.systemName === null) {
            this.stateService.go("users");
        }
    };

    /**
     * Deletes the current user.
     */
    onDelete () {
        var name = (ObjectUtils.isString(this.model.displayName) && this.model.displayName.length > 0) ? this.model.displayName : this.model.systemName;
        this.UserService.deleteUser(encodeURIComponent(this.model.systemName))
            .then(() => {
                this.snackBar.open("Successfully deleted the group " + name,"OK",{
                    duration : 3000
                });
                    this.stateService.go("users");
            }, () => {
                this._dialogService.openAlert({
                    message: "The user " + name + "could not be deleted. ",
                    viewContainerRef: this._viewContainerRef,
                    width: '300 px',
                    title: 'Delete Failed',
                    closeButton: 'Got it!',
                    ariaLabel: "Failed to delete user",
                    closeOnNavigation: true,
                    disableClose: false
                });
            });


    };

    /**
    //  * Creates a copy of the user model for editing.
     */
    onEdit () {
        this.enableEdit = true;
        this.editModel = CloneUtil.deepCopy(this.model);
    };

    /**
     * Loads the user details.
     */
    onLoad () {
        // Get the list of groups
        this.UserService.getGroups()
            .then((groups: any) => {
                this.groupList = [];
                this.groupMap = {};
                _.forEach(groups, (group: any) => {
                    this.groupList.push(group.systemName);
                    this.groupMap[group.systemName] = group;
                });
            });

        // Load allowed permissions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowAdmin = this.accessControlService.hasAction(AccessConstants.USERS_ADMIN, actionSet.actions);
            });

        // Load the user details
        if (ObjectUtils.isString(this.stateService.params.userId)) {
            this.UserService.getUser(this.stateService.params.userId)
                .then((user: any) => {
                    this.model = user;
                    this.loading = false;
                });

        } else {
            this.onEdit();
            this.isEditable = true;
            this.loading = false;

            this.UserService.getUsers()
                .then((users: any) => {
                    this.userMap = {};
                    _.each(users, (user: any) => {
                        this.userMap[user.systemName] = true;
                    });
                });
        }
    };



    /**
     * Saves the current user.
     */
    onSave () {
        var model = CloneUtil.deepCopy(this.editModel);
        this.UserService.saveUser(model)
            .then(() => {
                this.model = model;
            }).catch((error) => {
                this.model = this.editModel;
            });
    };

    addGroup ($event : any) {
        var groupName = $event.option.value;
        var groupsArray = this.editModel.groups;
        groupsArray.push(groupName);
        this.editModel.groups = groupsArray;
    }

    removeGroup (value : string ) {
        this.editModel.groups = this.editModel.groups.filter((group : any) => {
            return group !== value;
        });
    }


    /**
     * Filters the list of groups to those matching the specified query.
     *
     * @param {string} query the query string
     * @returns {Array.<string>} the list of matching groups
     */
    queryGroups (query: any) {
        var safeQuery = query.toLocaleUpperCase();
        return this.groupList
            // Filter groups that are already selected
            .filter((group: any) => {
                return (this.editModel.groups.indexOf(group) === -1);
            })
            // Find position of query term
            .map((group) => {
                var nameIndex = group.toLocaleUpperCase().indexOf(safeQuery);
                var titleIndex = ObjectUtils.isString(this.groupMap[group].title) ? this.groupMap[group].title.toLocaleUpperCase().indexOf(safeQuery) : -1;
                var index = (titleIndex > -1 && (nameIndex === -1 || nameIndex > titleIndex)) ? titleIndex : nameIndex;
                return { name: group, index: index };
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