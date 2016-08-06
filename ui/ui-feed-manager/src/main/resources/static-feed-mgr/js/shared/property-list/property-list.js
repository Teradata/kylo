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
         * Adds a new user-defined property.
         */
        self.addProperty = function() {
            if ($scope.model === null) {
                $scope.model = [];
            }
            $scope.model.push({systemName: "", value: "", $error: {}});
        };

        /**
         * Updates the model with changes to the property list.
         */
        self.onPropertyChange = function() {
            var keys = {};

            angular.forEach($scope.model, function(property) {
                // Validate property
                if (angular.isUndefined(property.$error)) {
                    property.$error = {};
                }

                property.$error.duplicate = angular.isDefined(keys[property.systemName]);

                // Add to user properties object
                if (property.systemName.length > 0) {
                    keys[property.systemName] = true;
                }
            });
        };

        /**
         * Deletes the item at the specified index from the user-defined properties list.
         *
         * @param {number} index the index of the property to delete
         */
        self.removeProperty = function(index) {
            $scope.model.splice(index, 1);
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
