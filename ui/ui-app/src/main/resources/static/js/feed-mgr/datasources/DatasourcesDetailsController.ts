
import * as angular from 'angular';
import * as _ from "underscore";
import {AccessControlService} from '../../services/AccessControlService';
import { DatasourcesService } from '../services/DatasourcesService';
import {StateService} from '../../services/StateService';
import { Transition } from '@uirouter/core';
import { EntityAccessControlService } from '../shared/entity-access-control/EntityAccessControlService';
const moduleName = require('feed-mgr/datasources/module-name');


const PASSWORD_PLACEHOLDER = "******";

export class DatasourcesDetailsController {

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
    * Angular Materials form for Details view.
    * @type {Object}
    */
    datasourceDetailsForm: any = {};
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

    $transition$: Transition;
    allowDelete: boolean = true;
    /**
     * Placeholder for editing a password.
     * @type {string}
     */
    static readonly $inject = ["$scope", "$mdDialog", "$mdToast", "$q", "AccessControlService",
        "DatasourcesService", "EntityAccessControlService", "StateService"]
    /**
     * Manages the Data Sources Details page for creating and editing data sources.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param {Object} $mdDialog the dialog service
     * @param {Object} $mdToast the toast notification service
     * @param {Object} $q the promise service
     * @param {Object} $transition$ the URL parameters
     * @param {AccessControlService} AccessControlService the access control service
     * @param {DatasourcesService} DatasourcesService the data sources service
     * @param {EntityAccessControlService} EntityAccessControlService the entity access control service
     * @param {StateService} StateService the page state service
     */
    constructor(private $scope: IScope, private $mdDialog: angular.material.IDialogService, private $mdToast: angular.material.IToastService, private $q: angular.IQService
        , private accessControlService: AccessControlService, private datasourcesService: DatasourcesService, private entityAccessControlService: EntityAccessControlService
        , private stateService: StateService) {

        this.model = this.datasourcesService.newJdbcDatasource();
        // Load the data source
        if (angular.isString(this.$transition$.params().datasourceId)) {
            this.datasourcesService.findById(this.$transition$.params().datasourceId)
                .then((model: any) => {
                    this.model = model;
                    this.loading = false;
                    if (this.model.controllerServiceId) {
                        //see if we can find the references and show them
                        this.datasourcesService.findControllerServiceReferences(this.model.controllerServiceId).then((references: any) => {
                            this.model.references = references;
                        });
                    }

                    $q.when(accessControlService.hasPermission(AccessControlService.DATASOURCE_EDIT, this.model, AccessControlService.ENTITY_ACCESS.DATASOURCE.EDIT_DETAILS))
                        .then((access: any) => {
                            this.allowEdit = access;
                        });
                    $q.when(accessControlService.hasPermission(AccessControlService.DATASOURCE_EDIT, this.model, AccessControlService.ENTITY_ACCESS.DATASOURCE.DELETE_DATASOURCE))
                        .then((access: any) => {
                            this.allowDelete = access;
                        });
                    $q.when(accessControlService.hasPermission(AccessControlService.DATASOURCE_EDIT, this.model, AccessControlService.ENTITY_ACCESS.DATASOURCE.CHANGE_DATASOURCE_PERMISSIONS))
                        .then((access: any) => {
                            this.allowChangePermissions = access;
                        });
                }, (error:any) => {
                    this.stateService.FeedManager().Datasource().navigateToDatasources();
                });
        } else {
            this.onEdit();
            this.isDetailsEditable = true;
            this.loading = false;
        }

    }

