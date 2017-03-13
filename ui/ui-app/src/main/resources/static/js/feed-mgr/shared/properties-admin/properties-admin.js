define(['angular',"feed-mgr/module-name"], function (angular,moduleName) {
    /**
     * A user-defined property field (or business metadata) for categories or feeds.
     *
     * @typedef {Object} UserField
     * @property {string|null} description a human-readable specification
     * @property {string|null} displayName a human-readable title
     * @property {number} order index for the display order from 0 and up
     * @property {boolean} required indicates that the value cannot be empty
     * @property {string} systemName an internal identifier
     * @property {Object.<string, boolean>} [$error] used for validation
     */

    /**
     * Manages a view containing a list of property fields.
     *
     * @constructor
     * @param $scope the application model
     */
    function PropertiesAdminController($scope) {
        var self = this;

        /**
         * Copy of model that mirrors the field list.
         * @type {Array.<UserField>}
         */
        self.lastModel = {};

        /**
         * List of fields in the model.
         * @type {Array.<UserField>}
         */
        $scope.fieldList = [];

        /**
         * Indicates if all fields are valid.
         * @type {boolean} {@code true} if all fields are valid, or {@code false} otherwise
         */
        $scope.isValid = true;

        // Watch for changes to model
        $scope.$watch(
                function() { return $scope.model; },
                function() { self.onModelChange(); },
                true
        );

        // Watch for changes to field list
        $scope.$watch(
                function() { return $scope.fieldList; },
                function() { self.onFieldChange(); },
                true
        );

        /**
         * Adds a new user-defined field.
         */
        self.addField = function() {
            $scope.fieldList.push({description: null, displayName: "", order: $scope.fieldList.length, required: false, systemName: "", $error: {}, $isNew: true});
        };

        /**
         * Moves the specified field down in the list.
         *
         * @param index the index of the field
         */
        self.moveDown = function(index) {
            $scope.fieldList.splice(index, 2, $scope.fieldList[index + 1], $scope.fieldList[index]);
        };

        /**
         * Moves the specified field up in the list.
         *
         * @param index the index of the field
         */
        self.moveUp = function(index) {
            $scope.fieldList.splice(index - 1, 2, $scope.fieldList[index], $scope.fieldList[index - 1]);
        };

        /**
         * Updates the model with changes to the field list.
         */
        self.onFieldChange = function() {
            // Convert fields to model
            var hasError = false;
            var keys = {};
            var model = [];
            var order = 0;

            angular.forEach($scope.fieldList, function(field) {
                // Validate field
                hasError |= (field.$error.duplicate = angular.isDefined(keys[field.systemName]));
                hasError |= (field.$error.missingName = (field.displayName.length === 0));

                // Add to user fields object
                if (field.systemName.length > 0) {
                    field.order = order++;
                    keys[field.systemName] = true;
                    model.push(angular.copy(field));
                }
            });

            // Update model
            $scope.isValid = !hasError;
            if (!hasError) {
                $scope.model = model;
                self.lastModel = angular.copy($scope.model);
            }
        };

        /**
         * Updates the field list with changes to the model.
         */
        self.onModelChange = function() {
            if (!angular.equals($scope.model, self.lastModel)) {
                // Convert model to fields
                $scope.fieldList = [];
                angular.forEach($scope.model, function(element) {
                    var field = angular.copy(element);
                    field.$error = {};
                    $scope.fieldList.push(field);
                });

                // Sort fields
                $scope.fieldList.sort(function(a, b) {
                    return a.order - b.order;
                });

                // Save a copy for update detection
                self.lastModel = angular.copy($scope.model);
            }
        };

        /**
         * Deletes the item at the specified index from the user-defined fields list.
         *
         * @param {number} index the index of the field to delete
         */
        self.removeField = function(index) {
            $scope.fieldList.splice(index, 1);
        };

        /**
         * Updates the system name property of the specified field.
         *
         * @param field the user-defined field
         */
        self.updateSystemName = function(field) {
            if (field.$isNew) {
                field.systemName = field.displayName
                        .replace(/[^a-zA-Z0-9]+([a-zA-Z0-9]?)/g, function(match, p1) { return p1.toUpperCase(); })
                        .replace(/^[A-Z]/, function(match) { return match.toLowerCase(); });
                self.onFieldChange();
            }
        }
    }

    /**
     * Creates a directive for displaying a list of fields.
     *
     * @returns {Object} the directive
     */
    function thinkbigPropertiesAdmin() {
        return {
            controller: "PropertiesAdminController",
            controllerAs: "vm",
            require: "ngModel",
            restrict: "E",
            scope: {
                model: "=fields",
                isValid: "=?"
            },
            templateUrl: "js/feed-mgr/shared/properties-admin/properties-admin.html"
        };
    }

    /**
     * Creates a directive for editing a list of fields.
     *
     * @returns {Object} the directive
     */
    function thinkbigPropertiesAdminEditor() {
        return _.defaults({
            templateUrl: "js/feed-mgr/shared/properties-admin/properties-admin-editor.html"
        }, thinkbigPropertiesAdmin());
    }

    angular.module(moduleName).controller("PropertiesAdminController", ["$scope",PropertiesAdminController]);
    angular.module(moduleName).directive("thinkbigPropertiesAdmin", thinkbigPropertiesAdmin);
    angular.module(moduleName).directive("thinkbigPropertiesAdminEditor", thinkbigPropertiesAdminEditor);
});
