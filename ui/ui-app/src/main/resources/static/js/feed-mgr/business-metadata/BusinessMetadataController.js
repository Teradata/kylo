define(['angular','feed-mgr/business-metadata/module-name'], function (angular,moduleName) {
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
        var self = this;

        /**
         * Indicates if the category fields may be edited.
         * @type {boolean}
         */
        self.allowCategoryEdit = false;

        /**
         * Indicates if the feed fields may be edited.
         * @type {boolean}
         */
        self.allowFeedEdit = false;

        /**
         * Model for editable sections.
         * @type {{categoryFields: Array, feedFields: Array}}
         */
        self.editModel = {categoryFields: [], feedFields: []};

        /**
         * Indicates that the editable section for categories is displayed.
         * @type {boolean}
         */
        self.isCategoryEditable = false;

        /**
         * Indicates that the editable section for categories is valid.
         * @type {boolean}
         */
        self.isCategoryValid = true;

        /**
         * Indicates that the editable section for categories is displayed.
         * @type {boolean}
         */
        self.isFeedEditable = false;

        /**
         * Indicates that the editable section for categories is valid.
         * @type {boolean}
         */
        self.isFeedValid = true;

        /**
         * Indicates that the loading progress bar is displayed.
         * @type {boolean}
         */
        self.loading = true;

        /**
         * Model for read-only sections.
         * @type {{categoryFields: Array, feedFields: Array}}
         */
        self.model = {categoryFields: [], feedFields: []};

        /**
         * Creates a copy of the category model for editing.
         */
        self.onCategoryEdit = function() {
            self.editModel.categoryFields = angular.copy(self.model.categoryFields);
        };

        /**
         * Saves the category model.
         */
        self.onCategorySave = function() {
            var model = angular.copy(self.model);
            model.categoryFields = self.editModel.categoryFields;

            $http({
                data: angular.toJson(model),
                headers: {'Content-Type': 'application/json; charset=UTF-8'},
                method: "POST",
                url: RestUrlService.ADMIN_USER_FIELDS
            }).then(function() {
                self.model = model;
            });
        };

        /**
         * Creates a copy of the feed model for editing.
         */
        self.onFeedEdit = function() {
            self.editModel.feedFields = angular.copy(self.model.feedFields);
        };

        /**
         * Saves the feed model.
         */
        self.onFeedSave = function() {
            var model = angular.copy(self.model);
            model.feedFields = self.editModel.feedFields;

            $http({
                data: angular.toJson(model),
                headers: {'Content-Type': 'application/json; charset=UTF-8'},
                method: "POST",
                url: RestUrlService.ADMIN_USER_FIELDS
            }).then(function() {
                self.model = model;
            });
        };

        // Load the field models
        $http.get(RestUrlService.ADMIN_USER_FIELDS).then(function(response) {
            self.model = response.data;
            self.loading = false;
        });

        // Load the permissions
        AccessControlService.getUserAllowedActions()
                .then(function(actionSet) {
                    self.allowCategoryEdit = AccessControlService.hasAction(AccessControlService.CATEGORIES_ADMIN, actionSet.actions);
                    self.allowFeedEdit = AccessControlService.hasAction(AccessControlService.FEEDS_ADMIN, actionSet.actions);
                });
    }

    // Register the controller
    angular.module(moduleName).controller('BusinessMetadataController', ["$scope","$http","AccessControlService","RestUrlService",BusinessMetadataController]);
});
