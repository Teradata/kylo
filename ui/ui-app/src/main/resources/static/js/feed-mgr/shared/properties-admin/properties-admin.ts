import * as angular from 'angular';

import {moduleName} from "../../module-name";;

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
class PropertiesAdminController {

    /**
     * Copy of model that mirrors the field list.
     * @type {Array.<UserField>}
     */
    lastModel: any = {};

    /**
     * List of fields in the model.
     * @type {Array.<UserField>}
     */
    fieldList: any[] = [];

    /**
     * Indicates if all fields are valid.
     * @type {boolean} {@code true} if all fields are valid, or {@code false} otherwise
     */
    isValid: boolean = true;

    model: any;

    static readonly $inject = ["$scope"];

    constructor(private $scope: IScope) {

        // Watch for changes to model
        $scope.$watch(
            () => { return this.model; },
            () => { this.onModelChange(); },
            true
        );

        // Watch for changes to field list
        $scope.$watch(
            () => { return this.$scope.fieldList; },
            () => { this.onFieldChange(); },
            true
        );

    }
    /**
    * Adds a new user-defined field.
    */
    addField = () => {
        this.$scope.fieldList.push({ description: null, displayName: "", order: this.$scope.fieldList.length, required: false, systemName: "", $error: {}, $isNew: true });
    };

    /**
     * Moves the specified field down in the list.
     *
     * @param index the index of the field
     */
    moveDown = (index: any) => {
        this.$scope.fieldList.splice(index, 2, this.$scope.fieldList[index + 1], this.$scope.fieldList[index]);
    };

    /**
     * Moves the specified field up in the list.
     *
     * @param index the index of the field
     */
    moveUp = (index: any) => {
        this.$scope.fieldList.splice(index - 1, 2, this.$scope.fieldList[index], this.$scope.fieldList[index - 1]);
    };

    /**
     * Updates the model with changes to the field list.
     */
    onFieldChange = () => {
        // Convert fields to model
        var hasError: any = false;
        var keys: any = {};
        var model: any = [];
        var order: any = 0;

        angular.forEach(this.$scope.fieldList, (field: any) => {
            let dn: any = (field.displayName.length === 0);
            // Validate field
            let _: any = (field.$error.duplicate = angular.isDefined(keys[field.systemName]));
            hasError |= _;
            hasError |= (field.$error.missingName = dn);

            // Add to user fields object
            if (field.systemName.length > 0) {
                field.order = order++;
                keys[field.systemName] = true;
                model.push(angular.copy(field));
            }
        });

        // Update model
        this.isValid = !hasError;
        if (!hasError) {
            this.model = model;
            this.lastModel = angular.copy(this.model);
        }
    };

    /**
     * Updates the field list with changes to the model.
     */
    onModelChange = () => {
        if (!angular.equals(this.model, this.lastModel)) {
            // Convert model to fields
            this.$scope.fieldList = [];
            angular.forEach(this.model, (element: any) => {
                var field = angular.copy(element);
                field.$error = {};
                this.$scope.fieldList.push(field);
            });

            // Sort fields
            this.$scope.fieldList.sort((a: any, b: any) => {
                return a.order - b.order;
            });

            // Save a copy for update detection
            this.lastModel = angular.copy(this.model);
        }
    };

    /**
     * Deletes the item at the specified index from the user-defined fields list.
     *
     * @param {number} index the index of the field to delete
     */
    removeField = (index: any) => {
        this.$scope.fieldList.splice(index, 1);
    };

    /**
     * Updates the system name property of the specified field.
     *
     * @param field the user-defined field
     */
    updateSystemName = (field: any) => {
        if (field.$isNew) {
            field.systemName = field.displayName
                .replace(/[^a-zA-Z0-9]+([a-zA-Z0-9]?)/g, (match: any, p1: any) => { return p1.toUpperCase(); })
                .replace(/^[A-Z]/, (match: any) => { return match.toLowerCase(); });
            this.onFieldChange();
        }
    }
}

angular.module(moduleName).component("thinkbigPropertiesAdmin", {
    controller: PropertiesAdminController,
    controllerAs: "vm",
    bindings: {
        model: "=fields",
        isValid: "=?"
    },
    templateUrl: "./properties-admin.html"
});

angular.module(moduleName).component("thinkbigPropertiesAdminEditor", {
    controller: PropertiesAdminController,
    controllerAs: "vm",
    bindings: {
        model: "=fields",
        isValid: "=?"
    },
    templateUrl: "./properties-admin-editor.html"
});

