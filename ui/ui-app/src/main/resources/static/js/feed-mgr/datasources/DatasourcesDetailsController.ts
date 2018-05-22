
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/datasources/module-name');


const PASSWORD_PLACEHOLDER = "******";

export class DatasourcesDetailsController {


    allowChangePermissions:any;
    allowEdit:any;
    datasourceAccessControlForm:any;
    datasourceDetailsForm:any;
    loading:any;
    editModel:any;
    isAccessControlEditable:any;
    isDetailsEditable:any;
    model:any;
    showIconPicker:any;
    isNew:any;
    onCancel:any;
    onDelete:any;
    onEdit:any;
    onPasswordChange:any;
    onAccessControlSave:any;
    onDetailsSave:any;
    saveModel:any;
    validate:any;
    testConnection:any;
    testConnectionResult:any;
    existingDatasourceNames:any;
    allowDelete:any;
    /**
     * Placeholder for editing a password.
     * @type {string}
     */

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
    constructor(private $scope:any, private $mdDialog:any, private $mdToast:any, private $q:any, private $transition$:any
        , private AccessControlService:any, private DatasourcesService:any, private EntityAccessControlService:any
        , private StateService:any) {
        var self = this;

        /**
         * Indicates that changing permissions is allowed.
         * @type {boolean}
         */
        self.allowChangePermissions = false;

        /**
         * Indicates that edit operations are allowed.
         * @type {boolean}
         */
        self.allowEdit = false;

        /**
         * Angular Materials form for Access Control view.
         * @type {Object}
         */
        self.datasourceAccessControlForm = {};

        /**
         * Angular Materials form for Details view.
         * @type {Object}
         */
        self.datasourceDetailsForm = {};

        /**
         * The set of existing data source names.
         * @type {Object.<string, boolean>}
         */
        self.existingDatasourceNames = {};

        /**
         * Indicates if the data source is currently being loaded.
         * @type {boolean} {@code true} if the data source is being loaded, or {@code false} if it has finished loading
         */
        self.loading = true;

        /**
         * Datasource model for the edit view.
         * @type {JdbcDatasource}
         */
        self.editModel = {};

        /**
         * Indicates if the Access Control edit view is displayed.
         * @type {boolean}
         */
        self.isAccessControlEditable = false;

        /**
         * Indicates if the Details edit view is displayed.
         * @type {boolean}
         */
        self.isDetailsEditable = false;

        /**
         * Data source model for the read-only view.
         * @type {JdbcDatasource}
         */
        self.model = DatasourcesService.newJdbcDatasource();

        /**
         * Result which is returned from server when user tests datasource connection
         * @type {Object}
         */
        self.testConnectionResult = {};

        /**
         * Shows the icon picker dialog.
         */
        self.showIconPicker = function () {
            $mdDialog.show({
                controller: "IconPickerDialog",
                templateUrl: "js/common/icon-picker-dialog/icon-picker-dialog.html",
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    iconModel: self.editModel
                }
            }).then(function (msg:any) {
                if (msg) {
                    self.editModel.icon = msg.icon;
                    self.editModel.iconColor = msg.color;
                }
            });
        };

        /**
         * Indicates if the data source is new and has not been saved.
         *
         * @returns {boolean} {@code true} if the data source is new, or {@code false} otherwise
         */
        self.isNew = function () {
            return (!angular.isString(self.model.id) || self.model.id.length === 0);
        };

        /**
         * Cancels the current edit operation. If a new data source is being created then redirects to the data sources page.
         */
        self.onCancel = function () {
            if (!angular.isString(self.model.id)) {
                StateService.FeedManager().Datasource().navigateToDatasources();
            }
        };

