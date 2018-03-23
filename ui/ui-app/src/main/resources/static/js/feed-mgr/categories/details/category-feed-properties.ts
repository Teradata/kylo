import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/categories/module-name');


/**
 * Creates a directive for the Category Feed Properties section.
 *
 * @returns {Object} the directive
 */
function thinkbigFeedCategoryProperties() {
    return {
        controller: "CategoryFeedPropertiesController",
        controllerAs: 'vm',
        restrict: "E",
        scope: {},
        templateUrl: 'js/feed-mgr/categories/details/category-feed-properties.html'
    };
}

export class CategoryFeedPropertiesController {

    allowEdit:any;
    editModel:any;
    isEditable:any;
    isNew:any;
    isValid:any;
    model:any;
    onEdit:any;
    onSave:any;
    /**
     * Manages the Category Feed Properties section of the Category Details page.
     *
     * @constructor
     * @param $scope the application model
     * @param $mdToast the toast service
     * @param {AccessControlService} AccessControlService the access control service
     * @param CategoriesService the category service
     */
    constructor(private $scope:any, private $mdToast:any, private $q:any, private AccessControlService:any
        , private EntityAccessControlService:any, private CategoriesService:any, private $mdDialog:any) {
        var self = this;

        /**
         * Indicates if the properties may be edited.
         */
        self.allowEdit = false;

        /**
         * Category data used in "edit" mode.
         * @type {CategoryModel}
         */
        self.editModel = CategoriesService.newCategory();

        /**
         * Indicates if the view is in "edit" mode.
         * @type {boolean} {@code true} if in "edit" mode or {@code false} if in "normal" mode
         */
        self.isEditable = false;

        /**
         * Indicates of the category is new.
         * @type {boolean}
         */
        self.isNew = true;
        $scope.$watch(
            function () {
                return CategoriesService.model.id
            },
            function (newValue:any) {
                self.isNew = !angular.isString(newValue)
            }
        );

        /**
         * Indicates if the properties are valid and can be saved.
         * @type {boolean} {@code true} if all properties are valid, or {@code false} otherwise
         */
        self.isValid = true;

        /**
         * Category data used in "normal" mode.
         * @type {CategoryModel}
         */
        self.model = CategoriesService.model;

        /**
         * Switches to "edit" mode.
         */
        self.onEdit = function () {
            self.editModel = angular.copy(self.model);
        };

        /**
         * Saves the category properties.
         */
        self.onSave = function () {
            var model = angular.copy(CategoriesService.model);
            model.id = self.model.id;
            model.userFields = self.editModel.userFields;
            model.userProperties = null;

            CategoriesService.save(model).then(function (response:any) {
                self.model = CategoriesService.model = response.data;
                CategoriesService.update(response.data);
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('Saved the Category')
                        .hideDelay(3000)
                );
            }, function (err:any) {
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Save Failed")
                        .textContent("The category '" + model.name + "' could not be saved. " + err.data.message)
                        .ariaLabel("Failed to save category")
                        .ok("Got it!")
                );
            });
        };

        //Apply the entity access permissions
        $q.when(AccessControlService.hasPermission(AccessControlService.CATEGORIES_ADMIN,self.model,AccessControlService.ENTITY_ACCESS.CATEGORY.EDIT_CATEGORY_DETAILS)).then(function(access:any) {
            self.allowEdit = access;
        });

        /*
         // Fetch the allowed actions
         AccessControlService.getAllowedActions()
         .then(function(actionSet) {
         self.allowEdit = AccessControlService.hasAction(AccessControlService.CATEGORIES_ADMIN, actionSet.actions);
         });
         */
    }


}

angular.module(moduleName).controller('CategoryFeedPropertiesController',
    ["$scope", "$mdToast", "$q", "AccessControlService", "EntityAccessControlService", "CategoriesService", "$mdDialog", CategoryFeedPropertiesController]);
angular.module(moduleName).directive('thinkbigCategoryFeedProperties', thinkbigFeedCategoryProperties);
