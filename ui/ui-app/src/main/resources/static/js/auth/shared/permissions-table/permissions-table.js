define(["require", "exports", "angular", "underscore", "../../module-name"], function (require, exports, angular, _, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var PermissionsTableController = /** @class */ (function () {
        function PermissionsTableController($scope, AccessControlService) {
            var _this = this;
            this.$scope = $scope;
            this.AccessControlService = AccessControlService;
            /**
            * List of available actions to be displayed.
            * @type {Array.<ActionState>}
            */
            this.available = [];
            /**
             * Copy of model for detecting outside changes.
             * @type {Array.<Action>}
             */
            this.lastModel = [];
            /**
             * List of top-level available actions.
             * @type {Array.<ActionState>}
             */
            this.roots = [];
            /**
               * Adds any allowed actions in the specified list to the model.
               *
               * @param {Array.<ActionState>} actions the list of actions
               * @param {Array.<Action>} target the destination
               */
            this.addAllowed = function (actions, target) {
                angular.forEach(actions, function (action) {
                    if (action.$$allowed) {
                        var copy = _.pick(action, "description", "systemName", "title");
                        if (angular.isArray(action.actions)) {
                            copy.actions = [];
                            _this.addAllowed(action.actions, copy.actions);
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
            this.addAction = function (action, level, parent) {
                var state = _.pick(action, "description", "systemName", "title");
                state.$$allowed = false;
                state.$$level = level;
                state.$$parent = parent;
                _this.available.push(state);
                if (angular.isArray(action.actions)) {
                    ++level;
                    state.actions = _.map(action.actions, function (action) {
                        return _this.addAction(action, level, state);
                    });
                }
                return state;
            };
            /**
             * Toggles the allowed state of the specified action.
             *
             * @param {ActionState} action the action
             */
            this.toggle = function (action) {
                if (angular.isUndefined(_this.$scope.readOnly) || !_this.$scope.readOnly) {
                    _this.setAllowed(action, !action.$$allowed);
                    // Update model
                    var model = [];
                    _this.addAllowed(_this.roots, model);
                    _this.$scope.model = _this.lastModel = model;
                }
            };
            // Watch for changes to the model
            $scope.$watch(function () { return $scope.model; }, function () { _this.refresh(); });
            // Fetch the list of available actions
            this.getAvailableActions();
            /* AccessControlService.getAvailableActions()
                         .then((actionSet:any)=> {
                             angular.forEach(actionSet.actions, (action:any)=> {
                                                                 var state = this.addAction(action, 0, null);
                                                                 this.roots.push(state);
                                                             });
                             this.refresh();
                     });*/
        }
        PermissionsTableController.prototype.getAvailableActions = function () {
            var _this = this;
            this.AccessControlService
                .getAvailableActions()
                .then(function (actionSet) {
                angular.forEach(actionSet.actions, function (action) {
                    var state = _this.addAction(action, 0, null);
                    _this.roots.push(state);
                });
                _this.refresh();
            });
        };
        /**
      * Returns an array containing the specified number of elements.
      *
      * @param {number} n the number of elements for the array
      * @returns {Array.<number>} the array
      */
        PermissionsTableController.prototype.range = function (n) {
            return _.range(n);
        };
        ;
        /**
         * Updates the UI action states.
         */
        PermissionsTableController.prototype.refresh = function () {
            // Function to map model of allowed actions
            var mapModel = function (actions, map) {
                angular.forEach(actions, function (action) {
                    map[action.systemName] = true;
                    if (angular.isArray(action.actions)) {
                        mapModel(action.actions, map);
                    }
                });
                return map;
            };
            // Determine if action states need updating
            if (angular.isDefined(this.$scope.model) && !angular.equals(this.$scope.model, this.lastModel) && this.available.length > 0) {
                // Update action states
                var allowed = mapModel(this.$scope.model, {});
                angular.forEach(this.available, function (action) {
                    action.$$allowed = angular.isDefined(allowed[action.systemName]);
                });
                // Save a copy for update detection
                this.lastModel = angular.copy(this.$scope.model);
            }
        };
        ;
        /**
              * Sets the state of the specified action to the specified value.
              *
              * @param {ActionState} action the action
              * @param {boolean} allowed {@code true} if the action is allowed, or {@code false} otherwise
              */
        PermissionsTableController.prototype.setAllowed = function (action, allowed) {
            var _this = this;
            // Update state
            action.$$allowed = allowed;
            if (allowed) {
                if (action.$$parent !== null) {
                    this.setAllowed(action.$$parent, allowed);
                }
            }
            else {
                if (angular.isArray(action.actions)) {
                    angular.forEach(action.actions, function (child) {
                        _this.setAllowed(child, allowed);
                    });
                }
            }
        };
        ;
        return PermissionsTableController;
    }());
    exports.PermissionsTableController = PermissionsTableController;
    /*export function thinkbigPermissionsTable(): ng.IDirective
        {
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
    */
    angular.module(module_name_1.moduleName).controller("PermissionsTableController", ["$scope", "AccessControlService", PermissionsTableController]);
    angular.module(module_name_1.moduleName).directive("thinkbigPermissionsTable", //[this.thinkbigPermissionsTable]);
    [function () {
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
        }]);
});
//# sourceMappingURL=permissions-table.js.map