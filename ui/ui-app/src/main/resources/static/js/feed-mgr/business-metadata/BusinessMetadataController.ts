import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "./module-name";

export class BusinessMetadataController implements ng.IComponentController{
    allowCategoryEdit: boolean;
    allowFeedEdit: boolean;
    editModel: any;
    isCategoryEditable: boolean;
    isCategoryValid: boolean;
    isFeedEditable: boolean;
    isFeedValid: boolean;
    loading: boolean;
    model: any;
/**
 * Controller for the business metadata page.
 *
 * @constructor
 * @param $scope the application model
 * @param $http the HTTP service
 * @param {AccessControlService} AccessControlService the access control service
 * @param RestUrlService the Rest URL service
 */
   constructor(private $scope: any,
                private $http:any, 
                private AccessControlService:any, 
                private RestUrlService:any) {
       /**
         * Indicates if the category fields may be edited.
         * @type {boolean}
         */
        this.allowCategoryEdit = false;

        /**
         * Indicates if the feed fields may be edited.
         * @type {boolean}
         */
        this.allowFeedEdit = false;

        /**
         * Model for editable sections.
         * @type {{categoryFields: Array, feedFields: Array}}
         */
        this.editModel = {categoryFields: [], feedFields: []};

        /**
         * Indicates that the editable section for categories is displayed.
         * @type {boolean}
         */
        this.isCategoryEditable = false;

        /**
         * Indicates that the editable section for categories is valid.
         * @type {boolean}
         */
        this.isCategoryValid = true;

        /**
         * Indicates that the editable section for categories is displayed.
         * @type {boolean}
         */
        this.isFeedEditable = false;

        /**
         * Indicates that the editable section for categories is valid.
         * @type {boolean}
         */
        this.isFeedValid = true;

        /**
         * Indicates that the loading progress bar is displayed.
         * @type {boolean}
         */
        this.loading = true;

        /**
         * Model for read-only sections.
         * @type {{categoryFields: Array, feedFields: Array}}
         */
        this.model = {categoryFields: [], feedFields: []};

        // Load the field models
        $http.get(RestUrlService.ADMIN_USER_FIELDS).then((response:any)=> {
            this.model = response.data;
            this.loading = false;
        });

        // Load the permissions
        AccessControlService.getUserAllowedActions()
                .then((actionSet:any)=> {
                    this.allowCategoryEdit = AccessControlService.hasAction(AccessControlService.CATEGORIES_ADMIN, actionSet.actions);
                    this.allowFeedEdit = AccessControlService.hasAction(AccessControlService.FEEDS_ADMIN, actionSet.actions);
                });
    }
     /**
         * Creates a copy of the category model for editing.
         */
        onCategoryEdit = ()=> {
            this.editModel.categoryFields = angular.copy(this.model.categoryFields);
        };
        /**
         * Saves the category model.
         */
        onCategorySave = ()=> {
            var model = angular.copy(this.model);
            model.categoryFields = this.editModel.categoryFields;

            this.$http({
                data: angular.toJson(model),
                headers: {'Content-Type': 'application/json; charset=UTF-8'},
                method: "POST",
                url: this.RestUrlService.ADMIN_USER_FIELDS
            }).then(()=> {
                this.model = model;
            });
        };

        /**
         * Creates a copy of the feed model for editing.
         */
        onFeedEdit = ()=> {
            this.editModel.feedFields = angular.copy(this.model.feedFields);
        };

        /**
         * Saves the feed model.
         */
        onFeedSave = ()=> {
            var model = angular.copy(this.model);
            model.feedFields = this.editModel.feedFields;

            this.$http({
                data: angular.toJson(model),
                headers: {'Content-Type': 'application/json; charset=UTF-8'},
                method: "POST",
                url: this.RestUrlService.ADMIN_USER_FIELDS
            }).then(()=> {
                this.model = model;
            });
        };

}
// Register the controller
angular.module(moduleName).controller('BusinessMetadataController', ["$scope","$http","AccessControlService","RestUrlService",BusinessMetadataController]);