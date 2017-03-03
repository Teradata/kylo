define(['angular',"feed-mgr/module-name"], function (angular,moduleName) {
    /**
     * A user-defined property (or business metadata) on a category or feed.
     *
     * @typedef {Object} UserProperty
     * @property {string|null} description a human-readable specification
     * @property {string|null} displayName a human-readable title
     * @property {boolean} locked indicates that only the value may be changed
     * @property {number} order index for the display order from 0 and up
     * @property {boolean} required indicates that the value cannot be empty
     * @property {string} systemName an internal identifier
     * @property {string} value the value assign to the property
     * @property {Object.<string, boolean>} [$error] used for validation
     */

    /**
     * Manages a view containing a list of properties.
     *
     * @constructor
     * @param $scope the application model
     */
    function PropertyListController($scope) {
        var self = this;

        /**
         * Copy of model that mirrors the property list.
         * @type {Array.<UserProperty>}
         */
        self.lastModel = {};

        /**
         * List of properties in the model.
         * @type {Array.<UserProperty>}
         */
        $scope.propertyList = [];

        /**
         * Indicates if all properties are valid.
         * @type {boolean} {@code true} if all properties are valid, or {@code false} otherwise
         */
        $scope.isValid = true;

        // Watch for changes to model
        $scope.$watch(
                function() { return $scope.model; },
                function() { self.onModelChange(); },
                true
        );

        // Watch for changes to property list
        $scope.$watch(
                function() { return $scope.propertyList; },
                function() { self.onPropertyChange(); },
                true
        );

        /**
         * Adds a new user-defined property.
         */
        self.addProperty = function() {
            $scope.propertyList.push({description: null, displayName: null, locked: false, order: $scope.propertyList.length, required: true, systemName: "", value: "", $error: {}});
        };

        /**
         * Updates the property list with changes to the model.
         */
        self.onModelChange = function() {
            if (!angular.equals($scope.model, self.lastModel)) {
                // Convert model to properties
                $scope.propertyList = [];
                angular.forEach($scope.model, function(element) {
                    var property = angular.copy(element);
                    property.$error = {};
                    $scope.propertyList.push(property);
                });

                // Sort properties
                $scope.propertyList.sort(function(a, b) {
                    if (a.order === null && b.order === null) {
                        return a.systemName.localeCompare(b.systemName);
                    }
                    if (a.order === null) {
                        return 1;
                    }
                    if (b.order === null) {
                        return -1;
                    }
                    return a.order - b.order;
                });

                // Save a copy for update detection
                self.lastModel = angular.copy($scope.model);
            }
        };

        /**
         * Updates the model with changes to the property list.
         */
        self.onPropertyChange = function() {
            // Convert properties to model
            var hasError = false;
            var keys = {};
            var model = [];

            angular.forEach($scope.propertyList, function(property) {
                // Validate property
                hasError |= (property.$error.duplicate = angular.isDefined(keys[property.systemName]));
                hasError |= (property.$error.missingName = (property.systemName.length === 0 && property.value.length > 0));
                hasError |= (property.$error.missingValue = (property.required && property.systemName.length > 0 && (property.value === null || property.value.length === 0)));

                // Add to user properties object
                if (property.systemName.length > 0) {
                    keys[property.systemName] = true;
                    model.push(angular.copy(property));
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
         * Deletes the item at the specified index from the user-defined properties list.
         *
         * @param {number} index the index of the property to delete
         */
        self.removeProperty = function(index) {
            $scope.propertyList.splice(index, 1);
        };
    }

    /**
     * Creates a directive for displaying a list of properties.
     *
     * @returns {Object} the directive
     */
    function thinkbigPropertyList() {
        return {
            controller: "PropertyListController",
            controllerAs: "vm",
            require: "ngModel",
            restrict: "E",
            scope: {
                model: "=properties",
                isValid: "=?"
            },
            templateUrl: "js/feed-mgr/shared/property-list/property-list.html"
        };
    }

    /**
     * Creates a directive for editing a list of properties.
     *
     * @returns {Object} the directive
     */
    function thinkbigPropertyListEditor() {
        return _.defaults({
            templateUrl: "js/feed-mgr/shared/property-list/property-list-editor.html"
        }, thinkbigPropertyList());
    }

    angular.module(moduleName).controller("PropertyListController",["$scope", PropertyListController]);
    angular.module(moduleName).directive("thinkbigPropertyList", thinkbigPropertyList);
    angular.module(moduleName).directive("thinkbigPropertyListEditor", thinkbigPropertyListEditor);
});
