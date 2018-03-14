define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var BusinessMetadataController = /** @class */ (function () {
        /**
         * Controller for the business metadata page.
         *
         * @constructor
         * @param $scope the application model
         * @param $http the HTTP service
         * @param {AccessControlService} AccessControlService the access control service
         * @param RestUrlService the Rest URL service
         */
        function BusinessMetadataController($scope, $http, AccessControlService, RestUrlService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.AccessControlService = AccessControlService;
            this.RestUrlService = RestUrlService;
            /**
                * Creates a copy of the category model for editing.
                */
            this.onCategoryEdit = function () {
                _this.editModel.categoryFields = angular.copy(_this.model.categoryFields);
            };
            /**
             * Saves the category model.
             */
            this.onCategorySave = function () {
                var model = angular.copy(_this.model);
                model.categoryFields = _this.editModel.categoryFields;
                _this.$http({
                    data: angular.toJson(model),
                    headers: { 'Content-Type': 'application/json; charset=UTF-8' },
                    method: "POST",
                    url: _this.RestUrlService.ADMIN_USER_FIELDS
                }).then(function () {
                    _this.model = model;
                });
            };
            /**
             * Creates a copy of the feed model for editing.
             */
            this.onFeedEdit = function () {
                _this.editModel.feedFields = angular.copy(_this.model.feedFields);
            };
            /**
             * Saves the feed model.
             */
            this.onFeedSave = function () {
                var model = angular.copy(_this.model);
                model.feedFields = _this.editModel.feedFields;
                _this.$http({
                    data: angular.toJson(model),
                    headers: { 'Content-Type': 'application/json; charset=UTF-8' },
                    method: "POST",
                    url: _this.RestUrlService.ADMIN_USER_FIELDS
                }).then(function () {
                    _this.model = model;
                });
            };
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
            this.editModel = { categoryFields: [], feedFields: [] };
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
            this.model = { categoryFields: [], feedFields: [] };
            // Load the field models
            $http.get(RestUrlService.ADMIN_USER_FIELDS).then(function (response) {
                _this.model = response.data;
                _this.loading = false;
            });
            // Load the permissions
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowCategoryEdit = AccessControlService.hasAction(AccessControlService.CATEGORIES_ADMIN, actionSet.actions);
                _this.allowFeedEdit = AccessControlService.hasAction(AccessControlService.FEEDS_ADMIN, actionSet.actions);
            });
        }
        return BusinessMetadataController;
    }());
    exports.BusinessMetadataController = BusinessMetadataController;
    // Register the controller
    angular.module(module_name_1.moduleName).controller('BusinessMetadataController', ["$scope", "$http", "AccessControlService", "RestUrlService", BusinessMetadataController]);
});
//# sourceMappingURL=BusinessMetadataController.js.map