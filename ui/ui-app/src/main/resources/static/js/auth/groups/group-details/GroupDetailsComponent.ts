import * as _ from 'underscore';
import {StateService as StateServices} from "../../../services/StateService";
import {UserService} from "../../services/UserService";
import {AccessControlService} from "../../../services/AccessControlService";
import AccessConstants from "../../../constants/AccessConstants";
import { Component, ViewContainerRef, Inject } from '@angular/core';
import { StateService as UiStateService } from '@uirouter/core';
import { TdDialogService } from '@covalent/core/dialogs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ObjectUtils } from '../../../../lib/common/utils/object-utils';
import { CloneUtil } from '../../../common/utils/clone-util';
import { TranslateService } from '@ngx-translate/core';
import {LoadingDialogService} from "../../../common/loading-dialog/loading-dialog";

@Component({
    templateUrl: "./group-details.html",
    selector: 'groups-Table',
    styles: [' .block { display : block; margin: 18px;}']
})
export class GroupDetailsComponent {
    /**
     * Indicates that admin operations are allowed.
     * @type {boolean}
     */
    allowAdmin: boolean = false;
    /**
    * User model for the edit view.
    * @type {UserPrincipal}
    */
    editModel: any = {};
    /**
     * Map of group system names to group objects.
     * @type {Object.<string, GroupPrincipal>}
     */
    groupMap: any = {};
    /**
     * Indicates if the edit view is displayed.
     * @type {boolean}
     */
    isEditable: boolean = false;
    groupId:any;
    /**
    * Indicates that the user is currently being loaded.
    * @type {boolean}
    */
    loading: boolean = true;
    /**
     * User model for the read-only view.
     * @type {UserPrincipal}
     */
    model: any = { description: null, memberCount: 0, systemName: null, title: null };
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

    ngOnInit() { 
        this.onLoad();
    }
    
    constructor(
        private accessControlService: AccessControlService,
        private UserService: UserService,
        private stateService: UiStateService,
        private _dialogService: TdDialogService,
        private _viewContainerRef: ViewContainerRef,
        private snackBar: MatSnackBar,
        private translate: TranslateService,
        @Inject("LoadingDialogService") private loadingDialog:LoadingDialogService,
        private statesService: StateServices) {}
    /**
     * Gets the display name of the specified user. Defaults to the system name if the display name is blank.
     * @param user the user
     * @returns {string} the display name
     */
    getUserName = (user: any) => {
        return (ObjectUtils.isString(user.displayName) && user.displayName.length > 0) ? user.displayName : user.systemName;
    };
    /**
     * Indicates if the user can be deleted. The main requirement is that the user exists.
     *
     * @returns {boolean} {@code true} if the user can be deleted, or {@code false} otherwise
     */
    canDelete = () => {
        return (!this.deleting && this.model.systemName !== null);
    };
    isgroupNameEmpty = () => {
        return !ObjectUtils.isString(this.editModel.systemName) || this.editModel.systemName.length === 0;
    }
    isgroupNameDuplicate = () => {
        return ObjectUtils.isString(this.editModel.systemName) && this.groupMap[this.editModel.systemName];
    }
    isFormValid = () => {
        return (!this.isgroupNameDuplicate() && !this.isgroupNameEmpty());
    }
    /**
     * Cancels the current edit operation. If a new user is being created then redirects to the users page.
     */
    onCancel = () => {
        if (this.model.systemName === null) {
            this.statesService.Auth.navigateToGroups();
        }
    };
        testLoading(){
            this.loadingDialog.showDialog();
        }
    /**
     * Deletes the current group.
     */
    onDelete () {
            this.deleting = true;
            this.loadingDialog.showDialog();
        var name = (ObjectUtils.isString(this.model.title) && this.model.title.length > 0) ? this.model.title : this.model.systemName;
        this.UserService.deleteGroup(this.model.systemName)
            .then(() => {
                        this.deleting = false;
                        this.loadingDialog.hideDialog()
                this.snackBar.open(this.translate.instant("views.common.delete.success",{entity:"the group"}) + name,this.translate.instant("views.common.ok"),{
                    duration : 3000
                });
                this.statesService.Auth.navigateToGroups();
            }, () => {
                        this.deleting = false
                        this.loadingDialog.hideDialog()
                this._dialogService.openAlert({
                    message: this.translate.instant('views.common.delete.failure',{entity : name}),
                    viewContainerRef: this._viewContainerRef,
                    width: '300 px',
                    title: this.translate.instant('views.common.delete.failure.title'),
                    closeButton: this.translate.instant('views.common.dialog.gotIt'),
                    ariaLabel: this.translate.instant('views.common.delete.failure',{entity:"group"}),
                    closeOnNavigation: true,
                    disableClose: false
                });
            });
    };
    /**
    //  * Creates a copy of the user model for editing.
     */
    onEdit () {
        this.editModel = CloneUtil.deepCopy(this.model);
    };

    onCancelEditPermissions () {
        this.editActions = [];
    }
    /**
        * Creates a copy of the permissions for editing.
        */
    onEditPermissions () {
        this.editActions = CloneUtil.deepCopy(this.actions);
    };
    /**
     * Loads the user details.
     */
    onLoad () {
        this.groupId = this.stateService.params.groupId;
        // Load allowed permissions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowAdmin = this.accessControlService.hasAction(AccessConstants.GROUP_ADMIN, actionSet.actions);
                this.allowUsers = this.accessControlService.hasAction(AccessConstants.USERS_ACCESS, actionSet.actions);
            });

        // Fetch group details
        if (ObjectUtils.isString(this.stateService.params.groupId)) {
            this.UserService.getGroup(this.stateService.params.groupId)
                .then((group: any) => {
                    this.model = group;
                    this.loading = false; // _this
                });
            this.UserService.getUsersByGroup(this.stateService.params.groupId)
                .then((users: any) => {
                    this.users = users;
                });
            this.accessControlService.getAllowedActions(null, null, this.stateService.params.groupId)
                .then((actionSet: any) => {
                    this.actions = actionSet.actions;
                });
        } else {
            this.onEdit();
            this.isEditable = true;
            this.loading = false;
            this.UserService.getGroups().then((groups: any) => {
                this.groupMap = {};
                _.each(groups, (group: any) => {
                    this.groupMap[group.systemName] = true;
                });
            });
        }
    };
    /**
     * Saves the current group.
     */
    onSave () {
        var model = CloneUtil.deepCopy(this.editModel);
        this.UserService.saveGroup(model)
                .then((updated: any) => {
                    this.model = updated;
                // this.groupId = this.model.systemName;
            });
    };
    /**
    * Saves the current permissions.
    */
    onSavePermissions () {
        var actions = CloneUtil.deepCopy(this.editActions);
        this.accessControlService.setAllowedActions(null, null, this.model.systemName, actions)
            .then((actionSet: any) => {
                this.actions = actionSet.actions;
            });
    };
    /**
     * Navigates to the details page for the specified user.
     *
     * @param user the user
     */
    onUserClick(user: any) {
        this.statesService.Auth.navigateToUserDetails(user.systemName);
    };
}
