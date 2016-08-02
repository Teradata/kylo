(function() {
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
         * @type {Object.<String, String>}
         */
        self.lastModel = {};

        /**
         * List of properties in the model.
         * @type {Array.<{key: string, value: string, $error: Object}>}
         */
        $scope.propertyList = [];

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
            $scope.propertyList.push({key: "", value: "", $error: {}});
        };

        /**
         * Updates the property list with changes to the model.
         */
        self.onModelChange = function() {
            if (!angular.equals($scope.model, self.lastModel)) {
                $scope.propertyList = [];
                angular.forEach($scope.model, function(value, key) {
                    $scope.propertyList.push({key: key, value: value, $error: {}});
                });
                self.lastModel = angular.copy($scope.model);
            }
        };

        /**
         * Updates the model with changes to the property list.
         */
        self.onPropertyChange = function() {
            var keys = {};
            $scope.model = {};

            angular.forEach($scope.propertyList, function(property) {
                // Validate property
                property.$error.duplicate = angular.isDefined(keys[property.key]);

                // Add to user properties object
                if (property.key.length > 0) {
                    keys[property.key] = true;
                    $scope.model[property.key] = property.value;
                }
            });

            self.lastModel = angular.copy($scope.model);
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
                model: "=properties"
            },
            templateUrl: "js/shared/property-list/property-list.html"
        };
    }

    /**
     * Creates a directive for editing a list of properties.
     *
     * @returns {Object} the directive
     */
    function thinkbigPropertyListEditor() {
        return _.defaults({
            templateUrl: "js/shared/property-list/property-list-editor.html"
        }, thinkbigPropertyList());
    }

    angular.module(MODULE_FEED_MGR).controller("PropertyListController", PropertyListController);
    angular.module(MODULE_FEED_MGR).directive("thinkbigPropertyList", thinkbigPropertyList);
    angular.module(MODULE_FEED_MGR).directive("thinkbigPropertyListEditor", thinkbigPropertyListEditor);
})();
