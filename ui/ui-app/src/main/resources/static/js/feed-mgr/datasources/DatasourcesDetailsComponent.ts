
import * as angular from 'angular';
import * as _ from "underscore";
import AccessControlService from '../../services/AccessControlService';
import { DatasourcesService } from '../services/DatasourcesService';
import { Transition, StateService } from '@uirouter/core';
import { EntityAccessControlService } from '../shared/entity-access-control/EntityAccessControlService';
import { Component, ViewContainerRef, Inject } from '@angular/core';
import { TdDialogService } from '@covalent/core/dialogs';
import IconPickerDialog from '../../common/icon-picker-dialog/icon-picker-dialog';
import { MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/interval';



const PASSWORD_PLACEHOLDER = "******";
@Component({
    templateUrl: "js/feed-mgr/datasources/details.html",
    styles: [' .block { display : block; margin: 18px;}']
})
export class DatasourcesDetailsComponent {

    /**
    * Indicates that changing permissions is allowed.
    * @type {boolean}
    */
    allowChangePermissions: boolean = false;
    /**
    * Indicates that edit operations are allowed.
    * @type {boolean}
    */
    allowEdit: boolean = false;
    /**
    * Angular Materials form for Access Control view.
    * @type {Object}
    */
    datasourceAccessControlForm: any = {};
    /**
    * The set of existing data source names.
    * @type {Object.<string, boolean>}
    */
    existingDatasourceNames: any = {};
    /**
    * Indicates if the data source is currently being loaded.
    * @type {boolean} {@code true} if the data source is being loaded, or {@code false} if it has finished loading
    */
    loading: boolean = true;
    /**
    * Datasource model for the edit view.
    * @type {JdbcDatasource}
    */
    editModel: any = {};
    /**
    * Indicates if the Access Control edit view is displayed.
    * @type {boolean}
    */
    isAccessControlEditable: boolean = false;
    /**
    * Indicates if the Details edit view is displayed.
    * @type {boolean}
    */
    isDetailsEditable: boolean = false;
    /**
    * Data source model for the read-only view.
    * @type {JdbcDatasource}
    */
    model: any;
    /**
    * Result which is returned from server when user tests datasource connection
    * @type {Object}
    */
    testConnectionResult: any = {};

    allowDelete: boolean = true;
    gettingDataSources : boolean = false;
    ngOnInit() {
        // Load the data source
        if (angular.isString(this.stateService.params.datasourceId)) {
            this.datasourcesService.findById(this.stateService.params.datasourceId)
                .then((model: any) => {
                    this.model = model;
                    this.loading = false;
                    if (this.model.controllerServiceId) {
                        //see if we can find the references and show them
                        this.datasourcesService.findControllerServiceReferences(this.model.controllerServiceId).then((references: any) => {
                            this.model.references = references;
                        });
                    }

                    Promise.resolve(this.accessControlService.hasPermission(AccessControlService.DATASOURCE_EDIT, this.model, AccessControlService.ENTITY_ACCESS.DATASOURCE.EDIT_DETAILS))
                        .then((access: any) => {
                            this.allowEdit = access;
                        });
                    Promise.resolve(this.accessControlService.hasPermission(AccessControlService.DATASOURCE_EDIT, this.model, AccessControlService.ENTITY_ACCESS.DATASOURCE.DELETE_DATASOURCE))
                        .then((access: any) => {
                            this.allowDelete = access;
                        });
                    Promise.resolve(this.accessControlService.hasPermission(AccessControlService.DATASOURCE_EDIT, this.model, AccessControlService.ENTITY_ACCESS.DATASOURCE.CHANGE_DATASOURCE_PERMISSIONS))
                        .then((access: any) => {
                            this.allowChangePermissions = access;
                        });
                }, () => {
                    this.stateService.go("datasources");
                });
        } else {
            this.onEdit();
            this.isDetailsEditable = true;
            this.loading = false;
        }
    }
    /**
     * Manages the Data Sources Details page for creating and editing data sources.
     *
     * @param {AccessControlService} AccessControlService the access control service
     * @param {DatasourcesService} DatasourcesService the data sources service
     * @param {EntityAccessControlService} EntityAccessControlService the entity access control service
     * @param {StateService} StateService the page state service 
     */
    constructor(private accessControlService: AccessControlService, private datasourcesService: DatasourcesService, private entityAccessControlService: EntityAccessControlService
        , private stateService: StateService,private _dialogService: TdDialogService,private _viewContainerRef: ViewContainerRef,private snackBar: MatSnackBar) {

        this.model = this.datasourcesService.newJdbcDatasource();
    }

    /**
     * Shows the icon picker dialog.
     */
    showIconPicker = () => {
        this._dialogService.open(IconPickerDialog,{
            viewContainerRef : this._viewContainerRef,
            disableClose : true,
            data : {
                iconModel : this.editModel
            },
            panelClass: "full-screen-dialog"
        }).afterClosed().subscribe((msg : any) => {
            if (msg) {
                this.editModel.icon = msg.icon;
                this.editModel.iconColor = msg.color;
            }
        });
    };

    /**
     * Indicates if the data source is new and has not been saved.
     *
     * @returns {boolean} {@code true} if the data source is new, or {@code false} otherwise
     */
    isNew = () => {
        return (!angular.isString(this.model.id) || this.model.id.length === 0);
    };

    /**
     * Cancels the current edit operation. If a new data source is being created then redirects to the data sources page.
     */
    onCancel = () => {
        if (!angular.isString(this.model.id)) {
            this.stateService.go("datasources");
        }
    };

    /**
     * Deletes the current data source.
     */
    onDelete = () => {
        if (!angular.isArray(this.model.sourceForFeeds) || this.model.sourceForFeeds.length === 0) {
            this.datasourcesService.deleteById(this.model.id)
                .then(() => {
                    this.snackBar.open("Successfully deleted the data source " + this.model.name + ".","OK",{
                        duration : 3000
                    });
                    this.stateService.go("datasources");
                }, (err: any) => {
                    this._dialogService.openAlert({
                        message: "The data source '" + this.model.name + "' could not be deleted." + err.data.message,
                        viewContainerRef: this._viewContainerRef,
                        width: '300 px',
                        title: 'Delete Failed',
                        closeButton: 'Got it!',
                        ariaLabel: "Failed to delete data source",
                        closeOnNavigation: true,
                        disableClose: false
                    });
                });
        } else {
            this._dialogService.openAlert({
                message: "This data source is currently being used by " + this.model.sourceForFeeds.length + " feed(s).",
                viewContainerRef: this._viewContainerRef,
                width: '300 px',
                title: 'Delete Failed',
                closeButton: 'Got it!',
                ariaLabel: "Failed to delete data source",
                closeOnNavigation: true,
                disableClose: false
            });
        }
    };

    /**
     * Creates a copy of the data source model for editing.
     */
    onEdit = () => {
        this.editModel = angular.copy(this.model);

        if (this.isNew()) {
            this.editModel.hasPasswordChanged = true;
        } else {
            this.editModel.password = PASSWORD_PLACEHOLDER;
            this.editModel.hasPasswordChanged = false;
        }
    };

    onPasswordChange = () => {
        this.editModel.hasPasswordChanged = true;
    };

    /**
     * Saves the Access Controls for the current data source.
     */
    onAccessControlSave = () => {
        // Prepare model
        var model = angular.copy(this.model);
        model.roleMemberships = this.editModel.roleMemberships;
        model.owner = this.editModel.owner;
        this.entityAccessControlService.updateRoleMembershipsForSave(model.roleMemberships);

        // Save the changes
        this.datasourcesService.saveRoles(model)
            .then((r: any) => {
                this.entityAccessControlService.mergeRoleAssignments(this.model, EntityAccessControlService.entityRoleTypes.DATASOURCE, this.model.roleMemberships);
            })
            .catch(() => {
                this.isAccessControlEditable = true;
            });
    };

    /**
     * Saves the Details for the current data source.
     */
    onDetailsSave = () => {
        // Prepare model
        var model = _.pick(this.editModel, (value: any, key: any) => {
            return (key !== "owner" && key !== "roleMemberships");
        });

        if (!angular.isString(model.type) || model.type.length === 0) {
            var matches = /^(?:jdbc:)?([^:]+):/.exec(model.databaseConnectionUrl);
            model.type = (matches !== null) ? matches[1] : model.databaseDriverClassName;
        }
        if (!this.isNew() && !this.editModel.hasPasswordChanged) {
            model.password = null;
        }
        this._dialogService.open(SaveDatasourceDialogComponent,{
            viewContainerRef : this._viewContainerRef,
            disableClose : true,
            data : {
                datasourceName: this.model.name
            },
            panelClass: "full-screen-dialog"
        }).afterClosed().subscribe((msg : any) => {
            if (msg) {
                this.editModel.icon = msg.icon;
                this.editModel.iconColor = msg.color;
            }
        });

        // Save the changes
        this.saveModel(model)
            .catch(() => {
                this._dialogService.closeAll();
                this.isDetailsEditable = true;
            });
    };

    /**
     * Saves the specified data source model.
     *
     * @param {JdbcDatasource} model the datasource to be saved
     */
    saveModel = (model: any) => {
        return this.datasourcesService.save(model)
            .then((savedModel: any) => {
                savedModel.owner = this.model.owner;
                savedModel.roleMemberships = this.model.roleMemberships;
                savedModel.references = this.model.references;
                this.model = savedModel;
                this._dialogService.closeAll();
                this.snackBar.open('Saved the data source ' + this.model.name,"OK",{
                    duration : 3000
                });
                return savedModel;
            }, (err: any) => {
                this.isDetailsEditable = true;
                this._dialogService.closeAll();
                this._dialogService.openAlert({
                    message: "The data source '" + model.name + "' could not be saved. " + err.data.message,
                    viewContainerRef: this._viewContainerRef,
                    width: '300 px',
                    title: "Save Failed",
                    closeButton: 'Got it!',
                    ariaLabel: "Failed to save data source",
                    closeOnNavigation: true,
                    disableClose: false
                });
                return err;
            });
    };

    /**
     * Validates the edit form.
     */
    validate = () => {
        if (angular.isDefined(this.editModel.name) && !this.gettingDataSources && this.editModel.name != '') {
            let isNew = angular.isUndefined(this.model) || angular.isUndefined(this.model.id);
            let unique = true;
            if (isNew || (!isNew && this.model.name.toLowerCase() != this.editModel.name.toLowerCase())) {
                unique = angular.isUndefined(this.existingDatasourceNames[this.editModel.name.toLowerCase()]);
            }
            return unique;
        }else {
            return true;
        }
    };

    isDataSourceNameEmpty = () => {
        return !angular.isString(this.editModel.name) || this.editModel.name.length === 0;
    }

    isDataSourceNameDuplicate = () => {
        if (!this.gettingDataSources && _.isEmpty(this.existingDatasourceNames)){
            this.gettingDataSources = true;
            this.datasourcesService.findAll()
                .then((datasources: any) => {
                    this.existingDatasourceNames = {};
                    angular.forEach(datasources, (datasource) => {
                        this.existingDatasourceNames[datasource.name.toLowerCase()] = true;
                    });
                }).then( () => this.gettingDataSources = false);
            return false;
        }else{
           return !this.validate();
        }
    }

    testConnection = () => {

        var model = _.pick(this.editModel, function (value:any, key:any) {
            return (key !== "owner" && key !== "roleMemberships");
        });

        this.testConnectionResult = {
        };
        this.datasourcesService.testConnection(model).then((response: any) => {
            const isConnectionOk = response.message === undefined;
            const msg = isConnectionOk ? "" : response.message;
            this.testConnectionResult = {
                msg: msg,
                status: isConnectionOk
            };
        });
    };

}

@Component({
    templateUrl: 'js/feed-mgr/datasources/datasource-saving-dialog.html',
})
export class SaveDatasourceDialogComponent {
    messageInterval: any = null;
    counter: number = 0;
    index: number = 0;
    messages: any = [];
    message : string ;
    dataSourceName : string = "";

    ngOnInit() {
        this.dataSourceName = this.data.datasourceName;
    }

    constructor(private dialogRef: MatDialogRef<SaveDatasourceDialogComponent>,
        @Inject(MAT_DIALOG_DATA) private data: any ) {


        this.message = "Saving the data source " + this.dataSourceName;

        this.messages.push("Hang tight. Still working.");
        this.messages.push("Just a little while longer.");
        this.messages.push("Saving the data source.");

        this.messageInterval = Observable.interval(3000).subscribe(() => {
            this.updateMessage();
          });
        // this.dialogRef.close();
        // this.hide = () => {

        // }
        // $scope.hide = () => {
        //     this.cancelMessageInterval();
        //     $mdDialog.hide();

        // };

        // $scope.cancel = () => {
        //     this.cancelMessageInterval();
        //     $mdDialog.cancel();
        // };

    }
    updateMessage = () => {
        this.index++;
        var len = this.messages.length;
        if (this.index == len) {
            this.index = 0;
        }
        this.message = this.messages[this.index];

    }
    cancelMessageInterval = () => {
        if (this.messageInterval != null) {
            this.messageInterval.cancel(this.messageInterval);
        }
    }
}
