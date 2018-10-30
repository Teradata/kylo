import * as angular from 'angular';
import * as _ from "underscore";
import { moduleName } from "./module-name";
import {AccessControlService} from '../../services/AccessControlService';
import './module-require';

export class BusinessMetadataController implements ng.IComponentController {
    /**
          * Indicates if the category fields may be edited.
          * @type {boolean}
          */
    allowCategoryEdit: boolean = false;
    /**
         * Indicates if the feed fields may be edited.
         * @type {boolean}
         */
    allowFeedEdit: boolean = false;
    /**
         * Model for editable sections.
         * @type {{categoryFields: Array, feedFields: Array}}
         */
    editModel: any = { categoryFields: [], feedFields: [] };
    /**
         * Indicates that the editable section for categories is displayed.
         * @type {boolean}
         */
    isCategoryEditable: boolean = false;

    /**
     * Indicates that the editable section for categories is valid.
     * @type {boolean}
     */
    isCategoryValid: boolean = true;
    /**
         * Indicates that the editable section for categories is displayed.
         * @type {boolean}
         */
    isFeedEditable: boolean = false;
    /**
         * Indicates that the editable section for categories is valid.
         * @type {boolean}
         */
    isFeedValid: boolean = true;
    /**
         * Indicates that the loading progress bar is displayed.
         * @type {boolean}
         */
    loading: boolean = true;
    /**
        * Model for read-only sections.
        * @type {{categoryFields: Array, feedFields: Array}}
        */
    model: any = { categoryFields: [], feedFields: [] };
    /**
     * Controller for the business metadata page.
     *
     * @constructor
     * @param $scope the application model
     * @param $http the HTTP service
     * @param {AccessControlService} AccessControlService the access control service
     * @param RestUrlService the Rest URL service
     */
    static readonly $inject = ["$scope", "$http", "AccessControlService", "RestUrlService"];
    constructor(private $scope: IScope,
        private $http: angular.IHttpService,
        private accessControlService: AccessControlService,
        private RestUrlService: any) {

        // Load the field models
        this.$http.get(RestUrlService.ADMIN_USER_FIELDS).then((response: any) => {
            this.model = response.data;
            this.loading = false;
        });

        // Load the permissions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowCategoryEdit = accessControlService.hasAction(AccessControlService.CATEGORIES_ADMIN, actionSet.actions);
                this.allowFeedEdit = accessControlService.hasAction(AccessControlService.FEEDS_ADMIN, actionSet.actions);
            });
    }
    /**
        * Creates a copy of the category model for editing.
        */
    onCategoryEdit = () => {
        this.editModel.categoryFields = angular.copy(this.model.categoryFields);
    };
    /**
     * Saves the category model.
     */
    onCategorySave = () => {
        var model = angular.copy(this.model);
        model.categoryFields = this.editModel.categoryFields;

        this.$http({
            data: angular.toJson(model),
            headers: { 'Content-Type': 'application/json; charset=UTF-8' },
            method: "POST",
            url: this.RestUrlService.ADMIN_USER_FIELDS
        }).then(() => {
            this.model = model;
        });
    };

    /**
     * Creates a copy of the feed model for editing.
     */
    onFeedEdit = () => {
        this.editModel.feedFields = angular.copy(this.model.feedFields);
    };

    /**
     * Saves the feed model.
     */
    onFeedSave = () => {
        var model = angular.copy(this.model);
        model.feedFields = this.editModel.feedFields;

        this.$http({
            data: angular.toJson(model),
            headers: { 'Content-Type': 'application/json; charset=UTF-8' },
            method: "POST",
            url: this.RestUrlService.ADMIN_USER_FIELDS
        }).then(() => {
            this.model = model;
        });
    };

}
// Register the controller
const module = angular.module(moduleName).component('businessMetadataController', {
    templateUrl: './business-metadata.html',
    controller: BusinessMetadataController,
    controllerAs: 'vm'
});
export default module;