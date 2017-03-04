define(['angular','auth/module-name'], function (angular,moduleName) {
    /**
     * A permission available to users and groups, including UI state.
     *
     * @typedef {Object} ActionState
     * @property {Array.<ActionState>} [actions] child action states
     * @property {string} description a human-readable summary
     * @property {string} systemName unique identifier
     * @property {string} title a human-readable name
     * @property {boolean} $$allowed {@code true} if the action is allowed, or {@code false} if denied
     * @property {number} $$level the indent level
     * @property {ActionState|null} $$parent the parent action
     */

    /**
     * Displays a list of permissions.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param {Array.<Action>} $scope.model list of allowed actions
     * @param {boolean} $scope.readOnly {@code true} if no changes may be made, or {@code false} otherwise
     * @param {function} $scope.$watch
     * @param {AccessControlService} AccessControlService the access control service
     */
    function PermissionsTableController($scope, AccessControlService) {
        var self = this;

        /**
         * List of available actions to be displayed.
         * @type {Array.<ActionState>}
         */
        self.available = [];

        /**
         * Copy of model for detecting outside changes.
         * @type {Array.<Action>}
         */
        self.lastModel = [];

        /**
         * List of top-level available actions.
         * @type {Array.<ActionState>}
         */
        self.roots = [];

        // Watch for changes to the model
        $scope.$watch(
                function() { return $scope.model; },
                function() { self.refresh(); },
                true
        );

        /**
         * Adds any allowed actions in the specified list to the model.
         *
         * @param {Array.<ActionState>} actions the list of actions
         * @param {Array.<Action>} target the destination
         */
        self.addAllowed = function(actions, target) {
            angular.forEach(actions, function(action) {
                if (action.$$allowed) {
                    var copy = _.pick(action, "description", "systemName", "title");
                    if (angular.isArray(action.actions)) {
                        copy.actions = [];
                        self.addAllowed(action.actions, copy.actions);
                    }
                    target.push(copy);
                }
            });
        };

        /**
         * Adds the specified action to the list of available actions.
         *
         * @param {Action} action the action
         * @param {number} level the indent level, starting at 0
         * @param {ActionState|null} parent the parent action
         */
        self.addAction = function(action, level, parent) {
            var state = _.pick(action, "description", "systemName", "title");
            state.$$allowed = false;
            state.$$level = level;
            state.$$parent = parent;
            self.available.push(state);

            if (angular.isArray(action.actions)) {
                ++level;
                state.actions = _.map(action.actions, function(action) {
                    return self.addAction(action, level, state);
                });
            }

            return state;
        };

        /**
         * Returns an array containing the specified number of elements.
         *
         * @param {number} n the number of elements for the array
         * @returns {Array.<number>} the array
         */
        self.range = function(n) {
            return _.range(n);
        };

        /**
         * Updates the UI action states.
         */
        self.refresh = function() {
            // Function to map model of allowed actions
            var mapModel = function(actions, map) {
                angular.forEach(actions, function(action) {
                    map[action.systemName] = true;
                    if (angular.isArray(action.actions)) {
                        mapModel(action.actions, map);
                    }
                });
                return map;
            };

            // Determine if action states need updating
            if (angular.isDefined($scope.model) && !angular.equals($scope.model, self.lastModel) && self.available.length > 0) {
                // Update action states
                var allowed = mapModel($scope.model, {});
                angular.forEach(self.available, function(action) {
                    action.$$allowed = angular.isDefined(allowed[action.systemName]);
                });

                // Save a copy for update detection
                self.lastModel = angular.copy($scope.model);
            }
        };

        /**
         * Sets the state of the specified action to the specified value.
         *
         * @param {ActionState} action the action
         * @param {boolean} allowed {@code true} if the action is allowed, or {@code false} otherwise
         */
        self.setAllowed = function(action, allowed) {
            // Update state
            action.$$allowed = allowed;

            if (allowed) {
                // Update parent action
                if (action.$$parent !== null) {
                    self.setAllowed(action.$$parent, allowed);
                }
            }
            else {
                // Update child actions
                if (angular.isArray(action.actions)) {
                    angular.forEach(action.actions, function(child) {
                        self.setAllowed(child, allowed);
                    });
                }
            }
        };

        /**
         * Toggles the allowed state of the specified action.
         *
         * @param {ActionState} action the action
         */
        self.toggle = function(action) {
            if (angular.isUndefined($scope.readOnly) || !$scope.readOnly) {
                self.setAllowed(action, !action.$$allowed);

                // Update model
                var model = [];
                self.addAllowed(self.roots, model);
                $scope.model = self.lastModel = model;
            }
        };

        // Fetch the list of available actions
        AccessControlService.getAvailableActions()
                .then(function(actionSet) {
                    angular.forEach(actionSet.actions, function(action) {
                        var state = self.addAction(action, 0, null);
                        self.roots.push(state);
                    });
                    self.refresh();
                });
    }

    /**
     * Creates a directive for displaying and editing permissions.
     *
     * @returns {Object} the directive
     */
    function thinkbigPermissionsTable() {
        return {
            controller: "PermissionsTableController",
            controllerAs: "vm",
            require: "ngModel",
            restrict: "E",
            scope: {
                model: "=allowed",
                readOnly: "=?"
            },
            templateUrl: "js/auth/shared/permissions-table/permissions-table.html"
        };
    }

    angular.module(moduleName).controller("PermissionsTableController",["$scope", "AccessControlService", PermissionsTableController]);
    angular.module(moduleName).directive("thinkbigPermissionsTable", thinkbigPermissionsTable);
});
