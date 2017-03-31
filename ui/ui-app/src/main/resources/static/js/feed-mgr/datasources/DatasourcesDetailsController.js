define(["angular", "feed-mgr/datasources/module-name"], function (angular, moduleName) {

    /**
     * Manages the Data Sources Details page for creating and editing data sources.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param {Object} $mdDialog the dialog service
     * @param {Object} $mdToast the toast notification service
     * @param {Object} $transition$ the URL parameters
     * @param {AccessControlService} AccessControlService the access control service
     * @param {DatasourcesService} DatasourcesService the data sources service
     * @param {StateService} StateService the page state service
     */
    function DatasourcesDetailsController($scope, $mdDialog, $mdToast, $transition$, AccessControlService, DatasourcesService, StateService) {
        var self = this;

        /**
         * Indicates that edit operations are allowed.
         * @type {boolean}
         */
        self.allowEdit = false;

        /**
         * Angular Materials form.
         * @type {Object}
         */
        self.datasourceForm = {};

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
         * Indicates if the edit view is displayed.
         * @type {boolean}
         */
        self.isEditable = false;

        /**
         * Data source model for the read-only view.
         * @type {JdbcDatasource}
         */
        self.model = DatasourcesService.newJdbcDatasource();

        /**
         * Indicates if the data source can be deleted. The main requirement is that the data source exists.
         *
         * @returns {boolean} {@code true} if the data source can be deleted, or {@code false} otherwise
         */
        self.canDelete = function () {
            return angular.isString(self.model.id);
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
                    }, function (err) {
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
        };

        /**
         * Saves the current data source.
         */
        self.onSave = function () {
            var model = angular.copy(self.editModel);
            if (!angular.isString(model.type) || model.type.length === 0) {
                var matches = /^(?:jdbc:)?([^:]+):/.exec(model.databaseConnectionUrl);
                model.type = (matches !== null) ? matches[1] : model.databaseDriverClassName;
            }

            DatasourcesService.save(model)
                .then(function (savedModel) {
                    self.model = savedModel;
                }, function (err) {
                    self.isEditable = true;
                    $mdDialog.show(
                        $mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title("Save Failed")
                            .textContent("The data source '" + model.name + "' could not be saved. " + err.data.message)
                            .ariaLabel("Failed to save data source")
                            .ok("Got it!")
                    );
                });
        };

        /**
         * Validates the edit form.
         */
        self.validate = function () {
            if (angular.isDefined(self.datasourceForm["datasourceName"])) {
                self.datasourceForm["datasourceName"].$setValidity("notUnique", angular.isUndefined(self.existingDatasourceNames[self.editModel.name.toLowerCase()]));
            }
        };

        // Fetch allowed permissions
        AccessControlService.getAllowedActions()
            .then(function (actionSet) {
                self.allowEdit = AccessControlService.hasAction(AccessControlService.DATASOURCE_EDIT, actionSet.actions);
            });

        // Load the data source
        if (angular.isString($transition$.params().datasourceId)) {
            DatasourcesService.findById($transition$.params().datasourceId)
                .then(function (model) {
                    self.model = model;
                    self.loading = false;
                }, function () {
                    StateService.FeedManager().Datasource().navigateToDatasources();
                });
        } else {
            self.onEdit();
            self.isEditable = true;
            self.loading = false;
        }

        // Watch for changes to data source name
        $scope.$watch(function () {
            return self.editModel.name;
        }, function () {
            if (_.isEmpty(self.existingDatasourceNames)) {
                DatasourcesService.findAll()
                    .then(function (datasources) {
                        self.existingDatasourceNames = {};
                        angular.forEach(datasources, function (datasource) {
                            self.existingDatasourceNames[datasource.name.toLowerCase()] = true;
                        });
                    })
                    .then(self.validate);
            } else {
                self.validate();
            }
        });
    }

    angular.module(moduleName).controller("DatasourcesDetailsController", ["$scope", "$mdDialog", "$mdToast", "$transition$", "AccessControlService", "DatasourcesService", "StateService",
                                                                           DatasourcesDetailsController]);
});
