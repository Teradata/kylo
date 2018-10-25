import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../../module-name";;

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
class PropertyListController {
    static readonly $inject = ["$scope"];
    /**
     * Copy of model that mirrors the property list.
     * @type {Array.<UserProperty>}
     */
    lastModel: any[] = [];

    /**
     * List of properties in the model.
     * @type {Array.<UserProperty>}
     */
    propertyList: any[] = [];

    /**
     * Indicates if all properties are valid.
     * @type {boolean} {@code true} if all properties are valid, or {@code false} otherwise
     */
    isValid: boolean = true;

    model: any;
    ngModel : any;

    constructor(private $scope: IScope) {
        // Watch for changes to model
        $scope.$watch(
            () => { return this.model; },
            () => { this.onModelChange(); },
            true
        );

        // Watch for changes to property list
        $scope.$watch(
            () => { return this.propertyList; },
            () => { this.onPropertyChange(); },
            true
        );

    }



    /**
     * Adds a new user-defined property.
     */
    addProperty = () => {
        this.propertyList.push({ description: null, displayName: null, locked: false, order: this.propertyList.length, required: true, systemName: "", value: "", $error: {} });
    };

    /**
     * Updates the property list with changes to the model.
     */
    onModelChange = () => {
        if (!angular.equals(this.model, this.lastModel)) {
            // Convert model to properties
            this.propertyList = [];
            angular.forEach(this.model, (element: any) => {
                var property = angular.copy(element);
                property.$error = {};
                this.propertyList.push(property);
            });

            // Sort properties
            this.propertyList.sort((a: any, b: any) => {
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
            this.lastModel = angular.copy(this.model);
        }
    };

    /**
     * Updates the model with changes to the property list.
     */
    onPropertyChange = () => {
        // Convert properties to model
        var hasError: any = false;
        var keys: any = {};
        var model: any = [];

        angular.forEach(this.propertyList, (property: any) => {
            let mn: any = (property.systemName.length === 0 && property.value.length > 0);
            let mv: any = (property.required && property.systemName.length > 0 && (angular.isUndefined(property.value) || property.value === null || property.value.length === 0));
            // Validate property
            let _: any = (property.$error.duplicate = angular.isDefined(keys[property.systemName]))
            hasError |= _;
            hasError |= (property.$error.missingName = mn);
            hasError |= (property.$error.missingValue = mv);

            // Add to user properties object
            if (property.systemName.length > 0) {
                keys[property.systemName] = true;
                model.push(angular.copy(property));
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
     * Deletes the item at the specified index from the user-defined properties list.
     *
     * @param {number} index the index of the property to delete
     */
    removeProperty = (index: any) => {
        this.propertyList.splice(index, 1);
    };
}


angular.module(moduleName).component("thinkbigPropertyList", {
    controller: PropertyListController,
    controllerAs: "vm",
    require: {
        ngModel: "^?ngModel"
    },
    bindings: {
        model: "=properties",
        isValid: "=?"
    },
    templateUrl: "./property-list.html"
});
angular.module(moduleName).component("thinkbigPropertyListEditor", {
    controller: PropertyListController,
    controllerAs: "vm",
    require: {
        ngModel: "^?ngModel"
    },
    bindings: {
        model: "=properties",
        isValid: "=?"
    },
    templateUrl: "./property-list-editor.html"
});