    /**
     * Shows the icon picker dialog.
     */
    showIconPicker = () => {
        this.$mdDialog.show({
            controller: "IconPickerDialog",
            templateUrl: "js/common/icon-picker-dialog/icon-picker-dialog.html",
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            fullscreen: true,
            locals: {
                iconModel: this.editModel
            }
        }).then((msg: any) => {
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
            this.stateService.FeedManager().Datasource().navigateToDatasources();
        }
    };

    /**
     * Deletes the current data source.
     */
    onDelete = () => {
        if (!angular.isArray(this.model.sourceForFeeds) || this.model.sourceForFeeds.length === 0) {
            this.datasourcesService.deleteById(this.model.id)
                .then(() => {
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent("Successfully deleted the data source " + this.model.name + ".")
                            .hideDelay(3000)
                    );
                    this.stateService.FeedManager().Datasource().navigateToDatasources();
                }, (err: any) => {
                    this.$mdDialog.show(
                        this.$mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title("Delete Failed")
                            .textContent("The data source '" + this.model.name + "' could not be deleted." + err.data.message)
                            .ariaLabel("Failed to delete data source")
                            .ok("Got it!")
                    );
                });
        } else {
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title("Delete Failed")
                    .textContent("This data source is currently being used by " + this.model.sourceForFeeds.length + " feed(s).")
                    .ariaLabel("Failed to delete data source")
                    .ok("Got it!")
            );
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
        this.$mdDialog.show({
            template: '<save-datasource-dialog-controller></save-datasource-dialog-controller>',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            fullscreen: true,
            locals: {
                datasourceName: this.model.name
            }
        });

        // Save the changes
        this.saveModel(model)
            .catch(() => {
                this.$mdDialog.hide()
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
                this.$mdDialog.hide()
                this.$mdToast.show(
                    this.$mdToast.simple()
                        .textContent('Saved the data source ' + this.model.name)
                        .hideDelay(3000)
                );
                return savedModel;
            }, (err: any) => {
                this.isDetailsEditable = true;
                this.$mdDialog.hide()
                this.$mdDialog.show(
                    this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Save Failed")
                        .textContent("The data source '" + model.name + "' could not be saved. " + err.data.message)
                        .ariaLabel("Failed to save data source")
                        .ok("Got it!")
                );
                return err;
            });
    };

    /**
     * Validates the edit form.
     */
    validate = () => {
        if (angular.isDefined(this.datasourceDetailsForm["datasourceName"])) {
            let isNew = angular.isUndefined(this.model) || angular.isUndefined(this.model.id);
            let unique = true;
            if (isNew || (!isNew && this.model.name.toLowerCase() != this.editModel.name.toLowerCase())) {
                unique = angular.isUndefined(this.existingDatasourceNames[this.editModel.name.toLowerCase()]);
            }
            this.datasourceDetailsForm["datasourceName"].$setValidity("notUnique", unique);

        }
    };

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

    /**
     * Watch for changes on the datasource name
     */
    onDatasourceNameChange() {
        if (_.isEmpty(this.existingDatasourceNames)) {
            this.datasourcesService.findAll()
                .then((datasources: any) => {
                    this.existingDatasourceNames = {};
                    angular.forEach(datasources, (datasource) => {
                        this.existingDatasourceNames[datasource.name.toLowerCase()] = true;
                    });
                })
                .then(this.validate);
        } else {
            this.validate();
        }
    }


}


/**
 * The Controller used for the abandon all
 */
var saveDatasourceDialogController = ($scope: any, $mdDialog: any, $interval: any, datasourceName: any) => {
    // 


};

export class SaveDatasourceDialogController {
    messageInterval: any = null;
    counter: number = 0;
    index: number = 0;
    messages: any = [];

    static readonly $inject = ["$scope", "$mdDialog", "$interval", "datasourceName"];
    constructor(private $scope: IScope, private $mdDialog: angular.material.IDialogService,
        private $interval: angular.IIntervalService, private datasourceName: any) {


        this.$scope.datasourceName = datasourceName;
        this.$scope.message = "Saving the data source " + datasourceName;

        this.messages.push("Hang tight. Still working.");
        this.messages.push("Just a little while longer.");
        this.messages.push("Saving the data source.");

        this.messageInterval = this.$interval(() => {
            this.updateMessage();

        }, 3000);
        $scope.hide = () => {
            this.cancelMessageInterval();
            $mdDialog.hide();

        };

        $scope.cancel = () => {
            this.cancelMessageInterval();
            $mdDialog.cancel();
        };

    }
    updateMessage = () => {
        this.index++;
        var len = this.messages.length;
        if (this.index == len) {
            this.index = 0;
        }
        this.$scope.message = this.messages[this.index];

    }
    cancelMessageInterval = () => {
        if (this.messageInterval != null) {
            this.$interval.cancel(this.messageInterval);
        }
    }
}

angular.module(moduleName).component('saveDatasourceDialogController', {
    controller: SaveDatasourceDialogController,
    templateUrl: 'js/feed-mgr/datasources/datasource-saving-dialog.html',
});


angular.module(moduleName).component("datasourcesDetailsController", {
    bindings: {
        $transition$: '<'
    },
    templateUrl: "js/feed-mgr/datasources/details.html",
    controller: DatasourcesDetailsController,
    controllerAs: "vm"
});