        /**
         * Deletes the current data source.
         */
        self.onDelete = function () {
            if (!angular.isArray(self.model.sourceForFeeds) || self.model.sourceForFeeds.length === 0) {
                DatasourcesService.deleteById(self.model.id)
                    .then(function () {
                        $mdToast.show(
                            $mdToast.simple()
                                .textContent("Successfully deleted the data source " + self.model.name + ".")
                                .hideDelay(3000)
                        );
                        StateService.FeedManager().Datasource().navigateToDatasources();
                    }, function (err:any) {
                        $mdDialog.show(
                            $mdDialog.alert()
                                .clickOutsideToClose(true)
                                .title("Delete Failed")
                                .textContent("The data source '" + self.model.name + "' could not be deleted." + err.data.message)
                                .ariaLabel("Failed to delete data source")
                                .ok("Got it!")
                        );
                    });
            } else {
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Delete Failed")
                        .textContent("This data source is currently being used by " + self.model.sourceForFeeds.length + " feed(s).")
                        .ariaLabel("Failed to delete data source")
                        .ok("Got it!")
                );
            }
        };

        /**
         * Creates a copy of the data source model for editing.
         */
        self.onEdit = function () {
            self.editModel = angular.copy(self.model);

            if (self.isNew()) {
                self.editModel.hasPasswordChanged = true;
            } else {
                self.editModel.password = PASSWORD_PLACEHOLDER;
                self.editModel.hasPasswordChanged = false;
            }
        };

        self.onPasswordChange = function () {
            self.editModel.hasPasswordChanged = true;
        };

        /**
         * Saves the Access Controls for the current data source.
         */
        self.onAccessControlSave = function () {
            // Prepare model
            var model = angular.copy(self.model);
            model.roleMemberships = self.editModel.roleMemberships;
            model.owner = self.editModel.owner;
            EntityAccessControlService.updateRoleMembershipsForSave(model.roleMemberships);

            // Save the changes
            DatasourcesService.saveRoles(model)
                .then(function (r:any) {
                    EntityAccessControlService.mergeRoleAssignments(self.model, EntityAccessControlService.entityRoleTypes.DATASOURCE, self.model.roleMemberships);
                })
                .catch(function () {
                    self.isAccessControlEditable = true;
                });
        };

        /**
         * Saves the Details for the current data source.
         */
        self.onDetailsSave = function () {
            // Prepare model
            var model = _.pick(self.editModel, function (value:any, key:any) {
                    return (key !== "owner" && key !== "roleMemberships");
                });

            if (!angular.isString(model.type) || model.type.length === 0) {
                var matches = /^(?:jdbc:)?([^:]+):/.exec(model.databaseConnectionUrl);
                model.type = (matches !== null) ? matches[1] : model.databaseDriverClassName;
            }
            if (!self.isNew() && !self.editModel.hasPasswordChanged) {
                model.password = null;
            }
            $mdDialog.show({
                controller:"SaveDatasourceDialogController",
                templateUrl: 'js/feed-mgr/datasources/datasource-saving-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    datasourceName: self.model.name
                }
            });

            // Save the changes
            self.saveModel(model)
                .catch(function () {
                    $mdDialog.hide()
                    self.isDetailsEditable = true;
                });
        };

        /**
         * Saves the specified data source model.
         *
         * @param {JdbcDatasource} model the datasource to be saved
         */
        self.saveModel = function (model:any) {
            return DatasourcesService.save(model)
                .then(function (savedModel:any) {
                    savedModel.owner = self.model.owner;
                    savedModel.roleMemberships = self.model.roleMemberships;
                    savedModel.references = self.model.references;
                    self.model = savedModel;
                    $mdDialog.hide()
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Saved the data source ' + self.model.name)
                            .hideDelay(3000)
                    );
                    return savedModel;
                }, function (err:any) {
                    self.isDetailsEditable = true;
                    $mdDialog.hide()
                    $mdDialog.show(
                        $mdDialog.alert()
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
        self.validate = function () {
            if (angular.isDefined(self.datasourceDetailsForm["datasourceName"])) {
                let isNew = angular.isUndefined(self.model) || angular.isUndefined(self.model.id);
                let unique = true;
                if(isNew|| (!isNew && self.model.name.toLowerCase() != self.editModel.name.toLowerCase())) {
                    unique = angular.isUndefined(self.existingDatasourceNames[self.editModel.name.toLowerCase()]);
                }
                self.datasourceDetailsForm["datasourceName"].$setValidity("notUnique", unique);

            }
        };

        self.testConnection = function() {

            var model = _.pick(self.editModel, function (value:any, key:any) {
                return (key !== "owner" && key !== "roleMemberships");
            });

            self.testConnectionResult = {
            };
            DatasourcesService.testConnection(model).then(function(response: any) {
                const isConnectionOk = response.message === undefined;
                const msg = isConnectionOk ? "" : response.message;
                self.testConnectionResult = {
                    msg: msg,
                    status: isConnectionOk
                };
            });
        };

        // Load the data source
        if (angular.isString($transition$.params().datasourceId)) {
            DatasourcesService.findById($transition$.params().datasourceId)
                .then(function (model:any) {
                    self.model = model;
                    self.loading = false;
                    if(self.model.controllerServiceId){
                        //see if we can find the references and show them
                        DatasourcesService.findControllerServiceReferences(self.model.controllerServiceId).then(function(references:any){
                            self.model.references = references;
                        });
                    }

                    $q.when(AccessControlService.hasPermission(AccessControlService.DATASOURCE_EDIT, self.model, AccessControlService.ENTITY_ACCESS.DATASOURCE.EDIT_DETAILS))
                        .then(function (access:any) {
                            self.allowEdit = access;
                        });
                    $q.when(AccessControlService.hasPermission(AccessControlService.DATASOURCE_EDIT, self.model, AccessControlService.ENTITY_ACCESS.DATASOURCE.DELETE_DATASOURCE))
                        .then(function (access:any) {
                            self.allowDelete = access;
                        });
                    $q.when(AccessControlService.hasPermission(AccessControlService.DATASOURCE_EDIT, self.model, AccessControlService.ENTITY_ACCESS.DATASOURCE.CHANGE_DATASOURCE_PERMISSIONS))
                        .then(function (access:any) {
                            self.allowChangePermissions = access;
                        });
                }, function () {
                    StateService.FeedManager().Datasource().navigateToDatasources();
                });
        } else {
            self.onEdit();
            self.isDetailsEditable = true;
            self.loading = false;
        }

    }

    /**
     * Watch for changes on the datasource name
     */
    onDatasourceNameChange(){
        if (_.isEmpty(this.existingDatasourceNames)) {
           this.DatasourcesService.findAll()
                .then((datasources:any) => {
                    this.existingDatasourceNames = {};
                    angular.forEach(datasources, (datasource) =>{
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
    var saveDatasourceDialogController = function ($scope:any, $mdDialog:any, $interval:any,datasourceName:any) {
        var self = this;

        $scope.datasourceName = datasourceName;
        $scope.message = "Saving the data source "+datasourceName;
        var counter = 0;
        var index = 0;
        var messages:any = [];
        messages.push("Hang tight. Still working.")
        messages.push("Just a little while longer.")
        messages.push("Saving the data source.")

        function updateMessage(){
            index++;
            var len = messages.length;
            if(index == len){
                index = 0;
            }
            $scope.message = messages[index];

        }
        var messageInterval = $interval(function() {
            updateMessage();

        },3000);

        function cancelMessageInterval(){
            if(messageInterval != null) {
                $interval.cancel(messageInterval);
            }
        }


        $scope.hide = function () {
            cancelMessageInterval();
            $mdDialog.hide();

        };

        $scope.cancel = function () {
            cancelMessageInterval();
            $mdDialog.cancel();
        };

    };

    angular.module(moduleName).controller('SaveDatasourceDialogController', ["$scope","$mdDialog","$interval","datasourceName",saveDatasourceDialogController]);


    angular.module(moduleName).controller("DatasourcesDetailsController", ["$scope", "$mdDialog", "$mdToast", "$q", "$transition$", "AccessControlService", "DatasourcesService",
                                                                           "EntityAccessControlService", "StateService", DatasourcesDetailsController]);
