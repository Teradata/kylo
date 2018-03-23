import * as angular from 'angular';
import * as _ from 'underscore';
//const moduleName = require('auth/module-name');
import {moduleName} from "../../module-name";
export interface IMyScope extends ng.IScope {
  model?: any;
  readOnly?: boolean;
}
export  class PermissionsTableController implements ng.IComponentController {
         /**
         * List of available actions to be displayed.
         * @type {Array.<ActionState>}
         */
         available:any = [];

        /**
         * Copy of model for detecting outside changes.
         * @type {Array.<Action>}
         */
        lastModel: any = [];

        /**
         * List of top-level available actions.
         * @type {Array.<ActionState>}
         */
        roots: any  = [];
        
    constructor (private $scope:IMyScope,
                 private AccessControlService:any) {
        // Watch for changes to the model
        $scope.$watch(() => { return $scope.model;},
                      () => { this.refresh();}
                      );
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
    
    getAvailableActions(){
        this.AccessControlService
            .getAvailableActions()
            .then((actionSet:any)=> {
                            angular.forEach(actionSet.actions, 
                                            (action:any)=> { //fun(val,key)
                                                            var state = this.addAction(action, 0, null);
                                                            this.roots.push(state);
                                                            });
                            this.refresh();
                    });
    }
      /**
         * Adds any allowed actions in the specified list to the model.
         *
         * @param {Array.<ActionState>} actions the list of actions
         * @param {Array.<Action>} target the destination
         */
        addAllowed = (actions: any[], target: any[]) =>{
            angular.forEach(actions, (action)=> {
                if (action.$$allowed) {
                    var copy = _.pick(action, "description", "systemName", "title");
                    if (angular.isArray(action.actions)) {
                        copy.actions = [];
                        this.addAllowed(action.actions, copy.actions);
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
        addAction = (action: any, level: any, parent: any) =>{
            var state = _.pick(action, "description", "systemName", "title");
            state.$$allowed = false;
            state.$$level = level;
            state.$$parent = parent;
            this.available.push(state);

            if (angular.isArray(action.actions)) {
                ++level;
                state.actions = _.map(action.actions,
                                     (action)=> {
                                                 return this.addAction(action, level, state);
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
        range(n:any):any{
            return _.range(n);
        };

        /**
         * Updates the UI action states.
         */
        refresh(){
            // Function to map model of allowed actions
            var mapModel =(actions: any, map: any)=>{
                angular.forEach(actions, (action)=> {
                    map[action.systemName] = true;
                    if (angular.isArray(action.actions)) 
                    {
                        mapModel(action.actions, map);
                    }
                });
                return map;
            };
              // Determine if action states need updating
            if (angular.isDefined(this.$scope.model) && !angular.equals(this.$scope.model, this.lastModel) && this.available.length > 0) {
                // Update action states
                var allowed = mapModel(this.$scope.model, {});
                angular.forEach(this.available, (action) =>{
                    action.$$allowed = angular.isDefined(allowed[action.systemName]);
                });

                // Save a copy for update detection
                this.lastModel = angular.copy(this.$scope.model);
            }
        };

   /**
         * Sets the state of the specified action to the specified value.
         *
         * @param {ActionState} action the action
         * @param {boolean} allowed {@code true} if the action is allowed, or {@code false} otherwise
         */
        setAllowed(action: any, allowed: any) {
            // Update state
            action.$$allowed = allowed;

            if (allowed) {// Update parent action
                if (action.$$parent !== null) {
                    this.setAllowed(action.$$parent, allowed);
                }
            }
            else { // Update child actions
                if (angular.isArray(action.actions)) 
                {
                    angular.forEach(action.actions, (child) =>{
                                this.setAllowed(child, allowed);
                });
                }
            }
        };

        /**
         * Toggles the allowed state of the specified action.
         *
         * @param {ActionState} action the action
         */
        toggle = (action: any)=> {
            if (angular.isUndefined(this.$scope.readOnly) || !this.$scope.readOnly) {
                this.setAllowed(action, !action.$$allowed);

                // Update model
                var model: any= [];
                this.addAllowed(this.roots, model);
                this.$scope.model = this.lastModel = model;
            }
        };

        
                    /**
     * Creates a directive for displaying and editing permissions.
     *
     * @returns {Object} the directive
     */
        }

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
angular.module(moduleName).controller("PermissionsTableController",["$scope", "AccessControlService", PermissionsTableController]);
angular.module(moduleName).directive("thinkbigPermissionsTable", //[this.thinkbigPermissionsTable]);
  [ () => { return {
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